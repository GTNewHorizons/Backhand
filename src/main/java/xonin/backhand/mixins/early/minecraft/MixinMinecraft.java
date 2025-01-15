package xonin.backhand.mixins.early.minecraft;

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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.IOffhandInventory;
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
    public int rightClickDelayTimer;

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
     */
    @Overwrite
    public void func_147121_ag() {
        rightClickDelayTimer = 4;
        ItemStack mainHandItem = thePlayer.inventory.getCurrentItem();
        ItemStack offhandItem = BackhandUtils.getOffhandItem(thePlayer);
        boolean useMainhand = true;
        boolean useOffhand = true;

        if (objectMouseOver == null) {
            logger.warn("Null returned as \'hitResult\', this shouldn\'t happen!");
        } else {
            switch (objectMouseOver.typeOfHit) {
                case ENTITY:
                    if (playerController.interactWithEntitySendPacket(thePlayer, objectMouseOver.entityHit)) {
                        useMainhand = false;
                    } else if (BackhandUtils.useOffhandItem(
                        thePlayer,
                        () -> playerController.interactWithEntitySendPacket(thePlayer, objectMouseOver.entityHit))) {
                            useOffhand = false;
                        }

                    break;
                case BLOCK:
                    int x = objectMouseOver.blockX;
                    int y = objectMouseOver.blockY;
                    int z = objectMouseOver.blockZ;

                    if (!theWorld.getBlock(x, y, z)
                        .isAir(theWorld, x, y, z)) {
                        int l = mainHandItem != null ? mainHandItem.stackSize : 0;

                        boolean result = !net.minecraftforge.event.ForgeEventFactory
                            .onPlayerInteract(
                                thePlayer,
                                net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
                                x,
                                y,
                                z,
                                objectMouseOver.sideHit,
                                theWorld)
                            .isCanceled();
                        if (result && playerController.onPlayerRightClick(
                            thePlayer,
                            theWorld,
                            mainHandItem,
                            x,
                            y,
                            z,
                            objectMouseOver.sideHit,
                            objectMouseOver.hitVec)) {
                            useMainhand = false;
                            thePlayer.swingItem();
                        } else if (offhandItem != null) {
                            PlayerInteractEvent event = new PlayerInteractEvent(
                                thePlayer,
                                PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
                                x,
                                y,
                                z,
                                objectMouseOver.sideHit,
                                theWorld);

                            if (!MinecraftForge.EVENT_BUS.post(event) && BackhandUtils.useOffhandItem(
                                thePlayer,
                                () -> playerController.onPlayerRightClick(
                                    thePlayer,
                                    theWorld,
                                    offhandItem,
                                    x,
                                    y,
                                    z,
                                    objectMouseOver.sideHit,
                                    objectMouseOver.hitVec))) {
                                useOffhand = false;
                                ((IBackhandPlayer) thePlayer).swingOffItem();
                            }
                        }

                        if (mainHandItem != null) {
                            if (mainHandItem.stackSize == 0) {
                                thePlayer.setCurrentItemOrArmor(0, null);
                            } else if (mainHandItem.stackSize != l || playerController.isInCreativeMode()) {
                                entityRenderer.itemRenderer.resetEquippedProgress();
                            }
                        }

                        if (offhandItem != null) {
                            if (offhandItem.stackSize == 0) {
                                thePlayer.inventory
                                    .setInventorySlotContents(IOffhandInventory.OFFHAND_HOTBAR_SLOT, null);
                            }
                        }
                    }
            }

            if (useMainhand) {
                ItemStack itemstack1 = thePlayer.inventory.getCurrentItem();

                boolean result = !net.minecraftforge.event.ForgeEventFactory
                    .onPlayerInteract(
                        thePlayer,
                        net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
                        0,
                        0,
                        0,
                        -1,
                        theWorld)
                    .isCanceled();
                if (result && itemstack1 != null && playerController.sendUseItem(thePlayer, theWorld, itemstack1)) {
                    entityRenderer.itemRenderer.resetEquippedProgress2();
                }
            }

            if (useOffhand && offhandItem != null && thePlayer.getItemInUse() == null) {
                boolean trySecondaryAction = !BackhandUtils.useOffhandItem(thePlayer, () -> {
                    PlayerInteractEvent useItemEvent = new PlayerInteractEvent(
                        thePlayer,
                        PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
                        0,
                        0,
                        -1,
                        0,
                        theWorld);
                    if (!MinecraftForge.EVENT_BUS.post(useItemEvent)) {
                        playerController.sendUseItem(thePlayer, theWorld, offhandItem);
                        return thePlayer.getItemInUse() != null;
                    }
                    return false;
                });

                if (trySecondaryAction && offhandItem.getItemUseAction() == EnumAction.none) {
                    switch (objectMouseOver.typeOfHit) {
                        case ENTITY -> BackhandUtils.useOffhandItem(thePlayer, () -> {
                            thePlayer.swingItem();
                            playerController.attackEntity(thePlayer, objectMouseOver.entityHit);
                        });
                        case BLOCK -> {
                            if (BackhandConfig.OffhandBreakBlocks) {
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
                    }
                }
            }
        }
    }

    @WrapWithCondition(
        method = "func_147115_a",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;resetBlockRemoving()V"))
    protected boolean backhand$pauseReset(PlayerControllerMP instance) {
        if (backhand$breakBlockTimer > 0) {
            backhand$breakBlockTimer--;
            return false;
        }
        return true;
    }

    @Inject(method = "func_147115_a", at = @At(value = "HEAD"))
    protected void backhand$breakBlockOffhand(boolean leftClick, CallbackInfo ci) {
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
}
