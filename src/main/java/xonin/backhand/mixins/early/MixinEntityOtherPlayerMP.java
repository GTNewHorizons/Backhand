package xonin.backhand.mixins.early;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.authlib.GameProfile;

@Mixin(EntityOtherPlayerMP.class)
public abstract class MixinEntityOtherPlayerMP extends AbstractClientPlayer {

    @Shadow
    private boolean isItemInUse;

    private MixinEntityOtherPlayerMP(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
    }

    // @Inject(
    // method = "onUpdate",
    // cancellable = true,
    // at = @At(
    // value = "FIELD",
    // target = "Lnet/minecraft/client/entity/EntityOtherPlayerMP;isItemInUse:Z",
    // shift = At.Shift.BEFORE,
    // ordinal = 0))
    // private void battlegear2$isItemInUseHook(CallbackInfo ci) {
    // if (BattlegearUtils.isPlayerInBattlemode(this)) {
    // ci.cancel();
    // ItemStack itemStack = this.getCurrentEquippedItem();
    // ItemStack offhand = ((IInventoryPlayerBattle) this.inventory).battlegear2$getCurrentOffhandWeapon();
    // if (offhand != null && BattlegearUtils.usagePriorAttack(offhand, this, true)) {
    // itemStack = offhand;
    // }
    // if (!this.isItemInUse && this.isEating() && itemStack != null) {
    // this.setItemInUse(itemStack, itemStack.getMaxItemUseDuration());
    // this.isItemInUse = true;
    // } else if (this.isItemInUse && !this.isEating()) {
    // this.clearItemInUse();
    // this.isItemInUse = false;
    // }
    // }
    // }

}
