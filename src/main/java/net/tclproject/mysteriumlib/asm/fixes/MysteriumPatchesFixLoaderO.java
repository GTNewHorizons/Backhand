package net.tclproject.mysteriumlib.asm.fixes;

import net.tclproject.mysteriumlib.asm.common.CustomClassTransformer;
import net.tclproject.mysteriumlib.asm.common.CustomLoadingPlugin;
import net.tclproject.mysteriumlib.asm.common.FirstClassTransformer;
import xonin.backhand.coremod.transformers.*;

public class MysteriumPatchesFixLoaderO extends CustomLoadingPlugin {

	TransformerBase[] bt_transformers = {
		new EntityPlayerTransformer(),
		new PlayerControllerMPTransformer(),
		new ItemInWorldTransformer(),
		new EntityAIControlledByPlayerTransformer(),
		new EntityOtherPlayerMPTransformer()
    };
	
    // Turns on MysteriumASM Lib. You can do this in only one of your Fix Loaders.
    @Override
    public String[] getASMTransformerClass() 
    {
        return new String[]{
			FirstClassTransformer.class.getName()
		};
    }

	@Override
	public void registerFixes()
	{
		for (TransformerBase transformer : bt_transformers) {
			CustomClassTransformer.registerPostTransformer(transformer);
		}

		registerClassWithFixes("net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO");
	}
}
