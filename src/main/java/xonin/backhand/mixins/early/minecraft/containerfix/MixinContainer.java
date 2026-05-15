package xonin.backhand.mixins.early.minecraft.containerfix;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xonin.backhand.Backhand;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(Container.class)
public class MixinContainer implements IContainerHook {

    @Shadow
    public List<Slot> inventorySlots;

    @Unique
    private boolean backhand$openedWithOffhand;

    @Override
    public final boolean backhand$wasOpenedWithOffhand() {
        return backhand$openedWithOffhand;
    }

    @Override
    public final void backhand$setOpenedWithOffhand() {
        backhand$openedWithOffhand = true;
    }

    @Inject(method = "addCraftingToCrafters", at = @At("HEAD"))
    private void backhand$setupOffhand(ICrafting p_75132_1_, CallbackInfo ci) {
        if (p_75132_1_ instanceof EntityPlayerMP player) {
            if (BackhandUtils.isUsingOffhand(player)) {
                backhand$setOpenedWithOffhand();
            }
        }
    }

    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
    private void backhand$swapWithOffhandSlot(int slotId, int clickedButton, int mode, EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir) {
        int offhandSlot = BackhandUtils.getOffhandSlot(player);
        if (mode != 2 || clickedButton != offhandSlot) {
            return;
        }

        if (slotId < 0 || slotId >= inventorySlots.size()) {
            cir.setReturnValue(null);
            return;
        }

        Slot slot = inventorySlots.get(slotId);
        if (slot == null || !slot.canTakeStack(player)) {
            cir.setReturnValue(null);
            return;
        }

        ItemStack slotStack = slot.getStack();
        if (Backhand.isOffhandBlacklisted(slotStack)) {
            cir.setReturnValue(null);
            return;
        }

        InventoryPlayer inventory = player.inventory;
        ItemStack offhandStack = inventory.getStackInSlot(offhandSlot);
        ItemStack result = slotStack == null ? null : slotStack.copy();

        boolean canSwapBack = offhandStack == null || slot.inventory == inventory && slot.isItemValid(offhandStack);
        int emptySlot = -1;
        if (!canSwapBack) {
            emptySlot = inventory.getFirstEmptyStack();
            canSwapBack = emptySlot > -1;
        }

        if (slot.getHasStack() && canSwapBack) {
            ItemStack swappedStack = slot.getStack();
            inventory.setInventorySlotContents(offhandSlot, swappedStack.copy());

            if ((slot.inventory != inventory || !slot.isItemValid(offhandStack)) && offhandStack != null) {
                if (emptySlot > -1) {
                    inventory.addItemStackToInventory(offhandStack);
                    slot.decrStackSize(swappedStack.stackSize);
                    slot.putStack(null);
                    slot.onPickupFromSlot(player, swappedStack);
                }
            } else {
                slot.decrStackSize(swappedStack.stackSize);
                slot.putStack(offhandStack);
                slot.onPickupFromSlot(player, swappedStack);
            }
        } else if (!slot.getHasStack() && offhandStack != null && slot.isItemValid(offhandStack)) {
            inventory.setInventorySlotContents(offhandSlot, null);
            slot.putStack(offhandStack);
        }

        cir.setReturnValue(result);
    }
}
