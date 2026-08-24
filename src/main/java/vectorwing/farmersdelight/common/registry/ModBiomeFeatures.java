package vectorwing.farmersdelight.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.world.configuration.WildCropConfiguration;
import vectorwing.farmersdelight.common.world.configuration.PatchConfiguration;
import vectorwing.farmersdelight.common.world.feature.RandomPatchFeature;
import vectorwing.farmersdelight.common.world.feature.WildCropFeature;
import vectorwing.farmersdelight.common.world.feature.WildRiceFeature;

import java.util.function.Supplier;

public class ModBiomeFeatures
{
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, FarmersDelight.MODID);

	public static final Supplier<Feature<PatchConfiguration>> RANDOM_PATCH = FEATURES.register("random_patch", () -> new RandomPatchFeature(PatchConfiguration.CODEC));
	public static final Supplier<Feature<PatchConfiguration>> WILD_RICE = FEATURES.register("wild_rice", () -> new WildRiceFeature(PatchConfiguration.CODEC));
	public static final Supplier<Feature<WildCropConfiguration>> WILD_CROP = FEATURES.register("wild_crop", () -> new WildCropFeature(WildCropConfiguration.CODEC));
}
