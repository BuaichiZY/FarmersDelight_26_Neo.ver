package vectorwing.farmersdelight.common.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.world.feature.RandomPatchFeature;
import vectorwing.farmersdelight.common.world.feature.WildCropFeature;
import vectorwing.farmersdelight.common.world.feature.WildRiceFeature;

import java.util.function.Supplier;

public class ModBiomeFeatures
{
	public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE_TYPE.key(), FarmersDelight.MODID);

	public static final Supplier<MapCodec<? extends Feature>> RANDOM_PATCH = FEATURES.register("random_patch", () -> RandomPatchFeature.CODEC);
	public static final Supplier<MapCodec<? extends Feature>> WILD_RICE = FEATURES.register("wild_rice", () -> WildRiceFeature.CODEC);
	public static final Supplier<MapCodec<? extends Feature>> WILD_CROP = FEATURES.register("wild_crop", () -> WildCropFeature.CODEC);
}
