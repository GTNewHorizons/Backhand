package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer {

    public MixinEntityPlayerMP(World p_i45324_1_, GameProfile p_i45324_2_) {
        super(p_i45324_1_, p_i45324_2_);
    }

    @Redirect(
        method = "localOnUpdate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"))
    private void backhand$detectAndSendChanges1(Container instance) {
        if (((IContainerHook) instance).backhand$wasOpenedWithBackhand()) {
            int heldItem = this.inventory.currentItem;
            this.inventory.currentItem = BackhandUtils.getOffhandSlot(this);
            instance.detectAndSendChanges();
            this.inventory.currentItem = heldItem;
        } else {
            instance.detectAndSendChanges();
        }
    }

    @Redirect(
        method = "onItemPickup",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"))
    private void backhand$detectAndSendChanges2(Container instance) {
        if (((IContainerHook) instance).backhand$wasOpenedWithBackhand()) {
            int heldItem = this.inventory.currentItem;
            this.inventory.currentItem = BackhandUtils.getOffhandSlot(this);
            instance.detectAndSendChanges();
            this.inventory.currentItem = heldItem;
        } else {
            instance.detectAndSendChanges();
        }
    }
}
