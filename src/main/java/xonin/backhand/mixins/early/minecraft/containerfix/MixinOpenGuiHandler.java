package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.common.network.internal.FMLMessage;
import cpw.mods.fml.common.network.internal.OpenGuiHandler;
import io.netty.channel.ChannelHandlerContext;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(value = OpenGuiHandler.class, remap = false)
public class MixinOpenGuiHandler {

    @Unique
    private int backhand$heldItemTemp;

    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void backhand$modifyHeldItemPre(ChannelHandlerContext ctx, FMLMessage.OpenGui msg, CallbackInfo ci) {
        if (((IContainerHook) msg).backhand$wasOpenedWithOffhand()) {
            backhand$heldItemTemp = BackhandUtils.swapToOffhand(Minecraft.getMinecraft().thePlayer);
        }
    }

    @Inject(method = "channelRead0", at = @At("RETURN"))
    private void backhand$modifyHeldItemPost(ChannelHandlerContext ctx, FMLMessage.OpenGui msg, CallbackInfo ci) {
        if (((IContainerHook) msg).backhand$wasOpenedWithOffhand()) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            BackhandUtils.swapBack(player, backhand$heldItemTemp);

            ((IContainerHook) player.openContainer).backhand$setOpenedWithOffhand();
        }
    }
}
