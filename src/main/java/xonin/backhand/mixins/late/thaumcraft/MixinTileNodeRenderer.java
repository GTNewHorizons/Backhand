package xonin.backhand.mixins.late.thaumcraft;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import thaumcraft.client.renderers.tile.TileNodeRenderer;
import thaumcraft.common.items.relics.ItemThaumometer;
import xonin.backhand.api.core.BackhandUtils;

@Mixin(TileNodeRenderer.class)
public class MixinTileNodeRenderer {

    @WrapOperation(
        method = "renderTileEntityAt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    private ItemStack backhand$fixNodeRender(InventoryPlayer instance, Operation<ItemStack> original) {
        ItemStack offhand = BackhandUtils.getOffhandItem(instance.player);

        if (offhand != null && offhand.getItem() instanceof ItemThaumometer) {
            return offhand;
        }

        return original.call(instance);
    }
}
