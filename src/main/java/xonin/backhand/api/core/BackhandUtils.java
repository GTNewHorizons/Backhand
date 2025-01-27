package xonin.backhand.api.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Store commonly used method, mostly for the {@link EntityPlayer} {@link ItemStack}s management
 */
@ParametersAreNonnullByDefault
public final class BackhandUtils {

    public static final List<Class<?>> offhandPriorityItems = new ArrayList<>();

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

    /**
     * Adds an item that when held in the main hand will execute the offhand item action before the main hand action
     */
    public static void addOffhandPriorityItem(Class<?> itemClass) {
        offhandPriorityItems.add(itemClass);
    }

    public static boolean isValidPlayer(@Nullable Entity entity) {
        return entity instanceof EntityPlayerMP playerMP
            && !(entity instanceof FakePlayer || playerMP.playerNetServerHandler == null);
    }
}
