package vectorwing.farmersdelight.common.crafting.condition;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.Configuration;

public final class FarmerTradesEnabledCondition implements ICondition
{
	public static final MapCodec<FarmerTradesEnabledCondition> CODEC = MapCodec.unit(new FarmerTradesEnabledCondition());

	@Override
	public boolean test(@NotNull IContext context) {
		return Configuration.ENABLE_FARMERS_BUY_FD_CROPS.get();
	}

	@Override
	public @NotNull MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}
