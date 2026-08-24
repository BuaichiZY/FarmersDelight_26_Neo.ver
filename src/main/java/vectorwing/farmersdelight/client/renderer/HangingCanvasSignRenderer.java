package vectorwing.farmersdelight.client.renderer;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.block.state.CanvasSign;
import vectorwing.farmersdelight.common.registry.ModAtlases;

import java.util.EnumMap;
import java.util.Map;

public class HangingCanvasSignRenderer extends AbstractCanvasSignRenderer<HangingCanvasSignRenderer.RenderState>
{
	private final Map<HangingSignBlock.Attachment, Model.Simple> models = new EnumMap<>(HangingSignBlock.Attachment.class);

	public HangingCanvasSignRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		for (HangingSignBlock.Attachment attachment : HangingSignBlock.Attachment.values()) {
			this.models.put(attachment, HangingSignRenderer.createSignModel(context.entityModelSet(), WoodType.SPRUCE, attachment));
		}
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
		state.backgroundColor = blockState.getBlock() instanceof CanvasSign canvasSign ? canvasSign.getBackgroundColor() : null;
	}

	@Override
	protected Model.Simple getSignModel(RenderState state) {
		return this.models.get(state.attachmentType);
	}

	@Override
	protected SpriteId getSignSprite(RenderState state) {
		return new SpriteId(Sheets.SIGN_SHEET, ModAtlases.getHangingCanvasSignMaterial(state.backgroundColor).sprite());
	}

	public static class RenderState extends HangingSignRenderState {
		public @Nullable DyeColor backgroundColor;
	}
}
