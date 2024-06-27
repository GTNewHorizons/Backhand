package net.tclproject.mysteriumlib.asm.core;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Custom ClassVisitor that visits the class of a target method. Returns custom FixInserters as method visitors and
 * creates new methods inside the visited class if necessary.
 */
public class FixInserterClassVisitor extends ClassVisitor {

    /** Fixes we have to insert. */
    List<ASMFix> fixes;

    /** Fixes we have already inserted. */
    List<ASMFix> insertedFixes = new ArrayList<>(1);

    /** If a method visitor created by this class visitor is currently in the process of inserting a fix. */
    boolean visitingFix;

    /** The TargetClassTransformer that has created this instance. */
    TargetClassTransformer transformer;

    /** Name of the superclass of the class this class is visiting. */
    String superName;

    public FixInserterClassVisitor(TargetClassTransformer transformer, ClassWriter cv, List<ASMFix> fixs) {
        super(Opcodes.ASM5, cv);
        this.fixes = fixs;
        this.transformer = transformer;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.superName = superName;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * Visits a method of the class but instead of returning a normal MethodVisitor returns a FixInserter from the set
     * factory, if the method is to be fixed.
     *
     * @param access     the method's access flags (see Opcodes). This parameter also indicates if the method is
     *                   synthetic and/or deprecated.
     * @param name       the method's name.
     * @param desc       the method's descriptor (see Type).
     * @param signature  the method's signature. May be null if the method parameters, return type and exceptions do not
     *                   use generic types.
     * @param exceptions the internal names of the method's exception classes (see getInternalName). May be null.
     * @return an object to visit the byte code of the method, or null if this class visitor is not interested in
     *         visiting the code of this method.
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        for (ASMFix fix : fixes) {
            if (isTheTarget(fix, name, desc) && !insertedFixes.contains(fix)) { // if it's the target and it has not
                                                                                // been inserted already
                mv = fix.getInjectorFactory()
                    .createFixInserter(mv, access, name, desc, fix, this); // create a new fix inserter for this method
                insertedFixes.add(fix); // set it so we know we have already inserted this fix
            }
        }
        return mv;
    }

    /**
     * Visits the end of the class. This method, which is the last one to be called,
     * is used to inform the visitor that all the fields and methods of the class have been visited.
     * This custom implementation creates new methods in the target class if there are fixes telling us to do so.
     */
    @Override
    public void visitEnd() {
        for (ASMFix fix : fixes) {
            if (fix.getCreateMethod() && !insertedFixes.contains(fix)) { // if the method is to be created and we
                                                                         // haven't done so already
                fix.createMethod(this); // create the said method
            }
        }
        super.visitEnd();
    }

    // Returns true if the method is the target method of the fix.
    protected boolean isTheTarget(ASMFix fix, String name, String desc) {
        return fix.isTheTarget(name, desc);
    }
}
