package vectorwing.farmersdelight.common.loot.modifier;

import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Optional;

final class LootModifierConditions
{
	private LootModifierConditions() {
	}

	static Optional<Holder<LootItemCondition>> combine(LootItemCondition[] conditions) {
		return ConditionUserBuilder.buildCondition(Arrays.stream(conditions).map(Holder::direct).toList());
	}
}
