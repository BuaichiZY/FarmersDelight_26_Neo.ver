package vectorwing.farmersdelight.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/** Exercises the packaged loot rules, not hand-constructed modifier instances. */
final class LootGameTests {
    private LootGameTests() {}

    static void testLootConditions(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var slime = helper.spawnWithNoFreeWill(EntityTypes.SLIME, BlockPos.ZERO);
        slime.setSize(1, true);
        for (ItemStack weapon : List.of(new ItemStack(ModItems.SKILLET.get()),
                new ItemStack(Items.IRON_SWORD), ItemStack.EMPTY, new ItemStack(ModItems.IRON_KNIFE.get()))) {
            for (int seed = 1; seed <= 32; seed++) {
                var drops = entityDrops(helper, slime, player, weapon, "slime", seed);
                helper.assertTrue(drops.stream().allMatch(stack -> stack.is(Items.SLIME_BALL))
                                && count(drops, Items.SLIME_BALL) <= 2,
                        "Slime loot was polluted using " + weapon + ": " + drops);
            }
        }
        slime.discard();

        validatePackagedConditions(helper);

        var shulker = helper.spawnWithNoFreeWill(EntityTypes.SHULKER, BlockPos.ZERO);
        for (int seed = 1; seed <= 32; seed++) {
            var skilletDrops = entityDrops(helper, shulker, player, new ItemStack(ModItems.SKILLET.get()), "shulker", seed);
            var knifeDrops = entityDrops(helper, shulker, player, new ItemStack(ModItems.IRON_KNIFE.get()), "shulker", seed);
            helper.assertTrue(skilletDrops.stream().allMatch(stack -> stack.is(Items.SHULKER_SHELL))
                            && knifeDrops.stream().allMatch(stack -> stack.is(Items.SHULKER_SHELL))
                            && count(knifeDrops, Items.SHULKER_SHELL) == count(skilletDrops, Items.SHULKER_SHELL) + 1,
                    "Shulker knife bonus did not respect the weapon condition");
        }
        shulker.discard();

        var hoglin = helper.spawnWithNoFreeWill(EntityTypes.HOGLIN, BlockPos.ZERO);
        var knife = new ItemStack(ModItems.IRON_KNIFE.get());
        var cold = entityDrops(helper, hoglin, player, knife, "hoglin", 1);
        helper.assertTrue(count(cold, ModItems.HAM.get()) == 1 && count(cold, ModItems.SMOKED_HAM.get()) == 0,
                "Unburnt hoglin must drop raw ham only with a knife");
        hoglin.setRemainingFireTicks(100);
        var hot = entityDrops(helper, hoglin, player, knife, "hoglin", 1);
        helper.assertTrue(count(hot, ModItems.SMOKED_HAM.get()) == 1 && count(hot, ModItems.HAM.get()) == 0,
                "Burning hoglin must drop smoked ham, not raw ham");
        hoglin.discard();

        var stone = blockDrops(helper, Blocks.STONE.defaultBlockState(), new ItemStack(ModItems.SKILLET.get()));
        helper.assertTrue(stone.size() == 1 && stone.getFirst().is(Items.COBBLESTONE), "Stone loot was polluted: " + stone);
        var pumpkin = blockDrops(helper, Blocks.PUMPKIN.defaultBlockState(), knife);
        helper.assertTrue(count(pumpkin, ModItems.PUMPKIN_SLICE.get()) == 4 && count(pumpkin, Items.PUMPKIN) == 0,
                "Knife must replace pumpkin with four slices");
        var wholePumpkin = blockDrops(helper, Blocks.PUMPKIN.defaultBlockState(), new ItemStack(ModItems.SKILLET.get()));
        helper.assertTrue(wholePumpkin.size() == 1 && count(wholePumpkin, Items.PUMPKIN) == 1,
                "Non-knife tool must not slice pumpkins");
        var matureWheat = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
        helper.assertTrue(count(blockDrops(helper, matureWheat, knife), ModItems.STRAW.get()) == 1,
                "Mature wheat must still yield straw when harvested with a knife");
        helper.assertTrue(count(blockDrops(helper, Blocks.WHEAT.defaultBlockState(), knife), ModItems.STRAW.get()) == 0
                        && count(blockDrops(helper, matureWheat, new ItemStack(ModItems.SKILLET.get())), ModItems.STRAW.get()) == 0,
                "Straw harvest ignored crop age or tool condition");
        helper.assertTrue(count(blockDrops(helper, Blocks.CAKE.defaultBlockState(), knife), ModItems.CAKE_SLICE.get()) == 7
                        && blockDrops(helper, Blocks.CAKE.defaultBlockState(), new ItemStack(ModItems.SKILLET.get())).isEmpty(),
                "Cake slicing ignored the knife condition");

        int chestAdditions = 0;
        for (int seed = 1; seed <= 32; seed++) {
            var params = new LootParams.Builder(helper.getLevel())
                    .withParameter(LootContextParams.ORIGIN, player.position()).create(LootContextParamSets.CHEST);
            var table = Identifier.withDefaultNamespace("chests/simple_dungeon");
            var context = new LootContext.Builder(params).withOptionalRandomSeed(seed).create(Optional.empty());
            var additions = CommonHooks.modifyLoot(table, new ObjectArrayList<>(), context);
            helper.assertTrue(additions.stream().allMatch(stack -> stack.is(ModItems.TOMATO_SEEDS.get())
                            || stack.is(ModItems.CABBAGE_SEEDS.get()) || stack.is(ModItems.ROPE.get())),
                    "Dungeon chest received unrelated modifiers: " + additions);
            chestAdditions += additions.size();
            var unrelated = new LootContext.Builder(params).withOptionalRandomSeed(seed).create(Optional.empty());
            helper.assertTrue(CommonHooks.modifyLoot(Identifier.withDefaultNamespace("chests/spawn_bonus_chest"),
                            new ObjectArrayList<>(), unrelated).isEmpty(), "Unrelated chest received Farmer's Delight loot");
        }
        helper.assertTrue(chestAdditions > 0, "Dungeon chest injection stopped working");
        helper.succeed();
    }

