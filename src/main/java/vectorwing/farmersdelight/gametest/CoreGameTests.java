package vectorwing.farmersdelight.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;
import vectorwing.farmersdelight.common.block.OrganicCompostBlock;
import vectorwing.farmersdelight.common.block.RichSoilBlock;
import vectorwing.farmersdelight.common.block.RichSoilFarmlandBlock;
import vectorwing.farmersdelight.common.block.entity.CabinetBlockEntity;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.function.Consumer;

/**
 * Small runtime regression suite for the 26.2 port. These tests are only registered when
 * NeoForge enables GameTests, so they do not add any production-world content.
 */
@SuppressWarnings("removal")
public final class CoreGameTests
{
	private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
			DeferredRegister.create(Registries.TEST_FUNCTION, FarmersDelight.MODID);
	private static final ResourceKey<Consumer<GameTestHelper>> RECIPES = functionKey("core_recipes");
	private static final ResourceKey<Consumer<GameTestHelper>> ITEM_DATA = functionKey("item_data_and_tags");
	private static final ResourceKey<Consumer<GameTestHelper>> CABINET_TRANSFER = functionKey("cabinet_transfer_transactions");
	private static final ResourceKey<Consumer<GameTestHelper>> SOIL_BEHAVIORS = functionKey("soil_behaviors");

	static {
		TEST_FUNCTIONS.register("core_recipes", () -> CoreGameTests::testRecipes);
		TEST_FUNCTIONS.register("item_data_and_tags", () -> CoreGameTests::testItemDataAndTags);
		TEST_FUNCTIONS.register("cabinet_transfer_transactions", () -> CoreGameTests::testCabinetTransferTransactions);
		TEST_FUNCTIONS.register("soil_behaviors", () -> CoreGameTests::testSoilBehaviors);
	}

	private CoreGameTests() {
	}

