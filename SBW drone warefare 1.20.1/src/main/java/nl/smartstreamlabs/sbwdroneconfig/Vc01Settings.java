package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class Vc01Settings {
 public static ForgeConfigSpec.BooleanValue ENABLED;
 public static ForgeConfigSpec.DoubleValue REDUCTION,HEALTH,DAMAGE;
 public static ForgeConfigSpec.IntValue COST;
 public static void configure(AddonSettingsBuilder b){b.push("VC01Protection");
  ENABLED=b.comment("Server-authoritative drone explosion interception and vehicle shielding.").define("enabled",true);
  REDUCTION=b.comment("Maximum reduction at full panel health; applied after SBW armor.").defineInRange("maximumReduction",.60,0,1);
  HEALTH=b.comment("Maximum panel health. Changes preserve the saved health percentage, never heal damaged panels.").defineInRange("panelHealth",200d,1,1000000);
  DAMAGE=b.comment("Panel damage per point of original explosion damage, before armor and cage reduction.").defineInRange("panelDamageScale",1d,0,100);
  COST=b.comment("Iron bars consumed per damaged panel repaired to full health. Sneak + right-click with iron bars.").defineInRange("repairIronBars",2,1,64);b.pop();
 }
}
