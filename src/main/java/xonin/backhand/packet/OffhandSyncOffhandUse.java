package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import xonin.backhand.api.core.IBackhandPlayer;

public final class OffhandSyncOffhandUse extends AbstractPacket {

    public static final String packetName = "SyncItemUsage";
    private boolean isUsingOffhand;
    private EntityPlayer player;

    public OffhandSyncOffhandUse(EntityPlayer player, boolean isUsingOffhand) {
        this.player = player;
        this.isUsingOffhand = isUsingOffhand;
    }

    public OffhandSyncOffhandUse() {}

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        this.player = player.worldObj.getPlayerEntityByName(ByteBufUtils.readUTF8String(inputStream));
        if (this.player == null) return;
        ((IBackhandPlayer) this.player).setOffhandItemInUse(inputStream.readBoolean());
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, player.getCommandSenderName());
        out.writeBoolean(isUsingOffhand);
    }
}
