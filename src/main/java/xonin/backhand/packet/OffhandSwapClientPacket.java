
package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.InventoryPlayerBackhand;
import xonin.backhand.client.ClientTickHandler;

/**
 * User: nerd-boy
 * Date: 26/06/13
 * Time: 1:40 PM
 */
public final class OffhandSwapClientPacket extends AbstractPacket {

    public static final String packetName = "MB2|SwapClient";
    private String user;
    private EntityPlayer player;

    public OffhandSwapClientPacket() {}

    public OffhandSwapClientPacket(EntityPlayer player) {
        this.player = player;
        this.user = player.getCommandSenderName();
    }

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        this.user = ByteBufUtils.readUTF8String(inputStream);
        this.player = player.worldObj.getPlayerEntityByName(user);
        if (this.player != null) {
            int slot = inputStream.readInt();
            if (InventoryPlayerBackhand.isValidSwitch(slot)) this.player.inventory.currentItem = slot;
            BackhandUtils.swapOffhandItem(player);
        }
        ClientTickHandler.allowSwap = true;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, user);
        out.writeInt(player.inventory.currentItem);
    }
}
