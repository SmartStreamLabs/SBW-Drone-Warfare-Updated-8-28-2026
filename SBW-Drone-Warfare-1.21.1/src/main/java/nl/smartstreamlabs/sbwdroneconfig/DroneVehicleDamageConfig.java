package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;
public final class DroneVehicleDamageConfig {
 public static ModConfigSpec.DoubleValue MULTIPLIER,UNPROTECTED;
 public static ModConfigSpec.ConfigValue<List<? extends String>> TYPES,PAYLOADS;
 public static void configure(AddonSettingsBuilder b){b.push("DroneVehicleDamage");
  MULTIPLIER=b.comment("Vehicle-only explosive drone damage AFTER SBW armour with 35% raw explosive damage penetration floor, BEFORE VC-01. Players, mobs and terrain are unchanged.").defineInRange("explosiveVehicleMultiplier",8d,0,100);
  UNPROTECTED=b.comment("Additional scale for identified explosive drone damage only when no intact VC-01 panel intercepts the blast. Multiplies the existing vehicle/drone/payload setting; intact cage damage and its reduction are unchanged.").defineInRange("unprotectedVehicleDamageScale",2d,0,100);
  TYPES=b.comment("Overrides: drone entity registry id=multiplier. Explosive attacks only.").defineListAllowEmpty("droneOverrides",List.of(),DroneAttackDamage::validOverride);
  PAYLOADS=b.comment("Payload item id=multiplier; takes priority over drone override. Native projectile id is used when no item identity exists.").defineListAllowEmpty("payloadOverrides",List.of(),DroneAttackDamage::validOverride);b.pop();
 }
}
