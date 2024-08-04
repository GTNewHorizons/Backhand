package xonin.backhand.client.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.client.world.ClientFakePlayer;
import xonin.backhand.client.world.DummyWorld;

public final class BackhandClientUtils {

    public static boolean disableMainhandAnimation = false;
    public static int countToCancel = 0;
    public static float firstPersonFrame;
    public static boolean offhandFPRender;
    public static boolean receivedConfigs = false;

    /**
     * Patch over EntityOtherPlayerMP#onUpdate() to update isItemInUse field
     *
     * @param player      the player whose #onUpdate method is triggered
     * @param isItemInUse the old value for isItemInUse field
     * @return the new value for isItemInUse field
     */
    public static boolean entityOtherPlayerIsItemInUseHook(EntityOtherPlayerMP player, boolean isItemInUse) {
        ItemStack itemStack = player.getCurrentEquippedItem();
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (BackhandUtils.usagePriorAttack(offhand)) itemStack = offhand;
        if (!isItemInUse && player.isEating() && itemStack != null) {
            player.setItemInUse(itemStack, itemStack.getMaxItemUseDuration());
            return true;
        } else if (isItemInUse && !player.isEating()) {
            player.clearItemInUse();
            return false;
        } else {
            return isItemInUse;
        }
    }

    public static boolean canBlockBeInteractedWith(ItemStack offhand, int x, int y, int z) {
        Minecraft mc = Minecraft.getMinecraft();
        MovingObjectPosition mop = mc.objectMouseOver;
        ItemStack stack = ItemStack.copyItemStack(offhand);

        if (mop == null) return false;

        float subX = (float) mop.hitVec.xCoord - x;
        float subY = (float) mop.hitVec.yCoord - y;
        float subZ = (float) mop.hitVec.zCoord - z;

        // Needs to be done before and after placing the block to ensure
        // that the block placement didn't change the item or player position
        ClientFakePlayer.INSTANCE.prepareForInteraction(mc.thePlayer, stack);
        Block block = DummyWorld.INSTANCE.copyAndSetBlock(mc.theWorld, x, y, z, mop);
        if (block == null || block == Blocks.air) return false;

        ClientFakePlayer.INSTANCE.prepareForInteraction(mc.thePlayer, stack);

        return block
            .onBlockActivated(DummyWorld.INSTANCE, x, y, z, ClientFakePlayer.INSTANCE, mop.sideHit, subX, subY, subZ);
    }
}
