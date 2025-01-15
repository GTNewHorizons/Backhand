package xonin.backhand.api.core;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public interface IOffhandInventory {

    int OFFHAND_HOTBAR_SLOT = 36;

    ItemStack backhand$getOffhandItem();

    void backhand$setOffhandItem(ItemStack stack);

    static boolean isValidSwitch(int id) {
        return (id >= 0 && id < InventoryPlayer.getHotbarSize()) || id == OFFHAND_HOTBAR_SLOT;
    }
}
