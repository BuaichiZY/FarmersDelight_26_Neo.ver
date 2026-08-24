package vectorwing.farmersdelight.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.block.state.CanvasSign;
import vectorwing.farmersdelight.common.registry.ModAtlases;

/** Canvas-specific standing-sign editor preview for the extracted 26.1 GUI renderer. */
public class CanvasSignEditScreen extends AbstractSignEditScreen
{
	private static final float SIGN_SCALE = 62.500004F;
	private static final Vector3f TEXT_SCALE = new Vector3f(0.9765628F, 0.9765628F, 0.9765628F);
	private final boolean isFrontText;
	private final @Nullable DyeColor backgroundColor;
	private Model.@Nullable Simple signModel;

	public CanvasSignEditScreen(SignBlockEntity signBlockEntity, boolean isFront, boolean isTextFilteringEnabled) {
		super(signBlockEntity, isFront, isTextFilteringEnabled);
		this.isFrontText = isFront;
		this.backgroundColor = signBlockEntity.getBlockState().getBlock() instanceof CanvasSign canvasSign
				? canvasSign.getBackgroundColor()
				: null;
	}

	@Override
	protected void init() {
		super.init();
		PlainSignBlock.Attachment attachment = PlainSignBlock.getAttachmentPoint(this.sign.getBlockState());
		this.signModel = StandingSignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType, attachment);
	}

	@Override
	protected float getSignYOffset() {
		return 90.0F;
	}

	@Override
	protected void extractSignBackground(GuiGraphicsExtractor graphics) {
		if (this.signModel == null) {
			return;
		}

		int centerX = this.width / 2;
		SpriteId sprite = new SpriteId(
				net.minecraft.client.renderer.Sheets.SIGN_SHEET,
				ModAtlases.getCanvasSignMaterial(this.backgroundColor).sprite()
		);
		graphics.submitPictureInPictureRenderState(new CanvasSignGuiRenderState(
				this.signModel,
				sprite,
				this.isFrontText,
				centerX - 48,
				66,
				centerX + 48,
				168,
				SIGN_SCALE,
				graphics.peekScissorStack()
		));
	}

	@Override
	protected Vector3f getSignTextScale() {
		return TEXT_SCALE;
	}

	public record CanvasSignGuiRenderState(
			Model.Simple signModel,
			SpriteId sprite,
			boolean isFrontText,
			int x0,
			int y0,
			int x1,
			int y1,
			float scale,
			@Nullable ScreenRectangle scissorArea,
			@Nullable ScreenRectangle bounds
	) implements PictureInPictureRenderState {
		public CanvasSignGuiRenderState(Model.Simple signModel, SpriteId sprite, boolean isFrontText,
				int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea) {
			this(signModel, sprite, isFrontText, x0, y0, x1, y1, scale, scissorArea,
					PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
		}
	}

	public static class CanvasSignGuiRenderer extends PictureInPictureRenderer<CanvasSignGuiRenderState> {
		private final SpriteGetter sprites;

		public CanvasSignGuiRenderer(MultiBufferSource.BufferSource bufferSource) {
			super(bufferSource);
			this.sprites = Minecraft.getInstance().getAtlasManager();
		}

		@Override
		public Class<CanvasSignGuiRenderState> getRenderStateClass() {
			return CanvasSignGuiRenderState.class;
		}

		@Override
		protected void renderToTexture(CanvasSignGuiRenderState state, PoseStack poseStack) {
			Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
			poseStack.translate(0.0F, -0.75F, 0.0F);
			if (!state.isFrontText()) {
				poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			}
			Model.Simple model = state.signModel();
			VertexConsumer buffer = state.sprite().buffer(this.sprites, this.bufferSource, model.renderType());
			model.renderToBuffer(poseStack, buffer, 15728880, OverlayTexture.NO_OVERLAY);
		}

		@Override
		protected String getTextureLabel() {
			return "farmersdelight_canvas_sign";
		}
	}
}
