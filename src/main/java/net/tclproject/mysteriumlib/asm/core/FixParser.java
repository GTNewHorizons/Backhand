package net.tclproject.mysteriumlib.asm.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting;
import net.tclproject.mysteriumlib.asm.annotations.Fix;
import net.tclproject.mysteriumlib.asm.annotations.FixOrder;
import net.tclproject.mysteriumlib.asm.annotations.LocalVariable;
import net.tclproject.mysteriumlib.asm.annotations.ReturnedValue;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/** Class for parsing fix methods and creating fixes out of them. */
public class FixParser {

    /** The class transformer that has created this fix parser. */
    private TargetClassTransformer transformer;
    /** The class to be parsed for fix methods. */
    private String fixesClassName;
    /** The method to be parsed. */
    private String currentFixMethodName;
    /** The descriptor of the method to be parsed. */
    private String currentFixMethodDescriptor;
    /** If the method to be parsed is public and static. */
    private boolean currentMethodIsPublicAndStatic;

    /**
     * Key: A key inside the annotation, Value: annotation key's value
     */
    private HashMap<String, Object> annotationValues;

    /**
     * Key: the number of the argument, value - the number of the local variable for interception
     * or -1 for the interception of the parameter on the top of the stack.
     */
    private HashMap<Integer, Integer> argumentAnnotations = new HashMap<>();

    /** If an annotation visitor is currenty visiting a @Fix annotation. */
    private boolean inFixAnnotation;

    /** The descriptor of the Fix annotation class. */
    private static final String fixDescriptor = Type.getDescriptor(Fix.class);
    /** The descriptor of the LocalVariable annotation class. */
    private static final String localVariableDescriptor = Type.getDescriptor(LocalVariable.class);
    /** The descriptor of the ReturnedValue annotation class. */
    private static final String returnedValueDescriptor = Type.getDescriptor(ReturnedValue.class);

