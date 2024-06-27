package mods.battlegear2.coremod.transformers;

import java.io.IOException;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class BattlegearAccessTransformer extends AccessTransformer {

    public BattlegearAccessTransformer() throws IOException {
        super("theoffhandmod_at.cfg");
    }
}
