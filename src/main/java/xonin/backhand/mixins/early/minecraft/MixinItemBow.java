package xonin.backhand.mixins.early.minecraft;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistrySimple;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(ItemBow.class)
public abstract class MixinItemBow extends Item {

    @ModifyExpressionValue(
        method = "onItemRightClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;hasItem(Lnet/minecraft/item/Item;)Z"))
    private boolean backhand$cancelAnimation(boolean original, @Local(argsOnly = true) EntityPlayer player) {
        if (!original) {
            ItemStack offhand = BackhandUtils.getOffhandItem(player);
            return offhand != null
                && ((RegistrySimple) BlockDispenser.dispenseBehaviorRegistry).containsKey(offhand.getItem());
        }
        return true;
    }
}
