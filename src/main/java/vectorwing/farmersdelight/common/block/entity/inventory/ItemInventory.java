package vectorwing.farmersdelight.common.block.entity.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

/** Mod-owned inventory contract; automation is exposed through ResourceHandler. */
public interface ItemInventory extends Container
{
	int getSlots();
	ItemStack getStackInSlot(int slot);
	void setStackInSlot(int slot, ItemStack stack);

	default int getSlotLimit(int slot) { return Item.ABSOLUTE_MAX_STACK_SIZE; }
	default boolean isItemValid(int slot, ItemStack stack) { return true; }

	default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
		ItemStack stored = getStackInSlot(slot);
		if (!stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, stack)) return stack;
		int moved = Math.min(stack.getCount(), Math.max(0,
				Math.min(getSlotLimit(slot), stack.getMaxStackSize()) - stored.getCount()));
		if (moved == 0) return stack;
		if (!simulate) setStackInSlot(slot, stack.copyWithCount(stored.getCount() + moved));
		return stack.copyWithCount(stack.getCount() - moved);
	}

	default ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack stored = getStackInSlot(slot);
		if (amount <= 0 || stored.isEmpty()) return ItemStack.EMPTY;
		int moved = Math.min(amount, Math.min(stored.getCount(), stored.getMaxStackSize()));
		ItemStack result = stored.copyWithCount(moved);
		if (!simulate) setStackInSlot(slot, stored.copyWithCount(stored.getCount() - moved));
		return result;
	}

	@Override
	default int getContainerSize() { return getSlots(); }
	@Override
	default ItemStack getItem(int slot) { return getStackInSlot(slot); }
	@Override
	default void setItem(int slot, ItemStack stack) { setStackInSlot(slot, stack); }
	@Override
	default ItemStack removeItem(int slot, int amount) { return extractItem(slot, amount, false); }
	@Override
	default ItemStack removeItemNoUpdate(int slot) {
		ItemStack stack = getStackInSlot(slot);
		setStackInSlot(slot, ItemStack.EMPTY);
		return stack;
	}
	@Override
	default boolean isEmpty() {
		for (int slot = 0; slot < getSlots(); slot++) if (!getStackInSlot(slot).isEmpty()) return false;
		return true;
	}
	@Override
	default void clearContent() {
		for (int slot = 0; slot < getSlots(); slot++) setStackInSlot(slot, ItemStack.EMPTY);
	}
	@Override
	default void setChanged() {}
	@Override
	default boolean stillValid(Player player) { return true; }
	@Override
	default boolean canPlaceItem(int slot, ItemStack stack) { return isItemValid(slot, stack); }
}
