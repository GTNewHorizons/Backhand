package net.tclproject.mysteriumlib.asm.common;

import java.util.HashMap;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;
import net.tclproject.mysteriumlib.asm.core.ASMFix;
import net.tclproject.mysteriumlib.asm.core.FixInserterClassVisitor;
import net.tclproject.mysteriumlib.asm.core.TargetClassTransformer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/**
 * This transformer transforms any classes that load before minecraft's classes do.
 * When minecraft classes start loading (actually, a bit earlier - in Loader.injectData)
 * all fixes from this class are transferred to CustomClassTransformer. This is needed
 * in order for the fixes in MysteriumASM Lib to be applied after forge deobfuscation.
 */
public class FirstClassTransformer extends TargetClassTransformer implements IClassTransformer {

    /** To check if some mod is using the library before it is loaded. */
    public static FirstClassTransformer instance = new FirstClassTransformer();
    /** If the fixes from here have been transferred into CustomClassTransformer. */
    boolean registeredBuiltinFixes;

    public FirstClassTransformer() {
        this.metaReader = CustomLoadingPlugin.getMetaReader();

        if (instance != null) {
            // If the lib has been loaded, clear all fixes that for some reason have been put in and transfer them into
            // this instance.
            this.fixesMap.putAll(FirstClassTransformer.instance.getFixesMap());
            FirstClassTransformer.instance.getFixesMap()
                .clear();
        } else {
            registerClassWithFixes(BuiltinFixes.class.getName()); // Register the built-in fixes
        }
        instance = this; // Change the global instance to this one.
    }

    /**
     * Forge passes in all the classes here to be transformed.
     * We are passing them to the actual transformer with the right arguments.
     */
    @Override
    public byte[] transform(String name, String deobfName, byte[] bytes) {
        return transform(deobfName, bytes);
    }

    /**
     * Creates a custom Class Visitor to return custom method visitors to insert fixes.
     * Has custom logic to see if the method is the target method, accounting for an obfuscated descriptor.
     */
    @Override
    public FixInserterClassVisitor createInserterClassVisitor(ClassWriter classWriter, List<ASMFix> fixes) {
        return new FixInserterClassVisitor(this, classWriter, fixes) {

            @Override
            protected boolean isTheTarget(ASMFix fix, String name, String descriptor) {
                return super.isTheTarget(fix, name, obfuscateDescriptor(descriptor));
            }
        };
    }

    /** Getter for fixesMap. */
    public HashMap<String, List<ASMFix>> getFixesMap() {
        return fixesMap;
    }

    /**
     * @param descriptor a deobfuscater descriptor.
     * @return an obfuscated equivalent of the descriptor.
     */
    static String obfuscateDescriptor(String descriptor) {
        if (!CustomLoadingPlugin.isObfuscated()) return descriptor;

        Type methodType = Type.getMethodType(descriptor);
        Type mappedReturnType = map(methodType.getReturnType());
        Type[] argTypes = methodType.getArgumentTypes();
        Type[] mappedArgTypes = new Type[argTypes.length];
        for (int i = 0; i < mappedArgTypes.length; i++) {
            mappedArgTypes[i] = map(argTypes[i]);
        }
        return Type.getMethodDescriptor(mappedReturnType, mappedArgTypes);
    }

    /**
     * Returns an obfuscated equivalent of a type.
     *
     * @param type A type.
     * @return an obfuscated equivalent of the type.
     */
    static Type map(Type type) {
        // void or primitive
        if (!CustomLoadingPlugin.isObfuscated() || (type.getSort() < 9)) return type;

        // array
        if (type.getSort() == 9) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < type.getDimensions(); i++) {
                sb.append("[");
            }
            boolean isPrimitiveArray = type.getSort() < 9;
            if (!isPrimitiveArray) sb.append("L");
            sb.append(map(type.getElementType()).getInternalName());
            if (!isPrimitiveArray) sb.append(";");
            return Type.getType(sb.toString());
        } else if (type.getSort() == 10) {
            String unmappedName = FMLDeobfuscatingRemapper.INSTANCE.map(type.getInternalName());
            return Type.getType("L" + unmappedName + ";");
        } else {
            throw new IllegalArgumentException("Can not map method type!");
        }
    }
}
