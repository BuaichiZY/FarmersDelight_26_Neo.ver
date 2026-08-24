package vectorwing.farmersdelight.common.mixin;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(Villager.class)
public interface VillagerAccessor
{
	@Accessor("FOOD_POINTS")
	@Mutable
	static void farmersdelight$setFoodPoints(Map<Item, Integer> foodPoints) {
		throw new AssertionError();
	}
}
