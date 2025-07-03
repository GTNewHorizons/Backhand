package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import xonin.backhand.api.core.EnumHand;

@Mixin(EntityAITempt.class)
public class MixinEntityAITempt {

    @Shadow
    private Item field_151484_k;

    @Shadow
    private EntityPlayer temptingPlayer;

    @ModifyReturnValue(method = "shouldExecute", at = @At(value = "RETURN", ordinal = 2))
    private boolean backhand$shouldExecute(boolean original) {
        if (original) return true;
        ItemStack offhandItemStack = EnumHand.OFF_HAND.getItem(temptingPlayer);
        return offhandItemStack != null && offhandItemStack.getItem() == field_151484_k;
    }
}
