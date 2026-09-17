package vectorwing.farmersdelight.common.block.entity.inventory;

import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.block.entity.Basket;

public class BasketInvWrapper implements ItemInventory {
    protected final Basket basket;

    public BasketInvWrapper(Basket basket) {
        this.basket = basket;
    }

    @Override
    public int getSlots() { return basket.getContainerSize(); }
    @Override
    public ItemStack getStackInSlot(int slot) { return basket.getItem(slot); }
    @Override
    public void setStackInSlot(int slot, ItemStack stack) { basket.setItem(slot, stack); basket.setChanged(); }
    @Override
    public int getSlotLimit(int slot) { return basket.getMaxStackSize(); }
    @Override
    public boolean isItemValid(int slot, ItemStack stack) { return basket.canPlaceItem(slot, stack); }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (simulate) {
            return ItemInventory.super.insertItem(slot, stack, true);
        } else {
            boolean wasEmpty = basket.isEmpty();
            int originalCount = stack.getCount();
            stack = ItemInventory.super.insertItem(slot, stack, false);
            if (wasEmpty && originalCount > stack.getCount()) {
                if (!basket.isOnCustomCooldown()) {
                    basket.setCooldown(8);
                }
            }
            return stack;
        }
    }
}