    private static void validatePackagedConditions(GameTestHelper helper) {
        var resources = helper.getLevel().getServer().getResourceManager().listResources("loot_modifiers",
                id -> id.getNamespace().equals("farmersdelight") && id.getPath().endsWith(".json"));
        helper.assertTrue(resources.size() >= 35, "Missing bundled loot modifier resources");
        var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        resources.forEach((id, resource) -> {
            try (var reader = resource.openAsReader()) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();
                helper.assertTrue(json.has("condition") && !json.has("conditions"), "Legacy loot conditions in " + id);
                var modifier = IGlobalLootModifier.DIRECT_CODEC.parse(ops, json).getOrThrow();
                var encoded = IGlobalLootModifier.DIRECT_CODEC.encodeStart(ops, modifier).getOrThrow().getAsJsonObject();
                helper.assertTrue(encoded.has("condition"), "Loot modifier silently lost conditions: " + id);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to read " + id, exception);
            }
        });
    }

    private static List<ItemStack> entityDrops(GameTestHelper helper, LivingEntity victim, Player player,
            ItemStack weapon, String entity, long seed) {
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, weapon);
        var params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.THIS_ENTITY, victim)
                .withParameter(LootContextParams.ORIGIN, victim.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, helper.getLevel().damageSources().playerAttack(player))
                .withParameter(LootContextParams.ATTACKING_ENTITY, player)
                .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .create(LootContextParamSets.ENTITY);
        var key = ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/" + entity));
        return helper.getLevel().getServer().reloadableRegistries().getLootTable(key).getRandomItems(params, seed)
                .stream().filter(stack -> !stack.isEmpty()).toList();
    }

    private static List<ItemStack> blockDrops(GameTestHelper helper, BlockState state, ItemStack tool) {
        return Block.getDrops(state, helper.getLevel(), helper.absolutePos(BlockPos.ZERO), null, null, tool);
    }

    private static int count(List<ItemStack> drops, Item item) {
        return drops.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }
}
