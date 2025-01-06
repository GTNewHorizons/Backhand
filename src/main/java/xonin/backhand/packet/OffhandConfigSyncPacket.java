
package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;

import io.netty.buffer.ByteBuf;
import xonin.backhand.client.utils.BackhandClientUtils;
import xonin.backhand.utils.BackhandConfig;

public final class OffhandConfigSyncPacket extends AbstractPacket {

    public static final String packetName = "MB2|ConfigSync";
    private EntityPlayer player;

    public OffhandConfigSyncPacket(EntityPlayer player) {
        this.player = player;
    }

    public OffhandConfigSyncPacket() {}

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        BackhandConfig.OffhandAttack = inputStream.readBoolean();
        BackhandConfig.EmptyOffhand = inputStream.readBoolean();
        BackhandConfig.OffhandBreakBlocks = inputStream.readBoolean();
        BackhandConfig.UseOffhandArrows = inputStream.readBoolean();
        BackhandConfig.UseOffhandBow = inputStream.readBoolean();
        BackhandConfig.OffhandTickHotswap = inputStream.readBoolean();
        BackhandConfig.AlternateOffhandSlot = inputStream.readInt();
        BackhandConfig.UseInventorySlot = inputStream.readBoolean();
        BackhandClientUtils.receivedConfigs = true;
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
        out.writeBoolean(BackhandConfig.UseOffhandArrows);
        out.writeBoolean(BackhandConfig.UseOffhandBow);
        out.writeBoolean(BackhandConfig.OffhandTickHotswap);
        out.writeInt(BackhandConfig.AlternateOffhandSlot);
        out.writeBoolean(BackhandConfig.UseInventorySlot);
    }
}
