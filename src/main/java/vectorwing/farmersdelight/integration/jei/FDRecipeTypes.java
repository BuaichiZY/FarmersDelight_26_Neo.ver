package vectorwing.farmersdelight.integration.jei;

import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.integration.jei.resource.DecompositionDummy;

public final class FDRecipeTypes
{
	public static final IRecipeHolderType<CookingPotRecipe> COOKING = IRecipeHolderType.create(ModRecipeTypes.COOKING.get());
	public static final IRecipeHolderType<CuttingBoardRecipe> CUTTING = IRecipeHolderType.create(ModRecipeTypes.CUTTING.get());
	public static final IRecipeType<DecompositionDummy> DECOMPOSITION = IRecipeType.create(FarmersDelight.MODID, "decomposition", DecompositionDummy.class);
}
