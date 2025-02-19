package xonin.backhand.mixins.early.minecraft;

import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;

import java.util.function.Predicate;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.item.EnumAction;
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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.client.utils.BackhandRenderHelper;
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

        boolean continueUsage = switch (objectMouseOver.typeOfHit) {
            case ENTITY -> backhand$useRightClick(
                stack -> playerController.interactWithEntitySendPacket(thePlayer, objectMouseOver.entityHit));
            case BLOCK -> {
                int x = objectMouseOver.blockX;
                int y = objectMouseOver.blockY;
                int z = objectMouseOver.blockZ;
                yield !theWorld.getBlock(x, y, z)
                    .isAir(theWorld, x, y, z)
                    && backhand$useRightClick(stack -> backhand$rightClickBlock(stack, x, y, z));
            }
            case MISS -> true;
        };

        if (!continueUsage) return;

        if (objectMouseOver.typeOfHit == MovingObjectType.ENTITY && BackhandConfig.OffhandAttack) {
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                rightClickDelayTimer = 10;
                thePlayer.swingItem();
                playerController.attackEntity(thePlayer, objectMouseOver.entityHit);
            });
            return;
        } else if (!backhand$useRightClick(this::backhand$rightClickItem)) {
            return;
        }

        ItemStack offhandItem = BackhandUtils.getOffhandItem(thePlayer);
        if (BackhandConfig.OffhandBreakBlocks && objectMouseOver.typeOfHit == MovingObjectType.BLOCK
            && offhandItem != null
            && offhandItem.getItemUseAction() == EnumAction.none) {
            BackhandUtils.useOffhandItem(thePlayer, () -> {
                backhand$breakBlockTimer = 5;
                playerController.clickBlock(
                    objectMouseOver.blockX,
                    objectMouseOver.blockY,
                    objectMouseOver.blockZ,
                    objectMouseOver.sideHit);
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

    @ModifyConstant(method = "func_147112_ai", constant = @Constant(intValue = 9))
    private int backhand$adjustSlotOffset(int constant) {
        return 10;
    }

    @Unique
    private boolean backhand$useRightClick(Predicate<ItemStack> action) {
        ItemStack mainHandItem = thePlayer.inventory.getCurrentItem();
        ItemStack offhandItem = BackhandUtils.getOffhandItem(thePlayer);
        boolean result;

        if (offhandItem != null && backhand$doesOffhandNeedPriority(mainHandItem, offhandItem)) {
            result = BackhandUtils.useOffhandItem(thePlayer, () -> action.test(offhandItem));
            if (!result) {
                result = action.test(mainHandItem);
            }
        } else {
            result = action.test(mainHandItem);
            if (!result && offhandItem != null) {
                result = BackhandUtils.useOffhandItem(thePlayer, () -> action.test(offhandItem));
            }
        }

        return !result;
    }

    @Unique
    private boolean backhand$rightClickItem(ItemStack stack) {
        PlayerInteractEvent useItemEvent = new PlayerInteractEvent(thePlayer, RIGHT_CLICK_AIR, 0, 0, 0, -1, theWorld);
        if (!MinecraftForge.EVENT_BUS.post(useItemEvent) && stack != null
            && playerController.sendUseItem(thePlayer, theWorld, stack)) {
            backhand$resetEquippedProgress();
        }

        return thePlayer.getItemInUse() != null;
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
        boolean result = false;
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
            result = true;
        }

        if (stack != null) {
            if (stack.stackSize == 0) {
                thePlayer.inventory.setInventorySlotContents(thePlayer.inventory.currentItem, null);
            } else if (stack.stackSize != originalSize) {
                backhand$resetEquippedProgress();
            }
        }

        return result;
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
