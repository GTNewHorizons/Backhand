package xonin.backhand.client.world;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;

public class DummyWorld extends World {

    private static final WorldSettings DEFAULT_SETTINGS = new WorldSettings(
        1L,
        WorldSettings.GameType.SURVIVAL,
        true,
        false,
        WorldType.DEFAULT);

    public static final DummyWorld INSTANCE = new DummyWorld();

    public Block boundBlock;
    public Vec3 boundBlockPos;

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
    public boolean updateLightByType(EnumSkyBlock p_147463_1_, int p_147463_2_, int p_147463_3_, int p_147463_4_) {
        return true;
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        if (boundBlock == null) return super.getTileEntity(x, y, z);
        return super.getTileEntity((int) boundBlockPos.xCoord, (int) boundBlockPos.yCoord, (int) boundBlockPos.zCoord);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        if (boundBlock == null) return super.getBlock(x, y, z);
        return boundBlock;
    }

    @Nullable
    public Block copyAndSetBlock(World world, int x, int y, int z, MovingObjectPosition mop) {
        reset();
        setBlockToAir(x, y, z);
        Block block = world.getBlock(x, y, z);

        if (block == null || block == Blocks.air) return null;

        int meta = block.getDamageValue(world, x, y, z);
        ItemStack stack = block.getPickBlock(mop, world, x, y, z, ClientFakePlayer.INSTANCE);

        if (stack == null || !stack.getItem()
            .onItemUse(stack, ClientFakePlayer.INSTANCE, this, x, y, z, mop.sideHit, x, y, z)) {
            setBlock(x, y, z, block, meta, 3);
            setBoundBlock(block, x, y, z);
        }

        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null) {
            NBTTagCompound tag = new NBTTagCompound();
            tile.writeToNBT(tag);
            TileEntity dummyTile = getTileEntity(x, y, z);
            if (dummyTile == null) {
                dummyTile = TileEntity.createAndLoadEntity(tag);
                setTileEntity(x, y, z, dummyTile);
            }
            dummyTile.readFromNBT(tag);
        }

        return getBlock(x, y, z);
    }

    public void setBoundBlock(Block block, int x, int y, int z) {
        boundBlock = block;
        boundBlockPos = Vec3.createVectorHelper(x, y, z);
    }

    public void reset() {
        boundBlock = null;
        boundBlockPos = null;
    }

}
