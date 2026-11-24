package vectorwing.farmersdelight.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;

/** Uses the transformed Item.useOn path so failed BlockItem fallback cannot silently regress. */
final class PumpkinPiePlacementGameTests {
    private PumpkinPiePlacementGameTests() {
    }

    static void testPlacement(GameTestHelper helper) {
        var previous = Configuration.ENABLE_PUMPKIN_PIE_SNEAK_TO_PLACE;
        try {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            BlockPos relative = new BlockPos(2, 2, 2);
            BlockPos pos = helper.absolutePos(relative);
            player.setPos(pos.getX() + 8.0, pos.getY() + 1.0, pos.getZ() + 8.0);
            for (boolean sneakRequired : new boolean[]{true, false}) {
                Configuration.ENABLE_PUMPKIN_PIE_SNEAK_TO_PLACE = () -> sneakRequired;
                player.setShiftKeyDown(sneakRequired);
                for (Direction face : Direction.values()) {
                    reset(helper, pos);
                    player.getFoodData().setFoodLevel(20);
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PUMPKIN_PIE, 2));
                    var result = Items.PUMPKIN_PIE.useOn(context(player, pos, face));
                    if (face == Direction.UP) {
                        helper.assertTrue(result.consumesAction()
                                        && helper.getLevel().getBlockState(pos.above()).is(ModBlocks.PUMPKIN_PIE.get()),
                                "Supported pumpkin pie placement failed");
                        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "pies after supported placement");
                    } else {
                        helper.assertTrue(!result.consumesAction()
                                        && helper.getLevel().getBlockState(pos.relative(face)).isAir(),
                                "Unsupported pumpkin pie placement changed the world: " + face);
                        helper.assertValueEqual(player.getMainHandItem().getCount(), 2, "pies after rejected placement");
                    }
                }
                // A supported side placement must still succeed when the adjacent space has a floor.
                reset(helper, pos);
                helper.getLevel().setBlockAndUpdate(pos.east().below(), Blocks.STONE.defaultBlockState());
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PUMPKIN_PIE, 2));
                helper.assertTrue(Items.PUMPKIN_PIE.useOn(context(player, pos, Direction.EAST)).consumesAction()
                                && helper.getLevel().getBlockState(pos.east()).is(ModBlocks.PUMPKIN_PIE.get()),
                        "Supported side placement was incorrectly blocked");

                // Preserve vanilla consumption fallback when a hungry player cannot place the pie.
                reset(helper, pos);
                player.getFoodData().setFoodLevel(10);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PUMPKIN_PIE, 2));
                helper.assertTrue(Items.PUMPKIN_PIE.useOn(context(player, pos, Direction.NORTH)).consumesAction(),
                        "Failed placement no longer falls back to eating for hungry players");
                player.stopUsingItem();
            }

            Configuration.ENABLE_PUMPKIN_PIE_SNEAK_TO_PLACE = () -> true;
            reset(helper, pos);
            player.setShiftKeyDown(false);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PUMPKIN_PIE, 2));
            helper.assertTrue(!Items.PUMPKIN_PIE.useOn(context(player, pos, Direction.UP)).consumesAction()
                            && helper.getLevel().getBlockState(pos.above()).isAir(),
                    "Sneak-to-place configuration was ignored");

            // Other placeable foods must continue to reject unsupported placement without crashing.
            player.setShiftKeyDown(true);
            player.getFoodData().setFoodLevel(20);
            Item[] dishes = {ModItems.APPLE_PIE.get(), ModItems.CHOCOLATE_PIE.get(),
                    ModItems.SWEET_BERRY_CHEESECAKE.get(), ModItems.ROAST_CHICKEN_BLOCK.get(),
                    ModItems.STUFFED_PUMPKIN_BLOCK.get(), ModItems.HONEY_GLAZED_HAM_BLOCK.get(),
                    ModItems.SHEPHERDS_PIE_BLOCK.get(), ModItems.GLEAMING_SALAD_BLOCK.get(),
                    ModItems.RICE_ROLL_MEDLEY_BLOCK.get()};
            for (Item dish : dishes) {
                for (Direction face : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                    reset(helper, pos);
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(dish));
                    helper.assertTrue(!dish.useOn(context(player, pos, face)).consumesAction(),
                            "Other dish accepted unsupported placement: " + dish);
                }
            }
            reset(helper, pos);
            player.getInventory().clearContent();
            helper.succeed();
        } finally {
            Configuration.ENABLE_PUMPKIN_PIE_SNEAK_TO_PLACE = previous;
        }
    }

    private static void reset(GameTestHelper helper, BlockPos pos) {
        for (Direction face : Direction.values()) {
            helper.getLevel().setBlockAndUpdate(pos.relative(face), Blocks.AIR.defaultBlockState());
            if (face != Direction.UP) helper.getLevel().setBlockAndUpdate(pos.relative(face).below(), Blocks.AIR.defaultBlockState());
        }
        helper.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
    }

    private static UseOnContext context(Player player, BlockPos pos, Direction face) {
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(new Vec3(pos.getX() + 0.5 + face.getStepX() * 0.5,
                        pos.getY() + 0.5 + face.getStepY() * 0.5,
                        pos.getZ() + 0.5 + face.getStepZ() * 0.5), face, pos, false));
    }
}
