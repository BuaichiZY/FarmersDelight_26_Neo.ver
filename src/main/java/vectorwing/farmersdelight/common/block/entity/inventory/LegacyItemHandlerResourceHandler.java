package vectorwing.farmersdelight.common.block.entity.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Transaction-aware bridge used while Farmer's Delight's internal inventories still expose the
 * legacy item-handler API. NeoForge 26.2 exposes automation through ResourceHandler instead.
 */
@SuppressWarnings("removal")
public final class LegacyItemHandlerResourceHandler implements ResourceHandler<ItemResource>
{
	private final IItemHandler handler;
	private final BiConsumer<Integer, ItemStack> stackSetter;
	private final List<SlotJournal> journals;

	public LegacyItemHandlerResourceHandler(IItemHandler handler, BiConsumer<Integer, ItemStack> stackSetter) {
		this.handler = handler;
		this.stackSetter = stackSetter;
		this.journals = new ArrayList<>(handler.getSlots());
		for (int slot = 0; slot < handler.getSlots(); slot++) {
			this.journals.add(new SlotJournal(slot));
		}
	}

	@Override
	public int size() {
		return handler.getSlots();
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(handler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getStackInSlot(index).getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return resource.isEmpty() || isValid(index, resource) ? handler.getSlotLimit(index) : 0;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return !resource.isEmpty() && handler.isItemValid(index, resource.toStack());
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		int offered = Math.min(amount, handler.getSlotLimit(index));
		if (offered == 0) return 0;

		ItemStack offeredStack = resource.toStack(offered);
		int simulated = offered - handler.insertItem(index, offeredStack, true).getCount();
		if (simulated <= 0) return 0;

		journals.get(index).updateSnapshots(transaction);
		return offered - handler.insertItem(index, offeredStack, false).getCount();
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		ItemStack stored = handler.getStackInSlot(index);
		if (!resource.matches(stored) || amount == 0) return 0;

		int simulated = handler.extractItem(index, amount, true).getCount();
		if (simulated <= 0) return 0;

		journals.get(index).updateSnapshots(transaction);
		return handler.extractItem(index, amount, false).getCount();
	}

	private final class SlotJournal extends SnapshotJournal<ItemStack>
	{
		private final int slot;

		private SlotJournal(int slot) {
			this.slot = slot;
		}

		@Override
		protected ItemStack createSnapshot() {
			return handler.getStackInSlot(slot).copy();
		}

		@Override
		protected void revertToSnapshot(ItemStack snapshot) {
			stackSetter.accept(slot, snapshot);
		}
	}
}
