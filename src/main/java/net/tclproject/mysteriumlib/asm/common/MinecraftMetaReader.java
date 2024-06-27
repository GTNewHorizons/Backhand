package net.tclproject.mysteriumlib.asm.common;

import java.io.IOException;
import java.lang.reflect.Method;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.tclproject.mysteriumlib.asm.core.MetaReader;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/** An add-on to MetaReader for getting information related to minecraft obfuscation and deobfuscation. */
public class MinecraftMetaReader extends MetaReader {

    /** The runTransformers method inside the LaunchClassLoader class. */
    private static Method runTransformers;

    static {
        try {
            runTransformers = LaunchClassLoader.class.getDeclaredMethod(
                "runTransformers", // get the runTransformers method frm LaunchClassLoader
                String.class,
                String.class,
                byte[].class); // arguments of runTransformers
            runTransformers.setAccessible(true); // make it ignore access checks
        } catch (Exception e) {
            FMLLog.log(
                "Mysterium Patches",
                Level.ERROR,
                "Error occured when making runTransformers in LaunchClassLoader usable.");
            FMLLog.log(
                "Mysterium Patches",
                Level.ERROR,
                "THIS IS MOST LIKELY HAPPENING BECAUSE OF MOD CONFLICTS. PLEASE CONTACT ME TO LET ME KNOW.");
            FMLLog.log("Mysterium Patches", Level.ERROR, e.getMessage());
        }
    }

    /**
     * Returns the deobfuscated class (bytes) from a deobfuscated name in an obfuscated environment.
     * 
     * @param name The unobfuscated class name.
     * @return The unobfuscated class (bytes).
     */
    @Override
    public byte[] classToBytes(String name) throws IOException {
        byte[] bytes = super.classToBytes(getRelevantName(name.replace('.', '/')));
        return deobfuscateClass(name, bytes);
    }

    /**
     * Checks if an srg-named method is the equivalent of an mcp-named method.
     * <p/>
     * NOTE: It does not check the mcp vs srg descriptors, it only checks if they are the same which in most cases will
     * not be the case if it's srg vs mcp.
     * 
     * @param obfuscatedName   the obfuscated (srg) name.
     * @param sourceDescriptor the descriptor of the first method.
     * @param mcpName          the deobfuscated (mcp) name.
     * @param targetDescriptor the descriptor of the second method.
     * @return If they are the same method.
     */
    @Override
    public boolean checkSameMethod(String obfuscatedName, String sourceDescriptor, String mcpName,
        String targetDescriptor) {
        return checkSameMethod(obfuscatedName, mcpName) && sourceDescriptor.equals(targetDescriptor);
    }

    // TODO: make a method for the automatic generation of obfuscated/non-obfuscated descriptors

    /**
     * Forge and others can create methods at runtime.
     * This method gets a method reference, even if the target method is runtime-generated
     * by other mods or forge.
     * 
     * @param ownerClass the class inside which the target method is located.
     * @param methodName the name of the target method.
     * @param descriptor the descriptor of the target method.
     * @return A MethodReference for the method or null if the method is not found.
     */
    @Override
    public MethodReference getMethodReferenceASM(String ownerClass, String methodName, String descriptor)
        throws IOException {
        FindMethodClassVisitor classVisitor = new FindMethodClassVisitor(methodName, descriptor);
        byte[] bytes = getTransformedBytes(ownerClass);
        acceptVisitor(bytes, classVisitor);
        return classVisitor.found
            ? new MethodReference(ownerClass, classVisitor.targetName, classVisitor.targetDescriptor)
            : null;
    }

    /**
     * Returns a deobfuscated version of a class.
     * 
     * @param className the name of the class.
     * @param bytes     the class (bytes).
     * @return the deobfuscated class (bytes).
     */
    public static byte[] deobfuscateClass(String className, byte[] bytes) {
        if (CustomLoadingPlugin.getDeobfuscationTransformer() != null) {
            bytes = CustomLoadingPlugin.getDeobfuscationTransformer()
                .transform(className, className, bytes);
        }
        return bytes;
    }

    /**
     * Gets the modified class from a deobfuscated class name.
     * The modified class is the class with all the fixes, mixins, asm etc.
     * applied to it from everywhere.
     * 
     * @param name The deobfuscated class name.
     * @return bytes the class (bytes) with all the changes applied to it.
     */
    public static byte[] getTransformedBytes(String name) throws IOException {
        String className = getRelevantName(name);
        byte[] bytes = Launch.classLoader.getClassBytes(className);
        if (bytes == null) {
            throw new RuntimeException("The byte representation of " + className + " cannot be found.");
        }
        try {
            bytes = (byte[]) runTransformers.invoke(Launch.classLoader, className, name, bytes);
        } catch (Exception e) {
            FMLLog.log(
                "Mysterium Patches",
                Level.ERROR,
                "Error occured when making runTransformers in LaunchClassLoader usable.");
            FMLLog.log(
                "Mysterium Patches",
                Level.ERROR,
                "THIS IS MOST LIKELY HAPPENING BECAUSE OF MOD CONFLICTS. PLEASE CONTACT ME TO LET ME KNOW.");
            FMLLog.log("Mysterium Patches", Level.ERROR, e.getMessage());
        }
        return bytes;
    }

    /**
     * Returns an obfuscated name from a deobfuscated one if run in an obfuscated environment.
     * If run in a deobfuscated environment, the name stays the same.
     * 
     * @param deobfName the deobfuscated name.
     * @return the name that is usable in the environment this method is called.
     */
    public static String getRelevantName(String deobfName) {
        if (CustomLoadingPlugin.isObfuscated()) {
            return FMLDeobfuscatingRemapper.INSTANCE.unmap(deobfName);
        }
        return deobfName;
    }

    /**
     * Checks if two method names are the same, accounting for if the first name is obfuscated.
     * 
     * @param srgName the name of the method that might be obfuscated.
     * @param mcpName a deobfuscated method name.
     * @return if the two names are the same.
     */
    public static boolean checkSameMethod(String srgName, String mcpName) {
        if (CustomLoadingPlugin.isObfuscated() && CustomClassTransformer.instance != null) {
            int methodId = CustomClassTransformer.getMethodIndex(srgName);
            String remappedName = CustomClassTransformer.instance.getMethodNames()
                .get(methodId);
            if (remappedName != null && remappedName.equals(mcpName)) {
                return true;
            }
        }
        return srgName.equals(mcpName);
    }
}
