package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(EntityOtherPlayerMP.class)
public abstract class MixinEntityOtherPlayerMP extends AbstractClientPlayer {

    @Shadow
    private boolean isItemInUse;

    private MixinEntityOtherPlayerMP(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
    }

    @Inject(
        method = "onUpdate",
        cancellable = true,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/entity/EntityOtherPlayerMP;isItemInUse:Z",
            ordinal = 0))
    private void backhand$isItemInUseHook(CallbackInfo ci) {
        ItemStack itemStack = getCurrentEquippedItem();
        ItemStack offhand = BackhandUtils.getOffhandItem(this);
        if (BackhandUtils.usagePriorAttack(offhand)) itemStack = offhand;
        if (!isItemInUse && isEating() && itemStack != null) {
            setItemInUse(itemStack, itemStack.getMaxItemUseDuration());
            isItemInUse = true;
        } else if (isItemInUse && !isEating()) {
            clearItemInUse();
            isItemInUse = false;
        }
        ci.cancel();
    }

}
