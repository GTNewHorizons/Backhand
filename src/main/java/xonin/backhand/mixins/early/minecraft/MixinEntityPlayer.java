package xonin.backhand.mixins.early.minecraft;

import java.util.Objects;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import xonin.backhand.Backhand;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.IOffhandInventory;
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
    private boolean backhand$isUsingOffhand = false;

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

    @Inject(
        method = "setItemInUse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setEating(Z)V"))
    private void backhand$onUpdate$getCurrentItem(ItemStack p_71008_1_, int p_71008_2_, CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (Objects.equals(p_71008_1_, BackhandUtils.getOffhandItem(player))) {
            Backhand.packetHandler
                .sendPacketToAllTracking(player, new OffhandSyncOffhandUse(player, true).generatePacket());
            setUsingOffhand(true);
        } else if (isUsingOffhand()) {
            Backhand.packetHandler
                .sendPacketToAllTracking(player, new OffhandSyncOffhandUse(player, false).generatePacket());
            setUsingOffhand(false);
        }
    }

    @Inject(
        method = "clearItemInUse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setEating(Z)V"))
    private void backhand$onUpdate$getCurrentItem(CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (isUsingOffhand()) {
            Backhand.packetHandler
                .sendPacketToAllTracking(player, new OffhandSyncOffhandUse(player, false).generatePacket());
            setUsingOffhand(false);
        }
    }

    @Override
    public void swingItem() {
        if (inventory.currentItem == IOffhandInventory.OFFHAND_HOTBAR_SLOT) {
            this.swingOffItem();
        } else {
            super.swingItem();
        }
    }

    @Override
    public void swingOffItem() {
        if (!this.backhand$isOffHandSwingInProgress
            || this.backhand$offHandSwingProgressInt >= this.getArmSwingAnimationEnd() / 2
            || this.backhand$offHandSwingProgressInt < 0) {
            this.backhand$offHandSwingProgressInt = -1;
            this.backhand$isOffHandSwingInProgress = true;
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
    protected void updateArmSwingProgress() {
        super.updateArmSwingProgress();
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

    @Override
    public void setUsingOffhand(boolean usingOffhand) {
        this.backhand$isUsingOffhand = usingOffhand;
    }

    @Override
    public boolean isUsingOffhand() {
        return this.backhand$isUsingOffhand;
    }
}
