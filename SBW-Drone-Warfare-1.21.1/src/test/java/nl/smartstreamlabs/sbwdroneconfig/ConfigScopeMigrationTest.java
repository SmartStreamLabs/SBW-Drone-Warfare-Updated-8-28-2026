package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import net.neoforged.neoforge.common.ModConfigSpec;
import com.electronwill.nightconfig.toml.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ConfigScopeMigrationTest {
 @Test void singleDefinitionRoutesIntoExactlyOneRealSpec(){
  var b=new AddonSettingsBuilder();b.push("drone");var range=b.comment("Server range").defineInRange("droneMaxRange",10000,1,30000);var fov=b.comment("Local FOV").defineInRange("fpvCameraFov",90d,20d,140d);b.pop();
  var s=b.build();var c=b.buildClient();
  assertSame(range,s.getValues().get(List.of("drone","droneMaxRange")));assertFalse(c.getValues().contains(List.of("drone","droneMaxRange")));
  assertSame(fov,c.getValues().get(List.of("drone","fpvCameraFov")));assertFalse(s.getValues().contains(List.of("drone","fpvCameraFov")));
  assertTrue(b.localValues().contains(fov));assertFalse(b.localValues().contains(range));
  ModConfigSpec.ValueSpec sr=s.getSpec().get(List.of("drone","droneMaxRange"));ModConfigSpec.ValueSpec cf=c.getSpec().get(List.of("drone","fpvCameraFov"));
  assertTrue(sr.getComment().startsWith("Server range"));assertTrue(cf.getComment().startsWith("Local FOV"));assertFalse(sr.test(30001));assertEquals(10000,sr.getDefault());
 }
 @Test void splitPreservesValuesWithoutCrossingScopeOrMutatingLegacy(){
  var b=new AddonSettingsBuilder();b.push("drone");b.defineInRange("droneMaxRange",100,1,30000);b.defineInRange("fpvCameraFov",90d,20d,140d);b.pop();var s=b.build();var c=b.buildClient();
  var old=new TomlParser().parse("[drone]\ndroneMaxRange=10000\nfpvCameraFov=106.5\nunknown=9\n");String bytes=new TomlWriter().writeToString(old);var server=new TomlParser().parse("");var client=new TomlParser().parse("");
  assertEquals(1,ConfigMigrations.seedLegacy(s,server,old));assertEquals(1,ConfigMigrations.seedLegacy(c,client,old));
  server=ConfigMigrations.prepare("server",s,server,List.of()).config();client=ConfigMigrations.prepare("client",c,client,List.of()).config();
  assertEquals(10000,((Number)server.get("drone.droneMaxRange")).intValue());assertFalse(server.contains("drone.fpvCameraFov"));assertEquals(106.5,((Number)client.get("drone.fpvCameraFov")).doubleValue());assertFalse(client.contains("drone.unknown"));assertEquals(bytes,new TomlWriter().writeToString(old));
  assertEquals(0,ConfigMigrations.seedLegacy(s,server,old));assertEquals(0,ConfigMigrations.seedLegacy(c,client,old));
 }
 @Test void explicitDestinationWinsEvenBeforeVersionMarker(){
  var b=ConfigSchema.builder();b.defineInRange("droneMaxRange",100,1,30000);var s=b.build();var target=new TomlParser().parse("droneMaxRange=12345\n");var old=new TomlParser().parse("droneMaxRange=10000\n");
  assertEquals(0,ConfigMigrations.seedLegacy(s,target,old));assertEquals(12345,((Number)target.get("droneMaxRange")).intValue());
 }
 @Test void schemaIdsAreUniqueAcrossClientAndServerFiles(){
  var a=ConfigSchema.builder();a.define("enabled",true);var b=ConfigSchema.builder();b.define("volume",.5d);String family="split-test-"+UUID.randomUUID();
  ConfigCatalog.capture(family,net.neoforged.fml.config.ModConfig.Type.SERVER,a.build(),"split-test-server.toml");ConfigCatalog.capture(family,net.neoforged.fml.config.ModConfig.Type.CLIENT,b.build(),"split-test-client.toml");
  assertEquals(4,ConfigCatalog.definitions().stream().filter(d->d.family().equals(family)).count());
 }
}
