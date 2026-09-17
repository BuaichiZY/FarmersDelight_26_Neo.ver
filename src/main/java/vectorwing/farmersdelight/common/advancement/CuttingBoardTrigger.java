package vectorwing.farmersdelight.common.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import vectorwing.farmersdelight.common.registry.ModAdvancements;

import java.util.Optional;

public class CuttingBoardTrigger extends SimpleCriterionTrigger<CuttingBoardTrigger.TriggerInstance>
{
	@Override
	public Codec<TriggerInstance> codec() {
		return CuttingBoardTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, TriggerInstance::test);
	}

	public static record TriggerInstance(
			Optional<Holder<LootItemCondition>> player) implements SimpleCriterionTrigger.SimpleInstance
	{
		public static final Codec<CuttingBoardTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
				builder -> builder.group(
								LootItemCondition.CODEC.optionalFieldOf("player").forGetter(CuttingBoardTrigger.TriggerInstance::player))
						.apply(builder, CuttingBoardTrigger.TriggerInstance::new)
		);

		public static Criterion<TriggerInstance> simple() {
			return ModAdvancements.USE_CUTTING_BOARD.get().createCriterion(
					new CuttingBoardTrigger.TriggerInstance(Optional.empty())
			);
		}

		public boolean test() {
			return true;
		}
	}
}
