
package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;

import io.netty.buffer.ByteBuf;
import xonin.backhand.utils.BackhandConfig;

public final class OffhandConfigSyncPacket extends AbstractPacket {

    public static final String packetName = "MB2|ConfigSync";

    public OffhandConfigSyncPacket() {}

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        BackhandConfig.OffhandAttack = inputStream.readBoolean();
        BackhandConfig.EmptyOffhand = inputStream.readBoolean();
        BackhandConfig.OffhandBreakBlocks = inputStream.readBoolean();
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        out.writeBoolean(BackhandConfig.OffhandAttack);
        out.writeBoolean(BackhandConfig.EmptyOffhand);
        out.writeBoolean(BackhandConfig.OffhandBreakBlocks);
    }
}
