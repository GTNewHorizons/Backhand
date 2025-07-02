package xonin.backhand.mixins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import xonin.backhand.coremod.BackhandLoadingPlugin;

public enum Mixins {

    MINECRAFT(new Builder("Shared MC Mixins")
        .addMixinClasses(
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
        .setPhase(Phase.EARLY)
        .setSide(Side.BOTH)
        .addTargetedMod(TargetedMod.VANILLA)),
    MINECRAFT_CLIENT(new Builder("Client MC Mixins")
        .addMixinClasses(
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
        .setPhase(Phase.EARLY)
        .setSide(Side.CLIENT)
        .addTargetedMod(TargetedMod.VANILLA)),
    GC_FIX_ARMOR_SLOT(new Builder("Fix GC boot slot clashing with offhand slot")
        .addMixinClasses("galacticraft.MixinContainerExtendedInventory")
        .setPhase(Phase.LATE)
        .setSide(Side.BOTH)
        .addTargetedMod(TargetedMod.GALACTICRAFT)),
    DE_FIX_ARMOR_SLOT(new Builder("Fix DE boot slot clashing with offhand slot")
        .addMixinClasses("draconicevolution.MixinGuiToolConfig")
        .setPhase(Phase.LATE)
        .setSide(Side.CLIENT)
        .addTargetedMod(TargetedMod.DRACONIOCEVOLUTION)),
    TFC_FIX_BACK_SLOT(new Builder("Fix TFC back slot clashing with offhand slot")
        .addMixinClasses(
            "terrafirmacraft.MixinPlayerInventory",
            "terrafirmacraft.MixinEntityLivingHandler",
            "terrafirmacraft.MixinContainerPlayerTFC")
        .setPhase(Phase.LATE)
        .setSide(Side.BOTH)
        .addTargetedMod(TargetedMod.TFC)),
    TFCPLUS_FIX_BACK_SLOT(new Builder("Fix TFC+ back slot clashing with offhand slot")
        .addMixinClasses(
            "terrafirmacraft.MixinPlayerInventory",
            "terrafirmacraft.MixinEntityLivingHandler",
            "terrafirmacraft.MixinContainerPlayerTFC")
        .setPhase(Phase.LATE)
        .setSide(Side.BOTH)
        .addTargetedMod(TargetedMod.TFCPLUS)),
    TINKERS_TOOL_RIGHT_CLICK(
        new Builder("Disable Tinker's default right click on harvest tools to place item in nearby slot")
            .addMixinClasses("ticon.MixinHarvestTool")
            .setPhase(Phase.LATE)
            .setSide(Side.BOTH)
            .addTargetedMod(TargetedMod.TINKERS)),
    WCT_FIX_ARMOR_SLOT(new Builder("Fix WirelessCraftingTerminal's armor slot")
        .addMixinClasses("wirelesscraftingterminal.MixinContainerWirelessCraftingTerminal")
        .setPhase(Phase.LATE)
        .setSide(Side.BOTH)
        .addTargetedMod(TargetedMod.WCT)),
    BIBLIOCRAFT_FIX_ARMOR_SLOT(
        new Builder("Fix Bibliocraft's armor slots").addMixinClasses("bibliocraft.MixinContainerArmor")
            .setPhase(Phase.LATE)
            .setSide(Side.BOTH)
            .addTargetedMod(TargetedMod.BIBLIOCRAFT)),;

    private final List<String> mixinClasses;
    private final Supplier<Boolean> applyIf;
    private final Phase phase;
    private final Side side;
    private final List<TargetedMod> targetedMods;
    private final List<TargetedMod> excludedMods;

    Mixins(Builder builder) {
        this.mixinClasses = builder.mixinClasses;
        this.applyIf = builder.applyIf;
        this.side = builder.side;
        this.targetedMods = builder.targetedMods;
        this.excludedMods = builder.excludedMods;
        this.phase = builder.phase;
        if (this.targetedMods.isEmpty()) {
            throw new RuntimeException("No targeted mods specified for " + this.name());
        }
        if (this.applyIf == null) {
            throw new RuntimeException("No ApplyIf function specified for " + this.name());
        }
    }

    public static List<String> getEarlyMixins(Set<String> loadedCoreMods) {
        // This may be possible to handle differently or fix.
        final List<String> mixins = new ArrayList<>();
        final List<String> notLoading = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Phase.EARLY) {
                if (mixin.shouldLoad(loadedCoreMods, Collections.emptySet())) {
                    mixins.addAll(mixin.mixinClasses);
                } else {
                    notLoading.addAll(mixin.mixinClasses);
                }
            }
        }
        BackhandLoadingPlugin.LOGGER.info("Not loading the following EARLY mixins: {}", notLoading);
        return mixins;
    }

