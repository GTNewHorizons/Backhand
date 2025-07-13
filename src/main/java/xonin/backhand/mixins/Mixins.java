package xonin.backhand.mixins;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // spotless:off
    MINECRAFT(new MixinBuilder()
        .addCommonMixins(
            "minecraft.MixinEntityPlayer",
            "minecraft.MixinNetHandlerPlayServer",
            "minecraft.MixinItemBow",
            "minecraft.MixinEntityFishHook",
            "minecraft.MixinInventoryPlayer",
            "minecraft.MixinContainerPlayer",
            "minecraft.MixinItemStack",
            "minecraft.MixinEntityLivingBase",
            "minecraft.MixinItemSword",
            "minecraft.MixinEntityAITempt")
        .addClientMixins(
            "minecraft.MixinEntityOtherPlayerMP",
            "minecraft.MixinEntityPlayerClient",
            "minecraft.MixinItemRenderer",
            "minecraft.MixinModelBiped",
            "minecraft.MixinNetHandlerPlayClient",
            "minecraft.MixinMinecraft",
            "minecraft.MixinGuiInventory",
            "minecraft.MixinEntityRenderer",
            "minecraft.MixinGuiContainerCreative",
            "minecraft.MixinRenderPlayer")
        .setPhase(Phase.EARLY)),
    GC_FIX_ARMOR_SLOT(new MixinBuilder("Fix GC boot slot clashing with offhand slot")
        .addCommonMixins("galacticraft.MixinContainerExtendedInventory")
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetedMod.GALACTICRAFT)),
    DE_FIX_ARMOR_SLOT(new MixinBuilder("Fix DE boot slot clashing with offhand slot")
        .addClientMixins("draconicevolution.MixinGuiToolConfig")
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetedMod.DRACONIC_EVOLUTION)),
    TFC_FIX_BACK_SLOT(new MixinBuilder("Fix TFC back slot clashing with offhand slot")
        .addCommonMixins(
            "terrafirmacraft.MixinPlayerInventory",
            "terrafirmacraft.MixinEntityLivingHandler",
            "terrafirmacraft.MixinContainerPlayerTFC")
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetedMod.TERRAFIRMACRAFT)),
    TFCPLUS_FIX_BACK_SLOT(new MixinBuilder("Fix TFC+ back slot clashing with offhand slot")
        .addCommonMixins(
            "terrafirmacraft.MixinPlayerInventory",
            "terrafirmacraft.MixinEntityLivingHandler",
            "terrafirmacraft.MixinContainerPlayerTFC")
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetedMod.TERRAFIRMACRAFT_PLUS)),
    TINKERS_TOOL_RIGHT_CLICK(
        new MixinBuilder("Disable Tinker's default right click on harvest tools to place item in nearby slot")
            .addCommonMixins("ticon.MixinHarvestTool")
            .setPhase(Phase.LATE)
            .addRequiredMod(TargetedMod.TINKERS_CONSTRUCT)),
    WCT_FIX_ARMOR_SLOT(new MixinBuilder("Fix WirelessCraftingTerminal's armor slot")
        .addCommonMixins("wirelesscraftingterminal.MixinContainerWirelessCraftingTerminal")
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetedMod.AE2_WIRELESS_CRAFTING_TERMINAL)),
    BIBLIOCRAFT_FIX_ARMOR_SLOT(
        new MixinBuilder("Fix Bibliocraft's armor slots")
            .addCommonMixins("bibliocraft.MixinContainerArmor")
            .setPhase(Phase.LATE)
            .addRequiredMod(TargetedMod.BIBLIOCRAFT));
    // spotless:on

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @NotNull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
