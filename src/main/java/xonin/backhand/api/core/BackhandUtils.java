package xonin.backhand.api.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraftforge.common.util.FakePlayer;

import tconstruct.library.tools.HarvestTool;
import xonin.backhand.utils.Mods;

/**
 * Store commonly used method, mostly for the {@link EntityPlayer} {@link ItemStack}s management
 */
@ParametersAreNonnullByDefault
public final class BackhandUtils {

    public static final List<Class<? extends Item>> offhandPriorityItems = new ArrayList<>();
    public static final List<Class<? extends Item>> deprioritizedMainhand = new ArrayList<>();

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
        player.inventory.currentItem = getOffhandSlot(player);

        if (syncSlot && player.worldObj.isRemote) {
            Minecraft.getMinecraft().playerController.syncCurrentPlayItem();
        }
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
     * Adds an item that when held in the offhand will execute the offhand item action before the main hand action
     */
    @SafeVarargs
    public static void addOffhandPriorityItem(Class<? extends Item>... itemClass) {
        offhandPriorityItems.addAll(Arrays.asList(itemClass));
    }

    /**
     * Adds an item that when held in the main hand will execute the offhand item action before the main hand action
     */
    @SafeVarargs
    public static void addDeprioritizedMainhandItem(@Nonnull Class<? extends Item>... itemClass) {
        deprioritizedMainhand.addAll(Arrays.asList(itemClass));
    }

    public static boolean isValidPlayer(@Nullable Entity entity) {
        return entity instanceof EntityPlayerMP playerMP
            && !(entity instanceof FakePlayer || playerMP.playerNetServerHandler == null);
    }

    public static boolean isItemTool(@Nullable Item item) {
        return (Mods.TINKERS_CONSTRUCT.isLoaded() && item instanceof HarvestTool) || item instanceof ItemTool
            || item instanceof ItemSword
            || item instanceof ItemHoe;
    }

    public static int swapToOffhand(EntityPlayer player) {
        int heldItemTemp = player.inventory.currentItem;
        player.inventory.currentItem = getOffhandSlot(player);
        return heldItemTemp;
    }

    public static void swapBack(EntityPlayer player, int heldItemTemp) {
        player.inventory.currentItem = heldItemTemp;
    }

}
