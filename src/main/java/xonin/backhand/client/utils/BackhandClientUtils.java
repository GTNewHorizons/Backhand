package xonin.backhand.client.utils;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.item.ItemStack;

import xonin.backhand.api.core.BackhandUtils;

public final class BackhandClientUtils {

    public static boolean disableMainhandAnimation = false;
    public static int countToCancel = 0;
    public static float onGround2;
    public static float firstPersonFrame;
    public static boolean offhandFPRender;
    public static boolean ignoreSetSlot = false;
    public static boolean receivedConfigs = false;

    /** If we have hotswapped the breaking item with the one in offhand and should hotswap it back when called next */
    public static boolean hotSwapped = false;

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
