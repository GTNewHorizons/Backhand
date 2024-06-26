package xonin.backhand;

import cpw.mods.fml.common.network.IGuiHandler;
import xonin.backhand.utils.EnumAnimations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CommonProxy implements IGuiHandler {
    public static ItemStack offhandItemUsed;

    public void load() {

    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        /*if(ID > EnumGuiType.values().length)
            return null;
        EnumGuiType gui = EnumGuiType.values()[ID];
        return getContainer(gui, player, x, y, z, npc);*/
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
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
