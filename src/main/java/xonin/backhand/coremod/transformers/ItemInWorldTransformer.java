package xonin.backhand.coremod.transformers;

import java.util.Iterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import xonin.backhand.api.core.BackhandTranslator;

public final class ItemInWorldTransformer extends TransformerMethodProcess {

    public ItemInWorldTransformer() {
        super(
            "net.minecraft.server.management.ItemInWorldManager",
            "func_73085_a",
            new String[] { "tryUseItem",
                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Z" });
    }

    private String entityPlayerClassName;
    private String inventoryPlayerClassName;
    private String itemStackClassName;

    private String playerInventoryFieldName;
    private String mainInventoryArrayFieldName;

    private String setInventorySlotMethodName;
    private String setInventorySlotMethodDesc;

    @Override
    void processMethod(MethodNode mn) {
        sendPatchLog("tryUseItem");
        replaceInventoryArrayAccess(mn, entityPlayerClassName, playerInventoryFieldName, 5, 7);

        InsnList newList = new InsnList();

        Iterator<AbstractInsnNode> it = mn.instructions.iterator();

        while (it.hasNext()) {
            AbstractInsnNode node = it.next();

            if (node instanceof FieldInsnNode && ((FieldInsnNode) node).owner.equals(inventoryPlayerClassName)
                && (((FieldInsnNode) node).name.equals(mainInventoryArrayFieldName.split("!")[0])
                    || ((FieldInsnNode) node).name.equals(mainInventoryArrayFieldName.split("!")[1]))
                && ((FieldInsnNode) node).desc.equals("[L" + itemStackClassName + ";")) {

                // Do Nothing
            } else if (node.getOpcode() == AASTORE) {
                newList.add(
                    new MethodInsnNode(
                        INVOKEVIRTUAL,
                        inventoryPlayerClassName,
                        setInventorySlotMethodName,
                        setInventorySlotMethodDesc));
            } else {
                newList.add(node);
            }
        }

        mn.instructions = newList;
    }

    @Override
    void setupMappings() {
        super.setupMappings();
        entityPlayerClassName = BackhandTranslator.getMapedClassName("entity.player.EntityPlayer");
        inventoryPlayerClassName = BackhandTranslator.getMapedClassName("entity.player.InventoryPlayer");
        itemStackClassName = BackhandTranslator.getMapedClassName("item.ItemStack");

        playerInventoryFieldName = "field_71071_by!inventory";
        mainInventoryArrayFieldName = "field_70462_a!mainInventory";
        setInventorySlotMethodName = BackhandTranslator.getMapedMethodName("func_70299_a", "setInventorySlotContents");
        setInventorySlotMethodDesc = "(IL" + itemStackClassName + ";)V";
    }

}