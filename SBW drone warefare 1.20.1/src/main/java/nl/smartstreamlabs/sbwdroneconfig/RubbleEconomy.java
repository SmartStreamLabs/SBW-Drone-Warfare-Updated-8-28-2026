package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;
/** A deterministic per-unit yield, unaffected by reloads, the current player or client RNG. */
public final class RubbleEconomy {
    public static boolean salvage(UUID id,double fraction){long bits=id.getMostSignificantBits()^Long.rotateLeft(id.getLeastSignificantBits(),17);return (bits>>>11)*0x1.0p-53<Math.max(0,Math.min(1,fraction));}
    public static int layers(int units){return Math.min(8,1+(int)Math.floor(Math.log(Math.max(1,units))/Math.log(2)));}
    private RubbleEconomy(){}
}
