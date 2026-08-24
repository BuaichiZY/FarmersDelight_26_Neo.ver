package vectorwing.farmersdelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.block.StoveBlock;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class DefaultStoveRenderer<T extends AbstractStoveBlockEntity> implements BlockEntityRenderer<T, DefaultStoveRenderer.RenderState>
{
	private static final float SIZE = 0.375F;
	private final ItemModelResolver itemModelResolver;

	public DefaultStoveRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(T stove, RenderState state, float partialTicks, Vec3 cameraPosition,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(stove, state, partialTicks, cameraPosition, breakProgress);
		state.direction = stove.getBlockState().getValue(StoveBlock.FACING).getOpposite();
		state.items = new ArrayList<>();
		state.offsets = new ArrayList<>();
		state.itemLightCoords = stove.getLevel() == null
				? state.lightCoords
				: LevelRenderer.getLightCoords(stove.getLevel(), stove.getBlockPos().above());

		var items = stove.getItems();
		int posLong = (int) stove.getBlockPos().asLong();

		for (int i = 0; i < items.getSlots(); ++i) {
			ItemStack stoveStack = items.getStackInSlot(i);
			if (stoveStack.isEmpty()) continue;
			ItemStackRenderState itemState = new ItemStackRenderState();
			this.itemModelResolver.updateForTopItem(itemState, stoveStack, ItemDisplayContext.FIXED, stove.getLevel(), null, posLong + i);
			state.items.add(itemState);
			state.offsets.add(stove.getStoveItemOffset(i));
		}
	}

	@Override
	public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		for (int i = 0; i < state.items.size(); ++i) {
			poseStack.pushPose();

			// Center item above the stove
			poseStack.translate(0.5D, 1.02D, 0.5D);

			// Rotate item to face the stove's front side
			float f = -state.direction.toYRot();
			poseStack.mulPose(Axis.YP.rotationDegrees(f));

			// Rotate item flat on the stove. Use X and Y from now on
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

			// Neatly align items according to their index
			Vec2 itemOffset = state.offsets.get(i);
			poseStack.translate(itemOffset.x, itemOffset.y, 0.0D);

			// Resize the items
			poseStack.scale(SIZE, SIZE, SIZE);

			state.items.get(i).submit(poseStack, submitNodeCollector, state.itemLightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	public static class RenderState extends BlockEntityRenderState {
		public Direction direction = Direction.NORTH;
		public int itemLightCoords;
		public List<ItemStackRenderState> items = List.of();
		public List<Vec2> offsets = List.of();
	}
}
