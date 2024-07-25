package xonin.backhand.coremod;

import net.tclproject.mysteriumlib.asm.common.CustomClassTransformer;
import net.tclproject.mysteriumlib.asm.common.CustomLoadingPlugin;
import net.tclproject.mysteriumlib.asm.common.FirstClassTransformer;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import xonin.backhand.coremod.transformers.EntityAIControlledByPlayerTransformer;
import xonin.backhand.coremod.transformers.EntityOtherPlayerMPTransformer;
import xonin.backhand.coremod.transformers.EntityPlayerTransformer;
import xonin.backhand.coremod.transformers.ItemInWorldTransformer;
import xonin.backhand.coremod.transformers.ModelBipedTransformer;
import xonin.backhand.coremod.transformers.NetClientHandlerTransformer;
import xonin.backhand.coremod.transformers.PlayerControllerMPTransformer;
import xonin.backhand.coremod.transformers.TransformerBase;

public final class BackhandLoadingPlugin extends CustomLoadingPlugin implements IFMLLoadingPlugin {

    TransformerBase[] bt_transformers = { new EntityPlayerTransformer(), new PlayerControllerMPTransformer(),
        new ItemInWorldTransformer(), new EntityAIControlledByPlayerTransformer(), new EntityOtherPlayerMPTransformer(),
        new ModelBipedTransformer(), new NetClientHandlerTransformer() };

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
}
