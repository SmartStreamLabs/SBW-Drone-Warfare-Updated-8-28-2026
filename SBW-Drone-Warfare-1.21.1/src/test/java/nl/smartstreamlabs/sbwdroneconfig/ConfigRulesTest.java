package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.config.ModConfig;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ConfigRulesTest {
 private List<ConfigCatalog.Definition> settings(){var b=new ModConfigSpec.Builder();b.defineInRange("minIntervalSeconds",60,60,180);b.defineInRange("maxIntervalSeconds",180,60,180);b.defineInRange("volume",.3,0,1);return ConfigCatalog.describe("DistanceWarConfig","test.toml",ModConfig.Type.SERVER,b.build());}
 private ConfigCatalog.Definition get(List<ConfigCatalog.Definition> all,String key){return all.stream().filter(d->d.path().equals(List.of(key))).findFirst().orElseThrow();}
 @Test void relatedChangesAreValidatedAsOneTransaction(){var all=settings();assertThrows(IllegalArgumentException.class,()->ConfigRules.validate(Map.of(get(all,"minIntervalSeconds"),180,get(all,"maxIntervalSeconds"),60),all));assertDoesNotThrow(()->ConfigRules.validate(Map.of(get(all,"minIntervalSeconds"),180,get(all,"maxIntervalSeconds"),180),all));}
 @Test void unrelatedEditsDoNotRewriteLegacyClampedPairs(){var all=settings();assertDoesNotThrow(()->ConfigRules.validate(Map.of(get(all,"volume"),.5),all));assertEquals(180,ConfigRules.effective(get(all,"maxIntervalSeconds"),all));}
 @Test void chainedRuntimeThresholdsMatchExistingBakedClamps(){var b=new ModConfigSpec.Builder();b.push("droneJammer");b.defineInRange("overlayStartThreshold",.8d,0d,.99d);b.defineInRange("controlInterferenceThreshold",.2d,0d,.99d);b.defineInRange("hardJamThreshold",.5d,.5d,1d);b.pop();var all=ConfigCatalog.describe("AddonConfig","test.toml",ModConfig.Type.SERVER,b.build());var hard=all.stream().filter(d->d.path().get(1).equals("hardJamThreshold")).findFirst().orElseThrow();assertEquals(.5d,hard.current());assertEquals(.8d,ConfigRules.effective(hard,all));}
 @Test void damageTypeListsRejectMalformedRegistryNames(){var b=new ModConfigSpec.Builder();b.push("damageSources");b.defineListAllowEmpty("additionalTypes",List.of(),o->o instanceof String);b.pop();var d=ConfigCatalog.describe("InjuryConfig","test.toml",ModConfig.Type.SERVER,b.build()).get(0);assertThrows(IllegalArgumentException.class,()->d.parse("[\"invalid id\"]"));assertEquals(List.of("minecraft:arrow"),d.parse("[\"minecraft:arrow\"]"));}
}
