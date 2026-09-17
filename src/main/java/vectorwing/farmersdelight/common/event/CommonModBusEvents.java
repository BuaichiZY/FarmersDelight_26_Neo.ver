package vectorwing.farmersdelight.common.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.FoodValues;
import vectorwing.farmersdelight.common.registry.ModContextIntProviders;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.function.Supplier;

@EventBusSubscriber(modid = FarmersDelight.MODID)
public class CommonModBusEvents
{
	@SubscribeEvent
	public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
		if (DatagenModLoader.isRunningDataGen()) {
			return;
		}
		if (Configuration.ENABLE_STACKABLE_SOUP_ITEMS.get()) {
			Configuration.SOUP_ITEM_LIST.get().forEach((key) -> {
				Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(key));
				event.modify(item, (builder, context, target) -> builder.set(DataComponents.MAX_STACK_SIZE, 16));
			});
		}
		if (Configuration.ENABLE_RABBIT_STEW_BUFF.get()) {
			event.modify(Items.RABBIT_STEW, (builder, context, target) -> builder
					.set(DataComponents.FOOD, FoodValues.RABBIT_STEW_BUFF)
					.set(DataComponents.CONSUMABLE, FoodValues.RABBIT_STEW_CONSUMABLE));
		}

		registerFuelComponents(event);
		registerCompostableComponents(event);
	}

	private static void registerFuelComponents(ModifyDefaultComponentsEvent event) {
		setFuel(event, ContextIntProviders.COOKING_TIME_DRY_PLANTS,
				ModItems.HALF_TATAMI_MAT, ModItems.STRAW);
		setFuel(event, ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE,
				ModItems.CUTTING_BOARD, ModItems.ROPE, ModItems.SAFETY_NET,
				ModItems.FULL_TATAMI_MAT, ModItems.CANVAS_RUG, ModItems.ROPE_FENCE,
				ModItems.ROPE_FENCE_GATE, ModItems.TREE_BARK);
		setFuel(event, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS,
				ModItems.WOODEN_BASKET, ModItems.BAMBOO_BASKET,
				ModItems.OAK_CABINET, ModItems.SPRUCE_CABINET, ModItems.BIRCH_CABINET,
				ModItems.JUNGLE_CABINET, ModItems.ACACIA_CABINET, ModItems.DARK_OAK_CABINET,
				ModItems.MANGROVE_CABINET, ModItems.CHERRY_CABINET, ModItems.BAMBOO_CABINET);
		setFuel(event, ModContextIntProviders.COOKING_TIME_400, ModItems.TATAMI, ModItems.CANVAS);
		setFuel(event, ModContextIntProviders.COOKING_TIME_1000, ModItems.STRAW_BALE);
	}

	private static void registerCompostableComponents(ModifyDefaultComponentsEvent event) {
		setCompostable(event, ContextIntProviders.COMPOSTABLE_LOW,
				ModItems.TREE_BARK, ModItems.STRAW, ModItems.CABBAGE_SEEDS, ModItems.TOMATO_SEEDS,
				ModItems.RICE, ModItems.RICE_PANICLE, ModItems.SANDY_SHRUB);
		setCompostable(event, ContextIntProviders.COMPOSTABLE_LOW_MEDIUM,
				ModItems.PUMPKIN_SLICE, ModItems.CABBAGE_LEAF, ModItems.KELP_ROLL_SLICE);
		setCompostable(event, ContextIntProviders.COMPOSTABLE_MEDIUM,
				ModItems.CABBAGE, ModItems.ONION, ModItems.TOMATO, ModItems.WILD_CABBAGES,
				ModItems.WILD_ONIONS, ModItems.WILD_TOMATOES, ModItems.WILD_CARROTS,
				ModItems.WILD_POTATOES, ModItems.WILD_BEETROOTS, ModItems.WILD_RICE, ModItems.PIE_CRUST);
		setCompostable(event, ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH,
				ModItems.RICE_BALE, ModItems.SWEET_BERRY_COOKIE, ModItems.HONEY_COOKIE,
				ModItems.CAKE_SLICE, ModItems.APPLE_PIE_SLICE, ModItems.SWEET_BERRY_CHEESECAKE_SLICE,
				ModItems.CHOCOLATE_PIE_SLICE, ModItems.RAW_PASTA, ModItems.ROTTEN_TOMATO, ModItems.KELP_ROLL);
		setCompostable(event, ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE,
				ModItems.APPLE_PIE, ModItems.SWEET_BERRY_CHEESECAKE, ModItems.CHOCOLATE_PIE,
				ModItems.DUMPLINGS, ModItems.STUFFED_PUMPKIN_BLOCK,
				ModItems.BROWN_MUSHROOM_COLONY, ModItems.RED_MUSHROOM_COLONY);
	}

	@SafeVarargs
	private static void setFuel(ModifyDefaultComponentsEvent event,
			ResourceKey<ContextIntProvider> burnTime, Supplier<Item>... items) {
		CookingFuel fuel = new CookingFuel(burnTime, ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER);
		for (Supplier<Item> item : items) {
			event.modify(item.get(), (builder, context, target) -> builder.set(DataComponents.COOKING_FUEL, fuel));
		}
	}

	@SafeVarargs
	private static void setCompostable(ModifyDefaultComponentsEvent event,
			ResourceKey<ContextIntProvider> layers, Supplier<Item>... items) {
		Compostable compostable = new Compostable(layers);
		for (Supplier<Item> item : items) {
			event.modify(item.get(), (builder, context, target) -> builder.set(DataComponents.COMPOSTABLE, compostable));
		}
	}
}
