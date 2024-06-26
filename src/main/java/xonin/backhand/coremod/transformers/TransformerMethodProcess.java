package xonin.backhand.coremod.transformers;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public abstract class TransformerMethodProcess extends TransformerBase {

    private final String meth;
    private final String devName;
    private final String desc;
    private String methName;

    public TransformerMethodProcess(String classPath, String method, String[] devs) {
        super(classPath);
        this.meth = method;
        this.devName = devs[0];
        this.desc = devs[1];
    }

    @Override
    boolean processMethods(List<MethodNode> methods) {
        for (MethodNode method : methods) {
            if ((method.name.equals(methName.split("!")[0]) || method.name.equals(methName.split("!")[1])) && method.desc.equals(desc)) {
                processMethod(method);
                return true;
            }
        }
        return false;
    }

    abstract void processMethod(MethodNode method);

    @Override
    boolean processFields(List<FieldNode> fields) {
        return true;
    }

    @Override
    void setupMappings() {
        methName = meth + "!" + devName;
    }

}
