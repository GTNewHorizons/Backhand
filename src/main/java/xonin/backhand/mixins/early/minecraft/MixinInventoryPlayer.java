package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IOffhandInventory;
import xonin.backhand.utils.BackhandConfig;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer implements IOffhandInventory {

    @Shadow
    public int currentItem;

    @Shadow
    public EntityPlayer player;

    @Shadow
    public ItemStack[] mainInventory = new ItemStack[37];

    @ModifyConstant(method = "readFromNBT", constant = @Constant(intValue = 36))
    private int backhand$correctInventorySize(int constant) {
        return 37;
    }

    @ModifyReturnValue(method = "getCurrentItem", at = @At("RETURN"))
    private ItemStack backhand$getOffhandItem(ItemStack original) {
        if (currentItem == OFFHAND_HOTBAR_SLOT) {
            return BackhandUtils.getOffhandItem(player);
        }
        return original;
    }

    @ModifyReturnValue(method = "getFirstEmptyStack", at = @At("RETURN"))
    private int backhand$checkOffhandPickup(int original) {
        if (!BackhandConfig.OffhandPickup && original == OFFHAND_HOTBAR_SLOT) {
            return -1;
        }
        return original;
    }

    @Override
    public ItemStack backhand$getOffhandItem() {
        return mainInventory[OFFHAND_HOTBAR_SLOT];
    }

    @Override
    public void backhand$setOffhandItem(ItemStack stack) {
        mainInventory[OFFHAND_HOTBAR_SLOT] = stack;
    }
}
