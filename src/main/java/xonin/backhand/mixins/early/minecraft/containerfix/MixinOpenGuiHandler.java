package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.internal.FMLMessage;
import cpw.mods.fml.common.network.internal.OpenGuiHandler;
import io.netty.channel.ChannelHandlerContext;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

/**
 * Client-side only.
 * Purpose: Use the backhand, if the Container was opened using the Backhand.
 */
@Mixin(value = OpenGuiHandler.class, remap = false)
public class MixinOpenGuiHandler {

    @Unique
    private static int backhand$heldItemTemp;

    // Swap to backhand
    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void backhand$modifyHeldItemPre(ChannelHandlerContext ctx, FMLMessage.OpenGui msg, CallbackInfo ci) {
        if (((IContainerHook) msg).backhand$wasOpenedWithBackhand()) {
            EntityPlayer player = FMLClientHandler.instance()
                .getClient().thePlayer;
            backhand$heldItemTemp = player.inventory.currentItem;
            player.inventory.currentItem = BackhandUtils.getOffhandSlot(player);
        }
    }

    // Swap back
    @Inject(method = "channelRead0", at = @At("RETURN"))
    private void backhand$modifyHeldItemPost(ChannelHandlerContext ctx, FMLMessage.OpenGui msg, CallbackInfo ci) {
        if (((IContainerHook) msg).backhand$wasOpenedWithBackhand()) {
            EntityPlayer player = FMLClientHandler.instance()
                .getClient().thePlayer;
            player.inventory.currentItem = backhand$heldItemTemp;

            ((IContainerHook) player.openContainer).backhand$setOpenedWithBackhand();
        }
    }
}
