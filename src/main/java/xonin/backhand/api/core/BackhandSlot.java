package xonin.backhand.api.core;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import xonin.backhand.Backhand;

public class BackhandSlot extends Slot {

    public BackhandSlot(IInventory inventory, int slotIndex, int xDisplayPosition, int yDisplayPosition) {
        super(inventory, slotIndex, xDisplayPosition, yDisplayPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !Backhand.isOffhandBlacklisted(stack);
    }
}
