package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;
class Rj01AssetTest {
 private static final Path ASSETS=Path.of("src/main/resources/assets/sbwdroneconfig");
 private static JsonObject json(String name)throws Exception{return JsonParser.parseString(Files.readString(ASSETS.resolve(name))).getAsJsonObject();}
 @Test void fullMeshIsPartitionedWithoutDuplicatedOrMissingFaces()throws Exception{
  var mesh=json("meshes/rj01.mesh.json");int moving=0;assertEquals(4,mesh.getAsJsonArray("batches").size());
  for(var e:mesh.getAsJsonArray("batches")){var v=e.getAsJsonObject().getAsJsonArray("v");assertEquals(0,v.size()%32);moving+=v.size()/32;}
  long fixed=Files.lines(ASSETS.resolve("models/block/radar_jammer_tower.obj")).filter(s->s.startsWith("f ")).count();
  long full=Files.lines(ASSETS.resolve("models/block/radar_jammer_tower_item.obj")).filter(s->s.startsWith("f ")).count();
  assertEquals(3684,moving);assertEquals(8652,fixed);assertEquals(12336,full);assertEquals(full,moving+fixed);
  var hierarchy=mesh.getAsJsonArray("hierarchy");assertEquals(16,hierarchy.size());
  for(var e:hierarchy){var b=e.getAsJsonObject();if(b.get("name").getAsString().equals("head_tilt"))assertEquals("head_yaw",b.get("parent").getAsString());if(b.get("name").getAsString().equals("beacon"))assertEquals("head_tilt",b.get("parent").getAsString());}
 }
 @Test void nativeDimensionsAndPivotsSurviveSingleUnitConversion()throws Exception{
  var meta=json("meshes/rj01_integration.json");assertEquals(16,meta.get("units_per_block").getAsInt());assertEquals(2.773125,meta.getAsJsonArray("model_dimensions_blocks").get(1).getAsDouble(),1e-9);
  for(var e:meta.getAsJsonObject("bone_pivots_units").entrySet()){var unit=e.getValue().getAsJsonArray();var blocks=meta.getAsJsonObject("obj_part_pivots_blocks").getAsJsonArray(e.getKey());for(int i=0;i<3;i++)assertEquals(unit.get(i).getAsDouble()/16,blocks.get(i).getAsDouble(),1e-9);}
  double lo=1e9,hi=-1e9;for(String s:Files.readAllLines(ASSETS.resolve("models/block/radar_jammer_tower.obj")))if(s.startsWith("v ")){double y=Double.parseDouble(s.split(" ")[2]);lo=Math.min(lo,y);hi=Math.max(hi,y);}assertEquals(0,lo,1e-8);assertEquals(2.773125,hi,1e-8);
 }
 @Test void activeAtlasPreservesEveryStationaryMaterialPixel()throws Exception{
  var a=ImageIO.read(ASSETS.resolve("textures/block/radar_jammer_tower_rj01.png").toFile());var b=ImageIO.read(ASSETS.resolve("textures/block/radar_jammer_tower_rj01_active.png").toFile());assertEquals(512,a.getWidth());assertEquals(a.getWidth(),b.getWidth());int changed=0;
  for(int y=0;y<512;y++)for(int x=0;x<512;x++){if(x<314||x>=442||y<158||y>=222)assertEquals(a.getRGB(x,y),b.getRGB(x,y));else if(a.getRGB(x,y)!=b.getRGB(x,y))changed++;}assertTrue(changed>500);
 }
}
