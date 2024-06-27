package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO;

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
        MysteriumPatchesFixesO.ignoreSetSlot = inputStream.readBoolean();
    }
}
