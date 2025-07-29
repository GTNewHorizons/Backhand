package xonin.backhand.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import xonin.backhand.api.core.IBackhandPlayer;

public final class OffhandSyncOffhandUse implements IMessage {

    private int entityId;
    private boolean isUsingOffhand;

    public OffhandSyncOffhandUse(EntityPlayer player, boolean isUsingOffhand) {
        this.entityId = player.getEntityId();
        this.isUsingOffhand = isUsingOffhand;
    }

    public OffhandSyncOffhandUse() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.isUsingOffhand = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(isUsingOffhand);
    }

    public static class Handler implements IMessageHandler<OffhandSyncOffhandUse, IMessage> {

        @Override
        public IMessage onMessage(OffhandSyncOffhandUse message, MessageContext ctx) {
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(message.entityId);
            if (entity instanceof EntityPlayer player) {
                ((IBackhandPlayer) player).setOffhandItemInUse(message.isUsingOffhand);
            }
            return null;
        }
    }
}
