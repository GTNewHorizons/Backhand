package xonin.backhand.mixins;

public enum TargetedMod {

    VANILLA("Minecraft", null),
    GALACTICRAFT("Galacticraft Core", null, "GalacticraftCore"),
    TFC("TerraFirmaCraft", null, "terrafirmacraft"),
    TFCPLUS("TerraFirmaCraft+", null, "terrafirmacraftplus"),
    DRACONIOCEVOLUTION("Draconic Evolution", null, "DraconicEvolution"),
    TINKERS("TConstruct", null, "TConstruct"),
    WCT("AE2 Wireless Crafting Terminal", null, "ae2wct"),
    BIBLIOCRAFT("BiblioCraft", null, "BiblioCraft"),;

    /** The "name" in the @Mod annotation */
    public final String modName;
    /** Class that implements the IFMLLoadingPlugin interface */
    public final String coreModClass;
    /** The "modid" in the @Mod annotation */
    public final String modId;

    TargetedMod(String modName, String coreModClass) {
        this(modName, coreModClass, null);
    }

    TargetedMod(String modName, String coreModClass, String modId) {
        this.modName = modName;
        this.coreModClass = coreModClass;
        this.modId = modId;
    }

    @Override
    public String toString() {
        return "TargetedMod{modName='" + modName + "', coreModClass='" + coreModClass + "', modId='" + modId + "'}";
    }
}
