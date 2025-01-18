package xonin.backhand.mixins.early.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.api.core.IOffhandInventory;
import xonin.backhand.utils.BackhandConfig;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer implements IOffhandInventory {

    @Shadow
    public int currentItem;

    @Shadow
    public EntityPlayer player;

    @Shadow
    public ItemStack[] mainInventory;

    @Shadow
    public abstract boolean addItemStackToInventory(ItemStack p_70441_1_);

    @Shadow
    public abstract int getInventoryStackLimit();

    @Unique
    private int backhand$offhandSlot;

    @Unique
    private List<ItemStack> backhand$bg2Stacks = new ArrayList<>();

    @Inject(
        method = "readFromNBT",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:[Lnet/minecraft/item/ItemStack;",
            shift = At.Shift.AFTER))
    private void backhand$addOffhandSlot(NBTTagList p_70443_1_, CallbackInfo ci) {
        backhand$offhandSlot = mainInventory.length;
        mainInventory = new ItemStack[mainInventory.length + 1];
    }

    @Inject(
        method = "readFromNBT",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:[Lnet/minecraft/item/ItemStack;",
            ordinal = 0))
    private void backhand$importBG2Items(NBTTagList p_70443_1_, CallbackInfo ci, @Local ItemStack stack,
        @Local(name = "j") int index) {
        if (index >= 150 && index < 168) {
            backhand$bg2Stacks.add(stack);
        }
    }

    @Inject(method = "readFromNBT", at = @At(value = "TAIL"))
    private void backhand$giveBG2Items(NBTTagList p_70443_1_, CallbackInfo ci) {
        for (ItemStack stack : backhand$bg2Stacks) {
            if (!addItemStackToInventory(stack)) {
                player.entityDropItem(stack, 0.0F);
            }
        }

        backhand$bg2Stacks = null;
    }

    @Inject(
        method = "<init>",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:[Lnet/minecraft/item/ItemStack;",
            shift = At.Shift.AFTER))
    private void backhand$addOffhandSlot(EntityPlayer p_i1750_1_, CallbackInfo ci) {
        backhand$offhandSlot = mainInventory.length;
        mainInventory = new ItemStack[mainInventory.length + 1];
    }

    @ModifyReturnValue(method = "getCurrentItem", at = @At("RETURN"))
    private ItemStack backhand$getOffhandItem(ItemStack original) {
        if (currentItem == backhand$getOffhandSlot()) {
            return backhand$getOffhandItem();
        }
        return original;
    }

    @ModifyReturnValue(method = "getFirstEmptyStack", at = @At("RETURN"))
    private int backhand$checkOffhandPickup(int original) {
        if (!BackhandConfig.OffhandPickup && original == backhand$getOffhandSlot()) {
            return -1;
        }
        return original;
    }
    @Inject(method = "storeItemStack", at = @At("HEAD"), cancellable = true)
    private void backhand$storeItemStack(ItemStack checkStack, CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = player.getHeldItem();
        if (stack != null && checkStack(stack, checkStack)) {
            cir.setReturnValue(currentItem);
        }

        stack = backhand$getOffhandItem();
        if (stack != null && checkStack(stack, checkStack)) {
            cir.setReturnValue(backhand$getOffhandSlot());
        }
    }

    @Unique
    private boolean checkStack(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.isStackable()
            && a.stackSize < a.getMaxStackSize()
            && a.stackSize < getInventoryStackLimit()
            && (!a.getHasSubtypes() || a.getItemDamage() == b.getItemDamage())
            && ItemStack.areItemStackTagsEqual(a, b);
    }

    @Override
    public ItemStack backhand$getOffhandItem() {
        return mainInventory[backhand$getOffhandSlot()];
    }

    @Override
    public void backhand$setOffhandItem(ItemStack stack) {
        mainInventory[backhand$getOffhandSlot()] = stack;
    }

    @Override
    public int backhand$getOffhandSlot() {
        return backhand$offhandSlot;
    }
}
