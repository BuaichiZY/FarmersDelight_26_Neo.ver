package vectorwing.farmersdelight.common.block.entity.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/** Fixed-size storage shared by the block entity, menu, recipes and automation bridge. */
public class StackInventory implements ItemInventory, ValueIOSerializable
{
	private final NonNullList<ItemStack> stacks;

	public StackInventory() { this(1); }
	public StackInventory(int size) { stacks = NonNullList.withSize(size, ItemStack.EMPTY); }
	@Override
	public int getSlots() { return stacks.size(); }
	@Override
	public ItemStack getStackInSlot(int slot) { return stacks.get(slot); }
	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		stacks.set(slot, stack);
		onContentsChanged(slot);
	}
	protected int getStackLimit(int slot, ItemStack stack) {
		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
	}
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
		ItemStack stored = getStackInSlot(slot);
		if (!stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, stack)) return stack;
		int moved = Math.min(stack.getCount(), Math.max(0, getStackLimit(slot, stack) - stored.getCount()));
		if (moved == 0) return stack;
		if (!simulate) setStackInSlot(slot, stack.copyWithCount(stored.getCount() + moved));
		return stack.copyWithCount(stack.getCount() - moved);
	}
	@Override
	public void setChanged() { onContentsChanged(-1); }
	protected void onContentsChanged(int slot) {}

	// Keep the existing block-entity inventory format so saved meals/items survive upgrades.
	@Override
	public void serialize(ValueOutput output) {
		output.putInt("Size", getSlots());
		var savedItems = output.list("Items", ItemStackWithSlot.CODEC);
		for (int slot = 0; slot < getSlots(); slot++) {
			if (!stacks.get(slot).isEmpty()) savedItems.add(new ItemStackWithSlot(slot, stacks.get(slot)));
		}
	}

	@Override
	public void deserialize(ValueInput input) {
		stacks.clear();
		for (ItemStackWithSlot entry : input.listOrEmpty("Items", ItemStackWithSlot.CODEC)) {
			if (entry.isValidInContainer(getSlots())) stacks.set(entry.slot(), entry.stack());
		}
	}
}
