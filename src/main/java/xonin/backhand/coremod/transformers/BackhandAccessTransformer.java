package xonin.backhand.coremod.transformers;

import java.io.IOException;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class BackhandAccessTransformer extends AccessTransformer {
	   public BackhandAccessTransformer() throws IOException {
	      super("theoffhandmod_at.cfg");
	   }
}