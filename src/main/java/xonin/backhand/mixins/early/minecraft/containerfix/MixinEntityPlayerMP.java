package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer {

    public MixinEntityPlayerMP(World p_i45324_1_, GameProfile p_i45324_2_) {
        super(p_i45324_1_, p_i45324_2_);
    }

    @WrapMethod(method = "closeContainer")
    private void backhand$wrapCloseContainer(Operation<Void> original) {
        boolean wasOffhand = ((IContainerHook) this.openContainer).backhand$wasOpenedWithOffhand();
        int heldItemTemp = wasOffhand ? BackhandUtils.swapToOffhand(this) : 0;
        try {
            original.call();
        } finally {
            if (wasOffhand) {
                BackhandUtils.swapBack(this, heldItemTemp);
            }
        }
    }

    @WrapMethod(method = "onUpdate")
    private void backhand$wrapOnUpdate(Operation<Void> original) {
        boolean wasOffhand = ((IContainerHook) this.openContainer).backhand$wasOpenedWithOffhand();
        int heldItemTemp = wasOffhand ? BackhandUtils.swapToOffhand(this) : 0;
        try {
            original.call();
        } finally {
            if (wasOffhand) {
                BackhandUtils.swapBack(this, heldItemTemp);
            }
        }
    }

    @Redirect(
        method = "onItemPickup",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"))
    private void backhand$detectAndSendChanges2(Container instance) {
        if (((IContainerHook) instance).backhand$wasOpenedWithOffhand()) {
            int currentItem = BackhandUtils.swapToOffhand(this);
            try {
                instance.detectAndSendChanges();
            } finally {
                BackhandUtils.swapBack(this, currentItem);
            }
        } else {
            instance.detectAndSendChanges();
        }
    }
}
