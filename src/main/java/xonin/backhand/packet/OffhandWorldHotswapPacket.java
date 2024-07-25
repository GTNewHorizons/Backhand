package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;

import io.netty.buffer.ByteBuf;
import xonin.backhand.client.utils.BackhandClientUtils;

public class OffhandWorldHotswapPacket extends AbstractPacket {

    public static final String packetName = "MB2|WorldHotswap";

    boolean ignoreSwitching;

    public OffhandWorldHotswapPacket() {}

    public OffhandWorldHotswapPacket(boolean bool) {
        this.ignoreSwitching = bool;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        out.writeBoolean(this.ignoreSwitching);
    }

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        BackhandClientUtils.ignoreSetSlot = inputStream.readBoolean();
    }
}
