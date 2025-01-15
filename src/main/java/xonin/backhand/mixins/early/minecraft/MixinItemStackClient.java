package xonin.backhand.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.client.ClientEventHandler;

@Mixin(ItemStack.class)
public abstract class MixinItemStackClient {

    @ModifyReturnValue(method = "getItemUseAction", at = @At(value = "TAIL"))
    private EnumAction backhand$getOffhandUseAction(EnumAction original) {
        if (original == EnumAction.none || ClientEventHandler.renderingPlayer == null) {
            return original;
        }
        EntityPlayer player = ClientEventHandler.renderingPlayer;
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (offhandItem != null) {
            ItemStack mainHandItem = player.getCurrentEquippedItem();
            if (mainHandItem != null && (BackhandUtils.checkForRightClickFunctionNoAction(mainHandItem)
                || backhand$isItemBlock(mainHandItem.getItem()))) {
                if (itemStack == offhandItem) {
                    return EnumAction.none;
                }
            } else if (itemStack == mainHandItem && (!(BackhandUtils.checkForRightClickFunctionNoAction(offhandItem)
                || backhand$isItemBlock(offhandItem.getItem())) || player.getItemInUse() != mainHandItem)) {
                    return EnumAction.none;
                }
        }
        return original;
    }

    @Unique
    private boolean backhand$isItemBlock(Item item) {
        return item instanceof ItemBlock || item instanceof ItemDoor
            || item instanceof ItemSign
            || item instanceof ItemReed
            || item instanceof ItemSeedFood
            || item instanceof ItemRedstone
            || item instanceof ItemBucket
            || item instanceof ItemSkull;
    }

}
