package xonin.backhand.coremod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public final class BackhandLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        return "mixins.backhand.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        mixins.add("MixinEntityPlayer");
        mixins.add("MixinEntityPlayerMP");
        mixins.add("MixinItemStack");
        mixins.add("MixinNetHandlerPlayServer");
        mixins.add("MixinEntityItem");
        if (FMLLaunchHandler.side()
            .isClient()) {
            mixins.add("MixinEntityOtherPlayerMP");
            mixins.add("MixinEntityPlayerClient");
            mixins.add("MixinItemRenderer");
            mixins.add("MixinModelBiped");
            mixins.add("MixinNetHandlerPlayClient");
            mixins.add("MixinPlayerControllerMP");
            mixins.add("MixinItemBow");
            mixins.add("MixinItemStackClient");
        }
        return mixins;
    }
}
