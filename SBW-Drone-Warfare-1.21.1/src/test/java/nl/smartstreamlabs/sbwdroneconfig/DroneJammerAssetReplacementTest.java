package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;import org.junit.jupiter.api.Test;import javax.imageio.ImageIO;import java.nio.file.*;import static org.junit.jupiter.api.Assertions.*;
class DroneJammerAssetReplacementTest {
 @Test void completeMeshUsesAnimatedItemRoute()throws Exception{
  var a=Path.of("src/main/resources/assets/sbwdroneconfig");var item=JsonParser.parseString(Files.readString(a.resolve("models/item/drone_jammer.json"))).getAsJsonObject();assertEquals("builtin/entity",item.getAsJsonObject("base").get("parent").getAsString());assertEquals(1,item.getAsJsonObject("perspectives").size());
  var mesh=JsonParser.parseString(Files.readString(a.resolve("meshes/portable_fpv_detector_pd01.json"))).getAsJsonObject().getAsJsonArray("bones");assertEquals(23,mesh.size());int triangles=0;var names=new java.util.HashSet<String>();double[] lo={1e9,1e9,1e9},hi={-1e9,-1e9,-1e9};for(var v:mesh){var b=v.getAsJsonObject();names.add(b.get("name").getAsString());if(!b.has("poly_mesh"))continue;var m=b.getAsJsonObject("poly_mesh");triangles+=m.getAsJsonArray("polys").size();var p=m.getAsJsonArray("positions");for(int i=0;i<p.size();i++){int axis=i%3;double n=p.get(i).getAsDouble()*(axis==0?-1:1);lo[axis]=Math.min(lo[axis],n);hi[axis]=Math.max(hi[axis],n);}}
  assertEquals(5720,triangles);for(String name:new String[]{"antenna_1","antenna_2","antenna_3","antenna_4","scan_line","hand_strap","grip_anchor"})assertTrue(names.contains(name));assertEquals(.48296875,(hi[1]-lo[1])/16,1e-8);assertEquals(.2503125,(hi[0]-lo[0])/16,1e-8);
 }
 @Test void completeAtlasesAndReadableIconArePackaged()throws Exception{
  var a=Path.of("src/main/resources/assets/sbwdroneconfig/textures/item");var icon=ImageIO.read(a.resolve("drone_jammer.png").toFile());assertEquals(64,icon.getWidth());assertEquals(64,icon.getHeight());int visible=0;for(int y=0;y<64;y++)for(int x=0;x<64;x++)if((icon.getRGB(x,y)>>>24)>32)visible++;assertTrue(visible>150);
  for(String suffix:new String[]{"","_standby","_alert"}){var image=ImageIO.read(a.resolve("portable_fpv_detector_pd01"+suffix+".png").toFile());assertEquals(512,image.getWidth());assertEquals(512,image.getHeight());}
 }
 @Test void statusAndSweepDoNotInventDetection(){assertEquals(0,Pd01VisualState.state(false,true,true));assertEquals(1,Pd01VisualState.state(true,false,true));assertEquals(1,Pd01VisualState.state(true,true,false));assertEquals(2,Pd01VisualState.state(true,true,true));assertEquals(0,Pd01VisualState.sweep(0),1e-9);assertEquals(.62/16,Pd01VisualState.sweep(16),1e-9);assertEquals(0,Pd01VisualState.sweep(32),1e-9);for(int i=0;i<100;i++)assertTrue(Pd01VisualState.sweep(i)>=0&&Pd01VisualState.sweep(i)<=.62/16);}
}
