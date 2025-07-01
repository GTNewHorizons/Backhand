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
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

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

    public static boolean isItemBlock(@Nullable Item item) {
        return item instanceof ItemBlock || item instanceof ItemDoor
            || item instanceof ItemSign
            || item instanceof ItemReed
            || item instanceof ItemSeedFood
            || item instanceof ItemRedstone
            || item instanceof ItemBucket
            || item instanceof ItemSkull;
    }

}
