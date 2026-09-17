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
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public record WildCropFeature(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> primaryFeature,
		Holder<PlacedFeature> secondaryFeature, @Nullable Holder<PlacedFeature> floorFeature) implements Feature
{
	public static final MapCodec<WildCropFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(64).forGetter(WildCropFeature::tries),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(4).forGetter(WildCropFeature::xzSpread),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(WildCropFeature::ySpread),
			PlacedFeature.CODEC.fieldOf("primary_feature").forGetter(WildCropFeature::primaryFeature),
			PlacedFeature.CODEC.fieldOf("secondary_feature").forGetter(WildCropFeature::secondaryFeature),
			PlacedFeature.CODEC.optionalFieldOf("floor_feature").forGetter(feature -> Optional.ofNullable(feature.floorFeature))
	).apply(instance, (tries, xzSpread, ySpread, primary, secondary, floor) ->
			new WildCropFeature(tries, xzSpread, ySpread, primary, secondary, floor.orElse(null))));

	@Override
	public MapCodec<WildCropFeature> codec() {
		return CODEC;
	}

	@Override
	public Stream<Holder<Feature>> getSubFeatures() {
		Stream<Holder<Feature>> features = Stream.concat(primaryFeature.value().getFeatures(), secondaryFeature.value().getFeatures());
		return floorFeature == null ? features : Stream.concat(features, floorFeature.value().getFeatures());
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int placed = 0;
		int horizontalSpread = xzSpread + 1;
		int verticalSpread = ySpread + 1;
		BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();

		if (floorFeature != null) {
			for (int attempt = 0; attempt < tries; ++attempt) {
				target.setWithOffset(origin, random.nextInt(horizontalSpread) - random.nextInt(horizontalSpread),
						random.nextInt(verticalSpread) - random.nextInt(verticalSpread),
						random.nextInt(horizontalSpread) - random.nextInt(horizontalSpread));
				if (floorFeature.value().place(level, generator, random, target)) {
					placed++;
				}
			}
		}

		int primarySpread = Math.max(1, horizontalSpread - 2);
		for (int attempt = 0; attempt < tries; ++attempt) {
			target.setWithOffset(origin, random.nextInt(primarySpread) - random.nextInt(primarySpread),
					random.nextInt(verticalSpread) - random.nextInt(verticalSpread),
					random.nextInt(primarySpread) - random.nextInt(primarySpread));
			if (primaryFeature.value().place(level, generator, random, target)) {
				placed++;
			}
		}

		for (int attempt = 0; attempt < tries; ++attempt) {
			target.setWithOffset(origin, random.nextInt(horizontalSpread) - random.nextInt(horizontalSpread),
					random.nextInt(verticalSpread) - random.nextInt(verticalSpread),
					random.nextInt(horizontalSpread) - random.nextInt(horizontalSpread));
			if (secondaryFeature.value().place(level, generator, random, target)) {
				placed++;
			}
		}

		return placed > 0;
	}
}
