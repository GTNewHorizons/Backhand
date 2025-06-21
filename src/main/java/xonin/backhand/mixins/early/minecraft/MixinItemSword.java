package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import xonin.backhand.utils.BackhandConfig;

@Mixin(ItemSword.class)
public abstract class MixinItemSword extends Item {

    @ModifyReturnValue(method = "getItemUseAction", at = @At("RETURN"))
    private EnumAction backhand$getItemUseAction(EnumAction original, ItemStack stack) {
        return BackhandConfig.RemoveSwordBlock ? EnumAction.none : original;
    }

    @WrapWithCondition(
        method = "onItemRightClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;setItemInUse(Lnet/minecraft/item/ItemStack;I)V"))
    private boolean backhand$onItemRightClick(EntityPlayer caller, ItemStack arg1, int arg2) {
        return !BackhandConfig.RemoveSwordBlock;
    }
}
