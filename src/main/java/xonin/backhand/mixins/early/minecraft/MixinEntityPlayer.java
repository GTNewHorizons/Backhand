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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.IOffhandInventory;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IBackhandPlayer {

    @Shadow
    private ItemStack itemInUse;
    @Shadow
    private int itemInUseCount;
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
}
