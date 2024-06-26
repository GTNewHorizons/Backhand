package xonin.backhand.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import xonin.backhand.api.core.IBattlePlayer;
import xonin.backhand.utils.EnumAnimations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;

/**
 * User: nerd-boy
 * Date: 26/06/13
 * Time: 1:47 PM
 */
public final class OffhandAnimationPacket extends AbstractPacket {

    public static final String packetName = "MB2|Animation";
	private EnumAnimations animation;
	private String username;

    public OffhandAnimationPacket(EnumAnimations animation, EntityPlayer user) {
    	this.animation = animation;
    	this.username = user.getCommandSenderName();
    }

    public OffhandAnimationPacket() {
	}

	@Override
    public void process(ByteBuf in,EntityPlayer player) {
        try {
            animation = EnumAnimations.values()[in.readInt()];
            username = ByteBufUtils.readUTF8String(in);
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        if (username != null && animation != null) {
            EntityPlayer entity = player.worldObj.getPlayerEntityByName(username);
            if(entity!=null){
                if (entity.worldObj instanceof WorldServer) {
                    ((WorldServer) entity.worldObj).getEntityTracker().func_151247_a(entity, this.generatePacket());
                }
                animation.processAnimation((IBattlePlayer)entity);
            }
        }
    }

	@Override
	public String getChannel() {
		return packetName;
	}

	@Override
	public void write(ByteBuf out) {
		out.writeInt(animation.ordinal());
        ByteBufUtils.writeUTF8String(out, username);
	}
}
