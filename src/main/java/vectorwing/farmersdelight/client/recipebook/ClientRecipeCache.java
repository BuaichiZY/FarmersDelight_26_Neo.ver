package vectorwing.farmersdelight.client.recipebook;

import net.minecraft.world.item.crafting.RecipeMap;

/**
 * Holds the full recipe contents explicitly synchronized by NeoForge.
 * Vanilla's 26.2 client recipe access only exposes recipe-book displays and
 * cannot provide custom recipe objects to recipe-viewer integrations.
 */
public final class ClientRecipeCache
{
	private static RecipeMap recipeMap = RecipeMap.EMPTY;

	private ClientRecipeCache() {
	}

	public static RecipeMap getRecipeMap() {
		return recipeMap;
	}

	public static void setRecipeMap(RecipeMap recipes) {
		recipeMap = recipes;
	}

	public static void clear() {
		recipeMap = RecipeMap.EMPTY;
	}
}
