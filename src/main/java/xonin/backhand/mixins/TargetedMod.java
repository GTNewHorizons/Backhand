package xonin.backhand.mixins;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

public enum TargetedMod implements ITargetMod {

    AE2_WIRELESS_CRAFTING_TERMINAL("ae2wct"),
    BIBLIOCRAFT("BiblioCraft"),
    DRACONIC_EVOLUTION("DraconicEvolution"),
    GALACTICRAFT("GalacticraftCore"),
    TERRAFIRMACRAFT("terrafirmacraft"),
    TERRAFIRMACRAFT_PLUS("terrafirmacraftplus"),
    TINKERS_CONSTRUCT("TConstruct");

    private final TargetModBuilder builder;

    TargetedMod(String modId) {
        this.builder = new TargetModBuilder().setModId(modId);
    }

    @NotNull
    @Override
    public TargetModBuilder getBuilder() {
        return builder;
    }
}
