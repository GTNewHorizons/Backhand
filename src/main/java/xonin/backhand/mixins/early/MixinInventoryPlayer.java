package xonin.backhand.mixins.early;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.InventoryPlayerBackhand;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {

    @Shadow
    public int currentItem;

    @Shadow
    public EntityPlayer player;

    @ModifyReturnValue(method = "getCurrentItem", at = @At("RETURN"))
    private ItemStack backhand$getOffhandItem(ItemStack original) {
        if (currentItem == InventoryPlayerBackhand.OFFHAND_HOTBAR_SLOT) {
            return BackhandUtils.getOffhandItem(player);
        }

        return original;
    }

    // @Unique
    // public boolean battlegear2$isDirty = true;
    // @Unique
    // private ItemStack[] battlegear2$extraItems = new ItemStack[EXTRA_INV_SIZE];
    //
    // @Shadow
    // public int currentItem;
    // @Shadow
    // public EntityPlayer player;
    //
    // @Shadow
    // public abstract ItemStack getStackInSlot(int slotIn);
    //
    // @Shadow
    // public abstract ItemStack getCurrentItem();
    //
    // @Shadow
    // public abstract void setInventorySlotContents(int index, ItemStack stack);
    //
    // @Inject(method = "getCurrentItem", at = @At("HEAD"), cancellable = true)
    // private void battlegear2$getCurrentItem(CallbackInfoReturnable<ItemStack> cir) {
    // if (battlegear2$isBattlemode()) {
    // cir.setReturnValue(battlegear2$extraItems[currentItem - OFFSET]);
    // }
    // }
    //
    // @Inject(method = "func_146023_a", at = @At("HEAD"), cancellable = true)
    // private void battlegear2$func_146023_a(Block block, CallbackInfoReturnable<Float> cir) {
    // if (battlegear2$isBattlemode()) {
    // ItemStack currentItemStack = getCurrentItem();
    // cir.setReturnValue(currentItemStack != null ? currentItemStack.func_150997_a(block) : 1.0F);
    // }
    // }
    //
    // @Inject(method = "getStackInSlot", at = @At("HEAD"), cancellable = true)
    // private void battlegear2$getStackInSlot(int slotIn, CallbackInfoReturnable<ItemStack> cir) {
    // if (slotIn >= OFFSET) {
    // cir.setReturnValue(battlegear2$extraItems[slotIn - OFFSET]);
    // }
    // }
    //
    // @ModifyReturnValue(method = "hasItem", at = @At("RETURN"))
    // private boolean battlegear2$hasItem(boolean original, Item item) {
    // if (original) {
    // return true;
    // }
    // return battlegear2$getInventorySlotContainItem(item) >= 0;
    // }
    //
    // /**
    // * Returns a slot index in extra item inventory containing a specific item
    // */
    // @Unique
    // private int battlegear2$getInventorySlotContainItem(Item item) {
    // for (int i = 0; i < this.battlegear2$extraItems.length; ++i) {
    // if (this.battlegear2$extraItems[i] != null && this.battlegear2$extraItems[i].getItem() == item) {
    // return i;
    // }
    // }
    // return -1;
    // }
    //
    // @Inject(method = "decrementAnimations", at = @At("RETURN"))
    // private void battlegear2$decrementAnimations(CallbackInfo ci) {
    // for (int i = 0; i < this.battlegear2$extraItems.length; ++i) {
    // if (this.battlegear2$extraItems[i] != null) {
    // this.battlegear2$extraItems[i]
    // .updateAnimation(this.player.worldObj, this.player, i + OFFSET, this.currentItem == i + OFFSET);
    // }
    // }
    // }
    //
    // @Inject(method = "decrStackSize", at = @At("HEAD"), cancellable = true)
    // private void battlegear2$decrStackSize(int index, int count, CallbackInfoReturnable<ItemStack> cir) {
    // if (index >= OFFSET) {
    // ItemStack targetStack = battlegear2$extraItems[index - OFFSET];
    // if (targetStack != null) {
    // battlegear2$isDirty = true;
    // if (targetStack.stackSize <= count) {
    // battlegear2$extraItems[index - OFFSET] = null;
    // } else {
    // targetStack = battlegear2$extraItems[index - OFFSET].splitStack(count);
    // if (battlegear2$extraItems[index - OFFSET].stackSize == 0) {
    // battlegear2$extraItems[index - OFFSET] = null;
    // }
    // }
    // cir.setReturnValue(targetStack);
    // return;
    // }
    // cir.setReturnValue(null);
    // }
    // }
    //
    // @Inject(method = "setInventorySlotContents", at = @At("HEAD"), cancellable = true)
    // private void battlegear2$setInventorySlotContents(int index, ItemStack stack, CallbackInfo ci) {
    // if (index >= OFFSET) {
    // battlegear2$isDirty = true;
    // battlegear2$extraItems[index - OFFSET] = stack;
    // ci.cancel();
    // }
    // }
    //
    // @Override
    // public void battlegear2$setInventorySlotContents(int index, ItemStack stack, boolean changed) {
    // if (index >= OFFSET) {
    // battlegear2$isDirty = changed;
    // battlegear2$extraItems[index - OFFSET] = stack;
    // } else {
    // this.setInventorySlotContents(index, stack);
    // }
    // }
    //
    // @ModifyReturnValue(method = "clearInventory", at = @At("RETURN"))
    // private int battlegear2$clearInventory(int original, Item targetItem, int targetDamage) {
    // int stacks = 0;
    // for (int i = 0; i < battlegear2$extraItems.length; i++) {
    // if (battlegear2$extraItems[i] != null
    // && (targetItem == null || battlegear2$extraItems[i].getItem() == targetItem)
    // && (targetDamage <= -1 || battlegear2$extraItems[i].getItemDamage() == targetDamage)) {
    // stacks += battlegear2$extraItems[i].stackSize;
    // battlegear2$extraItems[i] = null;
    // }
    // }
    // battlegear2$isDirty = stacks > 0;
    // return original + stacks;
    // }
    //
    // @Inject(method = "getStackInSlotOnClosing", at = @At("HEAD"), cancellable = true)
    // private void battlegear2$getStackInSlotOnClosing(int slot, CallbackInfoReturnable<ItemStack> cir) {
    // if (slot >= OFFSET) {
    // cir.setReturnValue(battlegear2$extraItems[slot - OFFSET]);
    // }
    // }
    //
    // @Inject(method = "consumeInventoryItem", at = @At("HEAD"), cancellable = true)
    // private void battlegear2$consumeInventoryItem(Item item, CallbackInfoReturnable<Boolean> cir) {
    // int j = battlegear2$getInventorySlotContainItem(item);
    // if (j >= 0) {
    // this.battlegear2$isDirty = true;
    // if (--this.battlegear2$extraItems[j].stackSize <= 0) {
    // this.battlegear2$extraItems[j] = null;
    // }
    // cir.setReturnValue(true);
    // }
    // }
    //
    // @Inject(method = "dropAllItems", at = @At("RETURN"))
    // private void battlegear2$dropAllItems(CallbackInfo ci) {
    // this.battlegear2$isDirty = true;
    // for (int i = 0; i < this.battlegear2$extraItems.length; ++i) {
    // if (this.battlegear2$extraItems[i] != null) {
    // this.player.func_146097_a(this.battlegear2$extraItems[i], true, false);
    // this.battlegear2$extraItems[i] = null;
    // }
    // }
    // }
    //
    // @Inject(method = "copyInventory", at = @At("RETURN"))
    // private void battlegear2$copyInventory(InventoryPlayer otherInventory, CallbackInfo ci) {
    // for (int i = 0; i < battlegear2$extraItems.length; i++) {
    // this.battlegear2$extraItems[i] = ItemStack.copyItemStack(otherInventory.getStackInSlot(i + OFFSET));
    // }
    // }
    //
    // @ModifyReturnValue(method = "writeToNBT", at = @At("RETURN"))
    // private NBTTagList battlegear2$writeToNBT(NBTTagList nbtTagList) {
    // NBTTagCompound nbttagcompound;
    // for (int i = 0; i < battlegear2$extraItems.length; ++i) {
    // if (battlegear2$extraItems[i] != null) {
    // nbttagcompound = new NBTTagCompound();
    // // This will be -ve, but meh still works
    // nbttagcompound.setByte("Slot", (byte) (i + OFFSET));
    // this.battlegear2$extraItems[i].writeToNBT(nbttagcompound);
    // nbtTagList.appendTag(nbttagcompound);
    // }
    // }
    // return nbtTagList;
    // }
    //
    // @Inject(method = "readFromNBT", at = @At("HEAD"))
    // private void battlegear2$readFromNBT$initInventory(NBTTagList taglist, CallbackInfo ci) {
    // this.battlegear2$extraItems = new ItemStack[EXTRA_INV_SIZE];
    // }
    //
    // @Inject(
    // method = "readFromNBT",
    // at = @At(
    // value = "INVOKE",
    // target =
    // "Lnet/minecraft/item/ItemStack;loadItemStackFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/item/ItemStack;",
    // shift = At.Shift.BY,
    // by = 2),
    // locals = LocalCapture.CAPTURE_FAILHARD)
    // private void battlegear2$readFromNBT$setItems(NBTTagList p_70443_1_, CallbackInfo ci, int i,
    // NBTTagCompound nbttagcompound, int j, ItemStack stack) {
    // if (stack != null && j >= OFFSET && j - OFFSET < this.battlegear2$extraItems.length) {
    // this.battlegear2$extraItems[j - OFFSET] = stack;
    // }
    // }
    //
    // @Override
    // public boolean battlegear2$isDirty() {
    // return battlegear2$isDirty;
    // }
    //
    // @Override
    // public void battlegear2$setDirty(boolean dirty) {
    // this.battlegear2$isDirty = dirty;
    // }
    //
    // /**
    // * @return true if the current item value is offset in the battle slot range
    // */
    // @Override
    // public boolean battlegear2$isBattlemode() {
    // return this.currentItem >= OFFSET && this.currentItem < OFFSET + EXTRA_ITEMS;
    // }
    //
    // /**
    // * Get the offset item (for the left hand)
    // *
    // * @return the item held in left hand, if any
    // */
    // @Override
    // public ItemStack battlegear2$getCurrentOffhandWeapon() {
    // if (battlegear2$isBattlemode()) {
    // return getStackInSlot(currentItem + WEAPON_SETS);
    // } else {
    // return null;
    // }
    // }

}
