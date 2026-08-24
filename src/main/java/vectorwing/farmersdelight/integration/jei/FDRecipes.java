package vectorwing.farmersdelight.integration.jei;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import vectorwing.farmersdelight.client.recipebook.ClientRecipeCache;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.utility.RecipeUtils;

import java.util.ArrayList;
import java.util.List;

public class FDRecipes
{
	private final RecipeMap recipeMap;

	public FDRecipes() {
		this.recipeMap = ClientRecipeCache.getRecipeMap();
	}

	public List<RecipeHolder<CookingPotRecipe>> getCookingPotRecipes() {
		return new ArrayList<>(recipeMap.byType(ModRecipeTypes.COOKING.get()));
	}

	public List<RecipeHolder<CuttingBoardRecipe>> getCuttingBoardRecipes() {
		return new ArrayList<>(recipeMap.byType(ModRecipeTypes.CUTTING.get()));
	}

	public List<RecipeHolder<CraftingRecipe>> getSpecialCraftingRecipes() {
		List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

		addValidatedSpecialRecipe(recipes, "wheat_dough_from_water", "fd_dough",
				List.of(
						Ingredient.of(Items.WHEAT),
						Ingredient.of(Items.WATER_BUCKET)
				),
				vectorwing.farmersdelight.common.registry.ModItems.WHEAT_DOUGH.get()
		);

		return recipes;
	}

	public void addValidatedSpecialRecipe(List<RecipeHolder<CraftingRecipe>> recipeList, String recipeId, String group, List<Ingredient> inputs, ItemLike output) {
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, RecipeUtils.FDLocation(recipeId));
		if (recipeMap.byKey(key) != null) {
			ShapelessRecipe displayRecipe = new ShapelessRecipe(
					new Recipe.CommonInfo(true),
					new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, group),
					new ItemStackTemplate(output.asItem()),
					inputs
			);
			recipeList.add(new RecipeHolder<>(key, displayRecipe));
		}
	}
}
