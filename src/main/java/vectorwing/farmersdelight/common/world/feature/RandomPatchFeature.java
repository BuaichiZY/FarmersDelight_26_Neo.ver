package vectorwing.farmersdelight.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import vectorwing.farmersdelight.common.world.configuration.PatchConfiguration;

public class RandomPatchFeature extends Feature<PatchConfiguration>
{
	public RandomPatchFeature(Codec<PatchConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<PatchConfiguration> context) {
		WorldGenLevel level = context.level();
		PatchConfiguration config = context.config();
		RandomSource random = context.random();
		BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
		int placed = 0;

		for (int attempt = 0; attempt < config.tries(); attempt++) {
			target.set(context.origin()).move(
					random.nextInt(config.xzSpread() + 1) - random.nextInt(config.xzSpread() + 1),
					random.nextInt(config.ySpread() + 1) - random.nextInt(config.ySpread() + 1),
					random.nextInt(config.xzSpread() + 1) - random.nextInt(config.xzSpread() + 1)
			);
			if (config.feature().value().place(level, context.chunkGenerator(), random, target)) {
				placed++;
			}
		}

		return placed > 0;
	}
}
