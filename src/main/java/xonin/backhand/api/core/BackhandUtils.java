package xonin.backhand.api.core;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Store commonly used method, mostly for the {@link EntityPlayer} {@link ItemStack}s management
 */
public class BackhandUtils {

    public static void swapOffhandItem(EntityPlayer player) {
        player.setCurrentItemOrArmor(0, BackhandUtils.getOffhandItem(player));
        BackhandUtils.setPlayerOffhandItem(player, BackhandUtils.getOffhandItem(player));
    }

    public static void setPlayerCurrentItem(EntityPlayer player, ItemStack stack) {
        player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
    }

    public static void setPlayerOffhandItem(EntityPlayer player, ItemStack stack) {
        ((IOffhandInventory) player.inventory).backhand$setOffhandItem(stack);
    }

    public static @Nullable ItemStack getOffhandItem(EntityPlayer player) {
        if (player instanceof FakePlayer) return null;
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
        player.inventory.currentItem = IOffhandInventory.OFFHAND_HOTBAR_SLOT;
        boolean result = action.getAsBoolean();
        player.inventory.currentItem = oldSlot;
        if (syncSlot && player.worldObj.isRemote) {
            Minecraft.getMinecraft().playerController.syncCurrentPlayItem();
        }
        return result;
    }

    public static boolean isValidPlayer(Entity entity) {
        return entity instanceof EntityPlayerMP playerMP
            && !(entity instanceof FakePlayer || playerMP.playerNetServerHandler == null);
    }
}