    public static List<String> getLateMixins(Set<String> loadedMods) {
        final List<String> mixins = new ArrayList<>();
        final List<String> notLoading = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Phase.LATE) {
                if (mixin.shouldLoad(Collections.emptySet(), loadedMods)) {
                    mixins.addAll(mixin.mixinClasses);
                } else {
                    notLoading.addAll(mixin.mixinClasses);
                }
            }
        }
        BackhandLoadingPlugin.LOGGER.info("Not loading the following LATE mixins: {}", notLoading.toString());
        return mixins;
    }

    private boolean shouldLoadSide() {
        return side == Side.BOTH || (side == Side.SERVER && FMLLaunchHandler.side()
            .isServer())
            || (side == Side.CLIENT && FMLLaunchHandler.side()
                .isClient());
    }

    private boolean allModsLoaded(List<TargetedMod> targetedMods, Set<String> loadedCoreMods, Set<String> loadedMods) {
        if (targetedMods.isEmpty()) return false;

        for (TargetedMod target : targetedMods) {
            if (target == TargetedMod.VANILLA) continue;

            // Check coremod first
            if (!loadedCoreMods.isEmpty() && target.coreModClass != null
                && !loadedCoreMods.contains(target.coreModClass)) return false;
            else if (!loadedMods.isEmpty() && target.modId != null && !loadedMods.contains(target.modId)) return false;
        }

        return true;
    }

    private boolean noModsLoaded(List<TargetedMod> targetedMods, Set<String> loadedCoreMods, Set<String> loadedMods) {
        if (targetedMods.isEmpty()) return true;

        for (TargetedMod target : targetedMods) {
            if (target == TargetedMod.VANILLA) continue;

            // Check coremod first
            if (!loadedCoreMods.isEmpty() && target.coreModClass != null
                && loadedCoreMods.contains(target.coreModClass)) return false;
            else if (!loadedMods.isEmpty() && target.modId != null && loadedMods.contains(target.modId)) return false;
        }

        return true;
    }

    private boolean shouldLoad(Set<String> loadedCoreMods, Set<String> loadedMods) {
        return (shouldLoadSide() && applyIf.get()
            && allModsLoaded(targetedMods, loadedCoreMods, loadedMods)
            && noModsLoaded(excludedMods, loadedCoreMods, loadedMods));
    }

    private static class Builder {

        private final List<String> mixinClasses = new ArrayList<>();
        private Supplier<Boolean> applyIf = () -> true;
        private Side side = Side.BOTH;
        private Phase phase = Phase.LATE;
        private final List<TargetedMod> targetedMods = new ArrayList<>();
        private final List<TargetedMod> excludedMods = new ArrayList<>();

        public Builder(@SuppressWarnings("unused") String description) {}

        public Builder addMixinClasses(String... mixinClasses) {
            this.mixinClasses.addAll(Arrays.asList(mixinClasses));
            return this;
        }

        public Builder setPhase(Phase phase) {
            this.phase = phase;
            return this;
        }

        public Builder setSide(Side side) {
            this.side = side;
            return this;
        }

        public Builder setApplyIf(Supplier<Boolean> applyIf) {
            this.applyIf = applyIf;
            return this;
        }

        public Builder addTargetedMod(TargetedMod mod) {
            this.targetedMods.add(mod);
            return this;
        }

        public Builder addExcludedMod(TargetedMod mod) {
            this.excludedMods.add(mod);
            return this;
        }
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    private static String[] addPrefix(String prefix, String... values) {
        return Arrays.stream(values)
            .map(s -> prefix + s)
            .collect(Collectors.toList())
            .toArray(new String[values.length]);
    }

    private enum Side {
        BOTH,
        CLIENT,
        SERVER
    }

    private enum Phase {
        EARLY,
        LATE,
    }
}
