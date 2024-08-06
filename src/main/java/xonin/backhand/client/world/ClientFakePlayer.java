package xonin.backhand.client.world;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.mojang.authlib.GameProfile;

public class ClientFakePlayer extends EntityPlayer {

    public static final ClientFakePlayer INSTANCE = new ClientFakePlayer();

    public ClientFakePlayer() {
        super(DummyWorld.INSTANCE, new GameProfile(UUID.randomUUID(), "[BackhandClient]"));
    }

    @Override
    public void addChatMessage(IChatComponent message) {}

    @Override
    public boolean canCommandSenderUseCommand(int i, String s) {
        return false;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
        return new ChunkCoordinates(0, 0, 0);
    }

    @Override
    public void addChatComponentMessage(IChatComponent message) {}

    @Override
    public void addStat(StatBase par1StatBase, int par2) {}

    @Override
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {}

    @Override
    public boolean isEntityInvulnerable() {
        return true;
    }

    @Override
    public boolean canAttackPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void onDeath(DamageSource source) {}

    @Override
    public void onUpdate() {}

    @Override
    public void travelToDimension(int dim) {}

    public boolean simulateBlockInteraction(ItemStack offhand, int x, int y, int z) {
        Minecraft mc = Minecraft.getMinecraft();
        MovingObjectPosition mop = mc.objectMouseOver;
        ItemStack stack = ItemStack.copyItemStack(offhand);

        if (mop == null) return false;

        float subX = (float) mop.hitVec.xCoord - x;
        float subY = (float) mop.hitVec.yCoord - y;
        float subZ = (float) mop.hitVec.zCoord - z;

        copyPlayerPosition(mc.thePlayer);
        Block block = DummyWorld.INSTANCE.copyAndSetBlock(mc.theWorld, x, y, z, mop);

        if (block == null || block == Blocks.air) return false;

        prepareForInteraction(mc.thePlayer, stack);

        try {
            return block.onBlockActivated(DummyWorld.INSTANCE, x, y, z, this, mop.sideHit, subX, subY, subZ);
        } catch (Exception e) {
            // Something went wrong, block the offhand interaction
            return true;
        }
    }

    private void prepareForInteraction(EntityPlayer player, ItemStack stack) {
        copyPlayerPosition(player);
        inventory.copyInventory(player.inventory);
        setCurrentItemOrArmor(0, stack);
        experienceLevel = player.experienceLevel;
        experienceTotal = player.experienceTotal;
        experience = player.experience;
        capabilities = player.capabilities;
        isAirBorne = player.isAirBorne;
        inWater = player.isInWater();
        fallDistance = player.fallDistance;
        this.setSprinting(player.isSprinting());
    }

    public void copyPlayerPosition(EntityPlayer player) {
        this.setPositionAndRotation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        this.setSneaking(player.isSneaking());
    }
}
