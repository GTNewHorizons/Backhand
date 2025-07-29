package xonin.backhand.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import xonin.backhand.api.core.BackhandUtils;

/**
 * User: nerd-boy
 * Date: 26/06/13
 * Time: 1:40 PM
 */
public final class OffhandSyncItemPacket implements IMessage {

    private int entityId;
    private ItemStack stack;

    public OffhandSyncItemPacket(EntityPlayer player) {
        this.entityId = player.getEntityId();
        this.stack = BackhandUtils.getOffhandItem(player);
    }

    public OffhandSyncItemPacket() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<OffhandSyncItemPacket, IMessage> {

        @Override
        public IMessage onMessage(OffhandSyncItemPacket message, MessageContext ctx) {
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(message.entityId);
            if (entity instanceof EntityPlayer player) {
                BackhandUtils.setPlayerOffhandItem(player, message.stack);
            }
            return null;
        }
    }
}
