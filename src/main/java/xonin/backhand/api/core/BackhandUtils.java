package xonin.backhand.api.core;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Store commonly used method, mostly for the {@link EntityPlayer} {@link ItemStack}s management
 */
@ParametersAreNonnullByDefault
public final class BackhandUtils {

    public static void swapOffhandItem(EntityPlayer player) {
        ItemStack mainHand = player.getCurrentEquippedItem();
        player.setCurrentItemOrArmor(0, BackhandUtils.getOffhandItem(player));
        BackhandUtils.setPlayerOffhandItem(player, mainHand);
    }

    public static void setPlayerOffhandItem(EntityPlayer player, @Nullable ItemStack stack) {
        ((IOffhandInventory) player.inventory).backhand$setOffhandItem(stack);
    }

    public static @Nullable ItemStack getOffhandItem(EntityPlayer player) {
        return ((IOffhandInventory) player.inventory).backhand$getOffhandItem();
    }

    public static void useOffhandItem(EntityPlayer player, Runnable action) {
        useOffhandItem(player, true, action);
    }

    public static void useOffhandItem(EntityPlayer player, boolean syncSlot, Runnable action) {
        useOffhandItem(player, syncSlot, () -> {
            action.run();
            return true;
        });
    }

    public static boolean useOffhandItem(EntityPlayer player, BooleanSupplier action) {
        return useOffhandItem(player, true, action);
    }

    public static boolean useOffhandItem(EntityPlayer player, boolean syncSlot, BooleanSupplier action) {
        int oldSlot = player.inventory.currentItem;
        player.inventory.currentItem = ((IOffhandInventory) player.inventory).backhand$getOffhandSlot();
        boolean result = action.getAsBoolean();
        player.inventory.currentItem = oldSlot;
        if (syncSlot && player.worldObj.isRemote) {
            Minecraft.getMinecraft().playerController.syncCurrentPlayItem();
        }
        return result;
    }

    public static boolean isUsingOffhand(EntityPlayer player) {
        return ((IBackhandPlayer) player).isUsingOffhand();
    }

    public static int getOffhandSlot(EntityPlayer player) {
        return ((IOffhandInventory) player.inventory).backhand$getOffhandSlot();
    }
}
