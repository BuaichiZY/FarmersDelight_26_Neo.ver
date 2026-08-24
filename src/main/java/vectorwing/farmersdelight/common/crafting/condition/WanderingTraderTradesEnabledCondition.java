package vectorwing.farmersdelight.common.crafting.condition;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.Configuration;

public final class WanderingTraderTradesEnabledCondition implements ICondition
{
	public static final MapCodec<WanderingTraderTradesEnabledCondition> CODEC = MapCodec.unit(new WanderingTraderTradesEnabledCondition());

	@Override
	public boolean test(@NotNull IContext context) {
		return Configuration.ENABLE_WANDERING_TRADER_SELLS_FD_ITEMS.get();
	}

	@Override
	public @NotNull MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}
