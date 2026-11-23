package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import com.electronwill.nightconfig.toml.TomlParser;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class DroneManualRangeDefaultTest {
 @Test void newWorldGetsTenKilometres(){
  assertEquals(10000,AddonConfig.DRONE_MAX_RANGE_VALUE.getDefault());
  var r=ConfigMigrations.prepare("AddonConfig",AddonConfig.SPEC,new TomlParser().parse(""),List.of());
  assertEquals(10000,((Number)r.config().get("drone.droneMaxRange")).intValue());
 }
 @Test void explicitServerChoicesAreNeverSilentlyOverridden(){
  for(int range:new int[]{500,15000,30000}){
   var r=ConfigMigrations.prepare("AddonConfig",AddonConfig.SPEC,new TomlParser().parse("[drone]\ndroneMaxRange="+range+"\n"),List.of());
   assertEquals(range,((Number)r.config().get("drone.droneMaxRange")).intValue());
  }
 }
 @Test void oldWorldAt100IsUpgradedOnceWithoutResettingOtherSettings(){
  var raw=new TomlParser().parse("_configVersion=1\n[drone]\ndroneMaxRange=100\ndroneVisualTrackingRange=192\n");
  var first=ConfigMigrations.prepare("AddonConfig",AddonConfig.SPEC,raw,List.of());
  assertEquals(10000,((Number)first.config().get("drone.droneMaxRange")).intValue());
  assertEquals(192,((Number)first.config().get("drone.droneVisualTrackingRange")).intValue());
  assertEquals(100,((Number)raw.get("drone.droneMaxRange")).intValue());
  assertFalse(ConfigMigrations.prepare("AddonConfig",AddonConfig.SPEC,first.config(),List.of()).changed());
  first.config().set("drone.droneMaxRange",100);
  var explicit=ConfigMigrations.prepare("AddonConfig",AddonConfig.SPEC,first.config(),List.of());
  assertEquals(100,((Number)explicit.config().get("drone.droneMaxRange")).intValue());
 }
 @Test void internalMarkerIsNotAnEditableGameplayOption(){
  var defs=ConfigCatalog.describe("AddonConfig","gameplay.toml",net.neoforged.fml.config.ModConfig.Type.SERVER,AddonConfig.SPEC);
  assertTrue(defs.stream().filter(d->d.path().contains("_manualRangeDefaultsVersion")).allMatch(ConfigCatalog.Definition::managed));
 }
 @Test void savedWorldConfigIsPhysicallyReplacedAndOriginalIsBackedUp(@org.junit.jupiter.api.io.TempDir java.nio.file.Path dir)throws Exception {
  var file=dir.resolve("sbwdroneconfig-gameplay.toml");
  String original="_configVersion=1\n[drone]\ndroneMaxRange=100\ndroneVisualTrackingRange=192\n";
  java.nio.file.Files.writeString(file,original);
  var prepared=ConfigMigrations.prepare("AddonConfig",AddonConfig.SPEC,new TomlParser().parse(java.nio.file.Files.readString(file)),List.of());
  var bytes=new com.electronwill.nightconfig.toml.TomlWriter().writeToString(prepared.config()).getBytes(java.nio.charset.StandardCharsets.UTF_8);
  assertTrue(AtomicConfigFiles.replace(file,bytes,true));
  var reloaded=new TomlParser().parse(java.nio.file.Files.readString(file));
  assertEquals(10000,((Number)reloaded.get("drone.droneMaxRange")).intValue());
  assertEquals(192,((Number)reloaded.get("drone.droneVisualTrackingRange")).intValue());
  try(var paths=java.nio.file.Files.list(dir)){
   var backup=paths.filter(f->f.toString().endsWith(".bak")).findFirst().orElseThrow();
   assertEquals(original,java.nio.file.Files.readString(backup));
  }
  assertFalse(ConfigMigrations.prepare("AddonConfig",AddonConfig.SPEC,reloaded,List.of()).changed());
 }
}
