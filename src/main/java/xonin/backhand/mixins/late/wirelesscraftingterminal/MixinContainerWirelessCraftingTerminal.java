package xonin.backhand.mixins.late.wirelesscraftingterminal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;

@Mixin(ContainerWirelessCraftingTerminal.class)
public class MixinContainerWirelessCraftingTerminal extends AEBaseContainer {

    public MixinContainerWirelessCraftingTerminal(InventoryPlayer ip, TileEntity myTile, IPart myPart) {
        super(ip, myTile, myPart);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
    private int backhand$init(int original, EntityPlayer player, InventoryPlayer inventoryPlayer) {
        return inventoryPlayer.getSizeInventory() - 1;
    }
}
