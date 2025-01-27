package xonin.backhand.packet;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class OffhandCancelUsage implements IMessage {

    public OffhandCancelUsage() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<OffhandCancelUsage, IMessage> {

        @Override
        public IMessage onMessage(OffhandCancelUsage message, MessageContext ctx) {
            Minecraft.getMinecraft().thePlayer.clearItemInUse();
            return null;
        }
    }
}
