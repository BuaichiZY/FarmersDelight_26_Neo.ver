package vectorwing.farmersdelight.common.world.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import vectorwing.farmersdelight.common.block.WildRiceBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.stream.Stream;

public record WildRiceFeature(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements Feature
{
	public static final MapCodec<WildRiceFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter(WildRiceFeature::tries),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(WildRiceFeature::xzSpread),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(WildRiceFeature::ySpread),
			PlacedFeature.CODEC.fieldOf("feature").forGetter(WildRiceFeature::feature)
	).apply(instance, WildRiceFeature::new));

	@Override
	public MapCodec<WildRiceFeature> codec() {
		return CODEC;
	}

	@Override
	public Stream<Holder<Feature>> getSubFeatures() {
		return feature.value().getFeatures();
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		BlockPos floor = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, origin);
		int placed = 0;
		BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();

		for (int attempt = 0; attempt < tries; ++attempt) {
			target.set(floor).move(
					random.nextInt(xzSpread + 1) - random.nextInt(xzSpread + 1),
					random.nextInt(ySpread + 1) - random.nextInt(ySpread + 1),
					random.nextInt(xzSpread + 1) - random.nextInt(xzSpread + 1));

			if (level.getBlockState(target).is(Blocks.WATER) && level.getBlockState(target.above()).isAir()) {
				BlockState bottomRiceState = ModBlocks.WILD_RICE.get().defaultBlockState().setValue(WildRiceBlock.HALF, DoubleBlockHalf.LOWER);
				if (bottomRiceState.canSurvive(level, target)) {
					DoublePlantBlock.placeAt(level, bottomRiceState, target, 2);
					placed++;
				}
			}
		}

		return placed > 0;
	}
}
