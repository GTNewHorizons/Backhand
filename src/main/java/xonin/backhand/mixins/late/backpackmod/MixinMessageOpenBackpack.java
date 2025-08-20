package xonin.backhand.mixins.late.backpackmod;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import de.eydamos.backpack.network.message.MessageOpenBackpack;
import io.netty.buffer.ByteBuf;
import xonin.backhand.hooks.containerfix.IContainerHook;

@Mixin(value = MessageOpenBackpack.class, remap = false)
public class MixinMessageOpenBackpack implements IContainerHook {

    @Unique
    private boolean backhand$openedWithOffhand;

    @Inject(method = "fromBytes", at = @At("RETURN"))
    public void backhand$readBackhandFromBytes(ByteBuf buffer, CallbackInfo ci) {
        backhand$openedWithOffhand = buffer.readBoolean();
    }

    @Inject(method = "toBytes", at = @At("RETURN"))
    public void backhand$addBackhandToBytes(ByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(backhand$openedWithOffhand);
    }

    @Inject(
        method = "onMessage(Lde/eydamos/backpack/network/message/MessageOpenBackpack;Lcpw/mods/fml/common/network/simpleimpl/MessageContext;)Lcpw/mods/fml/common/network/simpleimpl/IMessage;",
        at = @At("RETURN"))
    public void backhand$onMessage(MessageOpenBackpack message, MessageContext ctx,
        CallbackInfoReturnable<IMessage> cir, @Local EntityPlayer entityPlayer) {
        try {
            if (backhand$openedWithOffhand) {
                ((IContainerHook) entityPlayer.openContainer).backhand$setOpenedWithOffhand();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public final boolean backhand$wasOpenedWithOffhand() {
        return backhand$openedWithOffhand;
    }

    @Override
    public final void backhand$setOpenedWithOffhand() {
        backhand$openedWithOffhand = true;
    }
}
