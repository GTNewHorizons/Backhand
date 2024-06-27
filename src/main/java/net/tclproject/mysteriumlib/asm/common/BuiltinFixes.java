package net.tclproject.mysteriumlib.asm.common;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.tclproject.mysteriumlib.asm.annotations.Fix;

import cpw.mods.fml.common.Loader;

/** Contails built-in fixes. */
public class BuiltinFixes {

    /**
     * Built-in fix to register CustomClassTransformer after forge's deobfuscation
     * has already happened. This makes it easier to work with minecraft code from it.
     * <p/>
     * In order to work with other/non-mc code, use FirstClassTransformer, which is how this fix is applied.
     * It transforms classes earlier on.
     */
    @Fix
    public static void injectData(Loader loader, Object... data) {
        ClassLoader classLoader = BuiltinFixes.class.getClassLoader();
        if (classLoader instanceof LaunchClassLoader) {
            ((LaunchClassLoader) classLoader).registerTransformer(CustomClassTransformer.class.getName());
        } else {
            System.out.println(
                "MysteriumASM Lib was not loaded by LaunchClassLoader. Fixes for minecraft code will not have any effect.");
        }
    }

}
