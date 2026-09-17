package vectorwing.farmersdelight.common.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.world.filter.BiomeTagFilter;

import java.util.function.Supplier;

public class ModPlacementModifiers
{
	public static final DeferredRegister<MapCodec<? extends PlacementModifier>> PLACEMENT_MODIFIERS = DeferredRegister.create(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE.key(), FarmersDelight.MODID);

	public static final Supplier<MapCodec<? extends PlacementModifier>> BIOME_TAG = PLACEMENT_MODIFIERS.register("biome_tag", () -> BiomeTagFilter.CODEC);
}
