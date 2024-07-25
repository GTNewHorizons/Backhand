package xonin.backhand.mixins.early;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import xonin.backhand.HookContainerClass;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.client.ClientEventHandler;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Inject(method = "damageItem", at = @At(value = "TAIL"))
    private void backhand$damageOffhand(int p_77972_1_, EntityLivingBase entity, CallbackInfo ci) {
        if (!(entity instanceof EntityPlayer player)) return;

        ItemStack itemStack = (ItemStack) (Object) this;
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (offhandItem != null && itemStack == offhandItem && itemStack.stackSize == 0) {
            BackhandUtils.setPlayerOffhandItem(player, null);
            ForgeEventFactory.onPlayerDestroyItem(player, offhandItem);
        }
    }

    @ModifyReturnValue(method = "getItemUseAction", at = @At(value = "TAIL"))
    private EnumAction backhand$getOffhandUseAction(EnumAction original) {
        if (original == EnumAction.none || FMLCommonHandler.instance()
            .getEffectiveSide() == Side.CLIENT && ClientEventHandler.renderingPlayer == null) {
            return original;
        }
        EntityPlayer player = ClientEventHandler.renderingPlayer;
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (offhandItem != null) {
            ItemStack mainHandItem = player.getCurrentEquippedItem();
            if (mainHandItem != null && (BackhandUtils.checkForRightClickFunctionNoAction(mainHandItem)
                || HookContainerClass.isItemBlock(mainHandItem.getItem()))) {
                if (itemStack == offhandItem) {
                    return EnumAction.none;
                }
            } else if (itemStack == mainHandItem && (!(BackhandUtils.checkForRightClickFunctionNoAction(offhandItem)
                || HookContainerClass.isItemBlock(offhandItem.getItem())) || player.getItemInUse() != mainHandItem)) {
                    return EnumAction.none;
                }
        }
        return original;
    }

}
