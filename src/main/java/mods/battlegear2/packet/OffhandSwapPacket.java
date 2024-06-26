package mods.battlegear2.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import xonin.backhand.api.core.BattlegearUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import xonin.backhand.Backhand;

public class OffhandSwapPacket extends AbstractMBPacket {
    public static final String packetName = "MB2|Swap";

    private String user;
    EntityPlayer player;

    public OffhandSwapPacket(EntityPlayer player) {
        this.player = player;
        this.user = player.getCommandSenderName();
    }

    public OffhandSwapPacket() {}

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, player.getCommandSenderName());
    }

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        this.user = ByteBufUtils.readUTF8String(inputStream);
        this.player = player.worldObj.getPlayerEntityByName(user);
        if (this.player != null) {
            ItemStack offhandItem = BattlegearUtils.getOffhandItem(this.player);
            if (Backhand.isOffhandBlacklisted(player.getCurrentEquippedItem()) || Backhand.isOffhandBlacklisted(offhandItem))
                return;

            BattlegearUtils.setPlayerOffhandItem(this.player,this.player.getCurrentEquippedItem());
            BattlegearUtils.setPlayerCurrentItem(this.player,offhandItem);
            Backhand.packetHandler.sendPacketToPlayer(new OffhandSwapClientPacket(this.player).generatePacket(), (EntityPlayerMP) player);
        }
    }
}
