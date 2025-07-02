package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xonin.backhand.api.core.EnumHand;

@Mixin(EntityAITempt.class)
public class MixinEntityAITempt {

    @Shadow
    private Item field_151484_k;

    @Shadow
    private EntityPlayer temptingPlayer;

    @Inject(
        method = "shouldExecute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;getCurrentEquippedItem()Lnet/minecraft/item/ItemStack;"),
        cancellable = true)
    private void backhand$shouldExecute(CallbackInfoReturnable<Boolean> cir) {
        ItemStack offhandItemStack = EnumHand.OFF_HAND.getItem(temptingPlayer);
        if (offhandItemStack != null && offhandItemStack.getItem() == field_151484_k) {
            cir.setReturnValue(true);
        }
    }
}
