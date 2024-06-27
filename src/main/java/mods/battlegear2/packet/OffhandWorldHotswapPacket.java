package mods.battlegear2.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO;

import io.netty.buffer.ByteBuf;

public class OffhandWorldHotswapPacket extends AbstractMBPacket {

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
        MysteriumPatchesFixesO.ignoreSetSlot = inputStream.readBoolean();
    }
}
