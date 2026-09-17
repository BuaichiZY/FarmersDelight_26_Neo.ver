package vectorwing.farmersdelight.common.world.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.stream.Stream;

public record RandomPatchFeature(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements Feature
{
	public static final MapCodec<RandomPatchFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter(RandomPatchFeature::tries),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(RandomPatchFeature::xzSpread),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(RandomPatchFeature::ySpread),
			PlacedFeature.CODEC.fieldOf("feature").forGetter(RandomPatchFeature::feature)
	).apply(instance, RandomPatchFeature::new));

	@Override
	public MapCodec<RandomPatchFeature> codec() {
		return CODEC;
	}

	@Override
	public Stream<Holder<Feature>> getSubFeatures() {
		return feature.value().getFeatures();
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
		int placed = 0;

		for (int attempt = 0; attempt < tries; attempt++) {
			target.set(origin).move(
					random.nextInt(xzSpread + 1) - random.nextInt(xzSpread + 1),
					random.nextInt(ySpread + 1) - random.nextInt(ySpread + 1),
					random.nextInt(xzSpread + 1) - random.nextInt(xzSpread + 1)
			);
			if (feature.value().place(level, generator, random, target)) {
				placed++;
			}
		}

		return placed > 0;
	}
}
