package xonin.backhand.mixins.early.minecraft;

import java.util.Objects;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistrySimple;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.IOffhandInventory;
import xonin.backhand.packet.BackhandPacketHandler;
import xonin.backhand.packet.OffhandAnimationPacket;
import xonin.backhand.packet.OffhandSyncOffhandUse;

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

    @Unique
    private ItemStack backhand$getLegalStack(ItemStack stack) {
        if (stack == null || stack.stackSize == 0) return null;
        return stack;
    }
}
