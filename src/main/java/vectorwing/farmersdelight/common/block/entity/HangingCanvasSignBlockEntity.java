package vectorwing.farmersdelight.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.state.CanvasSign;
import vectorwing.farmersdelight.common.registry.ModBlockEntityTypes;

public class HangingCanvasSignBlockEntity extends HangingSignBlockEntity
{
	public HangingCanvasSignBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		if (state.getBlock() instanceof CanvasSign canvasSign && canvasSign.isDarkBackground()) {
			this.frontText = SignText.EMPTY.withColor(DyeColor.WHITE);
			this.backText = SignText.EMPTY.withColor(DyeColor.WHITE);
		}
	}

	@Override
	public BlockEntityType<?> getType() {
		return ModBlockEntityTypes.HANGING_CANVAS_SIGN.get();
	}

	@Override
	public boolean isValidBlockState(BlockState state) {
		return this.getType().isValid(state);
	}

	public int getTextLineHeight() {
		return 9;
	}

	public int getMaxTextLineWidth() {
		return 60;
	}
}
