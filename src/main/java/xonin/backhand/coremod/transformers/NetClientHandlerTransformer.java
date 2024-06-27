package xonin.backhand.coremod.transformers;

import java.util.List;
import java.util.ListIterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class NetClientHandlerTransformer extends TransformerBase {

    public NetClientHandlerTransformer() {
        super("net.minecraft.client.network.NetHandlerPlayClient");
    }

    private String netClientHandlerHandleBlockItemSwitchMethodName;
    private String netClientHandlerHandleBlockItemSwitchMethodDesc;

    @Override
    boolean processMethods(List<MethodNode> methods) {
        int found = 0;
        for (MethodNode method : methods) {
            if ((method.name.equals(netClientHandlerHandleBlockItemSwitchMethodName.split("!")[0])
                || method.name.equals(netClientHandlerHandleBlockItemSwitchMethodName.split("!")[1]))
                && method.desc.equals(netClientHandlerHandleBlockItemSwitchMethodDesc)) {
                sendPatchLog("handleHeldItemChange");

                ListIterator<AbstractInsnNode> insn = method.instructions.iterator();
                InsnList newList = new InsnList();

                while (insn.hasNext()) {

                    AbstractInsnNode nextNode = insn.next();

                    if (nextNode instanceof JumpInsnNode && nextNode.getOpcode() == IFLT) {
                        LabelNode label = ((JumpInsnNode) nextNode).label;
                        newList.add(
                            new MethodInsnNode(
                                INVOKESTATIC,
                                "xonin/backhand/api/core/InventoryPlayerBackhand",
                                "isValidSwitch",
                                "(I)Z"));
                        newList.add(new JumpInsnNode(IFEQ, label));// "if equal" branch

                        found++;
                        nextNode = insn.next();
                        while (insn.hasNext() && !(nextNode instanceof JumpInsnNode)
                            && nextNode.getOpcode() != IF_ICMPGE) {
                            nextNode = insn.next();// continue till "if int greater than or equal to" branch
                        }

                    } else {
                        newList.add(nextNode);
                    }

                }

                method.instructions = newList;
            }
        }
        return found == 1;
    }

    @Override
    boolean processFields(List<FieldNode> fields) {
        return true;
    }

    @Override
    void setupMappings() {
        netClientHandlerHandleBlockItemSwitchMethodName = "func_147257_a!handleHeldItemChange";
        netClientHandlerHandleBlockItemSwitchMethodDesc = "(Lnet/minecraft/network/play/server/S09PacketHeldItemChange;)V";
    }
}
