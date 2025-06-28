package xonin.backhand.mixins.early.minecraft;

import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
import static xonin.backhand.api.core.EnumHand.*;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.EnumHand;
import xonin.backhand.client.utils.BackhandRenderHelper;
import xonin.backhand.hooks.TorchHandler;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public EntityClientPlayerMP thePlayer;

    @Shadow
    public WorldClient theWorld;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Shadow
    public PlayerControllerMP playerController;

    @Shadow
    private int rightClickDelayTimer;

    @Shadow
    @Final
    private static Logger logger;

    @Shadow
    public EntityRenderer entityRenderer;

    @Shadow
    public EffectRenderer effectRenderer;

    /**
     * @author Lyft
     * @reason Offhand support
     *         Don't change this methods visibility despite what mixin debug says.
     *         Some mods AT this and changing the visibility will break them.
     */
    @Overwrite
    public void func_147121_ag() {
        rightClickDelayTimer = 4;
        if (objectMouseOver == null) {
            logger.warn("Null returned as 'hitResult', this shouldn't happen!");
            return;
        }

        ItemStack mainHandItem = MAIN_HAND.getItem(thePlayer);
        ItemStack offhandItem = OFF_HAND.getItem(thePlayer);
        EnumHand[] hands = backhand$doesOffhandNeedPriority(mainHandItem, offhandItem) ? HANDS_REV : HANDS;
        for (EnumHand hand : hands) {
            ItemStack handStack = hand == MAIN_HAND ? mainHandItem : offhandItem;

            if (hand == OFF_HAND) {
                if (objectMouseOver.typeOfHit != MovingObjectType.ENTITY
                    && !TorchHandler.shouldPlace(mainHandItem, offhandItem)) {
                    continue;
                }
            }

            boolean stopCheck = switch (objectMouseOver.typeOfHit) {
                case ENTITY -> backhand$useRightClick(
                    hand,
                    handStack,
                    stack -> playerController.interactWithEntitySendPacket(thePlayer, objectMouseOver.entityHit));
                case BLOCK -> {
                    int x = objectMouseOver.blockX;
                    int y = objectMouseOver.blockY;
                    int z = objectMouseOver.blockZ;
                    yield !theWorld.getBlock(x, y, z)
                        .isAir(theWorld, x, y, z)
                        && backhand$useRightClick(hand, handStack, stack -> backhand$rightClickBlock(stack, x, y, z));
                }
                default -> false;
            };

            if (stopCheck) return;

            if (backhand$useRightClick(hand, handStack, this::backhand$rightClickItem)) {
                return;
            }
        }
    }

    @ModifyExpressionValue(method = "func_147112_ai", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int backhand$adjustSlotOffset(int original) {
        return original - 1;
    }

    @Unique
    private boolean backhand$useRightClick(EnumHand hand, ItemStack handStack, Predicate<ItemStack> action) {
        if (hand == MAIN_HAND) {
            return action.test(handStack);
        } else {
            return BackhandUtils.useOffhandItem(thePlayer, () -> action.test(handStack));
        }
    }

    @Unique
    private boolean backhand$rightClickItem(ItemStack stack) {
        PlayerInteractEvent useItemEvent = new PlayerInteractEvent(thePlayer, RIGHT_CLICK_AIR, 0, 0, 0, -1, theWorld);
        if (!MinecraftForge.EVENT_BUS.post(useItemEvent) && stack != null
            && (playerController.sendUseItem(thePlayer, theWorld, stack) || thePlayer.getItemInUse() != null)) {
            backhand$resetEquippedProgress();
            return true;
        }

        return false;
    }

    @Unique
    private void backhand$resetEquippedProgress() {
        if (BackhandUtils.isUsingOffhand(thePlayer)) {
            BackhandRenderHelper.itemRenderer.resetEquippedProgress();
        } else {
            entityRenderer.itemRenderer.resetEquippedProgress();
        }
    }

    @Unique
    private boolean backhand$rightClickBlock(ItemStack stack, int x, int y, int z) {
        int originalSize = stack != null ? stack.stackSize : 0;
        PlayerInteractEvent useItemEvent = new PlayerInteractEvent(
            thePlayer,
            RIGHT_CLICK_BLOCK,
            x,
            y,
            z,
            objectMouseOver.sideHit,
            theWorld);
        if (!MinecraftForge.EVENT_BUS.post(useItemEvent) && playerController
            .onPlayerRightClick(thePlayer, theWorld, stack, x, y, z, objectMouseOver.sideHit, objectMouseOver.hitVec)) {
            thePlayer.swingItem();
            return true;
        }

        if (stack != null) {
            if (stack.stackSize == 0) {
                thePlayer.inventory.setInventorySlotContents(thePlayer.inventory.currentItem, null);
            } else if (stack.stackSize != originalSize) {
                backhand$resetEquippedProgress();
            }
        }

        return false;
    }

    @SuppressWarnings("ConstantConditions")
    @Unique
    private boolean backhand$doesOffhandNeedPriority(ItemStack mainHand, ItemStack offhand) {
        if (mainHand == null || offhand == null) return false;

        // spotless:off
        for (Class<?> clazz : BackhandUtils.offhandPriorityItems) {
            if (clazz.isAssignableFrom(offhand.getItem().getClass())) {
                return true;
            }
        }

        for (Class<?> clazz : BackhandUtils.deprioritizedMainhand) {
            if (clazz.isAssignableFrom(mainHand.getItem().getClass())) {
                return true;
            }
        }
        // spotless:on

        return false;
    }

}
