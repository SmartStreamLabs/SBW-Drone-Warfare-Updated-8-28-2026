package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import net.minecraftforge.common.ForgeConfigSpec;
import com.electronwill.nightconfig.toml.TomlParser;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ConfigMigrationsTest {
 private ForgeConfigSpec spec(){var b=ConfigSchema.builder();b.defineInRange("maxItems",64000000L,1L,Long.MAX_VALUE);b.defineInRange("gain",.5d,0d,1d);b.defineInRange("lifeTicks",200,20,2000);return b.build();}
 private ConfigMigrations.Alias alias(){return new ConfigMigrations.Alias(List.of("lifeSeconds"),List.of("lifeTicks"),v->Math.multiplyExact(((Number)v).intValue(),20));}
 @Test void validLegacyValuesAndFullLongPrecisionArePreserved(){var raw=new TomlParser().parse("maxItems=9223372036854775807\ngain=0.25\n");var r=ConfigMigrations.prepare("test",spec(),raw,List.of());assertEquals(Long.MAX_VALUE,((Number)r.config().get("maxItems")).longValue());assertEquals(.25,((Number)r.config().get("gain")).doubleValue());assertEquals(1,((Number)r.config().get(ConfigSchema.VERSION)).intValue());assertFalse(raw.contains(ConfigSchema.VERSION));}
 @Test void invalidFieldDoesNotResetValidSibling(){var raw=new TomlParser().parse("maxItems=\"broken\"\ngain=0.25\n");var r=ConfigMigrations.prepare("test",spec(),raw,List.of());assertEquals(64000000L,((Number)r.config().get("maxItems")).longValue());assertEquals(.25,((Number)r.config().get("gain")).doubleValue());assertTrue(r.notices().stream().anyMatch(s->s.contains("maxItems")));}
 @Test void explicitRenameConvertsSecondsToTicks(){var raw=new TomlParser().parse("lifeSeconds=15\n");var r=ConfigMigrations.prepare("test",spec(),raw,List.of(alias()));assertEquals(300,((Number)r.config().get("lifeTicks")).intValue());assertFalse(r.config().contains("lifeSeconds"));}
 @Test void explicitDestinationWinsOverOldAlias(){var raw=new TomlParser().parse("lifeSeconds=15\nlifeTicks=600\n");var r=ConfigMigrations.prepare("test",spec(),raw,List.of(alias()));assertEquals(600,((Number)r.config().get("lifeTicks")).intValue());}
 @Test void repeatedMigrationDoesNotRewriteOrChangeValues(){var s=spec();var first=ConfigMigrations.prepare("test",s,new TomlParser().parse("gain=0.25\n"),List.of());var again=ConfigMigrations.prepare("test",s,first.config(),List.of());assertFalse(again.changed());assertEquals(new com.electronwill.nightconfig.toml.TomlWriter().writeToString(first.config()),new com.electronwill.nightconfig.toml.TomlWriter().writeToString(again.config()));}
 @Test void invalidAliasDoesNotResetUnrelatedValue(){var raw=new TomlParser().parse("lifeSeconds=\"bad\"\ngain=0.25\n");var r=ConfigMigrations.prepare("test",spec(),raw,List.of(alias()));assertEquals(.25,((Number)r.config().get("gain")).doubleValue());assertEquals(200,((Number)r.config().get("lifeTicks")).intValue());}
 @Test void futureVersionIsRejectedWithoutTouchingSource(){var raw=new TomlParser().parse("_configVersion=2\ngain=0.25\n");assertThrows(IllegalArgumentException.class,()->ConfigMigrations.prepare("test",spec(),raw,List.of()));assertEquals(.25,((Number)raw.get("gain")).doubleValue());}
}
