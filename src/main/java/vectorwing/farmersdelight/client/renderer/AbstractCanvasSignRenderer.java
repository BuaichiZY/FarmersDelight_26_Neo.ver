package vectorwing.farmersdelight.client.renderer;

import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;

/**
 * Shared text renderer for canvas signs.
 * Minecraft 26.2 renders the sign body as a normal block model, while the
 * block-entity renderer is responsible only for the text.
 */
public abstract class AbstractCanvasSignRenderer<S extends SignRenderState> extends AbstractSignRenderer<S>
{
	protected AbstractCanvasSignRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}
}
