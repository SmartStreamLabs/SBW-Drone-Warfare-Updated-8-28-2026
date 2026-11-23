package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;

/** Version-reviewed building families, not a namespace-wide destruction permission. */
public final class SecurityCraftBlastPolicy {
    private static final Set<String> BUILDING = Set.of(
        "BaseReinforcedBlock", "ReinforcedAmethystBlock", "ReinforcedCarpetBlock",
        "ReinforcedChainBlock", "ReinforcedCryingObsidianBlock", "ReinforcedDirtPathBlock",
        "ReinforcedDoorBlock", "ReinforcedFallingBlock", "ReinforcedFenceBlock",
        "ReinforcedFenceGateBlock", "ReinforcedGlassBlock", "ReinforcedGlazedTerracottaBlock",
        "ReinforcedGrassBlock", "ReinforcedIronBarsBlock", "HorizontalReinforcedIronBars",
        "ReinforcedIronTrapDoorBlock", "ReinforcedLadderBlock", "ReinforcedMagmaBlock",
        "ReinforcedMudBlock", "ReinforcedNyliumBlock", "ReinforcedObsidianBlock",
        "ReinforcedPaneBlock", "ReinforcedRootedDirtBlock", "ReinforcedRotatedCrystalQuartzPillar",
        "ReinforcedRotatedPillarBlock", "ReinforcedScaffoldingBlock", "ReinforcedSlabBlock",
        "ReinforcedSnowyDirtBlock", "ReinforcedSoulSandBlock", "ReinforcedStainedGlassBlock",
        "ReinforcedStainedGlassPaneBlock", "ReinforcedStairsBlock", "ReinforcedTintedGlassBlock",
        "ReinforcedWallBlock", "ReinforcedCopperGrateBlock"
    );
    public static boolean buildingClass(String name) {
        String prefix = "net.geforcemods.securitycraft.blocks.reinforced.";
        return name.startsWith(prefix) && BUILDING.contains(name.substring(prefix.length()));
    }
    public static boolean number(String text) {
        try { double n=Double.parseDouble(text);return Double.isFinite(n)&&n>=0&&n<=1_000_000; }
        catch(NumberFormatException e){return false;}
    }
    public static double resistance(double original,double fallback,double multiplier,Double category,Double block) {
        double base=Double.isFinite(original)&&original>=0?original:fallback;
        if(category!=null)base=category;if(block!=null)base=block;
        return Math.min(1_000_000, Math.max(0,base)*multiplier);
    }
    private SecurityCraftBlastPolicy(){}
}
