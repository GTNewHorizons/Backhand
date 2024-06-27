package xonin.backhand.client.utils;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.item.ItemStack;

import xonin.backhand.api.core.BackhandUtils;

public final class BackhandClientUtils {

    /**
     * Patch over EntityOtherPlayerMP#onUpdate() to update isItemInUse field
     *
     * @param player      the player whose #onUpdate method is triggered
     * @param isItemInUse the old value for isItemInUse field
     * @return the new value for isItemInUse field
     */
    public static boolean entityOtherPlayerIsItemInUseHook(EntityOtherPlayerMP player, boolean isItemInUse) {
        ItemStack itemStack = player.getCurrentEquippedItem();
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (BackhandUtils.usagePriorAttack(offhand)) itemStack = offhand;
        if (!isItemInUse && player.isEating() && itemStack != null) {
            player.setItemInUse(itemStack, itemStack.getMaxItemUseDuration());
            return true;
        } else if (isItemInUse && !player.isEating()) {
            player.clearItemInUse();
            return false;
        } else {
            return isItemInUse;
        }
    }
}
