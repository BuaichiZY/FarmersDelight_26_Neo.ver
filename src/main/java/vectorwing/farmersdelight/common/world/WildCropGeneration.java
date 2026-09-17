package vectorwing.farmersdelight.common.world;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.world.feature.RandomPatchFeature;
import vectorwing.farmersdelight.common.world.feature.WildCropFeature;
import vectorwing.farmersdelight.common.world.feature.WildRiceFeature;
import vectorwing.farmersdelight.common.world.filter.BiomeTagFilter;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("SameParameterValue")
public class WildCropGeneration
{
	public static final ResourceKey<Feature> FEATURE_PATCH_SANDY_SHRUB = registerFeatureKey("patch_sandy_shrub");
	public static final ResourceKey<Feature> FEATURE_PATCH_WILD_CABBAGES = registerFeatureKey("patch_wild_cabbages");
	public static final ResourceKey<Feature> FEATURE_PATCH_WILD_ONIONS = registerFeatureKey("patch_wild_onions");
	public static final ResourceKey<Feature> FEATURE_PATCH_WILD_TOMATOES = registerFeatureKey("patch_wild_tomatoes");
	public static final ResourceKey<Feature> FEATURE_PATCH_WILD_CARROTS = registerFeatureKey("patch_wild_carrots");
	public static final ResourceKey<Feature> FEATURE_PATCH_WILD_POTATOES = registerFeatureKey("patch_wild_potatoes");
	public static final ResourceKey<Feature> FEATURE_PATCH_WILD_BEETROOTS = registerFeatureKey("patch_wild_beetroots");
	public static final ResourceKey<Feature> FEATURE_PATCH_WILD_RICE = registerFeatureKey("patch_wild_rice");
	public static final ResourceKey<Feature> FEATURE_PATCH_BROWN_MUSHROOM_COLONIES = registerFeatureKey("patch_brown_mushroom_colony");
	public static final ResourceKey<Feature> FEATURE_PATCH_RED_MUSHROOM_COLONIES = registerFeatureKey("patch_red_mushroom_colony");

	public static final ResourceKey<PlacedFeature> PATCH_WILD_CABBAGES = registerPlacedFeatureKey("patch_wild_cabbages");
	public static final ResourceKey<PlacedFeature> PATCH_WILD_ONIONS = registerPlacedFeatureKey("patch_wild_onions");
	public static final ResourceKey<PlacedFeature> PATCH_WILD_TOMATOES = registerPlacedFeatureKey("patch_wild_tomatoes");
	public static final ResourceKey<PlacedFeature> PATCH_WILD_CARROTS = registerPlacedFeatureKey("patch_wild_carrots");
	public static final ResourceKey<PlacedFeature> PATCH_WILD_POTATOES = registerPlacedFeatureKey("patch_wild_potatoes");
	public static final ResourceKey<PlacedFeature> PATCH_WILD_BEETROOTS = registerPlacedFeatureKey("patch_wild_beetroots");
	public static final ResourceKey<PlacedFeature> PATCH_WILD_RICE = registerPlacedFeatureKey("patch_wild_rice");
	public static final ResourceKey<PlacedFeature> PATCH_BROWN_MUSHROOM_COLONIES = registerPlacedFeatureKey("patch_brown_mushroom_colony");
	public static final ResourceKey<PlacedFeature> PATCH_RED_MUSHROOM_COLONIES = registerPlacedFeatureKey("patch_red_mushroom_colony");

