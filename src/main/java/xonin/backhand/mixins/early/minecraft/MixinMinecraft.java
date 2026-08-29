package xonin.backhand.mixins.early.minecraft;

import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
import static xonin.backhand.api.core.EnumHand.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
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

import xonin.backhand.Backhand;
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

    @Shadow
    public GameSettings gameSettings;

    @Unique
    private int backhand$breakBlockTimer = 0;
    @Unique
    private boolean backhand$blockRightClickCanceled;
    @Unique
    private boolean backhand$suppressNextOffhandBreakSwing;

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

        // Reset here so a cancellation from a previous right click can't leak into this one.
        backhand$blockRightClickCanceled = false;

        ItemStack mainHandItem = MAIN_HAND.getItem(thePlayer);
        ItemStack offhandItem = OFF_HAND.getItem(thePlayer);
        EnumHand[] hands = !Backhand.doesMainhandUseStopOffhandFallback(mainHandItem)
            && backhand$doesOffhandNeedPriority(mainHandItem, offhandItem) ? HANDS_REV : HANDS;

        int x = objectMouseOver.blockX;
        int y = objectMouseOver.blockY;
        int z = objectMouseOver.blockZ;
        boolean blockHit = objectMouseOver.typeOfHit == MovingObjectType.BLOCK && !theWorld.getBlock(x, y, z)
            .isAir(theWorld, x, y, z);
        boolean entityHit = objectMouseOver.typeOfHit == MovingObjectType.ENTITY;

        // Give one hand every chance (block/entity, then item-use) before the other, mirroring vanilla's
        // single-hand fallthrough instead of resolving both hands' block phase first.
        for (EnumHand hand : hands) {
            ItemStack handStack = hand == MAIN_HAND ? mainHandItem : offhandItem;

            if (backhand$tryHand(hand, handStack, mainHandItem, offhandItem, blockHit, entityHit, x, y, z)) {
                return;
            }

            if (blockHit && backhand$blockRightClickCanceled) {
                // Cancelled (e.g. by a protection mod) - stop entirely, same as vanilla does for its single hand.
                return;
            }

            if (hand == MAIN_HAND && Backhand.doesMainhandUseStopOffhandFallback(handStack)) {
                // This tool always does something we can't observe (e.g. a server-authoritative action), so never
                // let the offhand fire "for real" too on the assumption that the mainhand did nothing.
                return;
            }
        }

        if (BackhandConfig.OffhandAttack && objectMouseOver.typeOfHit == MovingObjectType.ENTITY
            && backhand$canUseOffhand(mainHandItem, offhandItem)) {
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                rightClickDelayTimer = 10;
                thePlayer.swingItem();
                playerController.attackEntity(thePlayer, objectMouseOver.entityHit);
            });
            return;
        }

        if (BackhandConfig.OffhandBreakBlocks && blockHit && backhand$canBreakWithOffhand(mainHandItem, offhandItem)) {
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                backhand$breakBlockTimer = 5;
                backhand$suppressNextOffhandBreakSwing = true;
                playerController.clickBlock(x, y, z, objectMouseOver.sideHit);
            });
        }
    }

    /**
     * Tries every action one hand can perform for this right click (block/entity, then item-use), returning true
     * as soon as one succeeds.
     */
    @Unique
    private boolean backhand$tryHand(EnumHand hand, ItemStack handStack, ItemStack mainHandItem, ItemStack offhandItem,
        boolean blockHit, boolean entityHit, int x, int y, int z) {
        if (blockHit) {
            // Only gates block placement - the offhand item can still act via its own item-use action below.
            boolean skipOffhandPlacement = hand == OFF_HAND && !TorchHandler.shouldPlace(mainHandItem, offhandItem);
            if (!skipOffhandPlacement) {
                if (backhand$useRightClick(hand, handStack, stack -> backhand$rightClickBlock(stack, x, y, z))) {
                    return true;
                }
                if (backhand$blockRightClickCanceled) {
                    // Still let this hand's item act once, but the other hand won't get to try the block.
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
                        if (!backhand$suppressNextOffhandBreakSwing) {
                            thePlayer.swingItem();
                        }
                    }
                    backhand$suppressNextOffhandBreakSwing = false;
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
        if (MinecraftForge.EVENT_BUS.post(useItemEvent) || stack == null) {
            return false;
        }

        // sendUseItem()'s stack-diff check for success is accurate in survival (e.g. food declining to eat when
        // full correctly returns the stack unchanged), so only fall back to the coarser "item can act at all"
        // check in creative, where items are never consumed and that diff is always unreliable.
        boolean handled = playerController.sendUseItem(thePlayer, theWorld, stack) || thePlayer.getItemInUse() != null
            || (thePlayer.capabilities.isCreativeMode && stack.getItem() != null
                && backhand$hasRightClickAction(stack.getItem()));
        if (handled) {
            backhand$resetEquippedProgress();
        }

        return handled;
    }

    @Unique
    private static final Map<Class<?>, Boolean> backhand$rightClickOverrideCache = new HashMap<>();

    @Unique
    private static final Class<?>[] RIGHT_CLICK_PARAM_TYPES = { ItemStack.class, World.class, EntityPlayer.class };

    // Resolved lazily (rather than as an eager static field) so a lookup failure can't throw out of this mixin's
    // share of Minecraft's <clinit>.
    @Unique
    private static boolean backhand$onItemRightClickNameResolved = false;

    @Unique
    private static String backhand$onItemRightClickName;

    // The dev environment resolves vanilla methods to their MCP name, a reobfuscated production jar to the SRG
    // name (e.g. func_77659_a) - try both once instead of hardcoding either.
    @Unique
    private static String backhand$resolveOnItemRightClickName() {
        for (String name : new String[] { "func_77659_a", "onItemRightClick" }) {
            try {
                Item.class.getMethod(name, RIGHT_CLICK_PARAM_TYPES);
                return name;
            } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }

    // Looked up by name instead of matching any method with this signature, since that also matched onEaten
    // (same erased signature, different method).
    @Unique
    private static boolean backhand$hasRightClickAction(Item item) {
        if (!backhand$onItemRightClickNameResolved) {
            backhand$onItemRightClickName = backhand$resolveOnItemRightClickName();
            backhand$onItemRightClickNameResolved = true;
        }
        if (backhand$onItemRightClickName == null) {
            return false;
        }

        return backhand$rightClickOverrideCache.computeIfAbsent(item.getClass(), clazz -> {
            try {
                return clazz.getMethod(backhand$onItemRightClickName, RIGHT_CLICK_PARAM_TYPES)
                    .getDeclaringClass() != Item.class;
            } catch (NoSuchMethodException e) {
                return false;
            }
        });
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
