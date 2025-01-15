package xonin.backhand.api.core;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemHangingEntity;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Store commonly used method, mostly for the {@link EntityPlayer} {@link ItemStack}s management
 */
public class BackhandUtils {

    private static String[] itemBlackListMethodNames = {
        BackhandTranslator.getMapedMethodName("Item", "func_77648_a", "onItemUse"),
        BackhandTranslator.getMapedMethodName("Item", "func_77659_a", "onItemRightClick"),
        BackhandTranslator.getMapedMethodName("Item", "func_111207_a", "itemInteractionForEntity") };

    /**
     * Method arguments classes that are not allowed in {@link Item} subclasses for common wielding
     */
    private static Class[][] itemBlackListMethodParams = {
        new Class[] { ItemStack.class, EntityPlayer.class, World.class, int.class, int.class, int.class, int.class,
            float.class, float.class, float.class },
        new Class[] { ItemStack.class, World.class, EntityPlayer.class },
        new Class[] { ItemStack.class, EntityPlayer.class, EntityLivingBase.class } };

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

    /**
     * Defines a item which "use" (effect on right click) should have priority over its "attack" (effect on left click)
     *
     * @param itemStack the item which will be "used", instead of attacking
     * @return true if such item prefer being "used"
     */
    public static boolean usagePriorAttack(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        if (itemStack.getItemUseAction() == EnumAction.drink || itemStack.getItemUseAction() == EnumAction.eat
            || itemStack.getItemUseAction() == EnumAction.bow) {
            return true;
        }
        return !(itemStack.getItem() instanceof ItemSword)
            && (checkForRightClickFunction(itemStack) || isCommonlyUsable(itemStack.getItem()));
    }

    /**
     * Defines items that are usually usable (the vanilla instances do, at least)
     *
     * @param item the instance to consider for usability
     * @return true if it is commonly usable
     */
    public static boolean isCommonlyUsable(Item item) {
        return isBow(item) || item.getClass()
            .toString()
            .equalsIgnoreCase("class D.f")
            || item instanceof ItemBed
            || item instanceof ItemHangingEntity
            || item instanceof ItemBook
            || isItemBlock(item)
            || item instanceof ItemHoe
            || item instanceof ItemSnowball
            || item instanceof ItemEnderPearl
            || item instanceof ItemEgg
            || item instanceof ItemMonsterPlacer;
    }

    /**
     * Defines a bow
     *
     * @param item the instance
     * @return true if it is considered a generic enough bow
     */
    public static boolean isBow(Item item) {
        return item instanceof ItemBow;
    }

    public static boolean isItemBlock(Item item) {
        return item instanceof ItemBlock || item instanceof ItemDoor
            || item instanceof ItemSign
            || item instanceof ItemReed
            || item instanceof ItemSeedFood
            || item instanceof ItemRedstone
            || item instanceof ItemBucket
            || item instanceof ItemSkull;
    }

    @SuppressWarnings("unchecked")
    public static boolean checkForRightClickFunction(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        try {
            if (stack.getItemUseAction() == EnumAction.block || stack.getItemUseAction() == EnumAction.none) {

                Class c = stack.getItem()
                    .getClass();
                while (!(c.equals(Item.class) || c.equals(ItemTool.class) || c.equals(ItemSword.class))) {
                    try {
                        for (int i = 0; i < itemBlackListMethodNames.length; i++) {
                            try {
                                c.getDeclaredMethod(itemBlackListMethodNames[i], itemBlackListMethodParams[i]);
                                return true;
                            } catch (NoSuchMethodException ignored) {}
                        }
                    } catch (NoClassDefFoundError ignored) {}

                    c = c.getSuperclass();
                }

                return false;
            } else {
                return true;
            }
        } catch (NullPointerException e) {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean checkForRightClickFunctionNoAction(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        try {
            Class c = stack.getItem()
                .getClass();
            while (!(c.equals(Item.class) || c.equals(ItemTool.class) || c.equals(ItemSword.class))) {
                try {
                    for (int i = 0; i < itemBlackListMethodNames.length; i++) {
                        try {
                            c.getDeclaredMethod(itemBlackListMethodNames[i], itemBlackListMethodParams[i]);
                            return true;
                        } catch (NoSuchMethodException ignored) {}
                    }
                } catch (NoClassDefFoundError ignored) {}

                c = c.getSuperclass();
            }

            return false;
        } catch (NullPointerException e) {
            return true;
        }
    }

    public static void useOffhandItem(EntityPlayer player, Runnable action) {
        useOffhandItem(player, () -> {
            action.run();
            return true;
        });
    }

    public static boolean useOffhandItem(EntityPlayer player, BooleanSupplier action) {
        int oldSlot = player.inventory.currentItem;
        player.inventory.currentItem = IOffhandInventory.OFFHAND_HOTBAR_SLOT;
        boolean result = action.getAsBoolean();
        player.inventory.currentItem = oldSlot;
        if (player.worldObj.isRemote) {
            Minecraft.getMinecraft().playerController.syncCurrentPlayItem();
        }
        return result;
    }
}
