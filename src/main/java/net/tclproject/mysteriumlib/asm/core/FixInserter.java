package net.tclproject.mysteriumlib.asm.core;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/** Custom MethodVisitor that calls the insert method in ASMFix to insert fixes. */
public abstract class FixInserter extends AdviceAdapter {

    /** The fix that this visitor needs to insert. */
    protected final ASMFix fix;
    /** The class visitor that visited this method and created this MethodVisitor. */
    protected final FixInserterClassVisitor classVisitor;
    /** The target method name. */
    public final String methodName;
    /** The target method return type. */
    public final Type methodType;
    /** If the target method is static. */
    public final boolean isStatic;

    protected FixInserter(MethodVisitor mv, int access, String name, String descriptor, ASMFix fix,
        FixInserterClassVisitor classVisitor) {
        super(Opcodes.ASM5, mv, access, name, descriptor);
        this.fix = fix;
        this.classVisitor = classVisitor;
        isStatic = (access & Opcodes.ACC_STATIC) != 0;
        this.methodName = name;
        this.methodType = Type.getMethodType(descriptor);
    }

    /**
     * Inserts the fix into the bytecode.
     */
    protected final void insertFix() {
        if (!classVisitor.visitingFix) {
            classVisitor.visitingFix = true;
            fix.insertFix(this);
            classVisitor.visitingFix = false;
        }
    }

    /**
     * Inserts the fix when visiting the start of the method.
     */
    public static class OnEnterInserter extends FixInserter {

        public OnEnterInserter(MethodVisitor mv, int access, String name, String desc, ASMFix fix,
            FixInserterClassVisitor cv) {
            super(mv, access, name, desc, fix, cv);
        }

        /**
         * Inserts the fix into the bytecode.
         */
        @Override
        protected void onMethodEnter() {
            insertFix();
        }

    }

    /**
     * Inserts the fix when visiting every exit from the method, except for exiting through throwing an error
     * (configurable).
     */
    public static class OnExitInserter extends FixInserter {

        public boolean insertOnThrows;

        public OnExitInserter(MethodVisitor mv, int access, String name, String desc, ASMFix fix,
            FixInserterClassVisitor cv) {
            super(mv, access, name, desc, fix, cv);
            this.insertOnThrows = false;
        }

        public OnExitInserter(MethodVisitor mv, int access, String name, String desc, ASMFix fix,
            FixInserterClassVisitor cv, boolean insertOnThrows) {
            super(mv, access, name, desc, fix, cv);
            this.insertOnThrows = insertOnThrows;
        }

        /**
         * Inserts the fix into the bytecode.
         */
        @Override
        protected void onMethodExit(int opcode) {
            if (opcode != Opcodes.ATHROW || this.insertOnThrows) {
                insertFix();
            }
        }
    }

    /**
     * Inserts the fix when visiting the specific line number.
     */
    public static class OnLineNumberInserter extends FixInserter {

        private int lineNumber;

        public OnLineNumberInserter(MethodVisitor mv, int access, String name, String desc, ASMFix fix,
            FixInserterClassVisitor cv, int lineNumber) {
            super(mv, access, name, desc, fix, cv);
            this.lineNumber = lineNumber;
        }

        /**
         * Inserts the fix into the bytecode.
         */
        @Override
        public void visitLineNumber(int lineVisiting, Label start) {
            super.visitLineNumber(lineVisiting, start);
            if (lineVisiting == this.lineNumber) {
                insertFix();
            }
        }
    }
}
