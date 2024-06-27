package net.tclproject.mysteriumlib.asm.common;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import net.tclproject.mysteriumlib.asm.core.ASMFix;
import net.tclproject.mysteriumlib.asm.core.MetaReader;
import net.tclproject.mysteriumlib.asm.core.TargetClassTransformer;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer;
import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import mods.battlegear2.api.core.BattlegearTranslator;
import mods.battlegear2.coremod.BattlegearLoadingPlugin;

/**
 * Custom IFMLLoadingPlugin implementation.
 * 
 * @see IFMLLoadingPlugin
 */
@IFMLLoadingPlugin.TransformerExclusions({ "net.tclproject", "mods.battlegear2.coremod" })
public class CustomLoadingPlugin implements IFMLLoadingPlugin {

    /** A DeobfuscationTransformer instance for use inside this class. */
    private static DeobfuscationTransformer deobfuscationTransformer;
    /** If we have checked if we're running inside an obfuscated environment. */
    private static boolean checkedObfuscation;
    /** If we're running inside an obfuscated environment. */
    private static boolean obfuscated;
    /** A Metadata Reader instance for use inside this class. */
    private static MetaReader mcMetaReader;

    public static boolean foundThaumcraft = false;
    private static boolean foundOptiFine = false;
    private static boolean confirmedOptiFine = false;
    public static boolean foundDragonAPI = false;
    public static boolean isDevEnvironment = false;

    public static File debugOutputLocation;

    static {
        mcMetaReader = new MinecraftMetaReader();
    }

    /**
     * Returns the transformer that we are using at the current moment in time to modify classes.
     * See why we have to use two separate ones in the documentation for FirstClassTransformer.
     * 
     * @return FirstClassTransformer if our built-in fixes haven't been applied, otherwise - CustomClassTransformer.
     */
    public static TargetClassTransformer getTransformer() {
        return FirstClassTransformer.instance.registeredBuiltinFixes ? CustomClassTransformer.instance
            : FirstClassTransformer.instance;
    }

    public static boolean isOptiFinePresent() {
        if (!confirmedOptiFine && foundOptiFine) {
            // Check presence of OptiFine core classes
            try {
                Class.forName("optifine.OptiFineForgeTweaker");
            } catch (ClassNotFoundException exception1) {
                try {
                    Class.forName("optifine.OptiFineTweaker");
                } catch (ClassNotFoundException exception2) {
                    foundOptiFine = false;
                }
            }
            if (foundOptiFine) {
                Logger.getGlobal()
                    .info("Core: OptiFine presence has been confirmed.");
            } else {
                Logger.getGlobal()
                    .info("Core: OptiFine doesn't seem to be there actually.");
            }
            confirmedOptiFine = true;
        }
        return foundOptiFine;
    }

    /**
     * Registers a single manually made ASMFix.
     * It is not the most efficient way to make fixes, but if you want to go this way,
     * look at how the code already there builds an ASMFix out of a fix method or just
     * take a look at the documentation of the builder class within ASMFix.
     */
    public static void registerFix(ASMFix fix) {
        getTransformer().registerFix(fix);
    }

    /** Registers all fix methods within a class. */
    public static void registerClassWithFixes(String className) {
        getTransformer().registerClassWithFixes(className);
    }

    /** Getter for mcMetaReader. */
    public static MetaReader getMetaReader() {
        return mcMetaReader;
    }

    static DeobfuscationTransformer getDeobfuscationTransformer() {
        if (isObfuscated() && deobfuscationTransformer == null) {
            deobfuscationTransformer = new DeobfuscationTransformer();
        }
        return deobfuscationTransformer;
    }

    /**
     * If the obfuscation has not yet been checked, checks and returns it.
     * If it has, returns the value that the previous check returned.
     * 
     * @return If the mod is run in an obfuscated environment.
     */
    public static boolean isObfuscated() {
        if (!checkedObfuscation) {
            try {
                Field deobfuscatedField = CoreModManager.class.getDeclaredField("deobfuscatedEnvironment");
                deobfuscatedField.setAccessible(true);
                obfuscated = !deobfuscatedField.getBoolean(null);
            } catch (Exception e) {
                FMLLog.log("Mysterium Patches", Level.ERROR, "Error occured when checking obfuscation.");
                FMLLog.log(
                    "Mysterium Patches",
                    Level.ERROR,
                    "THIS IS MOST LIKELY HAPPENING BECAUSE OF MOD CONFLICTS. PLEASE CONTACT ME TO LET ME KNOW.");
                FMLLog.log("Mysterium Patches", Level.ERROR, e.getMessage());
            }
            checkedObfuscation = true;
        }
        return obfuscated;
    }

    // For further methods, forge has way better documentation than what I could ever write.

    // Only exists in 1.7.10. Comment out if not needed.
    @Override
    public String getAccessTransformerClass() {
        return "mods.battlegear2.coremod.transformers.BattlegearAccessTransformer";
    }

    // This only exists in 1.6.x. Uncomment if needed.
    // public String[] getLibraryRequestClass() {
    // return null;
    // }

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
    public void injectData(Map<String, Object> data) {
        debugOutputLocation = new File(
            data.get("mcLocation")
                .toString(),
            "bg edited classes");
        BattlegearLoadingPlugin.debugOutputLocation = new File(
            data.get("mcLocation")
                .toString(),
            "bg edited classes");
        BattlegearTranslator.obfuscatedEnv = Boolean.class.cast(data.get("runtimeDeobfuscationEnabled"));
        if (((ArrayList) data.get("coremodList")).contains("DragonAPIASMHandler")) {
            Logger.getGlobal()
                .info("Core: Located DragonAPI in list of coremods");
            foundDragonAPI = true;
        }

        // This is very crude check for mods presence using filename.
        // Some mods may refer to others in their name, so we'll to confirm those assumption with class presence check.
        File loc = (File) data.get("mcLocation");

        Logger.getGlobal()
            .info("MC located at: " + loc.getAbsolutePath());
        isDevEnvironment = !(Boolean) data.get("runtimeDeobfuscationEnabled");

        File mcFolder = new File(loc.getAbsolutePath() + File.separatorChar + "mods");
        File mcVersionFolder = new File(mcFolder.getAbsolutePath() + File.separatorChar + "1.7.10");
        ArrayList<File> subfiles = new ArrayList<>();
        if (mcFolder.listFiles() != null) {
            subfiles = new ArrayList<>(Arrays.asList(mcFolder.listFiles()));
            if (mcVersionFolder.listFiles() != null) {
                subfiles.addAll(Arrays.asList(mcVersionFolder.listFiles()));
            }
        }
        for (File file : subfiles) {
            String name = file.getName();
            if (name != null) {
                name = name.toLowerCase();
                if (name.endsWith(".jar") || name.endsWith(".zip")) {
                    if (name.contains("thaumcraft")) {
                        Logger.getGlobal()
                            .info("Core: Located Thaumcraft in " + file.getName());
                        foundThaumcraft = true;
                    } else if (name.contains("optifine")) {
                        Logger.getGlobal()
                            .info("Core: Located OptiFine in " + file.getName() + ". We'll to confirm that...");
                        foundOptiFine = true;
                    } else if (name.contains("dragonapi")) {
                        Logger.getGlobal()
                            .info("Core: Located DragonAPI in " + file.getName());
                        foundDragonAPI = true;
                    }
                }
            }
        }
        registerFixes();
    }

    public void registerFixes() {}

}
