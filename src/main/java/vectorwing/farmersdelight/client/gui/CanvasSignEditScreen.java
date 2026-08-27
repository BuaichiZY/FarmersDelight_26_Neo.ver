package vectorwing.farmersdelight.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.joml.Vector3f;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.state.CanvasSign;

/** Canvas-specific standing-sign editor using Minecraft 26.2's 2D sign preview. */
public class CanvasSignEditScreen extends AbstractSignEditScreen
{
	private static final Vector3f TEXT_SCALE = new Vector3f(0.9765628F, 0.9765628F, 0.9765628F);
	private final Identifier texture;
	private final int displayedHeight;

	public CanvasSignEditScreen(SignBlockEntity signBlockEntity, boolean isFront, boolean isTextFilteringEnabled) {
		super(signBlockEntity, isFront, isTextFilteringEnabled);
		DyeColor dye = signBlockEntity.getBlockState().getBlock() instanceof CanvasSign canvasSign
				? canvasSign.getBackgroundColor()
				: null;
		String dyeName = dye != null ? "_" + dye.getName() : "";
		this.texture = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "canvas" + dyeName + ".png")
				.withPrefix("textures/gui/signs/");
		this.displayedHeight = PlainSignBlock.getAttachmentPoint(signBlockEntity.getBlockState()) == PlainSignBlock.Attachment.WALL
				? 12
				: 26;
	}

	@Override
	protected float getSignYOffset() {
		return 90.0F;
	}

	@Override
	protected void extractSignBackground(GuiGraphicsExtractor graphics) {
		graphics.pose().translate(0.0F, 27.0F);
		graphics.pose().scale(3.9F, 3.9F);
		graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, -12, -13,
				0.0F, 0.0F, 24, this.displayedHeight, 24, 26);
	}

	@Override
	protected Vector3f getSignTextScale() {
		return TEXT_SCALE;
	}
}
