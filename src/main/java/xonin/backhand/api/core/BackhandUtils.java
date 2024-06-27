package xonin.backhand.api.core;

import java.io.Closeable;
import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import xonin.backhand.Backhand;

/**
 * Store commonly used method, mostly for the {@link EntityPlayer} {@link ItemStack}s management
 */
public class BackhandUtils {

    private static String[] itemBlackListMethodNames = {
            BackhandTranslator.getMapedMethodName("Item", "func_77648_a", "onItemUse"),
            BackhandTranslator.getMapedMethodName("Item", "func_77659_a", "onItemRightClick"),
            BackhandTranslator.getMapedMethodName("Item", "func_111207_a", "itemInteractionForEntity")
    };

    /**
     * Method arguments classes that are not allowed in {@link Item} subclasses for common wielding
     */
    private static Class[][] itemBlackListMethodParams = {
        new Class[] { ItemStack.class, EntityPlayer.class, World.class, int.class, int.class, int.class, int.class,
            float.class, float.class, float.class },
        new Class[] { ItemStack.class, World.class, EntityPlayer.class },
        new Class[] { ItemStack.class, EntityPlayer.class, EntityLivingBase.class } };
    private static ItemStack prevNotWieldable;
    /**
     * The generic attack damage key for {@link ItemStack#getAttributeModifiers()}
     */
    private static String genericAttack = SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName();

    public static boolean hasOffhandInventory(EntityPlayer player) {
        return player.inventory instanceof InventoryPlayerBackhand;
    }

    public static void swapOffhandItem(EntityPlayer player) {
        final ItemStack mainhandItem = player.getCurrentEquippedItem();
        final ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        BackhandUtils.setPlayerCurrentItem(player, offhandItem);
        BackhandUtils.setPlayerOffhandItem(player, mainhandItem);
    }

    public static void setPlayerCurrentItem(EntityPlayer player, ItemStack stack) {
        player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
    }

    public static void setPlayerOffhandItem(EntityPlayer player, ItemStack stack) {
        if (!Backhand.isOffhandBlacklisted(stack)) {
            if (Backhand.UseInventorySlot) {
                player.inventory.setInventorySlotContents(Backhand.AlternateOffhandSlot, stack);
            } else {
                getOffhandEP(player).setOffhandItem(stack);
            }
        }
    }

    public static ItemStack getOffhandItem(EntityPlayer player) {
        if (Backhand.UseInventorySlot) {
            return player.inventory.getStackInSlot(Backhand.AlternateOffhandSlot);
        } else {
            return getOffhandEP(player).getOffhandItem();
        }
    }

    public static OffhandExtendedProperty getOffhandEP(EntityPlayer player) {
        return ((OffhandExtendedProperty) player.getExtendedProperties("OffhandStorage"));
    }

    /**
     * Defines a generic weapon
     * 
     * @param main the item to check
     * @return true if the item is a generic weapon
     */
    public static boolean isWeapon(ItemStack main) {
        if (main.getMaxStackSize() == 1 && main.getMaxDamage() > 0 && !main.getHasSubtypes())// Usual values for tools,
                                                                                             // sword, and bow
            return true;
        else if (main == prevNotWieldable)// Prevent lag from check spam
            return false;
        prevNotWieldable = main;
        return false;
    }

    /**
     * Defines a combination of left hand/right hand items that is valid to wield
     * 
     * @param main item to be in the right hand
     * @param off  item to be in the left hand
     * @return true if the right hand item allows left hand item
     */
    public static boolean isMainHand(ItemStack main, ItemStack off) {
        if (main == null) return true;
        else if (usagePriorAttack(main))// "Usable" item
            return off == null || !usagePriorAttack(off);// With empty hand or non "usable item"
        else if (isWeapon(main))// A generic weapon
            return main.getItemUseAction() == EnumAction.bow || main.getAttributeModifiers()
                .containsKey(genericAttack);// With either bow or generic attack, or registered
        return false;
    }

    /**
     * Defines a item which can be wield in the left hand
     * 
     * @param off the item to be wield in left hand
     * @return true if the item is allowed in left hand
     */
    public static boolean isOffHand(ItemStack off) {
        if (off == null) return true;
        else if (isWeapon(off))// A generic weapon
            return off.getAttributeModifiers()
                .containsKey(genericAttack);// with a generic attack or registered
        return false;
    }

