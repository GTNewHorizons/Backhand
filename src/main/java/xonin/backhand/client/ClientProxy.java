package xonin.backhand.client;

import net.minecraft.init.Items;
import net.minecraftforge.client.MinecraftForgeClient;

import xonin.backhand.CommonProxy;
import xonin.backhand.client.item.MapRenderer;

public class ClientProxy extends CommonProxy {

    @Override
    public void load() {
        super.load();
        MinecraftForgeClient.registerItemRenderer(Items.filled_map, new MapRenderer());
    }
}
