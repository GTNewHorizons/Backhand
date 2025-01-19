package xonin.backhand.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import tconstruct.library.tools.HarvestTool;
import xonin.backhand.CommonProxy;
import xonin.backhand.utils.Mods;

public class ClientProxy extends CommonProxy {

    public static final KeyBinding swapOffhand = new KeyBinding(
        "Swap Offhand",
        Keyboard.KEY_F,
        "key.categories.gameplay");

    public static final List<Class<?>> offhandPriorityItems = new ArrayList<>();

    @Override
    public void load() {
        ClientRegistry.registerKeyBinding(swapOffhand);

        if (Mods.TINKERS_CONSTRUCT.isLoaded()) {
            offhandPriorityItems.add(HarvestTool.class);
        }
    }
}