	public static void register(IEventBus modEventBus) {
		TEST_FUNCTIONS.register(modEventBus);
		modEventBus.addListener(CoreGameTests::registerTests);
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("core_environment"));
		registerTest(event, environment, "core_recipes", RECIPES);
		registerTest(event, environment, "item_data_and_tags", ITEM_DATA);
		registerTest(event, environment, "cabinet_transfer_transactions", CABINET_TRANSFER);
		registerTest(event, environment, "soil_behaviors", SOIL_BEHAVIORS);
	}

	private static void registerTest(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
			String name, ResourceKey<Consumer<GameTestHelper>> function) {
		TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
				environment, Identifier.withDefaultNamespace("empty"), 40, 0, true);
		event.registerTest(id(name), testData -> new FunctionGameTestInstance(function, testData), data);
	}

	private static void testRecipes(GameTestHelper helper) {
		RecipeManager recipes = helper.getLevel().getServer().getRecipeManager();
		long farmerRecipes = recipes.getRecipes().stream()
				.map(RecipeHolder::id)
				.map(ResourceKey::identifier)
				.filter(id -> FarmersDelight.MODID.equals(id.getNamespace()))
				.count();
		helper.assertTrue(farmerRecipes >= 250,
				"Expected at least 250 Farmer's Delight recipes, loaded " + farmerRecipes);

		Recipe<?> tomatoSauceValue = requireRecipe(helper, recipes, "cooking/tomato_sauce").value();
		helper.assertTrue(tomatoSauceValue instanceof CookingPotRecipe,
				"Tomato sauce did not load as a cooking pot recipe");
		CookingPotRecipe tomatoSauce = (CookingPotRecipe) tomatoSauceValue;
		ItemStackHandler cookingInventory = new ItemStackHandler(CookingPotRecipe.INPUT_SLOTS);
		cookingInventory.setStackInSlot(0, new ItemStack(ModItems.TOMATO.get()));
		cookingInventory.setStackInSlot(1, new ItemStack(ModItems.TOMATO.get()));
		RecipeWrapper cookingInput = new RecipeWrapper(cookingInventory);
		helper.assertTrue(tomatoSauce.matches(cookingInput, helper.getLevel()),
				"Tomato sauce recipe rejected two tomatoes");
		ItemStack tomatoSauceResult = tomatoSauce.assemble(cookingInput);
		helper.assertTrue(tomatoSauceResult.is(ModItems.TOMATO_SAUCE.get()) && tomatoSauceResult.getCount() == 1,
				"Tomato sauce recipe returned an unexpected result");

		Recipe<?> cabbageValue = requireRecipe(helper, recipes, "cutting/cabbage").value();
		helper.assertTrue(cabbageValue instanceof CuttingBoardRecipe,
				"Cabbage did not load as a cutting board recipe");
		CuttingBoardRecipe cabbage = (CuttingBoardRecipe) cabbageValue;
		CuttingBoardRecipeInput cuttingInput = new CuttingBoardRecipeInput(
				new ItemStack(ModItems.CABBAGE.get()), new ItemStack(ModItems.IRON_KNIFE.get()));
		helper.assertTrue(cabbage.matches(cuttingInput, helper.getLevel()),
				"Cabbage cutting recipe rejected an iron knife");
		ItemStack cabbageResult = cabbage.assemble(cuttingInput);
		helper.assertTrue(cabbageResult.is(ModItems.CABBAGE_LEAF.get()) && cabbageResult.getCount() == 2,
				"Cabbage cutting recipe returned an unexpected result");
		helper.succeed();
	}

	private static void testItemDataAndTags(GameTestHelper helper) {
		ItemStack tomato = new ItemStack(ModItems.TOMATO.get());
		ItemStack skillet = new ItemStack(ModItems.SKILLET.get());
		helper.assertTrue(tomato.get(DataComponents.FOOD) != null,
				"Tomato is missing its food component");
		helper.assertTrue(tomato.get(DataComponents.CONSUMABLE) != null,
				"Tomato is missing its consumable component");
		helper.assertTrue(skillet.get(DataComponents.WEAPON) != null
					&& skillet.get(DataComponents.WEAPON).itemDamagePerAttack() == 1,
				"Skillet is missing its 26.2 weapon component");
		helper.assertTrue(skillet.getSwingAnimation().type() == SwingAnimationType.WHACK
					&& skillet.getSwingAnimation().duration() == 6,
				"Skillet does not use the original six-tick whack animation");
		double skilletAttackSpeed = skillet.getAttributeModifiers()
				.compute(Attributes.ATTACK_SPEED, Attributes.DEFAULT_ATTACK_SPEED, EquipmentSlot.MAINHAND);
		helper.assertValueEqual(skilletAttackSpeed, 2.0, "skillet effective attack speed");
		helper.assertTrue(ModBlocks.ORGANIC_COMPOST.get().defaultBlockState()
				.is(BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT),
				"Organic compost does not override the mushroom light requirement");
		BlockPos compostPos = new BlockPos(1, 0, 1);
		BlockPos mushroomPos = compostPos.above();
		helper.setBlock(compostPos, ModBlocks.ORGANIC_COMPOST.get());
		helper.assertTrue(Blocks.BROWN_MUSHROOM.defaultBlockState()
				.canSurvive(helper.getLevel(), helper.absolutePos(mushroomPos)),
				"Brown mushroom cannot survive on organic compost");
		Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
		mockPlayer.setItemInHand(InteractionHand.MAIN_HAND, skillet);
		var target = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, BlockPos.ZERO);
		helper.assertTrue(skillet.hurtEnemy(target, mockPlayer),
				"Skillet did not enter the 26.2 weapon post-hit pipeline");
		skillet.postHurtEnemy(target, mockPlayer);
		helper.assertValueEqual(skillet.getDamageValue(), 1, "skillet durability after one attack");
		target.discard();
		helper.assertValueEqual(ModItems.ROPE_FENCE_GATE.get().getDescriptionId(),
				"block.farmersdelight.rope_fence_gate", "rope fence gate translation key");
		helper.assertValueEqual(ModItems.SKILLET.get().getDescriptionId(),
				"block.farmersdelight.skillet", "skillet translation key");
		helper.assertValueEqual(ModItems.TOMATO.get().getDescriptionId(),
				"item.farmersdelight.tomato", "tomato translation key");
		helper.assertValueEqual(ModItems.CABBAGE_SEEDS.get().getDescriptionId(),
				"item.farmersdelight.cabbage_seeds", "cabbage seeds translation key");
		helper.assertTrue(new ItemStack(ModItems.IRON_KNIFE.get()).is(ModTags.Items.KNIVES),
				"Iron knife is missing from farmersdelight:tools/knives");
		helper.succeed();
	}

	private static void testCabinetTransferTransactions(GameTestHelper helper) {
		BlockPos cabinetPos = BlockPos.ZERO;
		helper.setBlock(cabinetPos, ModBlocks.OAK_CABINET.get());
		CabinetBlockEntity cabinet = helper.getBlockEntity(cabinetPos, CabinetBlockEntity.class);
		ResourceHandler<ItemResource> handler = helper.requireCapability(
				Capabilities.Item.BLOCK, cabinetPos, null);
		ItemResource apples = ItemResource.of(Items.APPLE);

		try (Transaction transaction = Transaction.openRoot()) {
			int inserted = handler.insert(0, apples, 3, transaction);
			helper.assertValueEqual(inserted, 3, "simulated rollback insertion");
		}
		helper.assertValueEqual(handler.getAmountAsLong(0), 0L, "amount after insertion rollback");

		try (Transaction transaction = Transaction.openRoot()) {
			int inserted = handler.insert(0, apples, 3, transaction);
			helper.assertValueEqual(inserted, 3, "committed insertion");
			transaction.commit();
		}
		helper.assertTrue(handler.getResource(0).is(Items.APPLE),
				"Cabinet transfer handler stored the wrong resource");
		helper.assertValueEqual(handler.getAmountAsLong(0), 3L, "amount after insertion commit");
		helper.assertValueEqual(cabinet.getItem(0).getCount(), 3, "cabinet menu-visible amount after commit");

		try (Transaction transaction = Transaction.openRoot()) {
			int extracted = handler.extract(0, apples, 2, transaction);
			helper.assertValueEqual(extracted, 2, "rolled back extraction");
		}
		helper.assertValueEqual(handler.getAmountAsLong(0), 3L, "amount after extraction rollback");

		try (Transaction transaction = Transaction.openRoot()) {
			int extracted = handler.extract(0, apples, 2, transaction);
			helper.assertValueEqual(extracted, 2, "committed extraction");
			transaction.commit();
		}
		helper.assertValueEqual(handler.getAmountAsLong(0), 1L, "amount after extraction commit");
		helper.assertValueEqual(cabinet.getItem(0).getCount(), 1, "cabinet menu-visible amount after extraction");
		helper.succeed();
	}

	private static void testSoilBehaviors(GameTestHelper helper) {
		var level = helper.getLevel();
		OrganicCompostBlock compost = (OrganicCompostBlock) ModBlocks.ORGANIC_COMPOST.get();
		BlockPos compostRelative = new BlockPos(1, 1, 1);
		BlockPos compostPos = helper.absolutePos(compostRelative);
		BlockPos mushroomRelative = compostRelative.above();
		BlockPos waterRelative = compostRelative.east();

		helper.setBlock(compostRelative, ModBlocks.ORGANIC_COMPOST.get());
		float baseChance = compost.getCompostingChance(level, compostPos);
		helper.setBlock(mushroomRelative, Blocks.BROWN_MUSHROOM);
		float mushroomChance = compost.getCompostingChance(level, compostPos);
		helper.assertTrue(Math.abs(mushroomChance - baseChance - 0.02F) < 0.0001F,
				"A mushroom did not add 2% to organic compost progression");
		helper.setBlock(waterRelative, Blocks.WATER);
		float acceleratedChance = compost.getCompostingChance(level, compostPos);
		helper.assertTrue(Math.abs(acceleratedChance - mushroomChance - 0.1F) < 0.0001F,
				"Nearby water did not add 10% to organic compost progression");
		helper.assertTrue(Blocks.BROWN_MUSHROOM.defaultBlockState()
				.canSurvive(level, helper.absolutePos(mushroomRelative)),
				"Brown mushroom cannot survive on organic compost");
		helper.assertValueEqual(
				compost.getAnalogOutputSignal(compost.defaultBlockState(), level, compostPos, net.minecraft.core.Direction.UP),
				8, "organic compost initial comparator signal");

		RandomSource compostRandom = RandomSource.create(0xFAD26L);
		for (int attempt = 0; attempt < 1000 && level.getBlockState(compostPos).is(ModBlocks.ORGANIC_COMPOST.get()); attempt++) {
			BlockState state = level.getBlockState(compostPos);
			compost.randomTick(state, level, compostPos, compostRandom);
		}
		helper.assertBlockPresent(ModBlocks.RICH_SOIL.get(), compostRelative);

		RichSoilBlock richSoil = (RichSoilBlock) ModBlocks.RICH_SOIL.get();
		richSoil.randomTick(level.getBlockState(compostPos), level, compostPos, RandomSource.create(1L));
		helper.assertBlockPresent(ModBlocks.BROWN_MUSHROOM_COLONY.get(), mushroomRelative);
		helper.assertTrue(level.getBlockState(helper.absolutePos(mushroomRelative))
				.getValue(MushroomColonyBlock.COLONY_AGE) == 0,
				"Mushroom colony did not start at age zero");

		BlockPos saplingRelative = new BlockPos(4, 1, 1);
		BlockPos saplingPos = helper.absolutePos(saplingRelative.above());
		helper.setBlock(saplingRelative, ModBlocks.RICH_SOIL.get());
		helper.setBlock(saplingRelative.above(), Blocks.OAK_SAPLING);
		helper.assertTrue(RichSoilBlock.boostPlant(level.getBlockState(saplingPos), saplingPos, level),
				"Rich soil did not apply its bonemeal boost to a sapling");

		BlockPos farmlandRelative = new BlockPos(7, 1, 1);
		BlockPos farmlandPos = helper.absolutePos(farmlandRelative);
		BlockPos cropRelative = farmlandRelative.above();
		BlockPos farmlandWaterRelative = farmlandRelative.east();
		RichSoilFarmlandBlock farmland = (RichSoilFarmlandBlock) ModBlocks.RICH_SOIL_FARMLAND.get();
		CropBlock wheat = (CropBlock) Blocks.WHEAT;
		BlockState moistFarmland = farmland.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7);
		helper.setBlock(farmlandRelative, moistFarmland);
		helper.setBlock(cropRelative, Blocks.WHEAT);
		helper.setBlock(farmlandWaterRelative, Blocks.WATER);
		RandomSource farmlandRandom = RandomSource.create(3L);
		for (int attempt = 0; attempt < 64
				&& wheat.getAge(level.getBlockState(helper.absolutePos(cropRelative))) == 0; attempt++) {
			farmland.randomTick(level.getBlockState(farmlandPos), level, farmlandPos, farmlandRandom);
		}
		helper.assertTrue(wheat.getAge(level.getBlockState(helper.absolutePos(cropRelative))) > 0,
				"Moist rich soil farmland did not boost crop growth");
		helper.assertTrue(farmland.isFertile(level.getBlockState(farmlandPos), level, farmlandPos),
				"Moist rich soil farmland is not reported as fertile");

		BlockState dryFarmland = farmland.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 0);
		helper.setBlock(farmlandWaterRelative, Blocks.AIR);
		helper.setBlock(farmlandRelative, dryFarmland);
		helper.setBlock(cropRelative, Blocks.WHEAT);
		for (int attempt = 0; attempt < 64; attempt++) {
			farmland.randomTick(level.getBlockState(farmlandPos), level, farmlandPos, farmlandRandom);
		}
		helper.assertValueEqual(wheat.getAge(level.getBlockState(helper.absolutePos(cropRelative))),
				0, "crop age above dry rich soil farmland");
		helper.assertTrue(!farmland.isFertile(level.getBlockState(farmlandPos), level, farmlandPos),
				"Dry rich soil farmland is incorrectly reported as fertile");
		helper.assertBlockPresent(ModBlocks.RICH_SOIL_FARMLAND.get(), farmlandRelative);
		FarmlandBlock.turnToDirt(null, level.getBlockState(farmlandPos), level, farmlandPos);
		helper.assertBlockPresent(ModBlocks.RICH_SOIL_FARMLAND.get(), farmlandRelative);
		helper.succeed();
	}

	private static RecipeHolder<?> requireRecipe(GameTestHelper helper, RecipeManager recipes, String path) {
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id(path));
		return recipes.byKey(key).orElseThrow(() -> helper.assertionException("Missing recipe " + key.identifier()));
	}

	private static ResourceKey<Consumer<GameTestHelper>> functionKey(String path) {
		return ResourceKey.create(Registries.TEST_FUNCTION, id(path));
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, path);
	}
}
