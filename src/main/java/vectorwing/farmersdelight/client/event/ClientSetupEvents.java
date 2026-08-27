package vectorwing.farmersdelight.client.event;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.client.gui.CookingPotTooltip;
import vectorwing.farmersdelight.client.gui.HUDOverlays;
import vectorwing.farmersdelight.client.particle.SparkleParticle;
import vectorwing.farmersdelight.client.particle.StarParticle;
import vectorwing.farmersdelight.client.particle.SteamParticle;
import vectorwing.farmersdelight.client.recipebook.RecipeCategories;
import vectorwing.farmersdelight.client.renderer.*;
import vectorwing.farmersdelight.common.EnumParameters;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;
import vectorwing.farmersdelight.common.registry.*;

@EventBusSubscriber(modid = FarmersDelight.MODID, value = Dist.CLIENT)
public class ClientSetupEvents
{
	public static void init(final FMLClientSetupEvent event) {
	}

	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		 event.registerItem(new IClientItemExtensions()
		{
			@Override
			public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity living, InteractionHand hand, ItemStack stack) {
				return stack.has(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get()) ? EnumParameters.PROXY_SKILLET_FLIP.getValue() : null;
			}
		}, ModItems.SKILLET.get());
	}

	@SubscribeEvent
	public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
		event.register(
				net.minecraft.resources.Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "skillet"),
				SkilletItemRenderer.Unbaked.MAP_CODEC
		);
	}

	@SubscribeEvent
	public static void registerRecipeBookCategories(RegisterRecipeBookSearchCategoriesEvent event) {
		RecipeCategories.init(event);
	}

	@SubscribeEvent
	public static void registerCustomTooltipRenderers(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(CookingPotTooltip.CookingPotTooltipComponent.class, CookingPotTooltip::new);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void registerGuiLayers(RegisterGuiLayersEvent event) {
		HUDOverlays.register(event);
	}

	@SubscribeEvent
	public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntityTypes.ROTTEN_TOMATO.get(), ThrownItemRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntityTypes.STOVE.get(), DefaultStoveRenderer<StoveBlockEntity>::new);
		event.registerBlockEntityRenderer(ModBlockEntityTypes.CUTTING_BOARD.get(), CuttingBoardRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntityTypes.CANVAS_SIGN.get(), CanvasSignRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntityTypes.HANGING_CANVAS_SIGN.get(), HangingCanvasSignRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntityTypes.SKILLET.get(), SkilletRenderer::new);
	}

	@SubscribeEvent
	public static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenuTypes.COOKING_POT.get(), CookingPotScreen::new);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(ModParticleTypes.STAR.get(), StarParticle.Factory::new);
		event.registerSpriteSet(ModParticleTypes.STEAM.get(), SteamParticle.Factory::new);
		event.registerSpriteSet(ModParticleTypes.SPARKLE.get(), SparkleParticle.Factory::new);
	}

	@SubscribeEvent
	public static void receiveRecipeContents(RecipesReceivedEvent event) {
		vectorwing.farmersdelight.client.recipebook.ClientRecipeCache.setRecipeMap(event.getRecipeMap());
	}

	@SubscribeEvent
	public static void clearRecipeContents(ClientPlayerNetworkEvent.LoggingOut event) {
		vectorwing.farmersdelight.client.recipebook.ClientRecipeCache.clear();
	}
}
