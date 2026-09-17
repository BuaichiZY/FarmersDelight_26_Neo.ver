package vectorwing.farmersdelight.common.world;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import vectorwing.farmersdelight.FarmersDelight;

/**
 * Generates the dynamic registry entries used by Farmer's Delight worldgen.
 *
 * <p>This small provider intentionally lives outside the legacy data generator package so
 * normal 26.3 builds can regenerate the migrated feature format without compiling data
 * providers that still target older Minecraft APIs.</p>
 */
@EventBusSubscriber(modid = FarmersDelight.MODID)
public final class WorldgenDataGenerator
{
	private WorldgenDataGenerator() {}

	@SubscribeEvent
	public static void gatherServerData(GatherDataEvent.Client event) {
		RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder()
				.add(Registries.FEATURE, WildCropGeneration::bootstrapFeatures)
				.add(Registries.PLACED_FEATURE, WildCropGeneration::bootstrapPlacedFeatures);

		event.createWorldRegistryObjects(registrySetBuilder);
	}
}
