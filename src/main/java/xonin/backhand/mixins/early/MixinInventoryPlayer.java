package xonin.backhand.mixins.early;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.InventoryPlayerBackhand;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {

    // Todo: We're already replacing the inventory in EntityPlayer, can this instead be an @Override in
    // InventoryPlayerBackhand?

    @Shadow
    public int currentItem;

    @Shadow
    public EntityPlayer player;

    @ModifyReturnValue(method = "getCurrentItem", at = @At("RETURN"))
    private ItemStack backhand$getOffhandItem(ItemStack original) {
        if (currentItem == InventoryPlayerBackhand.OFFHAND_HOTBAR_SLOT) {
            return BackhandUtils.getOffhandItem(player);
        }

        return original;
    }
}
