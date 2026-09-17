package vectorwing.farmersdelight.common.block.entity.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record InventoryRecipeInput(ItemInventory inventory) implements RecipeInput
{
	@Override
	public ItemStack getItem(int slot) { return inventory.getStackInSlot(slot); }
	@Override
	public int size() { return inventory.getSlots(); }
}
