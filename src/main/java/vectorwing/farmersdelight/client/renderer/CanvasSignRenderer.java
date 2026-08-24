package vectorwing.farmersdelight.client.renderer;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.StandingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.block.state.CanvasSign;
import vectorwing.farmersdelight.common.registry.ModAtlases;

public class CanvasSignRenderer extends AbstractCanvasSignRenderer<CanvasSignRenderer.RenderState>
{
	private final Model.Simple standingModel;
	private final Model.Simple wallModel;

	public CanvasSignRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		this.standingModel = StandingSignRenderer.createSignModel(context.entityModelSet(), WoodType.SPRUCE, PlainSignBlock.Attachment.GROUND);
		this.wallModel = StandingSignRenderer.createSignModel(context.entityModelSet(), WoodType.SPRUCE, PlainSignBlock.Attachment.WALL);
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
		state.attachmentType = PlainSignBlock.getAttachmentPoint(blockState);
		if (blockState.getBlock() instanceof WallSignBlock) {
			state.transformations = StandingSignRenderer.TRANSFORMATIONS.wallTransformation(blockState.getValue(WallSignBlock.FACING));
		} else {
			state.transformations = StandingSignRenderer.TRANSFORMATIONS.freeTransformations(blockState.getValue(StandingSignBlock.ROTATION));
		}
		state.backgroundColor = blockState.getBlock() instanceof CanvasSign canvasSign ? canvasSign.getBackgroundColor() : null;
	}

	@Override
	protected Model.Simple getSignModel(RenderState state) {
		return state.attachmentType == PlainSignBlock.Attachment.GROUND ? this.standingModel : this.wallModel;
	}

	@Override
	protected SpriteId getSignSprite(RenderState state) {
		return new SpriteId(Sheets.SIGN_SHEET, ModAtlases.getCanvasSignMaterial(state.backgroundColor).sprite());
	}

	public static class RenderState extends StandingSignRenderState {
		public @Nullable DyeColor backgroundColor;
	}
}
