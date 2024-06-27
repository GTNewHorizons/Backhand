package mods.battlegear2.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.api.core.BattlegearUtils;

public class OffhandToServerPacket extends AbstractMBPacket {

    public static final String packetName = "MB2|OffhandToServer";

    private ItemStack offhandItem;
    private String user;
    EntityPlayer player;

    public OffhandToServerPacket(ItemStack offhandItem, EntityPlayer player) {
        this.offhandItem = offhandItem;
        this.player = player;
        this.user = player.getCommandSenderName();
    }

    public OffhandToServerPacket() {}

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, player.getCommandSenderName());
        ByteBufUtils.writeItemStack(out, offhandItem);
    }

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        this.user = ByteBufUtils.readUTF8String(inputStream);
        this.player = player.worldObj.getPlayerEntityByName(user);
        if (this.player != null) {
            ItemStack offhandItem = ByteBufUtils.readItemStack(inputStream);
            BattlegearUtils.setPlayerOffhandItem(this.player, offhandItem);
        }
    }
}
