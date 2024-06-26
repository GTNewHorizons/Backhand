package xonin.backhand.coremod.transformers;

import xonin.backhand.api.core.BackhandTranslator;
import org.objectweb.asm.tree.MethodNode;

public final class EntityAIControlledByPlayerTransformer extends TransformerMethodProcess {

    public EntityAIControlledByPlayerTransformer() {
        super("net.minecraft.entity.ai.EntityAIControlledByPlayer", "func_75246_d", new String[]{"updateTask", SIMPLEST_METHOD_DESC});
    }

    private String entityPlayerClassName;
    private String playerInventoryFieldName;

    @Override
    void processMethod(MethodNode method) {
        sendPatchLog("updateTask");
        replaceInventoryArrayAccess(method, entityPlayerClassName, playerInventoryFieldName, 8, 23);
    }

    @Override
    void setupMappings() {
        super.setupMappings();
        entityPlayerClassName = BackhandTranslator.getMapedClassName("entity.player.EntityPlayer");
        playerInventoryFieldName = "field_71071_by!inventory";
    }
}
