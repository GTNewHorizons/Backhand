package xonin.backhand.api.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public interface IOffhandInventory {

    ItemStack backhand$getOffhandItem();

    void backhand$setOffhandItem(ItemStack stack);

    int backhand$getOffhandSlot();

    static boolean isValidSwitch(int id, EntityPlayer player) {
        return (id >= 0 && id < InventoryPlayer.getHotbarSize()) || id == BackhandUtils.getOffhandSlot(player);
    }
}
