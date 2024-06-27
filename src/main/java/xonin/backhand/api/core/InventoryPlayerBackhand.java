package xonin.backhand.api.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import xonin.backhand.Backhand;

/**
 * User: nerd-boy
 * Date: 15/07/13
 * Time: 3:08 PM
 * Replacement for the player inventory
 */
public class InventoryPlayerBackhand extends InventoryPlayer {
    public static final int OFFHAND_HOTBAR_SLOT = 9;

    public InventoryPlayerBackhand(EntityPlayer entityPlayer) {
        super(entityPlayer);
    }

    /**
     * Patch used for "set current slot" vanilla packets
     * 
     * @param id the value to test for currentItem setting
     * @return true if it is possible for currentItem to be set with this value
     */
    public static boolean isValidSwitch(int id) {
        return (id >= 0 && id < getHotbarSize()) || id == OFFHAND_HOTBAR_SLOT;
    }

    public ItemStack getOffhandItem(){
        return BackhandUtils.getOffhandEP(player).getOffhandItem();
    }

    public void setOffhandItem(ItemStack stack) {
        BackhandUtils.getOffhandEP(player).setOffhandItem(stack);
    }

    public int clearInventory(Item item, int metadata) {
        int amount = 0;
        ItemStack itemstack = this.getOffhandItem();
        if (itemstack != null && (item == null || itemstack.getItem() == item)
            && (metadata <= -1 || itemstack.getItemDamage() == metadata)) {
            amount += itemstack.stackSize;
            this.setOffhandItem(null);
        }

        return amount + super.clearInventory(item, metadata);
    }

    public boolean addItemStackToInventory(ItemStack itemStack) {
        if (itemStack == null || itemStack.stackSize == 0 || itemStack.getItem() == null) return false;

        if (!Backhand.isOffhandBlacklisted(itemStack)) {
            if (this.getOffhandItem() == null && getFirstEmptyStack() == -1) {
                this.setOffhandItem(ItemStack.copyItemStack(itemStack));
                itemStack.stackSize = 0;
                BackhandUtils.getOffhandEP(player).syncOffhand = true;
                return true;
            }

            if (this.getOffhandItem() != null && this.getOffhandItem()
                .getItem() == itemStack.getItem()
                && this.getOffhandItem()
                    .isStackable()
                && this.getOffhandItem().stackSize < this.getOffhandItem()
                    .getMaxStackSize()
                && this.getOffhandItem().stackSize < this.getInventoryStackLimit()
                && (!this.getOffhandItem()
                    .getHasSubtypes()
                    || this.getOffhandItem()
                        .getItemDamage() == itemStack.getItemDamage())
                && ItemStack.areItemStackTagsEqual(this.getOffhandItem(), itemStack)) {
                if (this.getOffhandItem().stackSize + itemStack.stackSize > this.getOffhandItem()
                    .getMaxStackSize()) {
                    itemStack.stackSize -= this.getOffhandItem().stackSize;
                    this.getOffhandItem().stackSize = this.getOffhandItem().getMaxStackSize();
                    BackhandUtils.getOffhandEP(player).syncOffhand = true;
                    return super.addItemStackToInventory(itemStack);
                } else {
                    this.getOffhandItem().stackSize += itemStack.stackSize;
                    itemStack.stackSize = 0;
                    BackhandUtils.getOffhandEP(player).syncOffhand = true;
                    return true;
                }
            }
        }
        return super.addItemStackToInventory(itemStack);
    }

    public boolean consumeInventoryItem(Item item) {
        if (this.getOffhandItem() != null && this.getOffhandItem()
            .getItem() == item) {
            if (--this.getOffhandItem().stackSize <= 0) {
                this.setOffhandItem(null);
            }
            return true;
        } else {
            return super.consumeInventoryItem(item);
        }
    }

    public boolean hasItem(Item item) {
        if (this.getOffhandItem() != null && this.getOffhandItem()
            .getItem() == item) {
            return true;
        }

        return super.hasItem(item);
    }

    /**
     * Copy the slots content from another instance, usually for changing dimensions
     * 
     * @param par1InventoryPlayer the instance to copy from
     */
    @Override
    public void copyInventory(InventoryPlayer par1InventoryPlayer) {
        this.mainInventory = new ItemStack[par1InventoryPlayer.mainInventory.length];
        this.armorInventory = new ItemStack[par1InventoryPlayer.armorInventory.length];
        super.copyInventory(par1InventoryPlayer);
        if (par1InventoryPlayer instanceof InventoryPlayerBackhand) {
            this.setOffhandItem(ItemStack.copyItemStack(((InventoryPlayerBackhand) par1InventoryPlayer).getOffhandItem()));
        }
    }

    public void dropAllItems() {
        super.dropAllItems();
        this.player.func_146097_a(this.getOffhandItem(), true, false);
        this.setOffhandItem(null);
    }
}
