package vectorwing.farmersdelight.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.IdentityHashMap;
import java.util.Map;

public class FoodValues
{
	public static final int BRIEF_DURATION = 600;     // 30 seconds
	public static final int SHORT_DURATION = 1200;    // 1 minute
	public static final int MEDIUM_DURATION = 3600;   // 3 minutes
	public static final int LONG_DURATION = 6000;     // 5 minutes

	private static final float FAST_CONSUME_SECONDS = 0.8F;
	private static final Map<FoodProperties, Consumable> CUSTOM_CONSUMABLES = new IdentityHashMap<>();

	private static FoodProperties food(int nutrition, float saturationModifier) {
		return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationModifier).build();
	}

	private static FoodProperties food(int nutrition, float saturationModifier, boolean alwaysEdible) {
		FoodProperties.Builder builder = new FoodProperties.Builder()
				.nutrition(nutrition)
				.saturationModifier(saturationModifier);
		if (alwaysEdible) {
			builder.alwaysEdible();
		}
		return builder.build();
	}

	private static FoodProperties withConsumable(FoodProperties food, Consumable consumable) {
		CUSTOM_CONSUMABLES.put(food, consumable);
		return food;
	}

	private static Consumable consumable(MobEffectInstance effect, float probability) {
		return Consumables.defaultFood()
				.onConsume(new ApplyStatusEffectsConsumeEffect(effect, probability))
				.build();
	}

	private static Consumable fastConsumable() {
		return Consumables.defaultFood().consumeSeconds(FAST_CONSUME_SECONDS).build();
	}

	private static Consumable fastConsumable(MobEffectInstance effect, float probability) {
		return Consumables.defaultFood()
				.consumeSeconds(FAST_CONSUME_SECONDS)
				.onConsume(new ApplyStatusEffectsConsumeEffect(effect, probability))
				.build();
	}

	private static Consumable drinkConsumable(MobEffectInstance effect, float probability) {
		return Consumables.defaultDrink()
				.onConsume(new ApplyStatusEffectsConsumeEffect(effect, probability))
				.build();
	}

	public static Consumable consumableFor(FoodProperties food) {
		return CUSTOM_CONSUMABLES.getOrDefault(food, Consumables.DEFAULT_FOOD);
	}

	public static MobEffectInstance nourishment(int duration) {
		// Store the registry's real Holder.Reference in the item component. Hybrid
		// Bukkit servers do not recognize NeoForge's DeferredHolder wrapper when
		// converting potion-effect events to their Bukkit representation.
		return new MobEffectInstance(ModEffects.NOURISHMENT.getDelegate(), duration, 0, false, false);
	}

	// Raw Crops
	public static final FoodProperties CABBAGE = food(2, 0.4F);
	public static final FoodProperties TOMATO = food(1, 0.3F);
	public static final FoodProperties ONION = food(2, 0.4F);

	// Drinks (mostly for effects)
	public static final FoodProperties APPLE_CIDER = withConsumable(
			food(0, 0.0F, true),
			drinkConsumable(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0), 1.0F));

	// Basic Foods
	public static final FoodProperties FRIED_EGG = food(4, 0.4F);
	public static final FoodProperties TOMATO_SAUCE = food(4, 0.4F);
	public static final FoodProperties WHEAT_DOUGH = withConsumable(food(2, 0.3F),
			consumable(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F));
	public static final FoodProperties RAW_PASTA = withConsumable(food(2, 0.3F),
			consumable(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F));
	public static final FoodProperties PIE_CRUST = food(2, 0.2F);
	public static final FoodProperties PUMPKIN_SLICE = food(3, 0.3F);
	public static final FoodProperties CABBAGE_LEAF = withConsumable(food(1, 0.4F), fastConsumable());
	public static final FoodProperties MINCED_BEEF = withConsumable(food(2, 0.3F), fastConsumable());
	public static final FoodProperties BEEF_PATTY = withConsumable(food(4, 0.8F), fastConsumable());
	public static final FoodProperties CHICKEN_CUTS = withConsumable(food(1, 0.3F),
			fastConsumable(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F));
	public static final FoodProperties COOKED_CHICKEN_CUTS = withConsumable(food(3, 0.6F), fastConsumable());
	public static final FoodProperties BACON = withConsumable(food(2, 0.3F), fastConsumable());
	public static final FoodProperties COOKED_BACON = withConsumable(food(4, 0.8F), fastConsumable());
	public static final FoodProperties COD_SLICE = withConsumable(food(1, 0.1F), fastConsumable());
	public static final FoodProperties COOKED_COD_SLICE = withConsumable(food(3, 0.5F), fastConsumable());
	public static final FoodProperties SALMON_SLICE = withConsumable(food(1, 0.1F), fastConsumable());
	public static final FoodProperties COOKED_SALMON_SLICE = withConsumable(food(3, 0.8F), fastConsumable());
	public static final FoodProperties MUTTON_CHOPS = withConsumable(food(1, 0.3F), fastConsumable());
	public static final FoodProperties COOKED_MUTTON_CHOPS = withConsumable(food(3, 0.8F), fastConsumable());
	public static final FoodProperties HAM = food(5, 0.3F);
	public static final FoodProperties SMOKED_HAM = food(10, 0.8F);

	// Sweets
	public static final FoodProperties POPSICLE = withConsumable(food(3, 0.2F, true), fastConsumable());
	public static final FoodProperties COOKIES = withConsumable(food(2, 0.1F), fastConsumable());
	public static final FoodProperties CAKE_SLICE = withConsumable(food(2, 0.1F),
			fastConsumable(new MobEffectInstance(MobEffects.SPEED, 400, 0, false, false), 1.0F));
	public static final FoodProperties PIE_SLICE = withConsumable(food(3, 0.3F),
			fastConsumable(new MobEffectInstance(MobEffects.SPEED, 600, 0, false, false), 1.0F));
	public static final FoodProperties FRUIT_SALAD = withConsumable(food(6, 0.6F),
			consumable(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F));
	public static final FoodProperties GLOW_BERRY_CUSTARD = withConsumable(food(7, 0.6F, true),
			consumable(new MobEffectInstance(MobEffects.GLOWING, 100, 0), 1.0F));

	// Handheld Foods
	public static final FoodProperties MIXED_SALAD = withConsumable(food(6, 0.6F),
			consumable(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F));
	public static final FoodProperties NETHER_SALAD = withConsumable(food(5, 0.4F),
			consumable(new MobEffectInstance(MobEffects.NAUSEA, 240, 0), 0.3F));
	public static final FoodProperties BARBECUE_STICK = food(8, 0.9F);
	public static final FoodProperties EGG_SANDWICH = food(8, 0.8F);
	public static final FoodProperties CHICKEN_SANDWICH = food(10, 0.8F);
	public static final FoodProperties HAMBURGER = food(11, 0.8F);
	public static final FoodProperties BACON_SANDWICH = food(10, 0.8F);
	public static final FoodProperties MUTTON_WRAP = food(10, 0.8F);
	public static final FoodProperties DUMPLINGS = food(8, 0.8F);
	public static final FoodProperties STUFFED_POTATO = food(10, 0.7F);
	public static final FoodProperties CABBAGE_ROLLS = food(5, 0.5F);
	public static final FoodProperties SALMON_ROLL = food(7, 0.6F);
	public static final FoodProperties COD_ROLL = food(7, 0.6F);
	public static final FoodProperties KELP_ROLL = new FoodProperties(12, 12.0F, false);
	public static final FoodProperties KELP_ROLL_SLICE = withConsumable(food(6, 0.5F), fastConsumable());

	// Bowl Foods
	public static final FoodProperties COOKED_RICE = nourishmentFood(6, 0.4F, BRIEF_DURATION);
	public static final FoodProperties BONE_BROTH = nourishmentFood(8, 0.7F, SHORT_DURATION);
	public static final FoodProperties BEEF_STEW = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties VEGETABLE_SOUP = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties FISH_STEW = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties ONION_SOUP = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties CHICKEN_SOUP = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties FRIED_RICE = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties PUMPKIN_SOUP = nourishmentFood(14, 0.75F, LONG_DURATION);
	public static final FoodProperties BAKED_COD_STEW = nourishmentFood(14, 0.75F, LONG_DURATION);
	public static final FoodProperties NOODLE_SOUP = nourishmentFood(14, 0.75F, LONG_DURATION);

	// Plated Foods
	public static final FoodProperties BACON_AND_EGGS = nourishmentFood(10, 0.6F, SHORT_DURATION);
	public static final FoodProperties RATATOUILLE = nourishmentFood(10, 0.6F, SHORT_DURATION);
	public static final FoodProperties STEAK_AND_POTATOES = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties PASTA_WITH_MEATBALLS = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties PASTA_WITH_MUTTON_CHOP = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties MUSHROOM_RICE = nourishmentFood(12, 0.8F, MEDIUM_DURATION);
	public static final FoodProperties ROASTED_MUTTON_CHOPS = nourishmentFood(14, 0.75F, LONG_DURATION);
	public static final FoodProperties VEGETABLE_NOODLES = nourishmentFood(14, 0.75F, LONG_DURATION);
	public static final FoodProperties SQUID_INK_PASTA = nourishmentFood(14, 0.75F, LONG_DURATION);
	public static final FoodProperties GRILLED_SALMON = nourishmentFood(14, 0.75F, MEDIUM_DURATION);

	// Feast Portions
	public static final FoodProperties ROAST_CHICKEN = nourishmentFood(14, 0.75F, LONG_DURATION);
	public static final FoodProperties STUFFED_PUMPKIN = nourishmentFood(14, 0.75F, LONG_DURATION);
	public static final FoodProperties HONEY_GLAZED_HAM = nourishmentFood(14, 0.75F, LONG_DURATION);
	public static final FoodProperties SHEPHERDS_PIE = nourishmentFood(14, 0.75F, LONG_DURATION);
	public static final FoodProperties GLEAMING_SALAD = nourishmentFood(14, 0.75F, LONG_DURATION);

	public static final FoodProperties DOG_FOOD = food(4, 0.2F);

	// Vanilla soup effects are applied from the use-item event because these are vanilla items.
	public static final Map<Item, MobEffectInstance> VANILLA_SOUP_EFFECTS = new ImmutableMap.Builder<Item, MobEffectInstance>()
			.put(Items.MUSHROOM_STEW, nourishment(MEDIUM_DURATION))
			.put(Items.BEETROOT_SOUP, nourishment(MEDIUM_DURATION))
			.put(Items.RABBIT_STEW, nourishment(LONG_DURATION))
			.build();

	public static final FoodProperties RABBIT_STEW_BUFF = food(14, 0.75F);
	public static final Consumable RABBIT_STEW_CONSUMABLE = consumable(nourishment(LONG_DURATION), 1.0F);

	private static FoodProperties nourishmentFood(int nutrition, float saturationModifier, int duration) {
		return withConsumable(food(nutrition, saturationModifier), consumable(nourishment(duration), 1.0F));
	}
}
