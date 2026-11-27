package vectorwing.farmersdelight.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;

/** Exercises real block removal, not a direct call to an inventory drop helper. */
public final class BoardRemovalGameTests {
    private BoardRemovalGameTests() {
    }

    public static void testContentsDropped(GameTestHelper helper) {
        BlockPos relative = new BlockPos(2, 1, 2);
        BlockPos absolute = helper.absolutePos(relative);
        ItemStack ingredient = new ItemStack(ModItems.TOMATO.get(), 5);
        ItemStack tool = new ItemStack(ModItems.IRON_KNIFE.get());
        tool.setDamageValue(13);
        tool.set(DataComponents.CUSTOM_NAME, Component.literal("Removal regression"));

        for (String scenario : new String[]{"break", "creative", "support", "replace", "waterlogged", "tool"}) {
            clearDrops(helper, absolute);
            helper.setBlock(relative.below(), Blocks.STONE);
            helper.setBlock(relative, ModBlocks.CUTTING_BOARD.get());
            CuttingBoardBlockEntity board = helper.getBlockEntity(relative, CuttingBoardBlockEntity.class);
            ItemStack expected = scenario.equals("tool") ? tool : ingredient;
            if (scenario.equals("tool")) {
                helper.assertTrue(board.carveToolOnBoard(tool), "Could not prepare inserted tool");
            } else {
                helper.assertTrue(board.addItem(ingredient).isEmpty(), "Could not prepare board ingredient");
            }

            var originalState = board.getBlockState();
            helper.getLevel().setBlockAndUpdate(absolute,
                    originalState.setValue(CuttingBoardBlock.FACING, net.minecraft.core.Direction.EAST));
            helper.assertTrue(helper.getLevel().getBlockEntity(absolute) == board
                            && ItemStack.isSameItemSameComponents(board.getStoredItem(), expected)
                            && board.getStoredItem().getCount() == expected.getCount(),
                    "Same-block state update lost its inventory");
            helper.assertValueEqual(countDrops(helper, absolute, expected), 0, "drops after a state-only update");

            switch (scenario) {
                case "support" -> helper.getLevel().destroyBlock(absolute.below(), true);
                case "replace" -> helper.getLevel().setBlockAndUpdate(absolute, Blocks.STONE.defaultBlockState());
                case "creative" -> helper.getLevel().destroyBlock(absolute, false);
                case "waterlogged" -> {
                    helper.getLevel().setBlockAndUpdate(absolute,
                            board.getBlockState().setValue(CuttingBoardBlock.WATERLOGGED, true));
                    helper.assertValueEqual(countDrops(helper, absolute, expected), 0, "drops after waterlogging");
                    helper.getLevel().destroyBlock(absolute, true);
                }
                default -> helper.getLevel().destroyBlock(absolute, true);
            }
            helper.assertTrue(!(helper.getLevel().getBlockEntity(absolute) instanceof CuttingBoardBlockEntity),
                    "Board survived removal: " + scenario);
            helper.assertValueEqual(countDrops(helper, absolute, expected), expected.getCount(),
                    "stored item drops after " + scenario);
            helper.assertTrue(board.isEmpty(), "Removed board retained its dropped inventory: " + scenario);
            board.preRemoveSideEffects(absolute, originalState);
            helper.assertValueEqual(countDrops(helper, absolute, expected), expected.getCount(),
                    "stored item drops after repeated removal callback: " + scenario);
            int expectedBoardDrops = scenario.equals("creative") || scenario.equals("replace") ? 0 : 1;
            helper.assertValueEqual(countDrops(helper, absolute, new ItemStack(ModItems.CUTTING_BOARD.get())),
                    expectedBoardDrops, "board block drops after " + scenario);
            helper.getLevel().setBlockAndUpdate(absolute, Blocks.AIR.defaultBlockState());
        }

        clearDrops(helper, absolute);
        helper.setBlock(relative, ModBlocks.CUTTING_BOARD.get());
        helper.getLevel().destroyBlock(absolute, true);
        helper.assertValueEqual(countDrops(helper, absolute, ingredient), 0, "ingredient drops from an empty board");
        helper.assertValueEqual(countDrops(helper, absolute, new ItemStack(ModItems.CUTTING_BOARD.get())),
                1, "empty board block drops");
        clearDrops(helper, absolute);
        helper.succeed();
    }

    private static int countDrops(GameTestHelper helper, BlockPos pos, ItemStack expected) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.5)).stream()
                .map(ItemEntity::getItem)
                .filter(stack -> ItemStack.isSameItemSameComponents(stack, expected))
                .mapToInt(ItemStack::getCount).sum();
    }

    private static void clearDrops(GameTestHelper helper, BlockPos pos) {
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.5))
                .forEach(ItemEntity::discard);
    }
}
