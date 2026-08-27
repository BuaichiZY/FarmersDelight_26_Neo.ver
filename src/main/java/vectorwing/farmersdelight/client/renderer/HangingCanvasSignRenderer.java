package vectorwing.farmersdelight.client.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HangingCanvasSignRenderer extends AbstractCanvasSignRenderer<HangingCanvasSignRenderer.RenderState>
{
	public HangingCanvasSignRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(SignBlockEntity blockEntity, RenderState state, float partialTicks, Vec3 cameraPosition,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		BlockState blockState = blockEntity.getBlockState();
		state.attachmentType = HangingSignBlock.getAttachmentPoint(blockState);
		if (blockState.getBlock() instanceof WallHangingSignBlock) {
			state.transformations = HangingSignRenderer.TRANSFORMATIONS.wallTransformation(blockState.getValue(WallHangingSignBlock.FACING));
		} else {
			state.transformations = HangingSignRenderer.TRANSFORMATIONS.freeTransformations(blockState.getValue(CeilingHangingSignBlock.ROTATION));
		}
	}

	public static class RenderState extends HangingSignRenderState {}
}
