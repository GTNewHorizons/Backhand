package xonin.backhand.client.world;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class DummyWorld extends World {

    private static final WorldSettings DEFAULT_SETTINGS = new WorldSettings(
        1L,
        WorldSettings.GameType.SURVIVAL,
        true,
        false,
        WorldType.DEFAULT);

    public static final DummyWorld INSTANCE = new DummyWorld();
    private static final LongSet placedBlocks = new LongOpenHashSet();

    public DummyWorld() {
        super(new DummySaveHandler(), "DummyServer", DEFAULT_SETTINGS, new WorldProviderSurface(), new Profiler());
        // Guarantee the dimension ID was not reset by the provider
        this.provider.setDimension(Integer.MAX_VALUE);
        int providerDim = this.provider.dimensionId;
        this.provider.worldObj = this;
        this.provider.setDimension(providerDim);
        this.chunkProvider = this.createChunkProvider();
        this.calculateInitialSkylight();
        this.calculateInitialWeatherBody();
    }

    @Override
    public void updateEntities() {}

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

    @Override
    protected int func_152379_p() {
        return 0;
    }

    @Override
    public Entity getEntityByID(int p_73045_1_) {
        return null;
    }

    @Nonnull
    @Override
    protected IChunkProvider createChunkProvider() {
        return new DummyChunkProvider(this);
    }

    @Override
    protected boolean chunkExists(int x, int z) {
        return chunkProvider.chunkExists(x, z);
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block blockIn, int metadataIn, int flags) {
        if (super.setBlock(x, y, z, blockIn, metadataIn, flags)) {
            long key = CoordinatePacker.pack(x, y, z);
            if (blockIn != Blocks.air) {
                placedBlocks.add(key);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean updateLightByType(EnumSkyBlock p_147463_1_, int p_147463_2_, int p_147463_3_, int p_147463_4_) {
        return true;
    }

    @Nullable
    public Block copyAndSetBlock(World world, int x, int y, int z, MovingObjectPosition mop) {
        resetWorld();
        Block block = world.getBlock(x, y, z);

        if (block == null || block == Blocks.air) return null;

        int meta = block.getDamageValue(world, x, y, z);
        ItemStack stack = block.getPickBlock(mop, world, x, y, z, ClientFakePlayer.INSTANCE);
        boolean placed = false;

        if (stack != null) {
            for (int i = 0; i < 6; i++) {
                // Adjust when placing on a block "below"
                int aY = i == 1 ? y - 1 : y;
                if (stack.getItem()
                    .onItemUse(stack, ClientFakePlayer.INSTANCE, this, x, aY, z, i, x, aY, z)) {
                    placed = true;
                    break;
                }
            }
        }

        if (!placed) {
            setBlock(x, y, z, block, meta, 3);
        }

        return getBlock(x, y, z);
    }

    private void resetWorld() {
        if (placedBlocks.isEmpty()) return;
        for (long key : placedBlocks) {
            int x = CoordinatePacker.unpackX(key);
            int y = CoordinatePacker.unpackY(key);
            int z = CoordinatePacker.unpackZ(key);
            setBlockToAir(x, y, z);
        }
        placedBlocks.clear();
    }
}