    public FixParser(TargetClassTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Parses a class for fix methods via a FixClassVisitor.
     * 
     * @param className The full name of the class.
     */
    protected void parseForFixes(String className) {
        transformer.logger.debug("Parsing class with fix methods " + className);
        try {
            transformer.metaReader.acceptVisitor(className, new FixClassVisitor());
        } catch (IOException e) {
            transformer.logger.severe("Can not parse class with fix methods " + className, e);
        }
    }

    /**
     * Parses a class for fix methods via a FixClassVisitor.
     * 
     * @param classBytes The class (bytes).
     */
    protected void parseForFixes(byte[] classBytes) {
        FixClassVisitor fixMethodSearchClassVisitor = new FixClassVisitor();
        try {
            transformer.metaReader.acceptVisitor(classBytes, fixMethodSearchClassVisitor);
            transformer.logger.debug("Parsing class with fix methods " + fixMethodSearchClassVisitor.fixesClassName);
        } catch (Exception e) {
            transformer.logger.severe(
                fixMethodSearchClassVisitor.fixesClassName != ""
                    ? ("Can not parse class with fix methods " + fixMethodSearchClassVisitor.fixesClassName)
                    : ("Can not create a class visitor to search a class for fix methods."),
                e);
        }
    }

    /**
     * Displays a warning about an invalid fix method, e.g. if it's not public and static.
     * 
     * @param message A message to add to the warning.
     */
    private void warnInvalidFix(String message) {
        transformer.logger.warning("Found invalid fix " + fixesClassName + "#" + currentFixMethodName);
        transformer.logger.warning(message);
    }

    /**
     * Creates a fix out of the values currently stored in annotationValues, currentFixMethodName/Descriptor and
     * argumentAnnotations, and adds it to the list to be applied
     */
    private void createAndRegisterFix(String clsName) {
        ASMFix.Builder builder = ASMFix.newBuilder();
        Type methodType = Type.getMethodType(currentFixMethodDescriptor);
        Type[] argumentTypes = methodType.getArgumentTypes(); // The types of all the arguments of the fix method

        if (!currentMethodIsPublicAndStatic) {
            warnInvalidFix("Fix method must be public and static.");
            return;
        }

        if (argumentTypes.length < 1) {
            warnInvalidFix(
                "Fix method has no arguments. First argument of a fix method must be a of the type of the target class.");
            return;
        }

        if (argumentTypes[0].getSort() != Type.OBJECT) {
            warnInvalidFix(
                "First argument of the fix method is not an object. First argument of a fix method must be of the type of the target class.");
            return;
        }

        builder.setTargetClass(argumentTypes[0].getClassName()); // Set the target class to apply the fix to

        if (annotationValues.containsKey("targetMethod")) {
            builder.setTargetMethod((String) annotationValues.get("targetMethod")); // Set the target method to apply
                                                                                    // the fix to
        } else {
            builder.setTargetMethod(currentFixMethodName); // Set the target method to apply the fix to, if none is
                                                           // specified, we take it that it's the fix method's name
        }

        builder.setFixesClass(clsName); // Set the class with fixes from where this fix originated from
        builder.setFixMethod(currentFixMethodName); // Set name of the fix method
        builder.addThisToFixMethodParameters(); // Adds the target class to the arguments of the fix method and passes
                                                // this into it. If the target method is static, the value passed will
                                                // be null.

        boolean insertOnExit = Boolean.TRUE.equals(annotationValues.get("insertOnExit")); // If we have to insert the
                                                                                          // fix on the exits from a
                                                                                          // method

        int currentParameterId = 1;
        for (int i = 1; i < argumentTypes.length; i++) { // loop to deal with ReturnedValue or LocalVariable annotations
                                                         // inside the fix method's arguments
            Type currentArgumentType = argumentTypes[i];
            if (argumentAnnotations.containsKey(i)) { // if the argument is a ReturnedValue or LocalVariable annotation
                                                      // (all of those are added to argumentAnnotations)
                int stackIndexToBePassed = argumentAnnotations.get(i);
                if (stackIndexToBePassed == -1) { // if the stack index to be passed is -1, it's the value at the top
                    builder.setTargetMethodReturnType(currentArgumentType); // The return type of the target method
                                                                            // obviously has to be the returnedValue's
                                                                            // type we want passed into the fix method
                    builder.addReturnedValueToFixMethodParameters();
                } else {
                    builder.addFixMethodParameter(currentArgumentType, stackIndexToBePassed);
                }
            } else {
                builder.addTargetMethodParameters(currentArgumentType);
                builder.addFixMethodParameter(currentArgumentType, currentParameterId);
                currentParameterId += currentArgumentType == Type.LONG_TYPE || currentArgumentType == Type.DOUBLE_TYPE
                    ? 2
                    : 1;
            }
        }

        if (insertOnExit) builder.setInjectorFactory(ASMFix.ON_EXIT_FACTORY); // If we have to insert the fix on the
                                                                              // exits from a method, we set the factory
                                                                              // to the one that makes fixes that insert
                                                                              // themselves on the exits

        if (annotationValues.containsKey("insertOnLine")) {
            int lineToBeInsertedOn = (Integer) annotationValues.get("insertOnLine");
            builder.setInjectorFactory(new FixInserterFactory.OnLineNumber(lineToBeInsertedOn)); // If we have to insert
                                                                                                 // the fix on a line
                                                                                                 // number, we set the
                                                                                                 // factory to the one
                                                                                                 // that makes fixes
                                                                                                 // that inserts that
                                                                                                 // insert themselves on
                                                                                                 // the specific line
                                                                                                 // number
        }

        if (annotationValues.containsKey("returnedType")) {
            builder.setTargetMethodReturnType((String) annotationValues.get("returnedType"));
        }

        EnumReturnSetting EnumReturnSetting = net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.NEVER;
        if (annotationValues.containsKey("returnSetting")) {
            EnumReturnSetting = net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting
                .valueOf((String) annotationValues.get("returnSetting"));
            builder.setReturnSetting(EnumReturnSetting);
        }

        // A lot of this is easy to read without comments, and if not, you can look at the documentation of the methods
        // called.

        if (EnumReturnSetting != net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.NEVER) { // if we have
                                                                                                        // custom logic
                                                                                                        // for if we
                                                                                                        // return
                                                                                                        // something
                                                                                                        // different
                                                                                                        // from the
                                                                                                        // original
                                                                                                        // target method
            Object primitiveConstant = getAlwaysReturnedValue();
            if (primitiveConstant != null) {
                builder.setReturnType(net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.PRIMITIVE_CONSTANT);
                builder.setPrimitiveAlwaysReturned(primitiveConstant);
            } else if (Boolean.TRUE.equals(annotationValues.get("nullReturned"))) {
                builder.setReturnType(net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.NULL);
            } else if (annotationValues.containsKey("anotherMethodReturned")) {
                builder.setReturnType(
                    net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.ANOTHER_METHOD_RETURN_VALUE);
                builder.setReturnMethod((String) annotationValues.get("anotherMethodReturned"));
            } else if (methodType.getReturnType() != Type.VOID_TYPE) {
                builder
                    .setReturnType(net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.FIX_METHOD_RETURN_VALUE);
            }
        }
        // returnSetting and *AlwaysReturned set the type of the fix method, so we can only set them now

        builder.setFixMethodReturnType(methodType.getReturnType());

        if (EnumReturnSetting == net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.ON_TRUE
            && methodType.getReturnType() != Type.BOOLEAN_TYPE) {
            warnInvalidFix(
                "Fix method must return boolean if returnSetting is ON_TRUE. (if we only return our custom value/ the original value if the fix method returns true, how do we know if it's true if it's not a boolean?)");
            return;
        }
        if ((EnumReturnSetting == net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.ON_NULL
            || EnumReturnSetting == net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.ON_NOT_NULL)
            && methodType.getReturnType()
                .getSort() != Type.OBJECT
            && methodType.getReturnType()
                .getSort() != Type.ARRAY) {
            warnInvalidFix(
                "Fix method must return object if returnSetting is ON_NULL or ON_NOT_NULL. (if we only return our custom value/ the original value if the fix method returns a null/ non null object, how do we know if it's a null/ not null object if it's not an object?)");
            return;
        }

        if (annotationValues.containsKey("order")) {
            builder.setPriority(FixOrder.valueOf((String) annotationValues.get("order")));
        }

        if (annotationValues.containsKey("createNewMethod")) {
            builder.setCreateMethod(Boolean.TRUE.equals(annotationValues.get("createNewMethod")));
        }
        if (annotationValues.containsKey("isFatal")) {
            builder.setFatal(Boolean.TRUE.equals(annotationValues.get("isFatal")));
        }

        transformer.registerFix(builder.build()); // Adds the fix to the list to be inserted
    }

    /**
     * @return The value of the annotation key storing an always-returned value. Null if there is no such value
     *         specified.
     */
    private Object getAlwaysReturnedValue() {
        for (Entry<String, Object> entry : annotationValues.entrySet()) {
            if (entry.getKey()
                .endsWith("AlwaysReturned")) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Custom class visitor that visits the class with fix methods and returns custom method visitors to be executed.
     */
    private class FixClassVisitor extends ClassVisitor {

        String fixesClassName = "";

        public FixClassVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces) {
            fixesClassName = name.replace('/', '.');
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            currentFixMethodName = name; // Method currently parsing
            currentFixMethodDescriptor = desc; // Descriptor of method currently parsing
            currentMethodIsPublicAndStatic = (access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) != 0; // e.g.
                                                                                                                       // access
                                                                                                                       // codes
                                                                                                                       // 1000
                                                                                                                       // &
                                                                                                                       // 1000
                                                                                                                       // =
                                                                                                                       // 1000
                                                                                                                       // (8).
                                                                                                                       // 1000
                                                                                                                       // &
                                                                                                                       // 0001
                                                                                                                       // =
                                                                                                                       // 0.
                                                                                                                       // If
                                                                                                                       // access
                                                                                                                       // is
                                                                                                                       // the
                                                                                                                       // same,
                                                                                                                       // it
                                                                                                                       // will
                                                                                                                       // be
                                                                                                                       // the
                                                                                                                       // number
                                                                                                                       // of
                                                                                                                       // the
                                                                                                                       // access.
            return new FixMethodVisitor(fixesClassName); // Custom method visitor to be executed
        }
    }

    /**
     * Custom method visitor. If the method it's visiting has a @Fix annotaion, returns a FixAnnotationVisitor instead
     * of a normal AnnotationVisitor and creates a fix when finished reading the values.
     */
    private class FixMethodVisitor extends MethodVisitor {

        String clsName;

        public FixMethodVisitor(String className) {
            super(Opcodes.ASM5);
            clsName = className;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (fixDescriptor.equals(descriptor)) { // If it has a fix annotation
                annotationValues = new HashMap<>(); // Store it's keys and values
                inFixAnnotation = true;
            }
            return new FixAnnotationVisitor();
        }

        // An argument is meant by a parameter. This calls when visiting arguments of the method that are annotations.
        @Override
        public AnnotationVisitor visitParameterAnnotation(final int indexOfArgument, String descriptor,
            boolean visible) {
            if (returnedValueDescriptor.equals(descriptor)) {
                argumentAnnotations.put(indexOfArgument, -1); // If it is a returnedValue argument, we want to have the
                                                              // value on the top of the stack passed in to argument
                                                              // number indexOfArgument in the fix method
            }
            if (localVariableDescriptor.equals(descriptor)) {
                return new AnnotationVisitor(Opcodes.ASM5) { // If it is a localVariable argument, we return a custom
                                                             // AnnotationVisitor that will add the annotation to the
                                                             // list when visiting it

                    @Override
                    public void visit(String name, Object value) {
                        argumentAnnotations.put(indexOfArgument, (Integer) value); // We want to have the value at x
                                                                                   // index in stack passed in to
                                                                                   // argument number indexOfArgument in
                                                                                   // the fix method
                    }
                };
            }
            return null; // If it is some other annotation, ignore it
        }

        // We are at the end and have parsed the fix method
        @Override
        public void visitEnd() {
            if (annotationValues != null) {
                createAndRegisterFix(this.clsName); // If the annotation exists, we create a fix (there are some default
                                                    // values, so if it's there it's values are never null). We need
                                                    // this check because not all methods inside the class with fixes
                                                    // might be fixes.
            }
            // clean up the variables for the next fix method to occupy them
            argumentAnnotations.clear();
            currentFixMethodName = null;
            currentFixMethodDescriptor = null;
            currentMethodIsPublicAndStatic = false;
            annotationValues = null;
        }
    }

    /** Custom annotation visitor that stores the annotation's keys and values inside annotationValues. */
    private class FixAnnotationVisitor extends AnnotationVisitor {

        public FixAnnotationVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(String name, Object value) {
            if (inFixAnnotation) { // If we are currently visiting a fix annotation.
                annotationValues.put(name, value); // Store the keys and values inside the annotation
            }
        }

        // If a value in the annotation is an enum, it calls this one
        @Override
        public void visitEnum(String name, String descriptor, String value) {
            visit(name, value);
        }

        // When it has finished visiting an annotation, we are no longer inside one so we reset it back to false
        @Override
        public void visitEnd() {
            inFixAnnotation = false;
        }
    }
}
