package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cpw.mods.fml.common.eventhandler.Event;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {

    @Inject(method = "canInteractWith", at = @At("HEAD"), cancellable = true)
    private static void backhand$canInteractWithPre(EntityPlayer player, Container openContainer,
        CallbackInfoReturnable<Boolean> cir) {
        if (((IContainerHook) openContainer).backhand$wasOpenedWithOffhand()) {
            int currentItem = BackhandUtils.swapToOffhand(player);

            PlayerOpenContainerEvent event = new PlayerOpenContainerEvent(player, openContainer);
            MinecraftForge.EVENT_BUS.post(event);
            cir.setReturnValue(
                event.getResult() == Event.Result.DEFAULT ? event.canInteractWith
                    : event.getResult() == Event.Result.ALLOW);

            BackhandUtils.swapBack(player, currentItem);
        }
    }
}
