package xonin.backhand.mixins.early.minecraft.containerfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.common.network.internal.FMLMessage;
import io.netty.buffer.ByteBuf;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(value = FMLMessage.OpenGui.class, remap = false)
public class MixinFMLOpenGui implements IContainerHook {

    @Unique
    private boolean backhand$openedWithOffhand;

    @Inject(method = "toBytes", at = @At("RETURN"))
    private void backhand$injectBackhandCompat_Server(ByteBuf buf, CallbackInfo ci) {
        buf.writeBoolean(backhand$openedWithOffhand);
    }

    @Inject(method = "fromBytes", at = @At("RETURN"))
    private void backhand$injectBackhandCompat_Client(ByteBuf buf, CallbackInfo ci) {
        backhand$openedWithOffhand = buf.readBoolean();
    }

    @Override
    public final boolean backhand$wasOpenedWithOffhand() {
        return backhand$openedWithOffhand;
    }

    // Since I don't have the EntityPlayerMP reference, I need to inject into MixinFMLNetworkHandler to set it afterward
    @Override
    public final void backhand$setOpenedWithOffhand() {
        backhand$openedWithOffhand = true;
    }
}
