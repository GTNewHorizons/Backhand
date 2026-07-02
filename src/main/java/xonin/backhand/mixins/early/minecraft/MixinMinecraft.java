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
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.IFluidContainerItem;

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
import xonin.backhand.hooks.RightClickItemTracker;
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

    @Shadow
    public GameSettings gameSettings;

    @Unique
    private int backhand$breakBlockTimer = 0;

    @Unique
    private boolean backhand$suppressNextOffhandBreakSwing = false;

    @Unique
    private boolean backhand$wasUseItemKeyDown = false;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void backhand$resetUseItemKeyState(CallbackInfo ci) {
        if (gameSettings == null || !gameSettings.keyBindUseItem.getIsKeyPressed()) {
            backhand$wasUseItemKeyDown = false;
        }
    }

    /**
     * @author Lyft
     * @reason Offhand support
     *         Don't change this methods visibility despite what mixin debug says.
     *         Some mods AT this and changing the visibility will break them.
     */
    @Overwrite
    public void func_147121_ag() {
        boolean useItemKeyDown = gameSettings.keyBindUseItem.getIsKeyPressed();
        boolean repeatedUseClick = backhand$wasUseItemKeyDown && useItemKeyDown;
        backhand$wasUseItemKeyDown = useItemKeyDown;
        rightClickDelayTimer = 4;
        if (objectMouseOver == null) {
            logger.warn("Null returned as 'hitResult', this shouldn't happen!");
            return;
        }

        ItemStack mainHandItem = MAIN_HAND.getItem(thePlayer);
        ItemStack offhandItem = OFF_HAND.getItem(thePlayer);
        EnumHand[] hands = backhand$doesOffhandNeedPriority(mainHandItem, offhandItem) ? HANDS_REV : HANDS;

        int x = objectMouseOver.blockX;
        int y = objectMouseOver.blockY;
        int z = objectMouseOver.blockZ;
        boolean blockHit = objectMouseOver.typeOfHit == MovingObjectType.BLOCK && !theWorld.getBlock(x, y, z)
            .isAir(theWorld, x, y, z);
        boolean entityHit = objectMouseOver.typeOfHit == MovingObjectType.ENTITY;
        boolean suppressOffhandFallback = backhand$shouldSuppressOffhandFallback(repeatedUseClick);

        // Make sure no item gets used twice
        boolean mainHandUsedFluid = false;
        boolean offhandUsedFluid = false;
        for (EnumHand hand : hands) {
            ItemStack handStack = hand == MAIN_HAND ? mainHandItem : offhandItem;

            if (hand == OFF_HAND) {
                if (blockHit && !TorchHandler.shouldPlace(mainHandItem, offhandItem)) {
                    continue;
                }
            }

            if (blockHit) {
                if (backhand$useRightClick(hand, handStack, stack -> backhand$rightClickBlock(stack, x, y, z))) {
                    return;
                }
            } else if (entityHit) {
                if (backhand$useRightClick(
                    hand,
                    handStack,
                    stack -> playerController.interactWithEntitySendPacket(thePlayer, objectMouseOver.entityHit))) {
                    return;
                }
            }

            // Note: The bucket/IFluidContainerItem fix did not work, since fluid placement
            // is handled in backhand$rightClickBlock, not in backhand$rightClickItem
            if (handStack != null && handStack.getItem() != null
                && (handStack.getItem() instanceof ItemBucket || handStack.getItem() instanceof IFluidContainerItem)) {
                if (backhand$useRightClick(hand, handStack, stack -> backhand$rightClickItem(hand, stack))) {
                    return;
                }
                if (hand == MAIN_HAND) {
                    mainHandUsedFluid = true;
                } else {
                    offhandUsedFluid = true;
                }
            }
        }

        // process the potential entity/block placements first before trying the item right click actions
        for (EnumHand hand : hands) {
            ItemStack handStack;
            if (hand == MAIN_HAND) {
                if (mainHandUsedFluid) continue;
                handStack = mainHandItem;
            } else {
                if (offhandUsedFluid) continue;
                handStack = offhandItem;
            }
            if (backhand$useRightClick(hand, handStack, stack -> backhand$rightClickItem(hand, stack))) {
                return;
            }
        }

        if (!suppressOffhandFallback && BackhandConfig.OffhandBreakBlocks
            && blockHit
            && backhand$canBreakWithOffhand(mainHandItem, offhandItem)) {
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                backhand$breakBlockTimer = 5;
                backhand$suppressNextOffhandBreakSwing = true;
                thePlayer.swingItem();
                playerController.clickBlock(x, y, z, objectMouseOver.sideHit);
            });
            return;
        }

        if (!suppressOffhandFallback && BackhandConfig.OffhandAttack
            && backhand$canUseOffhand(mainHandItem, offhandItem)
            && (entityHit || backhand$shouldSwingOffhandUseFallback(offhandItem))) {
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                thePlayer.swingItem();
                if (entityHit) {
                    playerController.attackEntity(thePlayer, objectMouseOver.entityHit);
                }
            });
        }
    }

    @WrapWithCondition(
        method = "func_147115_a",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;resetBlockRemoving()V"))
    private boolean backhand$pauseReset(PlayerControllerMP instance) {
        if (backhand$breakBlockTimer > 0) {
            if (!gameSettings.keyBindUseItem.getIsKeyPressed()) {
                backhand$breakBlockTimer = 0;
                backhand$suppressNextOffhandBreakSwing = false;
                return true;
            }
            backhand$breakBlockTimer--;
            return false;
        }
        return true;
    }

    @Inject(method = "func_147115_a", at = @At(value = "HEAD"))
    private void backhand$breakBlockOffhand(boolean leftClick, CallbackInfo ci) {
        if (backhand$breakBlockTimer > 0) {
            if (!gameSettings.keyBindUseItem.getIsKeyPressed()) {
                backhand$breakBlockTimer = 0;
                backhand$suppressNextOffhandBreakSwing = false;
                return;
            }
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                int i = objectMouseOver.blockX;
                int j = objectMouseOver.blockY;
                int k = objectMouseOver.blockZ;

                if (theWorld.getBlock(i, j, k)
                    .getMaterial() != Material.air) {
                    playerController.onPlayerDamageBlock(i, j, k, objectMouseOver.sideHit);

                    if (thePlayer.isCurrentToolAdventureModeExempt(i, j, k)) {
                        effectRenderer.addBlockHitEffects(i, j, k, objectMouseOver);
                        if (backhand$suppressNextOffhandBreakSwing) {
                            backhand$suppressNextOffhandBreakSwing = false;
                        } else {
                            thePlayer.swingItem();
                        }
                    } else {
                        backhand$suppressNextOffhandBreakSwing = false;
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
    private boolean backhand$canUseOffhand(ItemStack mainHandItem, ItemStack offhandItem) {
        return offhandItem != null || mainHandItem == null && BackhandConfig.EmptyOffhand;
    }

    @Unique
    private boolean backhand$canBreakWithOffhand(ItemStack mainHandItem, ItemStack offhandItem) {
        return offhandItem != null ? BackhandUtils.isItemTool(offhandItem.getItem())
            : mainHandItem == null && BackhandConfig.EmptyOffhand;
    }

    @Unique
    private boolean backhand$shouldSwingOffhandUseFallback(ItemStack offhandItem) {
        return offhandItem == null || BackhandUtils.isItemTool(offhandItem.getItem());
    }

    @Unique
    private boolean backhand$shouldSuppressOffhandFallback(boolean repeatedUseClick) {
        if (repeatedUseClick) return true;
        return BackhandConfig.MainhandUseBlocksOffhandFallback && thePlayer.getItemInUse() != null;
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
    private boolean backhand$rightClickItem(EnumHand hand, ItemStack stack) {
        PlayerInteractEvent useItemEvent = new PlayerInteractEvent(thePlayer, RIGHT_CLICK_AIR, 0, 0, 0, -1, theWorld);
        if (MinecraftForge.EVENT_BUS.post(useItemEvent) || stack == null) {
            return false;
        }

        boolean trackingMainhand = hand == MAIN_HAND && BackhandConfig.MainhandUseBlocksOffhandFallback;
        if (trackingMainhand) {
            RightClickItemTracker.beginMainhandUse();
        }
        try {
            if (playerController.sendUseItem(thePlayer, theWorld, stack) || thePlayer.getItemInUse() != null
                || trackingMainhand && RightClickItemTracker.didCustomItemRightClickHandle()
                    && !(stack.getItem() instanceof ItemSword)) {
                backhand$resetEquippedProgress();
                return true;
            }
        } finally {
            if (trackingMainhand) {
                RightClickItemTracker.endMainhandUse();
            }
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
