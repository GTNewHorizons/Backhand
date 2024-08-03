package xonin.backhand;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.utils.EnumAnimations;

public class CommonProxy {

    public static ItemStack offhandItemUsed;

    public void load() {

    }

    public void onServerStopping(FMLServerStoppingEvent event) {
        for (EntityPlayer player : Backhand.getServer()
            .getConfigurationManager().playerEntityList) {
            if (BackhandUtils.getOffhandItem(player) != null) {
                BackhandUtils.resetAndDelayHotswap(player, 0);
            }
        }
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public void sendAnimationPacket(EnumAnimations animation, EntityPlayer entityPlayer) {}

    // Should not be called on the server anyway
    public boolean isRightClickHeld() {
        return false;
    }

    public int getRightClickCounter() {
        return 0;
    }

    public void setRightClickCounter(int i) {}

    public int getRightClickDelay() {
        return 0;
    }

    // Should not be called on the server anyway
    public boolean isLeftClickHeld() {
        return false;
    }

    // Should not be called on the server anyway
    public int getLeftClickCounter() {
        return 0;
    }

    // Should not be called on the server anyway
    public void setLeftClickCounter(int i) {}
}
