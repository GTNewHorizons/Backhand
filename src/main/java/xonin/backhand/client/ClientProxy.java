package xonin.backhand.client;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import xonin.backhand.CommonProxy;

public class ClientProxy extends CommonProxy {

    public static final KeyBinding swapOffhand = new KeyBinding(
        "Swap Offhand",
        Keyboard.KEY_F,
        "key.categories.gameplay");

    @Override
    public void load() {
        ClientRegistry.registerKeyBinding(swapOffhand);
    }
}
