package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import cpw.mods.fml.common.network.internal.FMLMessage;
import cpw.mods.fml.common.network.internal.OpenGuiHandler;
import io.netty.channel.ChannelHandlerContext;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(value = OpenGuiHandler.class, remap = false)
public class MixinOpenGuiHandler {

    @WrapMethod(method = "channelRead0")
    private void backhand$modifyHeldItem(ChannelHandlerContext ctx, FMLMessage.OpenGui msg, Operation<Void> original) {
        if (!((IContainerHook) msg).backhand$wasOpenedWithOffhand()) {
            original.call(ctx, msg);
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        int heldItemTemp = BackhandUtils.swapToOffhand(player);
        try {
            original.call(ctx, msg);
            ((IContainerHook) player.openContainer).backhand$setOpenedWithOffhand();
        } finally {
            BackhandUtils.swapBack(player, heldItemTemp);
        }
    }
}
