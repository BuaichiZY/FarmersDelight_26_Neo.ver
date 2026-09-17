package vectorwing.farmersdelight.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import vectorwing.farmersdelight.FarmersDelight;

/** Context-aware furnace burn times not supplied by vanilla. */
public final class ModContextIntProviders
{
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_400 = createKey("cooking/time_400");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_1000 = createKey("cooking/time_1000");

	private ModContextIntProviders() {}

	private static ResourceKey<ContextIntProvider> createKey(String path) {
		return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER,
				Identifier.fromNamespaceAndPath(FarmersDelight.MODID, path));
	}
}