	private static ResourceKey<Feature> registerFeatureKey(String name) {
		return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(FarmersDelight.MODID, name));
	}

	private static ResourceKey<PlacedFeature> registerPlacedFeatureKey(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(FarmersDelight.MODID, name));
	}

	public static void bootstrapFeatures(BootstrapContext<Feature> context) {
		context.register(FEATURE_PATCH_SANDY_SHRUB, new RandomPatchFeature(
				32, 2, 3, plantPlacedFeature(ModBlocks.SANDY_SHRUB.get(), BlockTags.SAND)));
		context.register(FEATURE_PATCH_WILD_CABBAGES, wildCropFeature(
				ModBlocks.WILD_CABBAGES.get(), ModBlocks.SANDY_SHRUB.get(), BlockTags.SAND));
		context.register(FEATURE_PATCH_WILD_BEETROOTS, wildCropFeature(
				ModBlocks.WILD_BEETROOTS.get(), ModBlocks.SANDY_SHRUB.get(), BlockTags.SAND));
		context.register(FEATURE_PATCH_WILD_CARROTS, wildCropFeature(
				ModBlocks.WILD_CARROTS.get(), Blocks.SHORT_GRASS, Blocks.COARSE_DIRT, BlockTags.DIRT));
		context.register(FEATURE_PATCH_WILD_ONIONS, wildCropFeature(
				ModBlocks.WILD_ONIONS.get(), Blocks.ALLIUM, BlockTags.DIRT));
		context.register(FEATURE_PATCH_WILD_POTATOES, wildCropFeature(
				ModBlocks.WILD_POTATOES.get(), Blocks.FERN, BlockTags.DIRT));
		context.register(FEATURE_PATCH_WILD_TOMATOES, wildCropFeature(
				ModBlocks.WILD_TOMATOES.get(), Blocks.DEAD_BUSH, ModTags.Blocks.TERRAIN));
		context.register(FEATURE_PATCH_WILD_RICE, new WildRiceFeature(
				96, 7, 3, plantPlacedFeature(ModBlocks.WILD_RICE.get(), BlockTags.DIRT)));
		context.register(FEATURE_PATCH_BROWN_MUSHROOM_COLONIES, mushroomColonyFeature(
				ModBlocks.BROWN_MUSHROOM_COLONY.get(), Blocks.BROWN_MUSHROOM));
		context.register(FEATURE_PATCH_RED_MUSHROOM_COLONIES, mushroomColonyFeature(
				ModBlocks.RED_MUSHROOM_COLONY.get(), Blocks.RED_MUSHROOM));
	}

	public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
		HolderGetter<Feature> featureLookup = context.lookup(Registries.FEATURE);
		context.register(PATCH_WILD_CABBAGES, createPlacedFeature(featureLookup, FEATURE_PATCH_WILD_CABBAGES, 30));
		context.register(PATCH_WILD_ONIONS, createPlacedFeature(featureLookup, FEATURE_PATCH_WILD_ONIONS, 120));
		context.register(PATCH_WILD_TOMATOES, createPlacedFeature(featureLookup, FEATURE_PATCH_WILD_TOMATOES, 100));
		context.register(PATCH_WILD_CARROTS, createPlacedFeature(featureLookup, FEATURE_PATCH_WILD_CARROTS, 120));
		context.register(PATCH_WILD_POTATOES, createPlacedFeature(featureLookup, FEATURE_PATCH_WILD_POTATOES, 100));
		context.register(PATCH_WILD_BEETROOTS, createPlacedFeature(featureLookup, FEATURE_PATCH_WILD_BEETROOTS, 30));
		context.register(PATCH_WILD_RICE, createPlacedFeature(featureLookup, FEATURE_PATCH_WILD_RICE, 20));
		context.register(PATCH_BROWN_MUSHROOM_COLONIES, createPlacedFeature(featureLookup, FEATURE_PATCH_BROWN_MUSHROOM_COLONIES, 15));
		context.register(PATCH_RED_MUSHROOM_COLONIES, createPlacedFeature(featureLookup, FEATURE_PATCH_RED_MUSHROOM_COLONIES, 15));
	}

	private static Feature wildCropFeature(Block primaryBlock, Block secondaryBlock, TagKey<Block> blocksToTarget) {
		return defaultWildCropFeature(
				plantPlacedFeature(primaryBlock, blocksToTarget),
				plantPlacedFeature(secondaryBlock, blocksToTarget),
				null);
	}

	private static Feature wildCropFeature(Block primaryBlock, Block secondaryBlock, Block floorBlock, TagKey<Block> blocksToTarget) {
		return defaultWildCropFeature(
				plantPlacedFeature(primaryBlock, blocksToTarget),
				plantPlacedFeature(secondaryBlock, blocksToTarget),
				floorPlacedFeature(floorBlock, blocksToTarget));
	}

	private static Feature mushroomColonyFeature(Block colonyBlock, Block mushroomBlock) {
		return defaultWildCropFeature(
				Holder.direct(new PlacedFeature(
						Holder.direct(new SimpleBlockFeature(new RandomizedIntStateProvider(
								BlockStateProvider.of(colonyBlock), MushroomColonyBlock.COLONY_AGE, UniformInt.of(0, 3)))),
						placeOnTopOfModifier(Blocks.MYCELIUM))),
				plantPlacedFeature(mushroomBlock, Blocks.MYCELIUM),
				null);
	}

	private static Feature defaultWildCropFeature(Holder<PlacedFeature> primaryFeature,
			Holder<PlacedFeature> secondaryFeature, @Nullable Holder<PlacedFeature> floorFeature) {
		return new WildCropFeature(64, 6, 3, primaryFeature, secondaryFeature, floorFeature);
	}

	private static Holder<PlacedFeature> plantPlacedFeature(Block block, Block blocksToPlaceOn) {
		return Holder.direct(new PlacedFeature(
				Holder.direct(new SimpleBlockFeature(BlockStateProvider.of(block))),
				placeOnTopOfModifier(blocksToPlaceOn)));
	}

	private static Holder<PlacedFeature> plantPlacedFeature(Block block, TagKey<Block> blocksToPlaceOn) {
		return Holder.direct(new PlacedFeature(
				Holder.direct(new SimpleBlockFeature(BlockStateProvider.of(block))),
				placeOnTopOfModifier(blocksToPlaceOn)));
	}

	private static Holder<PlacedFeature> floorPlacedFeature(Block block, TagKey<Block> blocksToReplace) {
		return Holder.direct(new PlacedFeature(
				Holder.direct(new SimpleBlockFeature(BlockStateProvider.of(block))),
				replaceBlockModifier(blocksToReplace)));
	}

	private static List<PlacementModifier> placeOnTopOfModifier(Block blockToPlaceOn) {
		return List.of(BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
				BlockPredicate.matchesTag(BlockTags.AIR),
				BlockPredicate.matchesBlocks(Direction.DOWN, blockToPlaceOn))));
	}

	private static List<PlacementModifier> placeOnTopOfModifier(TagKey<Block> blocksToPlaceOn) {
		return List.of(BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
				BlockPredicate.matchesTag(BlockTags.AIR),
				BlockPredicate.matchesTag(Direction.DOWN, blocksToPlaceOn))));
	}

	private static List<PlacementModifier> replaceBlockModifier(TagKey<Block> blocksToReplace) {
		return List.of(BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
				BlockPredicate.matchesTag(Direction.UP, BlockTags.REPLACEABLE),
				BlockPredicate.matchesTag(blocksToReplace))));
	}

	private static PlacedFeature createPlacedFeature(HolderGetter<Feature> featureGetter, ResourceKey<Feature> feature, int rarity) {
		return new PlacedFeature(featureGetter.getOrThrow(feature), List.of(
				RarityFilter.onAverageOnceEvery(rarity),
				InSquarePlacement.spread(),
				HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING),
				BiomeFilter.biome(),
				BiomeTagFilter.biomeIsInTag(BiomeTags.IS_OVERWORLD)));
	}
}
