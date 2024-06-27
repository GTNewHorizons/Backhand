package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import xonin.backhand.api.core.BackhandUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

/**
 * User: nerd-boy
 * Date: 26/06/13
 * Time: 1:40 PM
 */
public final class OffhandSyncItemPacket extends AbstractPacket {

    public static final String packetName = "MB2|SyncItem";
    private String user;
    private InventoryPlayer inventory;
    private EntityPlayer player;

    public OffhandSyncItemPacket(EntityPlayer player){
        this(player.getCommandSenderName(), player.inventory, player);
    }

    public OffhandSyncItemPacket(String user, InventoryPlayer inventory, EntityPlayer player) {
        this.user = user;
        this.inventory = inventory;
        this.player = player;
    }

    public OffhandSyncItemPacket() {
	}

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        this.user = ByteBufUtils.readUTF8String(inputStream);
        this.player = player.worldObj.getPlayerEntityByName(user);
        if (this.player != null) {
            ItemStack offhandItem = ByteBufUtils.readItemStack(inputStream);
            BackhandUtils.setPlayerOffhandItem(this.player, offhandItem);
            if(!player.worldObj.isRemote){//Using data sent only by client
                try {
                    ItemStack itemInUse = ByteBufUtils.readItemStack(inputStream);
                    int itemUseCount = inputStream.readInt();
                    this.player.setItemInUse(itemInUse, itemUseCount);
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, user);
        ByteBufUtils.writeItemStack(out, BackhandUtils.getOffhandItem(player));
        if(player.worldObj.isRemote){//client-side only thing
            ByteBufUtils.writeItemStack(out, player.getItemInUse());
            out.writeInt(player.getItemInUseCount());
        }
    }
}
