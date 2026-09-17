package vectorwing.farmersdelight.common.event;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.FoodValues;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.registry.ModBlocks;

@EventBusSubscriber(modid = FarmersDelight.MODID)
public class CommonEvents
{
	@SubscribeEvent
	public static void tillRichSoil(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		ItemStack tool = event.getItemStack();
		if (!level.getBlockState(event.getPos()).is(ModBlocks.RICH_SOIL.get())
				|| !tool.is(ItemTags.HOES)
				|| event.getFace() == Direction.DOWN
				|| !level.getBlockState(event.getPos().above()).isAir()) {
			return;
		}

		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, event.getPos(), tool);
		}
		tool.hurtAndBreak(1, event.getEntity(), event.getHand().asEquipmentSlot());
		level.setBlock(event.getPos(), ModBlocks.RICH_SOIL_FARMLAND.get().defaultBlockState(), 11);
		level.playSound(event.getEntity(), event.getPos(), SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
		level.gameEvent(GameEvent.BLOCK_CHANGE, event.getPos(), GameEvent.Context.of(event.getEntity(), ModBlocks.RICH_SOIL_FARMLAND.get().defaultBlockState()));
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void syncRecipeContents(OnDatapackSyncEvent event) {
		event.sendRecipes(
				ModRecipeTypes.COOKING.get(),
				ModRecipeTypes.CUTTING.get(),
				net.minecraft.world.item.crafting.RecipeType.CRAFTING
		);
	}

	@SubscribeEvent
	public static void handleVanillaSoupEffects(LivingEntityUseItemEvent.Finish event) {
		Item food = event.getItem().getItem();
		LivingEntity entity = event.getEntity();

		if (Configuration.ENABLE_RABBIT_STEW_BUFF.get() && food.equals(Items.RABBIT_STEW)) {
			return;
		}

		if (Configuration.ENABLE_VANILLA_SOUP_EXTRA_EFFECTS.get()) {
			MobEffectInstance soupEffect = FoodValues.VANILLA_SOUP_EFFECTS.get(food);

			if (soupEffect != null) {
				entity.addEffect(new MobEffectInstance(soupEffect));
			}
		}
	}
}