    /**
     * Patch in EntityPlayer#onUpdate() to support hotswap of itemInUse
     * 
     * @param entityPlayer
     * @param itemInUse
     * @return
     */
    public static ItemStack getCurrentItemOnUpdate(EntityPlayer entityPlayer, ItemStack itemInUse) {
        ItemStack itemStack = getOffhandItem(entityPlayer);
        if (itemInUse == itemStack) {
            return itemStack;
        }
        return entityPlayer.getCurrentEquippedItem();
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
    // item instanceof ItemCrossbow || item instanceof ItemMounter || item instanceof ItemTeleporter || item instanceof
    // ItemElementalStaff || item instanceof ItemGun || item instanceof ItemNpcMovingPath || item instanceof
    // ItemMachineGun || item instanceof ItemMusket || item instanceof ItemNpcCloner || item instanceof ItemNpcScripter
    // || item instanceof SpellBase || item instanceof ItemNpcWand || item instanceof ItemShield || item instanceof
    // ItemSlingshot || item instanceof ItemSoulstoneFilled || item instanceof ItemSoulstoneEmpty || item instanceof
    // ItemStaff || item instanceof ItemThrowingWeapon || item instanceof ItemThrowingShuriken

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

    public static boolean blockHasUse(Block block) {
        if (block == null || block == Blocks.air) {
            return false;
        }
        try {
            String mappedName = BackhandTranslator.getMapedMethodName("Block", "func_149727_a", "onBlockActivated");
            Class[] classParams = new Class[]{World.class, int.class, int.class, int.class, EntityPlayer.class, int.class, float.class, float.class, float.class};

            Class c = block.getClass();
            while (!(c.equals(Block.class))) {
                try {
                    try {
                        c.getDeclaredMethod(mappedName, classParams);
                        return true;
                    } catch (NoSuchMethodException ignored) {}
                } catch (NoClassDefFoundError ignored) {}

                c = c.getSuperclass();
            }

            return false;
        } catch (NullPointerException e) {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean hasEntityInteraction(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        try {
            Class c = stack.getItem()
                .getClass();
            while (!(c.equals(Item.class) || c.equals(ItemTool.class) || c.equals(ItemSword.class))) {
                try {
                    try {
                        c.getDeclaredMethod(itemBlackListMethodNames[2], itemBlackListMethodParams[2]);
                        return true;
                    } catch (NoSuchMethodException ignored) {}
                } catch (NoClassDefFoundError ignored) {}

                c = c.getSuperclass();
            }

            return false;
        } catch (NullPointerException e) {
            return true;
        }
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

    /**
     * Reads a {@link ItemStack} from the InputStream
     */
    public static ItemStack readItemStack(ByteArrayDataInput par0DataInputStream) throws IOException {
        ItemStack itemstack = null;
        int short1 = par0DataInputStream.readInt();

        if (short1 >= 0) {
            byte b0 = par0DataInputStream.readByte();
            short short2 = par0DataInputStream.readShort();
            itemstack = new ItemStack(Item.getItemById(short1), b0, short2);
            itemstack.stackTagCompound = readNBTTagCompound(par0DataInputStream);
        }

        return itemstack;
    }

    /**
     * Reads a compressed {@link NBTTagCompound} from the InputStream
     */
    public static NBTTagCompound readNBTTagCompound(ByteArrayDataInput par0DataInputStream) throws IOException {
        short short1 = par0DataInputStream.readShort();

        if (short1 < 0) {
            return null;
        } else {
            byte[] abyte = new byte[short1];
            par0DataInputStream.readFully(abyte);

            return CompressedStreamTools.func_152457_a(abyte, NBTSizeTracker.field_152451_a);
        }
    }

    /**
     * Writes a {@link ItemStack} to the OutputStream
     * 
     * @param par1DataOutputStream the output stream
     * @param par0ItemStack        to write
     * @throws IOException
     */
    public static void writeItemStack(ByteArrayDataOutput par1DataOutputStream, ItemStack par0ItemStack)
        throws IOException {

        if (par0ItemStack == null) {
            par1DataOutputStream.writeShort(-1);
        } else {
            par1DataOutputStream.writeInt(Item.getIdFromItem(par0ItemStack.getItem()));
            par1DataOutputStream.writeByte(par0ItemStack.stackSize);
            par1DataOutputStream.writeShort(par0ItemStack.getItemDamage());
            NBTTagCompound nbttagcompound = null;

            if (par0ItemStack.getItem()
                .isDamageable()
                || par0ItemStack.getItem()
                    .getShareTag()) {
                nbttagcompound = par0ItemStack.stackTagCompound;
            }

            writeNBTTagCompound(nbttagcompound, par1DataOutputStream);
        }
    }

    /**
     * Writes a compressed {@link NBTTagCompound} to the output
     * 
     * @param par0NBTTagCompound
     * @param par1DataOutputStream
     * @throws IOException
     */
    protected static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound,
        ByteArrayDataOutput par1DataOutputStream) throws IOException {
        if (par0NBTTagCompound == null) {
            par1DataOutputStream.writeShort(-1);
        } else {
            byte[] abyte = CompressedStreamTools.compress(par0NBTTagCompound);
            par1DataOutputStream.writeShort((short) abyte.length);
            par1DataOutputStream.write(abyte);
        }
    }

    /**
     * Basically, a copy of {@link EntityPlayer#attackTargetEntityWithCurrentItem(Entity)}, adapted for the offhand
     * Hotswap the "current item" value to the offhand, then refresh the player attributes according to the newly
     * selected item
     * Reset everything back if the attack is cancelled by {@link AttackEntityEvent} or
     * {@link Item#onLeftClickEntity(ItemStack, EntityPlayer, Entity)}
     * 
     * @param player     the attacker
     * @param par1Entity the attacked
     */
    public static void attackTargetEntityWithCurrentOffItem(EntityPlayer player, Entity par1Entity) {
        final ItemStack oldItem = player.getCurrentEquippedItem();
        final ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (!Backhand.OffhandAttack || (offhandItem == null && !Backhand.EmptyOffhand))
            return;

        BackhandUtils.setPlayerCurrentItem(player,offhandItem);
        refreshAttributes(player.getAttributeMap(), oldItem, player.getCurrentEquippedItem());

        int prevDamage = offhandItem != null ? offhandItem.getItemDamage() : 0;
        int damage = offhandItem == null ? 1 : offhandItem.getMaxDamage() - offhandItem.getItemDamage();
        player.attackTargetEntityWithCurrentItem(par1Entity);

        refreshAttributes(player.getAttributeMap(), player.getCurrentEquippedItem(), oldItem);
        BackhandUtils.setPlayerCurrentItem(player,oldItem);

        if (offhandItem != null && damage <= 0 && offhandItem.getItemDamage() < prevDamage) {
            BackhandUtils.setPlayerOffhandItem(player, null);
            ForgeEventFactory.onPlayerDestroyItem(player, offhandItem);
        }
    }

    /**
     * Refresh the attribute map by removing from the old item and applying the current item
     * 
     * @param attributeMap the map to refresh
     * @param oldItem      the old item whose attributes will be removed
     * @param currentItem  the current item whose attributes will be applied
     */
    public static void refreshAttributes(BaseAttributeMap attributeMap, ItemStack oldItem, ItemStack currentItem) {
        if (oldItem != null) attributeMap.removeAttributeModifiers(oldItem.getAttributeModifiers());
        if (currentItem != null) attributeMap.applyAttributeModifiers(currentItem.getAttributeModifiers());
    }

    /**
     * Helper to close a stream fail-safely by printing the error stack trace
     * 
     * @param c the stream to close
     */
    public static void closeStream(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Patch over the PlayerUseItemEvent.Finish in EntityPlayer#onItemUseFinish() to pass the previous stacksize
     * 
     * @param entityPlayer      the {@link EntityPlayer} who finished using the itemInUse
     * @param itemInUse         the {@link ItemStack} which finished being used
     * @param itemInUseCount    the {@link EntityPlayer} item use count
     * @param previousStackSize the itemInUse {@link ItemStack#stackSize} before
     *                          {@link ItemStack#onFoodEaten(World, EntityPlayer)}
     * @param result            from itemInUse#onFoodEaten(entityPlayer.worldObj, entityPlayer)
     * @return the final resulting {@link ItemStack}
     */
    public static ItemStack beforeFinishUseEvent(EntityPlayer entityPlayer, ItemStack itemInUse, int itemInUseCount,
        ItemStack result, int previousStackSize) {
        result = ForgeEventFactory.onItemUseFinish(entityPlayer, itemInUse, itemInUseCount, result);
        if (result != itemInUse || (result != null && result.stackSize != previousStackSize)) {
            // Compare with either hands content
            if (itemInUse == entityPlayer.getCurrentEquippedItem()) {
                if (result != null && result.stackSize == 0) {
                    setPlayerCurrentItem(entityPlayer, null);
                } else {
                    setPlayerCurrentItem(entityPlayer, result);
                }
            } else if (itemInUse == getOffhandItem(entityPlayer)) {
                if (result != null && result.stackSize == 0) {
                    setPlayerOffhandItem(entityPlayer, null);
                } else {
                    setPlayerOffhandItem(entityPlayer, result);
                }
            }
        }
        // Reset stuff so that vanilla doesn't do anything
        entityPlayer.clearItemInUse();
        return null;
    }
}
