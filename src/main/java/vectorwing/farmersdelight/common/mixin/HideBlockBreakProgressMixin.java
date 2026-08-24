package vectorwing.farmersdelight.common.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.registry.ModBlocks;

@Mixin(LevelRenderer.class)
public abstract class HideBlockBreakProgressMixin
{
	@Inject(method = "extractBlockDestroyAnimation", at = @At("RETURN"))
	private void hideBlockDamage(Camera camera, LevelRenderState renderState, CallbackInfo ci) {
		renderState.blockBreakingRenderStates.removeIf(state -> state.blockState().is(ModBlocks.CANVAS_RUG.get()));
	}
}
