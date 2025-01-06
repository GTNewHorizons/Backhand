package xonin.backhand.packet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import xonin.backhand.Backhand;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.utils.BackhandConfig;
import xonin.backhand.utils.EnumAnimations;

public class OffhandAttackPacket extends AbstractPacket {

    public static final String packetName = "MB2|Attack";
    private String user;
    private int targetId;

    public OffhandAttackPacket(EntityPlayer player, Entity target) {
        this.user = player.getCommandSenderName();
        this.targetId = target.getEntityId();
    }

    public OffhandAttackPacket() {}

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, user);
        out.writeInt(targetId);
    }

    @Override
    public void process(ByteBuf inputStream, EntityPlayer sender) {
        if (!BackhandConfig.OffhandAttack) {
            return;
        }

        this.user = ByteBufUtils.readUTF8String(inputStream);
        this.targetId = inputStream.readInt();

        EntityPlayer player = sender.worldObj.getPlayerEntityByName(user);
        Entity target = sender.worldObj.getEntityByID(this.targetId);
        if (player != null && target != null) {
            if (target instanceof EntityItem || target instanceof EntityXPOrb
                || target instanceof EntityArrow
                || target == player) {
                return;
            }
            ((IBackhandPlayer) player).attackTargetEntityWithCurrentOffItem(target);
            Backhand.packetHandler.sendPacketAround(
                player,
                120,
                new OffhandAnimationPacket(EnumAnimations.OffHandSwing, player).generatePacket());
        }
    }
}
