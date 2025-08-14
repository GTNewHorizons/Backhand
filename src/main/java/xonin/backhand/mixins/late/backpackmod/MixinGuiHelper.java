package xonin.backhand.mixins.late.backpackmod;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import de.eydamos.backpack.helper.GuiHelper;
import de.eydamos.backpack.network.message.MessageOpenBackpack;
import de.eydamos.backpack.saves.BackpackSave;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(value = GuiHelper.class, remap = false)
public class MixinGuiHelper {

    @Inject(
        method = "displayBackpack",
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/common/network/simpleimpl/SimpleNetworkWrapper;sendTo(Lcpw/mods/fml/common/network/simpleimpl/IMessage;Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    private static void backhand$addBackhandValue_Client(BackpackSave backpackSave, IInventory inventory,
        EntityPlayerMP entityPlayer, CallbackInfo ci, @Local MessageOpenBackpack messageGui) {
        if (BackhandUtils.isUsingOffhand(entityPlayer)) {
            ((IContainerHook) messageGui).backhand$setOpenedWithBackhand();
        }
    }
}
