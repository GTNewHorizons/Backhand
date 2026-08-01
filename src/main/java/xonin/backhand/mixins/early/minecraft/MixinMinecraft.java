package xonin.backhand.mixins.early.minecraft;

import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
import static xonin.backhand.api.core.EnumHand.*;

import java.util.function.Predicate;

import net.minecraft.block.material.Material;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.EnumHand;
import xonin.backhand.client.utils.BackhandRenderHelper;
import xonin.backhand.hooks.TorchHandler;
import xonin.backhand.utils.BackhandConfig;

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

    @Unique
    private int backhand$breakBlockTimer = 0;
    @Unique
    private boolean backhand$blockRightClickCanceled;

    /**
     * @author Lyft
     * @reason Offhand support
     *         Don't change this methods visibility despite what mixin debug says.
     *         Some mods AT this and changing the visibility will break them.
     */
    @SuppressWarnings("visibility")
    @Overwrite
    public void func_147121_ag() {
        rightClickDelayTimer = 4;
        if (objectMouseOver == null) {
            logger.warn("Null returned as 'hitResult', this shouldn't happen!");
            return;
        }

        // This flag is only meaningful right after a block-phase attempt in backhand$tryHand below, so reset
        // it here to make sure a cancellation from a previous right click can never leak into this one.
        backhand$blockRightClickCanceled = false;

        ItemStack mainHandItem = MAIN_HAND.getItem(thePlayer);
        ItemStack offhandItem = OFF_HAND.getItem(thePlayer);
        EnumHand[] hands = backhand$doesOffhandNeedPriority(mainHandItem, offhandItem) ? HANDS_REV : HANDS;

        int x = objectMouseOver.blockX;
        int y = objectMouseOver.blockY;
        int z = objectMouseOver.blockZ;
        boolean blockHit = objectMouseOver.typeOfHit == MovingObjectType.BLOCK && !theWorld.getBlock(x, y, z)
            .isAir(theWorld, x, y, z);
        boolean entityHit = objectMouseOver.typeOfHit == MovingObjectType.ENTITY;

        // Give one hand every chance it has (block/entity interaction, then its own item-use action) before
        // moving on to the other hand, instead of resolving the block interaction for both hands first and only
        // then looking at item-use actions. That older ordering let an offhand block/torch placement win over a
        // mainhand item (backpack, travel staff, ...) whose action only exists as an item-use (onItemRightClick),
        // since the mainhand never got a chance to try it before the offhand already placed its block and
        // returned. This mirrors vanilla's own single-hand fallthrough (block phase, then item-use phase).
        for (EnumHand hand : hands) {
            ItemStack handStack = hand == MAIN_HAND ? mainHandItem : offhandItem;

            if (backhand$tryHand(hand, handStack, mainHandItem, offhandItem, blockHit, entityHit, x, y, z)) {
                return;
            }

            if (blockHit && backhand$blockRightClickCanceled) {
                // The interaction was cancelled (e.g. by a protection mod) - don't let the other hand attempt
                // anything either, same as vanilla would simply stop for its single hand.
                return;
            }
        }

        if (BackhandConfig.OffhandAttack && objectMouseOver.typeOfHit == MovingObjectType.ENTITY
            && offhandItem != null) {
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                rightClickDelayTimer = 10;
                thePlayer.swingItem();
                playerController.attackEntity(thePlayer, objectMouseOver.entityHit);
            });
            return;
        }

        if (BackhandConfig.OffhandBreakBlocks && blockHit
            && offhandItem != null
            && BackhandUtils.isItemTool(offhandItem.getItem())) {
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                backhand$breakBlockTimer = 5;
                playerController.clickBlock(x, y, z, objectMouseOver.sideHit);
            });
        }
    }

    /**
     * Tries every action a single hand can perform for this right click: placing/using on the targeted block or
     * entity first, then falling back to the item's own right-click action (eating, opening a backpack, bucket
     * fill/empty, teleporting with a travel staff, ...). Returns true as soon as one of them succeeds.
     */
    @Unique
    private boolean backhand$tryHand(EnumHand hand, ItemStack handStack, ItemStack mainHandItem, ItemStack offhandItem,
        boolean blockHit, boolean entityHit, int x, int y, int z) {
        if (blockHit) {
            // The "don't place a torch/block from the offhand" option only ever gates this block-placement
            // attempt - the offhand item can still act through its own item-use action further below.
            boolean skipOffhandPlacement = hand == OFF_HAND && !TorchHandler.shouldPlace(mainHandItem, offhandItem);
            if (!skipOffhandPlacement) {
                if (backhand$useRightClick(hand, handStack, stack -> backhand$rightClickBlock(stack, x, y, z))) {
                    return true;
                }
                if (backhand$blockRightClickCanceled) {
                    // Still let this hand's item act once, but the caller stops entirely afterwards instead of
                    // letting the other hand try the block too.
                    if (handStack != null) {
                        backhand$useRightClick(hand, handStack, this::backhand$rightClickItem);
                    }
                    return false;
                }
            }
        } else if (entityHit) {
            if (backhand$useRightClick(
                hand,
                handStack,
                stack -> playerController.interactWithEntitySendPacket(thePlayer, objectMouseOver.entityHit))) {
                return true;
            }
        }

        return backhand$useRightClick(hand, handStack, this::backhand$rightClickItem);
    }

    @WrapWithCondition(
        method = "func_147115_a",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;resetBlockRemoving()V"))
    private boolean backhand$pauseReset(PlayerControllerMP instance) {
        if (backhand$breakBlockTimer > 0) {
            backhand$breakBlockTimer--;
            return false;
        }
        return true;
    }

    @Inject(method = "func_147115_a", at = @At(value = "HEAD"))
    private void backhand$breakBlockOffhand(boolean leftClick, CallbackInfo ci) {
        if (backhand$breakBlockTimer > 0) {
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                int i = objectMouseOver.blockX;
                int j = objectMouseOver.blockY;
                int k = objectMouseOver.blockZ;

                if (theWorld.getBlock(i, j, k)
                    .getMaterial() != Material.air) {
                    playerController.onPlayerDamageBlock(i, j, k, objectMouseOver.sideHit);

                    if (thePlayer.isCurrentToolAdventureModeExempt(i, j, k)) {
                        effectRenderer.addBlockHitEffects(i, j, k, objectMouseOver);
                        thePlayer.swingItem();
                    }
                }
            });
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
        backhand$blockRightClickCanceled = MinecraftForge.EVENT_BUS.post(useItemEvent);
        if (!backhand$blockRightClickCanceled && playerController
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
