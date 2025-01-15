package xonin.backhand.utils;

import cpw.mods.fml.common.Loader;

public enum Mods {

    INV_TWEAKS("inventorytweaks"),;

    private final String modId;
    private Boolean loaded;

    Mods(String modId) {
        this.modId = modId;
    }

    public boolean isLoaded() {
        if (loaded == null) {
            loaded = Loader.isModLoaded(modId);
        }
        return loaded;
    }
}
