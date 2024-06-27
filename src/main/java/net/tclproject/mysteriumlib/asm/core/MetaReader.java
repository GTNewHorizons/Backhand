package net.tclproject.mysteriumlib.asm.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import cpw.mods.fml.common.FMLLog;

/** Utilities for getting information out of classes and methods and performing basic tasks on them. */
public class MetaReader {

    /** The findLoadedClass method inside the ClassLoader class. */
    private static Method findLoadedClass;

    static {
        try {
            findLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            findLoadedClass.setAccessible(true);
        } catch (NoSuchMethodException e) {
            FMLLog.log(
                "Mysterium Patches",
                Level.ERROR,
                "Error occured when making findLoadedClass in ClassLoader usable.");
            FMLLog.log(
                "Mysterium Patches",
                Level.ERROR,
                "THIS IS MOST LIKELY HAPPENING BECAUSE OF MOD CONFLICTS. PLEASE CONTACT ME TO LET ME KNOW.");
            FMLLog.log("Mysterium Patches", Level.ERROR, e.getMessage());
        }
    }

    // TODO: test a static method
    /**
     * Gets a list of local variables (index: , type, name) in a method.
     * 
     * @param classBytes    the class that contains the method (bytes).
     * @param methodName    the name of the target method.
     * @param argumentTypes the types of the arguments in the method, represented by Type instances.
     * @return a list of local variables.
     */
    public List<String> getLocalVariables(byte[] classBytes, final String methodName, Type... argumentTypes) {
        final List<String> localVariables = new ArrayList<>();
        String methodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, argumentTypes); // We don't actually use the
                                                                                           // return type, hence the
                                                                                           // next variable
        final String methodDescriptorWithoutReturnType = methodDescriptor.substring(0, methodDescriptor.length() - 1);

        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM5) {

            @Override
            public MethodVisitor visitMethod(final int access, String name, String descriptor, String signature,
                String[] exceptions) {
                if (methodName.equals(name) && descriptor.startsWith(methodDescriptorWithoutReturnType)) {
                    return new MethodVisitor(Opcodes.ASM5) {

                        @Override
                        public void visitLocalVariable(String name, String descriptor, String signature, Label start,
                            Label end, int index) {
                            String typeName = Type.getType(descriptor)
                                .getClassName();
                            int fixedIndex = index + ((access & Opcodes.ACC_STATIC) == 0 ? 0 : 1); // If access is
                                                                                                   // static, we add 1,
                                                                                                   // if not, we add 0.
                            localVariables.add(fixedIndex + ": " + typeName + " " + name);
                        }
                    };
                }
                return null;
            }
        };

        acceptVisitor(classBytes, classVisitor);
        return localVariables;
    }

    /**
     * Gets a list of local variables (index: , type, name) in a method.
     * 
     * @param className     the class that contains the method (full class path without ".class").
     * @param methodName    the name of the target method.
     * @param argumentTypes the types of the arguments in the method, represented by Type instances.
     * @return a list of local variables.
     */
    public List<String> getLocalVariables(String className, final String methodName, Type... argTypes)
        throws IOException {
        return getLocalVariables(classToBytes(className), methodName, argTypes);
    }

    /**
     * Prints a list of local variables (index: , type, name) in a method.
     * 
     * @param classBytes    the class that contains the method (bytes).
     * @param methodName    the name of the target method.
     * @param argumentTypes the types of the arguments in the method, represented by Type instances.
     */
    public void printLocalVariables(byte[] classBytes, String methodName, Type... argumentTypes) {
        List<String> locals = getLocalVariables(classBytes, methodName, argumentTypes);
        for (String str : locals) {
            System.out.println(str);
        }
    }

    /**
     * Prints a list of local variables (index: , type, name) in a method.
     * 
     * @param className     the class that contains the method (full class path without ".class").
     * @param methodName    the name of the target method.
     * @param argumentTypes the types of the arguments in the method, represented by Type instances.
     */
    public void printLocalVariables(String className, String methodName, Type... argumentTypes) throws IOException {
        printLocalVariables(classToBytes(className), methodName, argumentTypes);
    }

    /**
     * Gets the passed in class in an InputStream.
     * 
     * @param name full class path without ".class".
     * @return the passed in class (InputStream)
     */
    public static InputStream classToStream(String name) {
        String classResourceName = '/' + name.replace('.', '/') + ".class";
        return MetaReader.class.getResourceAsStream(classResourceName);
    }

    /**
     * Gets the passed in class in bytes.
     * 
     * @param name full class path without ".class".
     * @return the passed in class (bytes)
     */
    public byte[] classToBytes(String name) throws IOException {
        String classLocationName = '/' + name.replace('.', '/') + ".class";
        return IOUtils.toByteArray(MetaReader.class.getResourceAsStream(classLocationName));
    }

    /**
     * Makes the given visitor visit the Java class.
     * 
     * @param classBytes class (bytes).
     * @param visitor    your class visitor.
     */
    public void acceptVisitor(byte[] classBytes, ClassVisitor visitor) {
        new ClassReader(classBytes).accept(visitor, 0);
    }

    /**
     * Makes the given visitor visit the Java class.
     * 
     * @param name    full class path without ".class".
     * @param visitor your class visitor.
     */
    public void acceptVisitor(String name, ClassVisitor visitor) throws IOException {
        acceptVisitor(classToBytes(name), visitor);
    }

    /**
     * Makes the given visitor visit the Java class.
     * 
     * @param classStream the class as an InputStream.
     * @param visitor     your class visitor.
     */
    public static void acceptVisitor(InputStream classStream, ClassVisitor visitor) {
        try {
            ClassReader reader = new ClassReader(classStream);
            reader.accept(visitor, 0);
            classStream.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Finds a method in a Java class. The method must be virtual.
     * 
     * @param owner      the class that has the method.
     * @param methodName the method name.
     * @param descriptor the method descriptor.
     * @return a MethodReference of the method.
     */
    public MethodReference findMethod(String owner, String methodName, String descriptor) {
        ArrayList<String> superClasses = getSuperClasses(owner);
        for (int i = superClasses.size() - 1; i > 0; i--) { // There is no use in checking the current class, for that
                                                            // we have getMethodReference.
            String className = superClasses.get(i);
            MethodReference methodReference = getMethodReference(className, methodName, descriptor);
            if (methodReference != null) {
                return methodReference;
            }
        }
        return null;
    }

    /**
     * Helper method to create a MethodReference out of a method, tries Reflection if ASM fails.
     * 
     * @param className  full class path without ".class".
     * @param methodName the name of the method.
     * @param descriptor the descriptor of the method.
     * @return a MethodReference.
     */
    public MethodReference getMethodReference(String className, String methodName, String descriptor) {
        try {
            return getMethodReferenceASM(className, methodName, descriptor);
        } catch (Exception e) {
            return getMethodReferenceReflect(className, methodName, descriptor);
        }
    }

    /**
     * Helper method to create a MethodReference out of a method, using ASM.
     * 
     * @param className  full class path without ".class".
     * @param methodName the name of the method.
     * @param descriptor the descriptor of the method.
     * @return a MethodReference.
     */
    public MethodReference getMethodReferenceASM(String className, String methodName, String descriptor)
        throws IOException {
        FindMethodClassVisitor cv = new FindMethodClassVisitor(methodName, descriptor);
        acceptVisitor(className, cv);
        if (cv.found) {
            return new MethodReference(className, cv.targetName, cv.targetDescriptor);
        }
        return null;
    }

    /**
     * Helper method to create a MethodReference out of a method, using Reflection.
     * 
     * @param className  full class path without ".class".
     * @param methodName the name of the method.
     * @param descriptor the descriptor of the method.
     * @return a MethodReference.
     */
    public MethodReference getMethodReferenceReflect(String className, String methodName, String descriptor) {
        Class loadedClass = getLoadedClass(className);
        if (loadedClass != null) {
            for (Method m : loadedClass.getDeclaredMethods()) {
                if (checkSameMethod(methodName, descriptor, m.getName(), Type.getMethodDescriptor(m))) {
                    return new MethodReference(className, m.getName(), Type.getMethodDescriptor(m));
                }
            }
        }
        return null;
    }

    /**
     * Checks if two methods are the same.
     * 
     * @param sourceName first method name
     * @param sourceDesc first method descriptor
     * @param targetName second method name
     * @param targetDesc second method descriptor
     * @return if the two names and descriptors are equal.
     */
    public boolean checkSameMethod(String sourceName, String sourceDesc, String targetName, String targetDesc) {
        return sourceName.equals(targetName) && sourceDesc.equals(targetDesc);
    }

    /**
     * Gets all the super classes of a class.
     *
     * @param name full class path without ".class".
     * @return superclasses in order of increasing specificity (starting with java/lang/Object
     *         and ending with the name passed in)
     */
    public ArrayList<String> getSuperClasses(String name) {
        ArrayList<String> superClasses = new ArrayList<>(1);
        superClasses.add(name);

        // Loop that iterates over all super classes, getting super class of super class of super class etc
        while ((name = getSuperClass(name)) != null) {
            superClasses.add(name);
        }

        Collections.reverse(superClasses);
        return superClasses;
    }

    /**
     * Gets the class from the name passed in.
     * 
     * @param name full class path without ".class".
     * @return the class.
     */
    public Class getLoadedClass(String name) {
        if (findLoadedClass != null) {
            try {
                // We get the class loader that loaded our class.
                ClassLoader classLoader = MetaReader.class.getClassLoader();
                // We invoke our class loader on the class name and it returns to us the loaded class.
                return (Class) findLoadedClass.invoke(classLoader, name.replace('/', '.'));
            } catch (Exception e) {
                FMLLog.log("Mysterium Patches", Level.ERROR, "Error occured when getting a class from a name.");
                FMLLog.log(
                    "Mysterium Patches",
                    Level.ERROR,
                    "THIS IS MOST LIKELY HAPPENING BECAUSE OF MOD CONFLICTS. PLEASE CONTACT ME TO LET ME KNOW.");
                FMLLog.log("Mysterium Patches", Level.ERROR, e.getMessage());
            }
        }
        return null;
    }

    /**
     * Gets the super class of the class passed in using all methods (if ASM doesn't work, tries reflection).
     * 
     * @param name full class path without ".class".
     * @return full class path of the superclass without ".class".
     */
    public String getSuperClass(String name) {
        try {
            return getSuperClassASM(name);
        } catch (Exception e) {
            return getSuperClassReflect(name);
        }
    }

    /**
     * Gets the super class of the class passed in using ASM using a custom class visitor.
     * 
     * @param name full class path without ".class".
     * @return full class path of the superclass without ".class".
     */
    public String getSuperClassASM(String name) throws IOException {
        CheckSuperClassVisitor cv = new CheckSuperClassVisitor();
        acceptVisitor(name, cv);
        return cv.superClassName;
    }

    /**
     * Gets the super class of the class passed in using reflection.
     * 
     * @param name full class path without ".class".
     * @return full class path of the superclass without ".class".
     */
    public String getSuperClassReflect(String name) {
        Class loadedClass = getLoadedClass(name);
        if (loadedClass != null) {
            if (loadedClass.getSuperclass() == null) return null;
            return loadedClass.getSuperclass()
                .getName()
                .replace('.', '/');
        }
        return "java/lang/Object";
    }

    /** Custom class visitor that stores the superclass name. */
    protected class CheckSuperClassVisitor extends ClassVisitor {

        /** Name of superclass */
        String superClassName;

        public CheckSuperClassVisitor() {
            // ASM version is 5
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces) {
            this.superClassName = superName;
        }
    }

    /** Custom class visitor that finds a method in a class. */
    protected class FindMethodClassVisitor extends ClassVisitor {

        public String targetName;
        public String targetDescriptor;
        public boolean found;

        public FindMethodClassVisitor(String name, String desctiptor) {
            super(Opcodes.ASM5);
            this.targetName = name;
            this.targetDescriptor = desctiptor;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desctiptor, String signature,
            String[] exceptions) {

            // Check ofr if access is public (01(public),10(private),11(protected) & 10 will only be 00 if it's 01) and
            // it's the same method you're looking for.
            if ((access & Opcodes.ACC_PRIVATE) == 0
                && checkSameMethod(name, desctiptor, targetName, targetDescriptor)) {
                found = true;
                targetName = name;
                targetDescriptor = desctiptor;
            }
            return null;
        }
    }

    /** Helper class that stores a method reference. */
    public static class MethodReference {

        /** Class that contains the said method. */
        public final String owner;

        /** The method name. */
        public final String name;

        /** The method descriptor. */
        public final String descriptor;

        public MethodReference(String owner, String name, String descriptor) {
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
        }

        /** Gets the return type of the method. */
        public Type getReturnType() {
            return Type.getMethodType(descriptor);
        }

        @Override
        public String toString() {
            return "MethodReference{" + "owner='"
                + owner
                + '\''
                + ", name='"
                + name
                + '\''
                + ", desc='"
                + descriptor
                + '\''
                + '}';
        }
    }
}
