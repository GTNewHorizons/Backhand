package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import xonin.backhand.api.core.BackhandUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import xonin.backhand.Backhand;

public class OffhandSwapPacket extends AbstractPacket {
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
            ItemStack offhandItem = BackhandUtils.getOffhandItem(this.player);
            if (Backhand.isOffhandBlacklisted(player.getCurrentEquippedItem()) || Backhand.isOffhandBlacklisted(offhandItem))
                return;

            BackhandUtils.setPlayerOffhandItem(this.player,this.player.getCurrentEquippedItem());
            BackhandUtils.setPlayerCurrentItem(this.player,offhandItem);
            Backhand.packetHandler.sendPacketToPlayer(new OffhandSwapClientPacket(this.player).generatePacket(), (EntityPlayerMP) player);
        }
    }
}
