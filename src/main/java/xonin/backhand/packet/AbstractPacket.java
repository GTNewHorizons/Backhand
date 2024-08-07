package xonin.backhand.packet;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * User: nerd-boy
 * Date: 26/06/13
 * Time: 1:35 PM
 */
public abstract class AbstractPacket {

    public final FMLProxyPacket generatePacket() {
        ByteBuf buf = Unpooled.buffer();
        write(buf);
        return new FMLProxyPacket(buf, getChannel());
    }

    public abstract String getChannel();

    public abstract void write(ByteBuf out);

    public abstract void process(ByteBuf in, EntityPlayer player);
}
