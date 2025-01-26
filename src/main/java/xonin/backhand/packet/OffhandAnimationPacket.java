package xonin.backhand.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import xonin.backhand.api.core.IBackhandPlayer;

public class OffhandAnimationPacket implements IMessage {

    private int entityId;

    public OffhandAnimationPacket() {}

    public OffhandAnimationPacket(EntityPlayer player) {
        this.entityId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<OffhandAnimationPacket, IMessage> {

        @Override
        public IMessage onMessage(OffhandAnimationPacket message, MessageContext ctx) {
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(message.entityId);
            if (entity instanceof IBackhandPlayer player) {
                player.swingOffItem();
            }
            return null;
        }
    }
}
