package net.tclproject.mysteriumlib.asm.core;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.BYTE_TYPE;
import static org.objectweb.asm.Type.CHAR_TYPE;
import static org.objectweb.asm.Type.DOUBLE_TYPE;
import static org.objectweb.asm.Type.FLOAT_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;
import static org.objectweb.asm.Type.SHORT_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;

import java.util.ArrayList;
import java.util.List;

import net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting;
import net.tclproject.mysteriumlib.asm.annotations.EnumReturnType;
import net.tclproject.mysteriumlib.asm.annotations.FixOrder;
import net.tclproject.mysteriumlib.asm.core.FixInserterFactory.OnExit;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * The main ASM class, responsible for the insertion of a fix into a method.
 * PLEASE DO NOT MODIFY ANY OF THE FIELDS INSIDE MANUALLY, USE THE BUILDER AND IT'S SETTERS
 * OR YOU RISK BREAKING THINGS.
 * <p/>
 * --*--
 * <p/>
 * Terms used:
 * <p/>
 * Fix - the execution of your static code from minecraft's, forge's, or another mod's code
 * <p/>
 * Target Method - method, inside which you put your fix
 * <p/>
 * Target Class - the class that has the method inside which you put your fix
 * <p/>
 * Fix Method - your static method that gets called from other's code
 * <p/>
 * Fix Class (Class with fixes) - the class, inside which your fix method/s is located.
 */
public class ASMFix implements Cloneable, Comparable<ASMFix> {

    // Variables

    /** List of arguments the target method takes. */
    public List<Type> targetMethodArguments = new ArrayList<>(2);
    public List<Integer> transmittableVariableIndexes = new ArrayList<>(2);
    /** List of arguments the fix method takes. */
    public List<Type> fixMethodArguments = new ArrayList<>(2);

    /** The return type of the fix method. */
    public Type fixMethodReturnType = Type.VOID_TYPE;
    /** The return type of the target method. If it isn't specified, isn't verified. */
    public Type targetMethodReturnType;

    /** The order in which the fixes for the same target method will be inserted. */
    public FixOrder priority = FixOrder.USUAL;

    /** What the fix method returns. e.g. another method, a constant integer, etc. */
    EnumReturnType EnumReturnType = net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.VOID;

    /** In what situations the fix method returns something. */
    EnumReturnSetting EnumReturnSetting = net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.NEVER;

    /** The fix inserter factory used. */
    public FixInserterFactory injectorFactory = ON_ENTER_FACTORY;
    /** An instance of a factory that makes fix inserters that insert fixes on the start of the method. */
    public static final FixInserterFactory ON_ENTER_FACTORY = FixInserterFactory.OnEnter.INSTANCE;
    /** An instance of a factory that makes fix inserters that insert fixes on the end of the method. */
    public static final FixInserterFactory ON_EXIT_FACTORY = FixInserterFactory.OnExit.INSTANCE;

    /** The primitive value that is to be always returned, if specified. */
    private Object primitiveAlwaysReturned;

    /** The name of the class that holds the target method. */
    public String targetClassName;
    /** The target method's name. */
    public String targetMethodName;
    /** The class with the fix method from which this fix has been created. */
    public String classWithFixes;
    /** The fix method's name. */
    public String fixMethodName;
    /** The target method's descriptor. Can be without the return type. */
    public String targetMethodDescriptor;
    /** The fix method's descriptor. */
    public String fixMethodDescriptor;
    /**
     * The return method's name. The return method is the method the return value of which is passed if returnType is
     * ANOTHER_METHOD_RETURN_VALUE.
     */
    public String returnMethodName;
    /**
     * The return method's descriptor. Can be without the return type. The return method is the method the return value
     * of which is passed if returnType is ANOTHER_METHOD_RETURN_VALUE.
     */
    public String returnMethodDescriptor;
    /** If the value from return is passed to the fix method. */
    public boolean hasReturnedValueParameter;
    /** If the fix needs to create a method to be inserted into. */
    public boolean createMethod;
    /** If the game should crash if the inserting fails. */
    public boolean isFatal;

    // Methods

    /** Getter for targetClassName. */
    public String getTargetClassName() {
        return targetClassName;
    }

    /** Getter for targetClassName with '.'s replaced with '/'s. */
    public String getTargetClassInternalName() {
        return targetClassName.replace('.', '/');
    }

    /** Getter for classWithFixes with '.'s replaced with '/'s. */
    public String getClassWithFixesInternalName() {
        return classWithFixes.replace('.', '/');
    }

    /** Returns if the method is the target of this ASMFix. */
    public boolean isTheTarget(String name, String descriptor) {
        return (targetMethodReturnType == null && descriptor.startsWith(targetMethodDescriptor)
            || descriptor.equals(targetMethodDescriptor)) && name.equals(targetMethodName);
    }

    /** Getter for createMethod. */
    public boolean getCreateMethod() {
        return createMethod;
    }

    /** Getter for isFatal. */
    public boolean isMandatory() {
        return isFatal;
    }

