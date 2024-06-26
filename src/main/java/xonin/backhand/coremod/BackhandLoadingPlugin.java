package xonin.backhand.coremod;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import xonin.backhand.api.core.BackhandTranslator;

import java.io.File;
import java.util.Map;

@TransformerExclusions({"xonin.backhand.coremod"})
@Name("Mine and Blade: Battlegear2")
@SortingIndex(1500)
public final class BackhandLoadingPlugin implements IFMLLoadingPlugin {

    public static final String EntityPlayerTransformer = "xonin.backhand.coremod.transformers.EntityPlayerTransformer";
    public static final String NetClientHandlerTransformer = "xonin.backhand.coremod.transformers.NetClientHandlerTransformer";
    public static final String PlayerControllerMPTransformer = "xonin.backhand.coremod.transformers.PlayerControllerMPTransformer";
    public static final String ItemInWorldTransformer = "xonin.backhand.coremod.transformers.ItemInWorldTransformer";
    public static final String EntityAIControlledTransformer = "xonin.backhand.coremod.transformers.EntityAIControlledByPlayerTransformer";
    public static final String EntityOtherPlayerMPTransformer = "xonin.backhand.coremod.transformers.EntityOtherPlayerMPTransformer";
    public static final String AccessTransformer = "xonin.backhand.coremod.transformers.BattlegearAccessTransformer";
    public static File debugOutputLocation;

    public static final String[] transformers = 
    		new String[]{
		        EntityPlayerTransformer,
		        NetClientHandlerTransformer,
		        PlayerControllerMPTransformer,
		        ItemInWorldTransformer,
		        EntityAIControlledTransformer,
		        EntityOtherPlayerMPTransformer
   			};

    @Override
    public String[] getASMTransformerClass() {
        return transformers;
    }

    @Override
    public String getAccessTransformerClass() { return AccessTransformer; }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    	debugOutputLocation = new File(data.get("mcLocation").toString(), "bg edited classes");
        BackhandTranslator.obfuscatedEnv = Boolean.class.cast(data.get("runtimeDeobfuscationEnabled"));
    }

}
