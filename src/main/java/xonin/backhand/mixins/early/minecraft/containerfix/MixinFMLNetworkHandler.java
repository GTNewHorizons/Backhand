package xonin.backhand.mixins.early.minecraft.containerfix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import cpw.mods.fml.common.network.internal.FMLMessage;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.hooks.containerfix.IContainerHook;

/**
 * Server-side only.
 * Purpose: Set openedWithBackhand in FMLMessage.OpenGui. This gets sent to the client.
 */
@Mixin(value = FMLNetworkHandler.class, remap = false)
public class MixinFMLNetworkHandler {

    @Inject(
        method = "openGui",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETSTATIC,
            target = "Lcpw/mods/fml/common/network/internal/FMLNetworkHandler;channelPair:Ljava/util/EnumMap;"))
    private static void backhand$onOpenGui(EntityPlayer entityPlayer, Object mod, int modGuiId, World world, int x,
        int y, int z, CallbackInfo ci, @Local FMLMessage.OpenGui guiMessage) {
        if (BackhandUtils.isUsingOffhand(entityPlayer)) {
            ((IContainerHook) guiMessage).backhand$setOpenedWithBackhand();
        }
    }

}
