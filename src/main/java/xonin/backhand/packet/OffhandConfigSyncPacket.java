
package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO;

import io.netty.buffer.ByteBuf;
import xonin.backhand.Backhand;

public final class OffhandConfigSyncPacket extends AbstractPacket {

    public static final String packetName = "MB2|ConfigSync";
    private EntityPlayer player;

    public OffhandConfigSyncPacket(EntityPlayer player) {
        this.player = player;
    }

    public OffhandConfigSyncPacket() {}

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        Backhand.OffhandAttack = inputStream.readBoolean();
        Backhand.EmptyOffhand = inputStream.readBoolean();
        Backhand.OffhandBreakBlocks = inputStream.readBoolean();
        Backhand.UseOffhandArrows = inputStream.readBoolean();
        Backhand.UseOffhandBow = inputStream.readBoolean();
        Backhand.OffhandTickHotswap = inputStream.readBoolean();
        Backhand.AlternateOffhandSlot = inputStream.readInt();
        Backhand.UseInventorySlot = inputStream.readBoolean();
        MysteriumPatchesFixesO.receivedConfigs = true;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        out.writeBoolean(Backhand.OffhandAttack);
        out.writeBoolean(Backhand.EmptyOffhand);
        out.writeBoolean(Backhand.OffhandBreakBlocks);
        out.writeBoolean(Backhand.UseOffhandArrows);
        out.writeBoolean(Backhand.UseOffhandBow);
        out.writeBoolean(Backhand.OffhandTickHotswap);
        out.writeInt(Backhand.AlternateOffhandSlot);
        out.writeBoolean(Backhand.UseInventorySlot);
    }
}
