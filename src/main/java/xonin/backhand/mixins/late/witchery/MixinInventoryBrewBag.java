package xonin.backhand.mixins.late.witchery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.emoniph.witchery.item.ItemBrewBag;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import xonin.backhand.api.core.BackhandUtils;

@Mixin(value = ItemBrewBag.InventoryBrewBag.class, remap = false)
public class MixinInventoryBrewBag {

    @Shadow
    protected EntityPlayer player;

    @ModifyExpressionValue(
        method = { "hasInventory", "createInventory" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;getHeldItem()Lnet/minecraft/item/ItemStack;"))
    private ItemStack backhand$fixBrewBagCrash(ItemStack original) {
        if (original != null && original.getItem() instanceof ItemBrewBag) return original;
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        return (offhand != null && offhand.getItem() instanceof ItemBrewBag) ? offhand : original;
    }
}