    /** Getter for injectorFactory. */
    public FixInserterFactory getInjectorFactory() {
        return injectorFactory;
    }

    /**
     * Returns if a fix method is stored inside this ASMFix (in other words, if fixMethodName != null && classWithFixes
     * != null).
     */
    public boolean hasFixMethod() {
        return fixMethodName != null && classWithFixes != null;
    }

    /** Creates a method in the target class. */
    public void createMethod(FixInserterClassVisitor classVisitor) {
        MetaReader.MethodReference superMethod = classVisitor.transformer.metaReader
            .findMethod(getTargetClassInternalName(), targetMethodName, targetMethodDescriptor);
        // We're using the name of the super method to avoid any errors
        MethodVisitor methodVisitor = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC,
            superMethod == null ? targetMethodName : superMethod.name,
            targetMethodDescriptor,
            null,
            null);
        if (methodVisitor instanceof FixInserter) { // If the method is, indeed, to be created
            FixInserter inserter = (FixInserter) methodVisitor;
            inserter.visitCode();
            inserter.visitLabel(new Label());
            if (superMethod == null) {
                insertPushDefaultReturnValue(inserter, targetMethodReturnType);
            } else {
                insertSuperCall(inserter, superMethod);
            }
            insertReturn(inserter, targetMethodReturnType);
            inserter.visitLabel(new Label());
            inserter.visitMaxs(0, 0);
            inserter.visitEnd();
        } else {
            throw new IllegalArgumentException(
                "A fix inserter hasn't been created for this method, which means the method isn't to be fixed. Likely, something is broken.");
        }
    }

    /**
     * Inserts the fix needed.
     * 
     * @param inserter the FixInserter that has called this method.
     */
    public void insertFix(FixInserter inserter) {
        Type targetMethodReturnType = inserter.methodType.getReturnType();

        // Store the value that has been passed into return to a local variable
        int returnLocalIndex = -1;
        if (hasReturnedValueParameter) {
            returnLocalIndex = inserter.newLocal(targetMethodReturnType);
            inserter.visitVarInsn(targetMethodReturnType.getOpcode(54), returnLocalIndex); // storeLocal
        }

        // insert a call to the fix method
        int fixResultLocalIndex = -1;
        if (hasFixMethod()) {
            insertInvokeStatic(inserter, returnLocalIndex, fixMethodName, fixMethodDescriptor);

            if (EnumReturnType == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.FIX_METHOD_RETURN_VALUE
                || EnumReturnSetting.conditionRequiredToReturn) {
                fixResultLocalIndex = inserter.newLocal(fixMethodReturnType);
                inserter.visitVarInsn(fixMethodReturnType.getOpcode(54), fixResultLocalIndex); // storeLocal
            }
        }

        // insert return
        if (EnumReturnSetting != net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.NEVER) {
            Label label = inserter.newLabel();

            // insert a GOTO to label after return has been called
            if (EnumReturnSetting != net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.ALWAYS) {
                inserter.visitVarInsn(fixMethodReturnType.getOpcode(21), fixResultLocalIndex); // loadLocal
                if (EnumReturnSetting == net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.ON_TRUE) {
                    inserter.visitJumpInsn(IFEQ, label);
                } else if (EnumReturnSetting == net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.ON_NULL) {
                    inserter.visitJumpInsn(IFNONNULL, label);
                } else if (EnumReturnSetting
                    == net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.ON_NOT_NULL) {
                        inserter.visitJumpInsn(IFNULL, label);
                    }
            }

            // insert the value that has to be returned into the stack
            if (EnumReturnType == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.NULL) {
                inserter.visitInsn(Opcodes.ACONST_NULL);
            } else
                if (EnumReturnType == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.PRIMITIVE_CONSTANT) {
                    inserter.visitLdcInsn(primitiveAlwaysReturned);
                } else if (EnumReturnType
                    == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.FIX_METHOD_RETURN_VALUE) {
                        inserter.visitVarInsn(fixMethodReturnType.getOpcode(21), fixResultLocalIndex); // loadLocal
                    } else if (EnumReturnType
                        == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.ANOTHER_METHOD_RETURN_VALUE) {
                            String returnMethodDescription = this.returnMethodDescriptor;
                            // If the needed target method's return type hasn't been specified, adding it to the
                            // descriptor
                            if (returnMethodDescription.endsWith(")")) {
                                returnMethodDescription += targetMethodReturnType.getDescriptor();
                            }
                            insertInvokeStatic(inserter, returnLocalIndex, returnMethodName, returnMethodDescription);
                        }

            // call return
            insertReturn(inserter, targetMethodReturnType);

            // inserting a label, to which GOTO will jump
            inserter.visitLabel(label);
        }

        // putting the returned value into the stack
        if (hasReturnedValueParameter) {
            insertLoad(inserter, targetMethodReturnType, returnLocalIndex);
        }
    }

    /**
     * Inserts a load instruction for the type passed. A load instruction loads a variable from the local variable table
     * onto the stack.
     * 
     * @param inserter      Inserter that is inserting this fix.
     * @param parameterType The type of variable to be loaded.
     * @param variableIndex The index of the variable in the table.
     */
    public void insertLoad(FixInserter inserter, Type parameterType, int variableIndex) {
        int opcode;
        if (parameterType == INT_TYPE || parameterType == BYTE_TYPE
            || parameterType == CHAR_TYPE
            || parameterType == BOOLEAN_TYPE
            || parameterType == SHORT_TYPE) {
            opcode = ILOAD;
        } else if (parameterType == LONG_TYPE) {
            opcode = LLOAD;
        } else if (parameterType == FLOAT_TYPE) {
            opcode = FLOAD;
        } else if (parameterType == DOUBLE_TYPE) {
            opcode = DLOAD;
        } else {
            opcode = ALOAD;
        }
        inserter.visitVarInsn(opcode, variableIndex);
    }

    /**
     * Inserts a call to super().
     * 
     * @param inserter Inserter that is inserting this fix.
     */
    public void insertSuperCall(FixInserter inserter, MetaReader.MethodReference method) {
        int variableIndex = 0;
        for (int i = 0; i <= targetMethodArguments.size(); i++) {
            Type argumentType = i == 0 ? TypeUtils.getType(targetClassName) : targetMethodArguments.get(i - 1);
            insertLoad(inserter, argumentType, variableIndex);
            if (argumentType.getSort() == Type.DOUBLE || argumentType.getSort() == Type.LONG) { // if it is a long or a
                                                                                                // double, it occupies
                                                                                                // two variables,
                                                                                                // therefore we need to
                                                                                                // insert the load for
                                                                                                // the one two later,
                                                                                                // skipping one
                variableIndex += 2;
            } else {
                variableIndex++;
            }
        }
        inserter.visitMethodInsn(INVOKESPECIAL, method.owner, method.name, method.descriptor, false);
    }

    /**
     * Inserts an instruction to push the target method return type's default value onto the stack, e.g. int -> 0,
     * object -> null
     * 
     * @param inserter               Inserter that is inserting this fix.
     * @param targetMethodReturnType the return type of the target method.
     */
    public void insertPushDefaultReturnValue(FixInserter inserter, Type targetMethodReturnType) {
        switch (targetMethodReturnType.getSort()) {
            case Type.VOID:
                break;
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                inserter.visitInsn(Opcodes.ICONST_0);
                break;
            case Type.FLOAT:
                inserter.visitInsn(Opcodes.FCONST_0);
                break;
            case Type.LONG:
                inserter.visitInsn(Opcodes.LCONST_0);
                break;
            case Type.DOUBLE:
                inserter.visitInsn(Opcodes.DCONST_0);
                break;
            default:
                inserter.visitInsn(Opcodes.ACONST_NULL);
                break;
        }
    }

    /**
     * Inserts a return instruction.
     * 
     * @param inserter               Inserter that is inserting this fix.
     * @param targetMethodReturnType the return type of the target method.
     */
    public void insertReturn(FixInserter inserter, Type targetMethodReturnType) {
        if (targetMethodReturnType == INT_TYPE || targetMethodReturnType == SHORT_TYPE
            || targetMethodReturnType == BOOLEAN_TYPE
            || targetMethodReturnType == BYTE_TYPE
            || targetMethodReturnType == CHAR_TYPE) {
            inserter.visitInsn(IRETURN);
        } else if (targetMethodReturnType == LONG_TYPE) {
            inserter.visitInsn(LRETURN);
        } else if (targetMethodReturnType == FLOAT_TYPE) {
            inserter.visitInsn(FRETURN);
        } else if (targetMethodReturnType == DOUBLE_TYPE) {
            inserter.visitInsn(DRETURN);
        } else if (targetMethodReturnType == VOID_TYPE) {
            inserter.visitInsn(RETURN);
        } else {
            inserter.visitInsn(ARETURN);
        }
    }

    /**
     * Inserts an instruction to call a static method. This instruction is used to insert a call to the fix methods
     * inside the target methods.
     * 
     * @param inserter              Inserter that is inserting this fix.
     * @param indexOfReturnArgument Index at which the argument for passing the return value is.
     */
    public void insertInvokeStatic(FixInserter inserter, int indexOfReturnArgument, String name, String descriptor) {
        for (int i = 0; i < fixMethodArguments.size(); i++) { // Iterate through all the fix method's arguments
            Type parameterType = fixMethodArguments.get(i);
            int variableIndex = transmittableVariableIndexes.get(i);
            if (inserter.isStatic) {
                // if we need to pass this from a static method, we pass null
                if (variableIndex == 0) {
                    inserter.visitInsn(Opcodes.ACONST_NULL);
                    continue;
                }
                // otherwise, move the index of the local variable
                if (variableIndex > 0) variableIndex--;
            }
            if (variableIndex == -1) variableIndex = indexOfReturnArgument;
            insertLoad(inserter, parameterType, variableIndex);
        }

        inserter.visitMethodInsn(INVOKESTATIC, getClassWithFixesInternalName(), name, descriptor, false); // instruction,
                                                                                                          // owner
                                                                                                          // class,
                                                                                                          // method
                                                                                                          // name,
                                                                                                          // method
                                                                                                          // descriptor,
                                                                                                          // if the
                                                                                                          // owner class
                                                                                                          // is an
                                                                                                          // interface
    }

    /** Gets the target method's full name, as class#method+descriptor. */
    public String getFullTargetMethodName() {
        return targetClassName + '#' + targetMethodName + targetMethodDescriptor;
    }

    // already has good java docs
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ASMFix: ");

        sb.append(targetClassName)
            .append('#')
            .append(targetMethodName);
        sb.append(targetMethodDescriptor);
        sb.append(" -> ");
        sb.append(classWithFixes)
            .append('#')
            .append(fixMethodName);
        sb.append(fixMethodDescriptor);

        sb.append(", EnumReturnSetting=" + EnumReturnSetting);
        sb.append(", EnumReturnType=" + EnumReturnType);
        if (EnumReturnType == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.PRIMITIVE_CONSTANT)
            sb.append(", Constant=" + primitiveAlwaysReturned);
        sb.append(
            ", InjectorFactory: " + injectorFactory.getClass()
                .getName());
        sb.append(", CreateMethod = " + createMethod);

        return sb.toString();
    }

    // also has good java docs, this one just compares the order at which the fixes will be inserted. 1: the priority is
    // greater (HIGHEST equiv.), -1: less (LOWEST equiv.)
    @Override
    public int compareTo(ASMFix fix) {
        if (injectorFactory.priorityReversed && fix.injectorFactory.priorityReversed) { // if both are inserted at the
                                                                                        // end
            return priority.ordinal() > fix.priority.ordinal() ? -1 : 1; // Since priority is reversed, if our priority
                                                                         // is greater (>) it is actually less (-1)
        } else if (!injectorFactory.priorityReversed && !fix.injectorFactory.priorityReversed) { // if both are inserted
                                                                                                 // at the beginning
            return priority.ordinal() > fix.priority.ordinal() ? 1 : -1; // If our priority is greater (>), it's greater
                                                                         // (1)
        } else {
            return injectorFactory.priorityReversed ? 1 : -1; // If both are inserted at different places: If we are
                                                              // inserted at the end, we have greater priority.
        }
    }

    /**
     * A factory for a builder object.
     * 
     * @return a new builder for a new ASMFix object.
     */
    public static Builder newBuilder() {
        return new ASMFix().new Builder();
    }

    /** ASMFix with additional setters. */
    public class Builder extends ASMFix {

        private Builder() {

        }

        /**
         * -----[NECESSARY TO CALL THIS FOR EVERY BUILDER]-----
         * <p/>
         * Defines the name of the class, into which the fix will be inserted.
         *
         * @param name The full name and path of the class, e.g. net.minecraft.world.World
         * @return this
         */
        public Builder setTargetClass(String name) {
            ASMFix.this.targetClassName = name;
            return this;
        }

        /**
         * -----[NECESSARY TO CALL THIS FOR EVERY BUILDER]-----
         * <p/>
         * Defines the name of the method, into which the fix will be inserted.
         * If fixing a constructor is needed, the method name is <init>.
         *
         * @param name Name of the target method, e.g. getBlockId.
         * @return this
         */
        public Builder setTargetMethod(String name) {
            ASMFix.this.targetMethodName = name;
            return this;
        }

        /**
         * -----[NECESSARY TO CALL THIS IF THE TARGET METHOD HAS ARGUMENTS]-----
         * <p/>
         * Adds one or more arguments to the list of arguments of the target method.
         * <p/>
         * Those arguments are used to create the descriptor of the target method.
         * In order to precisely find the target method, the name by itself is not enough, as methods with the same name
         * but different arguments exist.
         * <p/>
         * Example of use:
         * <p/>
         * import static net.tclproject.mysteriumlib.asm.core.TypeUtils.*
         * <p/>
         * //...
         * <p/>
         * addTargetMethodParameters(Type.INT_TYPE)
         * <p/>
         * Type worldType = getType("net.minecraft.world.World")
         * <p/>
         * Type playerType = getType("net.minecraft.entity.player.EntityPlayer")
         * <p/>
         * addTargetMethodParameters(worldType, playerType, playerType)
         *
         * @param argumentTypes The types of arguments of the target method
         * @see TypeUtils
         * @return this
         */
        public Builder addTargetMethodParameters(Type... argumentTypes) {
            for (Type type : argumentTypes) {
                ASMFix.this.targetMethodArguments.add(type);
            }
            return this;
        }

        /**
         * Adds one or more arguments to the list of arguments of the target method.
         * This is an abstraction layer for addTargetMethodParameters(Type... parameterTypes), that resolves the types
         * from names.
         * <p/>
         * 
         * @param argumentTypeNames Names of classes of the arguments of the target method, e.g.
         *                          net.minecraft.world.World
         * @return this
         */
        public Builder addTargetMethodParameters(String... argumentTypeNames) {
            Type[] types = new Type[argumentTypeNames.length];
            for (int i = 0; i < argumentTypeNames.length; i++) {
                types[i] = TypeUtils.getType(argumentTypeNames[i]);
            }
            return addTargetMethodParameters(types);
        }

        /**
         * Specifies the return type of the target method.
         * The return type is used to create the descriptor of the target method.
         * In order to precisely find the target method, the name by itself is not enough, the descriptor is also
         * needed.
         * By default, the fix inserts into all methods with the right name and arguments.
         *
         * @param returnType The return type of the target method.
         * @see TypeUtils
         * @return this
         */
        public Builder setTargetMethodReturnType(Type returnType) {
            ASMFix.this.targetMethodReturnType = returnType;
            return this;
        }

        /**
         * Specifies the return type of the target method.
         * This is an abstraction layer for setTargetMethodReturnType(Type returnType), that resolves the type from it's
         * name.
         *
         * @param returnType Name of the class, instance of which the target method returns.
         * @return this
         */
        public Builder setTargetMethodReturnType(String returnType) {
            return setTargetMethodReturnType(TypeUtils.getType(returnType));
        }

        /**
         * -----[NECESSARY TO CALL THIS IF A FULL FIX IS NEEDED (NOT ONLY AlwaysReturned)]-----
         * <p/>
         * Specifies the name of the class with the fix method.
         *
         * @param name The full name of the class, e.g. com.example.examplemod.asm.MyFixes
         * @return this
         */
        public Builder setFixesClass(String name) {
            ASMFix.this.classWithFixes = name;
            return this;
        }

        // TODO: Add a check
        /**
         * -----[NECESSARY TO CALL THIS IF A FULL FIX IS NEEDED (NOT ONLY AlwaysReturned)]-----
         * <p/>
         * Specifies the name of the fix method.
         * The fix method MUST be static, and there is no check to verify that. Be careful, please.
         *
         * @param name The name of the fix method, e.g. myFix
         * @return this
         */
        public Builder setFixMethod(String name) {
            ASMFix.this.fixMethodName = name;
            return this;
        }

        /**
         * -----[NECESSARY TO CALL THIS IF THE FIX METHOD HAS ARGUMENTS]-----
         * <p/>
         * Adds an argument to the list of arguments of the fix method.
         * In bytecode, the names of the arguments aren't saved. Instead, the use of indexes is needed.
         * <p/>
         * For example, in the class EntityLivingBase there is a method: attackEntityFrom(DamageSource damageSource,
         * float damage).
         * Inside it, these indexes will be used:
         * <p/>
         * 1 - damageSource
         * 2 - damage
         * <p/>
         * IMPORTANT: longs and doubles take up two spaces.
         * Theoretically, not only the original aguments can be passed in to the fix method but also local variables,
         * but their indexes will be way harder to calculate.
         * <p/>
         * For example, in the class Entity there is a method: setPosition(double x, double y, double z).
         * Inside it, these indexes will be used:
         * <p/>
         * 1 - x
         * 2 - пропущено
         * 3 - y
         * 4 - пропущено
         * 5 - z
         * 6 - пропущено
         * <p/>
         * The code of this method:
         * <p/>
         * //...
         * float f = ...;
         * float f1 = ...;
         * //...
         * <p/>
         * In this case, the index of f will be 7, and f1: 8.
         * <p/>
         * If the target method is static, you don't start counting the indexes from 0,
         * The numbers will be moved automatically.
         *
         * @param parameterType Type of argument of the fix method
         * @param variableIndex Index of the variable passed to the fix method
         * @throws IllegalStateException If the name of the fix method or class isn't specified
         * @return this
         */
        public Builder addFixMethodParameter(Type parameterType, int variableIndex) {
            if (!ASMFix.this.hasFixMethod()) {
                throw new IllegalStateException(
                    "Fix method is not specified, can't append argument to its arguments list.");
            }
            ASMFix.this.fixMethodArguments.add(parameterType);
            ASMFix.this.transmittableVariableIndexes.add(variableIndex);
            return this;
        }

        /**
         * Adds an argument to the list of arguments of the fix method.
         * This is an abstraction layer for addFixMethodParameter(Type parameterType, int variableIndex), that resolves
         * the type from it's name.
         *
         * @param parameterTypeName Name of the type of the argument, e.g. net.minecraft.world.World
         * @param variableIndex     Index of the variable passed to the fix method
         * @return this
         */
        public Builder addFixMethodParameter(String parameterTypeName, int variableIndex) {
            return addFixMethodParameter(TypeUtils.getType(parameterTypeName), variableIndex);
        }

        /**
         * Adds the target class to the fix method's list of arguments to later be passed this.
         * If the target method is static, null will be passed.
         *
         * @throws IllegalStateException If the fix method is not specified
         * @return this
         */
        public Builder addThisToFixMethodParameters() {
            if (!ASMFix.this.hasFixMethod()) {
                throw new IllegalStateException(
                    "Fix method is not specified, can't append argument to its arguments list.");
            }
            ASMFix.this.fixMethodArguments.add(TypeUtils.getType(ASMFix.this.targetClassName));
            ASMFix.this.transmittableVariableIndexes.add(0);
            return this;
        }

        /**
         * Adds the return type of the target method to the fix method's list of arguments to later be passed the
         * returned value.
         * In other words, it's the top value on the stack when the fix method is called.
         * <p/>
         * e.g. this is the code of a method:
         * <p/>
         * int foo = bar();
         * <p/>
         * return foo;
         * <p/>
         * Or this is the code of the method:
         * <p/>
         * return bar()
         * <p/>
         * In both cases we can pass the fix method the returned value before return is called, because of how bytecode
         * works.
         *
         * @throws IllegalStateException if the target method returns void
         * @throws IllegalStateException if the fix method isn't specified
         * @return this
         */
        public Builder addReturnedValueToFixMethodParameters() {
            if (!ASMFix.this.hasFixMethod()) {
                throw new IllegalStateException(
                    "Fix method is not specified, can't append argument to its arguments list.");
            }
            if (ASMFix.this.targetMethodReturnType == Type.VOID_TYPE) {
                throw new IllegalStateException(
                    "Target method's return type is void so it doesn't make sense to transmit it's return value to the fix method, as frankly, there is none.");
            }
            ASMFix.this.fixMethodArguments.add(ASMFix.this.targetMethodReturnType);
            ASMFix.this.transmittableVariableIndexes.add(-1);
            ASMFix.this.hasReturnedValueParameter = true;
            return this;
        }

        /**
         * Specifies a setting, in which, return is called after the fix method has been.
         * By default, return isn't called at all.
         * <p/>
         * Besides that, this method changes the return type of the fix method:
         * <p/>
         * NEVER -> void
         * ALWAYS -> void
         * ON_TRUE -> boolean
         * ON_NULL -> Object
         * ON_NOT_NULL -> Object
         *
         * @param setting The condition, if which, it will exit from the target method after the fix method has been
         *                called.
         * @throws IllegalArgumentException if setting == ON_TRUE, ON_NULL or ON_NOT_NULL, but the fix method isn't
         *                                  specified.
         * @see EnumReturnSetting
         * @return this
         */
        public Builder setReturnSetting(EnumReturnSetting setting) {
            if (setting.conditionRequiredToReturn && ASMFix.this.fixMethodName == null) {
                throw new IllegalArgumentException(
                    "Fix method isn't specified, can't use a return condition that depends on it.");
            }

            ASMFix.this.EnumReturnSetting = setting;
            Type returnType;
            switch (setting) {
                case NEVER:
                case ALWAYS:
                    returnType = VOID_TYPE;
                    break;
                case ON_TRUE:
                    returnType = BOOLEAN_TYPE;
                    break;
                default:
                    returnType = getType(Object.class);
                    break;
            }
            ASMFix.this.fixMethodReturnType = returnType;
            return this;
        }

        /**
         * -----[NECESSARY TO CALL THIS IF THE TARGET METHOD DOESN'T RETURN void AND setReturnSetting HAS BEEN
         * CALLED]-----
         * <p/>
         * Specifies the type that gets returned after the fix method has been called.
         * Call this after setReturnSetting.
         * By default, void is returned.
         * <p/>
         * Besides that, if value == EnumReturnType.FIX_RETURN_VALUE, this method changes the return type
         * of the fix method to the type, specified in setTargetMethodReturnType()
         *
         * @param type the return type, e.g. EnumReturnType.FIX_RETURN_VALUE, PRIMITIVE_CONSTANT etc.
         * @throws IllegalStateException    if EnumReturnSetting == NEVER it doesn't make sense to specify the return
         *                                  type, since return doesn't get called.
         * @throws IllegalArgumentException if value == EnumReturnType.FIX_RETURN_VALUE, but the return type of the
         *                                  target method is void (or setTargetMethodReturnType hasn't been called).
         *                                  It doesn't make sense to use the value that the fix method returned, if it
         *                                  returned void (it must match the type returned by the target method).
         * @return this
         */
        public Builder setReturnType(EnumReturnType type) {
            classWithFixes = ASMFix.this.classWithFixes;
            fixMethodName = ASMFix.this.fixMethodName;
            if (ASMFix.this.EnumReturnSetting == net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting.NEVER) {
                throw new IllegalStateException(
                    "Current return condition is never, so it does not make sense to specify the return value.");
            }
            Type returnType = ASMFix.this.targetMethodReturnType;
            if (type != net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.VOID && returnType == VOID_TYPE) {
                throw new IllegalArgumentException(
                    "Target method return type is void, so it does not make sense to return anything else.");
            }
            if (type == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.VOID && returnType != VOID_TYPE) {
                throw new IllegalArgumentException(
                    "Target method return type is not void, so it is impossible to return void.");
            }
            if (type == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.PRIMITIVE_CONSTANT
                && returnType != null
                && !isPrimitive(returnType)) {
                throw new IllegalArgumentException(
                    "Target method return type isn't a primitive, so it is impossible to return one.");
            }
            if (type == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.NULL && returnType != null
                && isPrimitive(returnType)) {
                throw new IllegalArgumentException(
                    "Target method return type is a primitive, so it is impossible to return null.");
            }
            if (type == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.FIX_METHOD_RETURN_VALUE
                && !hasFixMethod()) {
                throw new IllegalArgumentException("Fix method is not specified, can't use it's return value.");
            }

            ASMFix.this.EnumReturnType = type;
            if (type == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.FIX_METHOD_RETURN_VALUE) { // If the
                                                                                                              // return
                                                                                                              // type is
                                                                                                              // the
                                                                                                              // type of
                                                                                                              // the fix
                                                                                                              // method,
                                                                                                              // we set
                                                                                                              // the
                                                                                                              // return
                                                                                                              // type of
                                                                                                              // the fix
                                                                                                              // method
                                                                                                              // to one
                                                                                                              // of the
                                                                                                              // target
                                                                                                              // method.
                ASMFix.this.fixMethodReturnType = ASMFix.this.targetMethodReturnType; // That is because the type must
                                                                                      // fit to be returned from the
                                                                                      // target method.
            }
            return this;
        }

        /**
         * Getter for fixMethodReturnType (In case it's hard to figure out).
         *
         * @return the fix method's return type.
         */
        public Type getFixMethodReturnType() {
            return fixMethodReturnType;
        }

        /**
         * Setter for fixMethodReturnType.
         *
         * @param type the fix method's return type.
         */
        public void setFixMethodReturnType(Type type) {
            ASMFix.this.fixMethodReturnType = type;
        }

        /**
         * Figures out if the type passed in is a primitive.
         * 
         * @param type a type.
         * @return if the type passed in is a primitive.
         */
        public boolean isPrimitive(Type type) {
            return type.getSort() > 0 && type.getSort() < 9; // more than 0 and less than 9
        }

        /**
         * -----[NECESSARY TO CALL THIS IF setReturnType HAS BEEN CALLED AND SET TO PRIMITIVE_CONSTANT]-----
         * <p/>
         * Call this after setReturnType(EnumReturnType.PRIMITIVE_CONSTANT).
         * Specifies the primitive, that will always be returned.
         *
         * @param object Object, class of which represents the primitive, e.g. int -> Integer.
         * @throws IllegalStateException    If specified return type isn't PRIMITIVE_CONSTANT
         * @throws IllegalArgumentException If the class of the passed in value isn't a wrapper for a primitive
         * @return this
         */
        public Builder setPrimitiveAlwaysReturned(Object object) {
            if (ASMFix.this.EnumReturnType
                != net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.PRIMITIVE_CONSTANT) {
                throw new IllegalStateException(
                    "Return type is not PRIMITIVE_CONSTANT, so it doesn't make sense to specify that constant.");
            }
            Type returnType = ASMFix.this.targetMethodReturnType;
            if (returnType == BOOLEAN_TYPE && !(object instanceof Boolean)
                || returnType == CHAR_TYPE && !(object instanceof Character)
                || returnType == BYTE_TYPE && !(object instanceof Byte)
                || returnType == SHORT_TYPE && !(object instanceof Short)
                || returnType == INT_TYPE && !(object instanceof Integer)
                || returnType == LONG_TYPE && !(object instanceof Long)
                || returnType == FLOAT_TYPE && !(object instanceof Float)
                || returnType == DOUBLE_TYPE && !(object instanceof Double)) {
                throw new IllegalArgumentException(
                    "Given object class does not match the target method's return type.");
            }

            ASMFix.this.primitiveAlwaysReturned = object;
            return this;
        }

        /**
         * -----[NECESSARY TO CALL THIS IF setReturnType HAS BEEN CALLED AND SET TO ANOTHER_METHOD_RETURN_METHOD]-----
         * <p/>
         * Call this after setReturnType(EnumReturnType.ANOTHER_METHOD_RETURN_VALUE).
         * Specifies the method, the return value of which will be returned.
         *
         * @param name the method's name
         * @throws IllegalStateException if the return type isn't ANOTHER_METHOD_RETURN_VALUE
         * @return this
         */
        public Builder setReturnMethod(String name) {
            if (ASMFix.this.EnumReturnType
                != net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.ANOTHER_METHOD_RETURN_VALUE) {
                throw new IllegalStateException(
                    "Return type is not ANOTHER_METHOD_RETURN_VALUE, so it does not make sence to specify that method.");
            }

            ASMFix.this.returnMethodName = name;
            return this;
        }

        /**
         * Sets the factory that will specify the inserter type.
         * In other words, specifies, if the fix will be inserted at the start (ASMFix.ON_ENTER_FACTORY) or end
         * (ON_EXIT_FACTORY) of a method. If you need OnLine, create your own factory.
         *
         * @param factory Factory, creating the inserter for this fix
         * @return this
         */
        public Builder setInjectorFactory(FixInserterFactory factory) {
            ASMFix.this.injectorFactory = factory;
            return this;
        }

        /**
         * Sets the priority of inserting this fix.
         * Fixes with higher priority will be inserted first.
         * 
         * @return this
         */
        public Builder setPriority(FixOrder priority) {
            ASMFix.this.priority = priority;
            return this;
        }

        // TODO: Create a way to actually have the fix method called inside the newly created method.
        /**
         * Setter for createMethod.
         * NOTE: The method body is a call to super() if it's an override, or only the return statement otherwise.
         * 
         * @return this
         */
        public Builder setCreateMethod(boolean createMethod) {
            ASMFix.this.createMethod = createMethod;
            return this;
        }

        /**
         * Setter for isFatal. (if a fix with isFatal == true fails, the game crashes)
         * 
         * @return this
         */
        public Builder setFatal(boolean isMandatory) {
            ASMFix.this.isFatal = isMandatory;
            return this;
        }

        /**
         * Builds a method descriptor from the return type and arguments.
         * 
         * @param returnType    the return type of the method.
         * @param argumentTypes the argument types of the method.
         * @return a string method descriptor.
         */
        private String getMethodDescriptor(Type returnType, List<Type> argumentTypes) {
            Type[] paramTypesArray = argumentTypes.toArray(new Type[0]);
            if (returnType == null) {
                String voidDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, paramTypesArray);
                return voidDescriptor.substring(0, voidDescriptor.length() - 1);
            } else {
                return Type.getMethodDescriptor(returnType, paramTypesArray);
            }
        }

        /**
         * Creates an ASMFix from the specified parameters.
         *
         * @return The ASMFix.
         * @throws IllegalStateException If one or more of the necessary methods hasn't been called.
         */
        public ASMFix build() {
            ASMFix fix = ASMFix.this;

            if (fix.createMethod && fix.targetMethodReturnType == null) {
                fix.targetMethodReturnType = fix.fixMethodReturnType; // If we're to create a method, it's return type
                                                                      // is the one of the fix method
            }
            fix.targetMethodDescriptor = getMethodDescriptor(fix.targetMethodReturnType, fix.targetMethodArguments); // Creates
                                                                                                                     // the
                                                                                                                     // target
                                                                                                                     // method
                                                                                                                     // descriptor
                                                                                                                     // out
                                                                                                                     // of
                                                                                                                     // the
                                                                                                                     // arguments
                                                                                                                     // and
                                                                                                                     // return
                                                                                                                     // type

            if (fix.hasFixMethod()) {
                fix.fixMethodDescriptor = Type
                    .getMethodDescriptor(fix.fixMethodReturnType, fix.fixMethodArguments.toArray(new Type[0])); // If we
                                                                                                                // have
                                                                                                                // a
                                                                                                                // full
                                                                                                                // fix
                                                                                                                // method
                                                                                                                // specified,
                                                                                                                // we
                                                                                                                // create
                                                                                                                // it's
                                                                                                                // descriptor
                                                                                                                // out
                                                                                                                // of
                                                                                                                // it's
                                                                                                                // return
                                                                                                                // type
                                                                                                                // and
                                                                                                                // arguments
            }
            if (fix.EnumReturnType
                == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.ANOTHER_METHOD_RETURN_VALUE) {
                fix.returnMethodDescriptor = getMethodDescriptor(fix.targetMethodReturnType, fix.fixMethodArguments); // If
                                                                                                                      // we're
                                                                                                                      // to
                                                                                                                      // return
                                                                                                                      // what
                                                                                                                      // a
                                                                                                                      // third
                                                                                                                      // method
                                                                                                                      // returns,
                                                                                                                      // we
                                                                                                                      // create
                                                                                                                      // it's
                                                                                                                      // desctiptor
                                                                                                                      // out
                                                                                                                      // of
                                                                                                                      // it's
                                                                                                                      // return
                                                                                                                      // type
                                                                                                                      // and
                                                                                                                      // arguments
            }

            try {
                fix = (ASMFix) ASMFix.this.clone(); // We turn this ASMFix builder into an ASMFix.
            } catch (CloneNotSupportedException impossible) {
                // It will never happen but I don't want to document the throws keyword if I put it in
            }

            if (fix.targetClassName == null) {
                throw new IllegalStateException(
                    "Target class name is not specified. Call setTargetClassName() before build().");
            }

            if (fix.targetMethodName == null) {
                throw new IllegalStateException(
                    "Target method name is not specified. Call setTargetMethodName() before build().");
            }

            if (fix.EnumReturnType == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.PRIMITIVE_CONSTANT
                && fix.primitiveAlwaysReturned == null) {
                throw new IllegalStateException(
                    "Return type is PRIMITIVE_CONSTANT, but the constant is not specified. Call setReturnType() before build().");
            }

            if (fix.EnumReturnType
                == net.tclproject.mysteriumlib.asm.annotations.EnumReturnType.ANOTHER_METHOD_RETURN_VALUE
                && fix.returnMethodName == null) {
                throw new IllegalStateException(
                    "Return type is ANOTHER_METHOD_RETURN_VALUE, but the method is not specified. Call setReturnMethod() before build().");
            }

            if (!(fix.injectorFactory instanceof OnExit) && fix.hasReturnedValueParameter) {
                throw new IllegalStateException(
                    "Can not pass the returned value to the fix method because the fix is not inserted on exit.");
            }

            return fix;
        }

    }
}
