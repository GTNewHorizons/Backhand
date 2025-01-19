package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(EntityFishHook.class)
public abstract class MixinEntityFishHook {

    @Shadow
    public EntityPlayer field_146042_b;

    @Inject(
        method = "onUpdate",
        cancellable = true,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/EntityFishHook;setDead()V", ordinal = 0))
    private void backhand$onUpdate(CallbackInfo ci) {
        ItemStack itemstack = BackhandUtils.getOffhandItem(field_146042_b);
        if (itemstack != null && Items.fishing_rod.equals(itemstack.getItem())) {
            ci.cancel();
        }
    }
}
