package xonin.backhand.mixins.early.minecraft;

import static xonin.backhand.api.core.EnumHand.MAIN_HAND;

import java.util.Objects;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistrySimple;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.IOffhandInventory;
import xonin.backhand.hooks.containerfix.IContainerHook;
import xonin.backhand.packet.BackhandPacketHandler;
import xonin.backhand.packet.OffhandAnimationPacket;
import xonin.backhand.packet.OffhandSyncOffhandUse;
import xonin.backhand.utils.BackhandConfig;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IBackhandPlayer {

    @Shadow
    private ItemStack itemInUse;
    @Shadow
    public InventoryPlayer inventory;
    @Unique
    private float backhand$offHandSwingProgress = 0F;
    @Unique
    private float backhand$prevOffHandSwingProgress = 0F;
    @Unique
    private int backhand$offHandSwingProgressInt = 0;
    @Unique
    private boolean backhand$isOffHandSwingInProgress = false;
    @Unique
    private boolean backhand$isOffhandItemInUs = false;
    @Unique
    private int backhand$mainhandSlot;
    @Unique
    private int backhand$lastAttackTargetId = -1;
    @Unique
    private int backhand$lastAttackTick = Integer.MIN_VALUE;
    @Unique
    private boolean backhand$lastAttackWasOffhand = false;

    private MixinEntityPlayer(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @WrapMethod(method = "onItemUseFinish")
    private void backhand$onItemUseFinishEnd(Operation<Void> original) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (Objects.equals(itemInUse, BackhandUtils.getOffhandItem(player))) {
            BackhandUtils.useOffhandItem(player, () -> original.call());
        } else {
            original.call();
        }
    }

    @ModifyExpressionValue(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    private ItemStack backhand$onUpdate$getCurrentItem(ItemStack original) {
        ItemStack itemStack = BackhandUtils.getOffhandItem((EntityPlayer) (Object) this);
        if (itemInUse == itemStack) {
            return itemStack;
        }

        return original;
    }

    @Inject(method = "setItemInUse", at = @At(value = "TAIL"))
    private void backhand$setItemInUse(ItemStack p_71008_1_, int p_71008_2_, CallbackInfo ci) {
        if (Objects.equals(p_71008_1_, BackhandUtils.getOffhandItem((EntityPlayer) (Object) this))) {
            backhand$updateOffhandUse(true);
        } else if (isOffhandItemInUse()) {
            backhand$updateOffhandUse(false);
        }
    }

    @Inject(method = "clearItemInUse", at = @At(value = "TAIL"))
    private void backhand$clearOffhand(CallbackInfo ci) {
        if (isOffhandItemInUse()) {
            backhand$updateOffhandUse(false);
        }
    }

    @Inject(method = "updateEntityActionState", at = @At(value = "TAIL"))
    private void backhand$updateOffhandSwingProgress(CallbackInfo ci) {
        this.backhand$prevOffHandSwingProgress = this.backhand$offHandSwingProgress;
        int var1 = this.getArmSwingAnimationEnd();
        if (this.backhand$isOffHandSwingInProgress) {
            ++this.backhand$offHandSwingProgressInt;
            if (this.backhand$offHandSwingProgressInt >= var1) {
                this.backhand$offHandSwingProgressInt = 0;
                this.backhand$isOffHandSwingInProgress = false;
            }
        } else {
            this.backhand$offHandSwingProgressInt = 0;
        }

        this.backhand$offHandSwingProgress = (float) this.backhand$offHandSwingProgressInt / (float) var1;
    }

    @WrapWithCondition(
        method = "stopUsingItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;onPlayerStoppedUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;I)V"))
    private boolean backhand$stopUsingItem(ItemStack stack, World world, EntityPlayer player, int p_77974_3_) {
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (offhand != null && !isUsingOffhand()
            && stack.getItemUseAction() == EnumAction.bow
            && ((RegistrySimple) BlockDispenser.dispenseBehaviorRegistry).containsKey(offhand.getItem())) {
            // Swap the offhand item into the first available slot to give it usage priority
            int slot = (inventory.currentItem == 0) ? 1 : 0;
            ItemStack swappedStack = player.inventory.mainInventory[slot];
            inventory.mainInventory[slot] = offhand;
            BackhandUtils.setPlayerOffhandItem(player, swappedStack);
            stack.onPlayerStoppedUsing(world, player, p_77974_3_);
            player.inventory.mainInventory[slot] = backhand$getLegalStack(swappedStack);
            BackhandUtils.setPlayerOffhandItem(player, backhand$getLegalStack(offhand));
            return false;
        }
        return true;
    }

    // Backhand Containerfix
    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "net/minecraftforge/common/ForgeHooks.canInteractWith(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/inventory/Container;)Z",
            remap = false))
    private boolean backhand$canInteractWith(EntityPlayer player, Container openContainer) {
        if (((IContainerHook) openContainer).backhand$wasOpenedWithOffhand()) {
            int currentItem = BackhandUtils.swapToOffhand(player);
            boolean retValue = ForgeHooks.canInteractWith(player, openContainer);
            BackhandUtils.swapBack(player, currentItem);
            return retValue;
        }
        return ForgeHooks.canInteractWith(player, openContainer);
    }

    @WrapOperation(
        method = "attackTargetEntityWithCurrentItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;getAttributeValue()D"))
    private double backhand$getOffhandDamage(IAttributeInstance instance, Operation<Double> original) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (BackhandUtils.isUsingOffhand(player)) {
            ItemStack mainHand = MAIN_HAND.getItem(player);
            ItemStack offHand = BackhandUtils.getOffhandItem(player);
            backhand$refreshAttributes(mainHand, offHand);
            double result = original.call(instance);
            backhand$refreshAttributes(offHand, mainHand);
            return result;
        }
        return original.call(instance);
    }

    @WrapOperation(
        method = "attackTargetEntityWithCurrentItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean backhand$adjustDualWieldAttackIFrames(Entity targetEntity, DamageSource source, float amount,
        Operation<Boolean> original) {
        if (!BackhandConfig.OffhandAttack) {
            return original.call(targetEntity, source, amount);
        }

        EntityPlayer player = (EntityPlayer) (Object) this;
        boolean usingOffhand = BackhandUtils.isUsingOffhand(player);
        boolean dualWieldCombo = backhand$isDualWieldCombo(targetEntity, usingOffhand);

        if (dualWieldCombo) {
            backhand$capTargetIFrames((EntityLivingBase) targetEntity);
        }

        boolean result = original.call(targetEntity, source, amount);
        if (result) {
            backhand$lastAttackTargetId = targetEntity.getEntityId();
            backhand$lastAttackTick = ticksExisted;
            backhand$lastAttackWasOffhand = usingOffhand;

            if (dualWieldCombo) {
                backhand$capTargetIFrames((EntityLivingBase) targetEntity);
            }
        }
        return result;
    }

    @Unique
    private void backhand$refreshAttributes(ItemStack oldItem, ItemStack newItem) {
        if (oldItem != null) {
            getAttributeMap().removeAttributeModifiers(oldItem.getAttributeModifiers());
        }

        if (newItem != null) {
            getAttributeMap().applyAttributeModifiers(newItem.getAttributeModifiers());
        }
    }

    @Unique
    private boolean backhand$isDualWieldCombo(Entity targetEntity, boolean usingOffhand) {
        if (!(targetEntity instanceof EntityLivingBase target)) return false;
        int dualWieldIFrames = backhand$getDualWieldAttackIFrames();
        if (dualWieldIFrames >= 20 || targetEntity.getEntityId() != backhand$lastAttackTargetId
            || usingOffhand == backhand$lastAttackWasOffhand) {
            return false;
        }

        int ticksSinceLastAttack = ticksExisted - backhand$lastAttackTick;
        return ticksSinceLastAttack >= 0 && ticksSinceLastAttack <= target.maxHurtResistantTime;
    }

    @Unique
    private int backhand$getDualWieldAttackIFrames() {
        int dualWieldIFrames = BackhandConfig.DualWieldAttackIFrames;
        if (dualWieldIFrames < 0) return 0;
        if (dualWieldIFrames > 20) return 20;
        return dualWieldIFrames;
    }

    @Unique
    private void backhand$capTargetIFrames(EntityLivingBase target) {
        int dualWieldIFrames = backhand$getDualWieldAttackIFrames();
        if (target.hurtResistantTime > dualWieldIFrames) {
            target.hurtResistantTime = dualWieldIFrames;
        }
    }

    @Unique
    private void backhand$updateOffhandUse(boolean state) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        setOffhandItemInUse(state);

        if (!worldObj.isRemote) {
            BackhandPacketHandler.sendPacketToAllTracking(player, new OffhandSyncOffhandUse(player, state));
        }
    }

    @Override
    public void swingItem() {
        if (isUsingOffhand()) {
            this.swingOffItem();
        } else {
            super.swingItem();
        }
    }

    @Override
    public void swingOffItem() {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack stack = BackhandUtils.getOffhandItem(player);
        if (stack != null && stack.getItem() != null
            && BackhandUtils.useOffhandItem(
                player,
                false,
                () -> stack.getItem()
                    .onEntitySwing(player, stack))) {
            return;
        }

        if (!this.backhand$isOffHandSwingInProgress
            || this.backhand$offHandSwingProgressInt >= this.getArmSwingAnimationEnd() / 2
            || this.backhand$offHandSwingProgressInt < 0) {
            this.backhand$offHandSwingProgressInt = -1;
            this.backhand$isOffHandSwingInProgress = true;

            if (!worldObj.isRemote) {
                BackhandPacketHandler.sendPacketToAllTracking(player, new OffhandAnimationPacket(player));
            }
        }
    }

    @Override
    public float getOffSwingProgress(float frame) {
        float diff = this.backhand$offHandSwingProgress - this.backhand$prevOffHandSwingProgress;
        if (diff < 0.0F) {
            ++diff;
        }

        return this.backhand$prevOffHandSwingProgress + diff * frame;
    }

    @Override
    public void setOffhandItemInUse(boolean usingOffhand) {
        this.backhand$isOffhandItemInUs = usingOffhand;
    }

    @Override
    public boolean isOffhandItemInUse() {
        return this.backhand$isOffhandItemInUs;
    }

    @Override
    public boolean isUsingOffhand() {
        return inventory.currentItem == ((IOffhandInventory) inventory).backhand$getOffhandSlot();
    }

    @Override
    public void setMainhandSlot(int slot) {
        backhand$mainhandSlot = slot;
    }

    @Override
    public ItemStack getMainhandItem() {
        return inventory.getStackInSlot(backhand$mainhandSlot);
    }

    @Unique
    private ItemStack backhand$getLegalStack(ItemStack stack) {
        if (stack == null || stack.stackSize == 0) return null;
        return stack;
    }
}
