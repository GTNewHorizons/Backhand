package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xonin.backhand.hooks.RightClickItemTracker;

@Mixin(Item.class)
public abstract class MixinItem {

    @Inject(method = "onItemRightClick", at = @At("HEAD"))
    private void backhand$markBaseRightClick(ItemStack stack, World world, EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir) {
        RightClickItemTracker.markBaseItemRightClick();
    }
}
