package xonin.backhand.coremod.transformers;

import org.objectweb.asm.tree.MethodNode;

import xonin.backhand.api.core.BackhandTranslator;

public final class PlayerControllerMPTransformer extends TransformerMethodProcess {

    // Todo: This is converted and clashing with mixin but replaceInventoryArrayAccess is needed?
    public PlayerControllerMPTransformer() {
        super(
            "net.minecraft.client.multiplayer.PlayerControllerMP",
            "func_78769_a",
            new String[] { "sendUseItem",
                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Z" });
    }

    private String entityPlayerClassName;
    private String playerInventoryFieldName;

    @Override
    void processMethod(MethodNode method) {
        sendPatchLog("sendUseItem");
        replaceInventoryArrayAccess(method, entityPlayerClassName, playerInventoryFieldName, 11, 13);
    }

    @Override
    void setupMappings() {
        super.setupMappings();
        entityPlayerClassName = BackhandTranslator.getMapedClassName("entity.player.EntityPlayer");
        playerInventoryFieldName = "field_71071_by!inventory";
    }

}
