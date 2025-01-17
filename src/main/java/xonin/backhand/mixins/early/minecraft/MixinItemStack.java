package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @WrapWithCondition(
        method = "updateAnimation",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/Item;onUpdate(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V"))
    private boolean backhand$updateOffhand(Item item, ItemStack stack, World worldIn, Entity entityIn, int index,
        boolean p_77663_5_) {
        if (entityIn instanceof EntityPlayer player && index == BackhandUtils.getOffhandSlot(player)) {
            BackhandUtils.useOffhandItem(player, () -> item.onUpdate(stack, worldIn, entityIn, index, p_77663_5_));
            return false;
        }
        return true;
    }
}
