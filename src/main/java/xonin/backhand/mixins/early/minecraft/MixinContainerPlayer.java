package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.IOffhandInventory;

@Mixin(ContainerPlayer.class)
public abstract class MixinContainerPlayer extends Container {

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void backhand2$addOffhandSlot(InventoryPlayer p_i1819_1_, boolean p_i1819_2_, EntityPlayer p_i1819_3_,
        CallbackInfo ci) {
        addSlotToContainer(new Slot(p_i1819_1_, IOffhandInventory.OFFHAND_HOTBAR_SLOT, 80, 65));
    }
}
