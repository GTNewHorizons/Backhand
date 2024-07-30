package xonin.backhand.client.world;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatBase;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
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
    public void onDeath(DamageSource source) {
        return;
    }

    @Override
    public void onUpdate() {
        return;
    }

    @Override
    public void travelToDimension(int dim) {
        return;
    }
}
