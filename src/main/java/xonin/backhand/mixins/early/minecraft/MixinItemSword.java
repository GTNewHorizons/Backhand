package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(ItemSword.class)
public abstract class MixinItemSword extends Item {

    @WrapMethod(method = "onItemRightClick")
    private ItemStack backhand$onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player,
        Operation<ItemStack> original) {
        if (BackhandUtils.getOffhandItem(player) != null) {
            return itemStackIn;
        } else {
            return original.call(itemStackIn, worldIn, player);
        }
    }
}
