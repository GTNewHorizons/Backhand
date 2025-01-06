
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
        BackhandConfig.general.OffhandAttack = inputStream.readBoolean();
        BackhandConfig.general.EmptyOffhand = inputStream.readBoolean();
        BackhandConfig.general.OffhandBreakBlocks = inputStream.readBoolean();
        BackhandConfig.general.UseOffhandArrows = inputStream.readBoolean();
        BackhandConfig.general.UseOffhandBow = inputStream.readBoolean();
        BackhandConfig.general.OffhandTickHotswap = inputStream.readBoolean();
        BackhandConfig.general.AlternateOffhandSlot = inputStream.readInt();
        BackhandConfig.general.UseInventorySlot = inputStream.readBoolean();
        BackhandClientUtils.receivedConfigs = true;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        out.writeBoolean(BackhandConfig.general.OffhandAttack);
        out.writeBoolean(BackhandConfig.general.EmptyOffhand);
        out.writeBoolean(BackhandConfig.general.OffhandBreakBlocks);
        out.writeBoolean(BackhandConfig.general.UseOffhandArrows);
        out.writeBoolean(BackhandConfig.general.UseOffhandBow);
        out.writeBoolean(BackhandConfig.general.OffhandTickHotswap);
        out.writeInt(BackhandConfig.general.AlternateOffhandSlot);
        out.writeBoolean(BackhandConfig.general.UseInventorySlot);
    }
}
