package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;import org.junit.jupiter.api.Test;import java.nio.file.*;import java.util.*;import org.joml.*;import static org.junit.jupiter.api.Assertions.*;
class Lc01VisualTest {
 @Test void fullNativeMeshHasExactDimensionsAndIndependentPropeller()throws Exception{
  var j=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/geo/lucas_drone.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
  var map=new HashMap<String,JsonObject>();int faces=0;double[] lo={1e9,1e9,1e9},hi={-1e9,-1e9,-1e9};
  for(var x:j.getAsJsonArray("bones")){var b=x.getAsJsonObject();map.put(b.get("name").getAsString(),b);if(b.has("poly_mesh")){var mesh=b.getAsJsonObject("poly_mesh");faces+=mesh.getAsJsonArray("polys").size();var p=mesh.getAsJsonArray("positions");for(int i=0;i<p.size();i++){int a=i%3;double v=p.get(i).getAsDouble();lo[a]=java.lang.Math.min(lo[a],v);hi[a]=java.lang.Math.max(hi[a],v);}}}
  assertEquals(18,map.size());assertEquals(7732,faces);assertEquals(2.5,(hi[0]-lo[0])/16,1e-8);assertEquals(3,(hi[2]-lo[2])/16,1e-8);
  var prop=map.get("propeller");var pivot=prop.getAsJsonArray("pivot");assertEquals(0,pivot.get(0).getAsDouble(),1e-8);assertEquals(3.3,pivot.get(1).getAsDouble(),1e-8);assertEquals(23.58,pivot.get(2).getAsDouble(),1e-8);
  assertTrue(map.containsKey("elevon_L")&&map.containsKey("elevon_R"));assertNotEquals("propeller",map.get("engine_exterior").get("parent").getAsString());
 }
 @Test void levelAndTiltedModelFollowExistingHeadingWithUnitScale(){
  for(float yaw:new float[]{0,90,180,270})for(float pitch:new float[]{0,-18,12}){
   var m=Lc01VisualPose.matrix(yaw,pitch,0,true);var f=m.transformDirection(new Vector3d(0,0,-1));
   double yr=java.lang.Math.toRadians(yaw),pr=java.lang.Math.toRadians(pitch);
   assertEquals(-java.lang.Math.sin(yr)*java.lang.Math.cos(pr),f.x,1e-8);assertEquals(-java.lang.Math.sin(pr),f.y,1e-8);assertEquals(java.lang.Math.cos(yr)*java.lang.Math.cos(pr),f.z,1e-8);
   assertEquals(1,m.transformDirection(new Vector3d(1,0,0)).length(),1e-8);
  }
 }
 @Test void cullingContainsPropellerSweepWithoutChangingPhysicalDimensions(){
  for(float yaw:new float[]{0,45,90,180})for(float pitch:new float[]{-40,0,40})for(float roll:new float[]{-70,0,70}){
   var m=Lc01VisualPose.matrix(yaw,pitch,roll,false);
   for(double x:new double[]{-1.25,1.25})for(double y:new double[]{-.045,.458})for(double z:new double[]{-1.5,1.5})assertTrue(m.transformPosition(new Vector3d(x,y,z)).length()<Lc01VisualPose.CULL_RADIUS);
  }
 }
}
