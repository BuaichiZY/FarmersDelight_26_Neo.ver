package vectorwing.farmersdelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;
import vectorwing.farmersdelight.common.registry.ModDataComponents;

import java.util.function.Consumer;

/**
 * Renders the ingredient stored in a handheld skillet through the 26.1 special
 * item-model pipeline. The skillet body remains a regular cuboid item model.
 */
public final class SkilletItemRenderer implements SpecialModelRenderer<SkilletItemRenderer.RenderData>
{
	@Override
	public @Nullable RenderData extractArgument(ItemStack skilletStack) {
		ItemStack ingredient = skilletStack
				.getOrDefault(ModDataComponents.SKILLET_INGREDIENT.get(), ItemStackWrapper.EMPTY)
				.getStack();
		if (ingredient.isEmpty()) {
			return null;
		}

		Minecraft minecraft = Minecraft.getInstance();
		ItemStackRenderState ingredientState = new ItemStackRenderState();
		minecraft.getItemModelResolver().updateForTopItem(
				ingredientState,
				ingredient,
				ItemDisplayContext.FIXED,
				minecraft.level,
				null,
				0
		);
		return new RenderData(
				ingredientState,
				skilletStack.get(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get()),
				skilletStack.getOrDefault(ModDataComponents.SKILLET_FLIPPED.get(), false)
		);
	}

	@Override
	public void submit(@Nullable RenderData data, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
		if (data == null || data.ingredient().isEmpty()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		float animation = 0.0F;
		if (data.flipTimestamp() != null && minecraft.level != null) {
			float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			animation = ((minecraft.level.getGameTime() - data.flipTimestamp()) + partialTicks) / SkilletItem.FLIP_TIME;
			animation = Mth.clamp(animation, 0.0F, 1.0F);
		}

		poseStack.pushPose();
		poseStack.translate(0.5F, 1.0F / 16.0F, 0.5F);
		if (animation > 0.0F) {
			poseStack.translate(0.0F, 0.4F * Mth.sin(animation * Mth.PI), 0.0F);
			float rotationAnimation = data.flipped() ? animation + 1.0F : animation;
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F * rotationAnimation));
		} else if (data.flipped()) {
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
		}

		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.scale(0.5F, 0.5F, 0.5F);
		data.ingredient().submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
		poseStack.popPose();
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		output.accept(new Vector3f(0.25F, 0.0F, 0.25F));
		output.accept(new Vector3f(0.75F, 0.75F, 0.75F));
	}

	public record RenderData(ItemStackRenderState ingredient, @Nullable Long flipTimestamp, boolean flipped) {
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked<RenderData> {
		public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

		@Override
		public @Nullable SkilletItemRenderer bake(SpecialModelRenderer.BakingContext context) {
			return new SkilletItemRenderer();
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}

	public static class ArmPoseTransformer implements IArmPoseTransformer {
		@Override
		public void applyTransform(HumanoidModel<?> model, HumanoidRenderState entity, HumanoidArm arm) {
			ItemStack stack = entity.getUseItemStackForArm(arm);
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.level == null || !stack.has(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get())) {
				return;
			}

			long time = stack.get(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get());
			float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			float animation = ((minecraft.level.getGameTime() - time) + partialTicks) / SkilletItem.FLIP_TIME;
			animation = Mth.clamp(animation, 0, 1);
			float armRotation = (-Mth.sin(animation * Mth.TWO_PI) * 15 - 20) * Mth.DEG_TO_RAD;
			if (arm == HumanoidArm.LEFT) {
				model.leftArm.xRot = armRotation;
			} else {
				model.rightArm.xRot = armRotation;
			}
		}
	}
}
