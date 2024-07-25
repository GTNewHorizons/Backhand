package xonin.backhand.coremod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.tclproject.mysteriumlib.asm.common.CustomLoadingPlugin;
import net.tclproject.mysteriumlib.asm.common.FirstClassTransformer;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public final class BackhandLoadingPlugin extends CustomLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { FirstClassTransformer.class.getName() };
    }

    @Override
    public void registerFixes() {
        registerClassWithFixes("net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO");
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
        mixins.add("MixinInventoryPlayer");
        mixins.add("MixinNetHandlerPlayServer");
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
