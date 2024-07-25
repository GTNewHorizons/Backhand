package xonin.backhand.coremod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.tclproject.mysteriumlib.asm.common.CustomClassTransformer;
import net.tclproject.mysteriumlib.asm.common.CustomLoadingPlugin;
import net.tclproject.mysteriumlib.asm.common.FirstClassTransformer;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import xonin.backhand.coremod.transformers.EntityAIControlledByPlayerTransformer;
import xonin.backhand.coremod.transformers.EntityOtherPlayerMPTransformer;
import xonin.backhand.coremod.transformers.EntityPlayerTransformer;
import xonin.backhand.coremod.transformers.ItemInWorldTransformer;
import xonin.backhand.coremod.transformers.NetClientHandlerTransformer;
import xonin.backhand.coremod.transformers.PlayerControllerMPTransformer;
import xonin.backhand.coremod.transformers.TransformerBase;

public final class BackhandLoadingPlugin extends CustomLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    TransformerBase[] bt_transformers = { new EntityPlayerTransformer(), new PlayerControllerMPTransformer(),
        new ItemInWorldTransformer(), new EntityAIControlledByPlayerTransformer(), new EntityOtherPlayerMPTransformer(),
        new NetClientHandlerTransformer() };

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { FirstClassTransformer.class.getName() };
    }

    @Override
    public void registerFixes() {
        for (TransformerBase transformer : bt_transformers) {
            CustomClassTransformer.registerPostTransformer(transformer);
        }

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
        mixins.add("MixinItemStack");
        mixins.add("MixinInventoryPlayer");
        mixins.add("MixinNetHandlerPlayServer");
        if (FMLLaunchHandler.side()
            .isClient()) {
            mixins.add("MixinEntityOtherPlayerMP");
            mixins.add("MixinInventoryPlayerClient");
            mixins.add("MixinItemRenderer");
            mixins.add("MixinModelBiped");
            mixins.add("MixinNetHandlerPlayClient");
            mixins.add("MixinPlayerControllerMP");
            mixins.add("MixinItemBow");
        }
        return mixins;
    }
}
