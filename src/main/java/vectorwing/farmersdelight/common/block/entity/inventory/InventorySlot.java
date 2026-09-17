package vectorwing.farmersdelight.common.block.entity.inventory;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Vanilla menu slot backed by the same inventory that the block entity owns. */
public class InventorySlot extends Slot
{
	private final ItemInventory inventory;
	private final int inventorySlot;

	public InventorySlot(ItemInventory inventory, int slot, int x, int y) {
		super(inventory, slot, x, y);
		this.inventory = inventory;
		this.inventorySlot = slot;
	}
	@Override
	public boolean mayPlace(ItemStack stack) { return inventory.isItemValid(inventorySlot, stack); }
	@Override
	public int getMaxStackSize() { return inventory.getSlotLimit(inventorySlot); }
	@Override
	public int getMaxStackSize(ItemStack stack) { return Math.min(getMaxStackSize(), stack.getMaxStackSize()); }
}
