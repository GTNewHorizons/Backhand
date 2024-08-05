package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Inject(method = "damageItem", at = @At(value = "TAIL"))
    private void backhand$damageOffhand(int p_77972_1_, EntityLivingBase entity, CallbackInfo ci) {
        if (!(entity instanceof EntityPlayer player) || entity instanceof FakePlayer) return;

        ItemStack itemStack = (ItemStack) (Object) this;
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (offhandItem != null && itemStack == offhandItem && itemStack.stackSize == 0) {
            BackhandUtils.setPlayerOffhandItem(player, null);
            ForgeEventFactory.onPlayerDestroyItem(player, offhandItem);
        }
    }
}
