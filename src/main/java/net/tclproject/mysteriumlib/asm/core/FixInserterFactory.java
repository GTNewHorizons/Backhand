package net.tclproject.mysteriumlib.asm.core;

import org.objectweb.asm.MethodVisitor;

/**
 * Factory, specifying the type of the fix inserter. Factually, from the choice of the factory depends
 * in what part of the code the fix will be inserted.
 * By deault, there are two types of inserters: OnEnter, which inserts the fix to the start of the method,
 * and OnExit, which inserts it on every exit.
 */
public abstract class FixInserterFactory {

    /**
     * The method AdviceAdapter#visitInsn() is a weird thing. For some reason, the calling of the next MethodVisitor
     * is done after the logic and not before, like in all the other cases. That's why for MethodExit the priority
     * of fixes is the reverse.
     */
    protected boolean priorityReversed = false;

    /** Creates a fix inserter object. A fix inserter will insert the fix using methods in ASMFix. */
    abstract FixInserter createFixInserter(MethodVisitor mv, int access, String name, String desc, ASMFix fix,
        FixInserterClassVisitor cv);

    /** Creates an inserter that will insert fixes at the start of a method. */
    public static class OnEnter extends FixInserterFactory {

        public static final OnEnter INSTANCE = new OnEnter();

        public OnEnter() {}

        @Override
        public FixInserter createFixInserter(MethodVisitor mv, int access, String name, String desc, ASMFix fix,
            FixInserterClassVisitor cv) {
            return new FixInserter.OnEnterInserter(mv, access, name, desc, fix, cv);
        }

    }

    /** Creates an inserter that will insert fixes at exits from a method. */
    public static class OnExit extends FixInserterFactory {

        public static final OnExit INSTANCE = new OnExit();
        public boolean insertOnThrows;

        public OnExit() {
            priorityReversed = true;
            insertOnThrows = false;
        }

        public OnExit(boolean insertOnThrows) {
            this.insertOnThrows = insertOnThrows;
            priorityReversed = true;
        }

        @Override
        public FixInserter createFixInserter(MethodVisitor mv, int access, String name, String desc, ASMFix fix,
            FixInserterClassVisitor cv) {
            return new FixInserter.OnExitInserter(mv, access, name, desc, fix, cv, insertOnThrows);
        }
    }

    /** Creates an inserter that will insert fixes at a specific line number in a method. */
    public static class OnLineNumber extends FixInserterFactory {

        private int lineNumber;

        public OnLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        @Override
        public FixInserter createFixInserter(MethodVisitor mv, int access, String name, String desc, ASMFix fix,
            FixInserterClassVisitor cv) {
            return new FixInserter.OnLineNumberInserter(mv, access, name, desc, fix, cv, lineNumber);
        }
    }
}
