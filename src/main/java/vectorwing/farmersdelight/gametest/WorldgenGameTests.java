package vectorwing.farmersdelight.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.List;
import java.util.EnumSet;

/** Checks loaded biome injection and the full placement pipeline on controlled terrain. */
final class WorldgenGameTests {
    private record Crop(String name, Block plant, Block floor, ResourceKey<Biome> biome, boolean aquatic) {}

    static void testWildCropWorldgen(GameTestHelper helper) {
        var cases = List.of(
                new Crop("wild_carrots", ModBlocks.WILD_CARROTS.get(), Blocks.GRASS_BLOCK, Biomes.PLAINS, false),
                new Crop("wild_onions", ModBlocks.WILD_ONIONS.get(), Blocks.GRASS_BLOCK, Biomes.PLAINS, false),
                new Crop("wild_potatoes", ModBlocks.WILD_POTATOES.get(), Blocks.PODZOL, Biomes.TAIGA, false),
                new Crop("wild_tomatoes", ModBlocks.WILD_TOMATOES.get(), Blocks.SAND, Biomes.DESERT, false),
                new Crop("wild_cabbages", ModBlocks.WILD_CABBAGES.get(), Blocks.SAND, Biomes.BEACH, false),
                new Crop("wild_beetroots", ModBlocks.WILD_BEETROOTS.get(), Blocks.SAND, Biomes.BEACH, false),
                new Crop("wild_rice", ModBlocks.WILD_RICE.get(), Blocks.MUD, Biomes.SWAMP, true),
                new Crop("brown_mushroom_colony", ModBlocks.BROWN_MUSHROOM_COLONY.get(), Blocks.MYCELIUM, Biomes.MUSHROOM_FIELDS, false),
                new Crop("red_mushroom_colony", ModBlocks.RED_MUSHROOM_COLONY.get(), Blocks.MYCELIUM, Biomes.MUSHROOM_FIELDS, false));
        var level = helper.getLevel();
        var registries = level.registryAccess();
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        var features = registries.lookupOrThrow(Registries.PLACED_FEATURE);
        var modifiers = registries.lookupOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS);
        var noise = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD);
        // Isolated test area in the disposable GameTest world, away from the other suites.
        BlockPos origin = helper.absolutePos(new BlockPos(96, 8, 96));
        origin = new BlockPos((origin.getX() >> 4) << 4, origin.getY(), (origin.getZ() >> 4) << 4);
        for (Crop crop : cases) {
            helper.assertTrue(modifiers.get(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id(crop.name))).isPresent(),
                    "Missing biome modifier: " + crop.name);
            Holder<Biome> biome = biomes.getOrThrow(crop.biome);
            PlacedFeature feature = features.getOrThrow(ResourceKey.create(Registries.PLACED_FEATURE, id("patch_" + crop.name))).value();
            helper.assertTrue(biome.value().getGenerationSettings().hasFeature(feature),
                    crop.name + " was not injected into " + crop.biome.identifier());
            helper.assertTrue(!biomes.getOrThrow(Biomes.NETHER_WASTES).value().getGenerationSettings().hasFeature(feature)
                            && !biomes.getOrThrow(Biomes.THE_END).value().getGenerationSettings().hasFeature(feature),
                    crop.name + " leaked into another dimension");
            var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(biome), noise);
            for (int cx = -1; cx <= 2; cx++) {
                for (int cz = -1; cz <= 2; cz++) {
                    level.getChunk((origin.getX() >> 4) + cx, (origin.getZ() >> 4) + cz)
                            .fillBiomesFromNoise((x, y, z, sampler) -> biome, level.getChunkSource().randomState().sampler());
                }
            }
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -2, -8), origin.offset(23, 5, 23))) {
                Block block = pos.getY() < origin.getY() ? crop.floor
                        : crop.aquatic && pos.getY() == origin.getY() ? Blocks.WATER : Blocks.AIR;
                level.setBlock(pos, block.defaultBlockState(), 2);
            }
            // Runtime chunks do not normally retain the worldgen-only heightmaps.
            // Rebuild the ocean-floor map used by rice, just as terrain generation does.
            for (int cx = -1; cx <= 2; cx++) {
                for (int cz = -1; cz <= 2; cz++) {
                    Heightmap.primeHeightmaps(level.getChunk((origin.getX() >> 4) + cx, (origin.getZ() >> 4) + cz),
                            EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG));
                }
            }
            RandomSource random = RandomSource.create(9182026L);
            int count = 0;
            for (int attempt = 1; attempt <= 2048 && count == 0; attempt++) {
                if (feature.placeWithBiomeCheck(level, generator, random, origin)) {
                    for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, 0, -8), origin.offset(23, 2, 23))) {
                        if (level.getBlockState(pos).is(crop.plant)) {
                            helper.assertTrue(level.getBlockState(pos).canSurvive(level, pos),
                                    crop.name + " cannot survive on its generated substrate");
                            count++;
                        }
                    }
                }
            }
            helper.assertTrue(count > 0, crop.name + " placed no crops through its loaded placement pipeline");
            FarmersDelight.LOGGER.info("Worldgen regression: {} placed {} blocks in {}", crop.name, count, crop.biome.identifier());
        }
        helper.succeed();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, path);
    }
}
