package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import com.google.gson.*;import java.nio.file.*;import java.util.*;import org.joml.*;
class Dd01VisualTest {
 @Test void completeNativeMeshHasSixCorrectIndependentRotorsAndNoFittingChest()throws Exception{
  var g=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/geo/delivery_drone.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();var map=new HashMap<String,JsonObject>();int faces=0;double minY=1e9;
  for(var v:g.getAsJsonArray("bones")){var b=v.getAsJsonObject();map.put(b.get("name").getAsString(),b);if(b.has("poly_mesh")){var mesh=b.getAsJsonObject("poly_mesh");faces+=mesh.getAsJsonArray("polys").size();var p=mesh.getAsJsonArray("positions");for(int i=1;i<p.size();i+=3)minY=java.lang.Math.min(minY,p.get(i).getAsDouble());}}
  assertEquals(29,map.size());assertEquals(13856,faces);assertEquals(0,minY,1e-8);assertFalse(map.keySet().stream().anyMatch(n->n.toLowerCase().contains("chest")||n.toLowerCase().contains("bag")));
  for(int i=1;i<=6;i++){var r=map.get("rotor_"+i);assertNotNull(r);assertEquals(36.5,r.getAsJsonArray("pivot").get(1).getAsDouble(),1e-8);assertEquals(31,java.lang.Math.hypot(r.getAsJsonArray("pivot").get(0).getAsDouble(),r.getAsJsonArray("pivot").get(2).getAsDouble()),1e-8);assertNotEquals("rotor_"+i,map.get("arm_"+i).get("parent").getAsString());}
 }
 @Test void groundedFeetAndBodyCenterStayCorrectWithOneModelBasis(){
  assertEquals(0,Dd01VisualPose.matrix(0,0,0).transformPosition(new Vector3d(1,0,2)).y,1e-9);
  for(float yaw:new float[]{0,45,90,180,270})for(float pitch:new float[]{-30,0,30})for(float roll:new float[]{-45,0,45}){
   var m=Dd01VisualPose.matrix(yaw,pitch,roll);assertTrue(m.transformPosition(new Vector3d(0,2,0)).distance(new Vector3d(0,2,0))<1e-8);assertEquals(1,m.determinant3x3(),1e-8);
   if(roll==0){var f=m.transformDirection(new Vector3d(0,0,-1));assertEquals(-java.lang.Math.sin(java.lang.Math.toRadians(pitch)),f.y,1e-8);}
  }
 }
 @Test void cargoIsAFullBlockCenteredInsideTheUnscaledReservedBay(){
  for(float yaw:new float[]{0,90,180,270})for(float pitch:new float[]{-20,0,20})for(float roll:new float[]{-30,0,30}){
   var body=Dd01VisualPose.matrix(yaw,pitch,roll);var cargo=new Matrix4d(body).translate(0,Dd01VisualPose.CARGO_CENTER_Y,0);
   assertTrue(cargo.transformPosition(new Vector3d()).distance(body.transformPosition(new Vector3d(0,.75,0)))<1e-8);
   for(int i=0;i<8;i++){var v=new Vector3d((i&1)==0?-.5:.5,(i&2)==0?-.5:.5,(i&4)==0?-.5:.5);cargo.transformPosition(v);new Matrix4d(body).invert().transformPosition(v);assertTrue(v.x>=-.625&&v.x<=.625&&v.z>=-.625&&v.z<=.625&&v.y>=2.5/16&&v.y<=22.5/16);}
  }
 }
 @Test void cullingIncludesEveryRotorAngleWithoutAddingPhysicalBounds()throws Exception{
  var meta=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/dd01_integration.json"))).getAsJsonObject();
  for(var entry:meta.getAsJsonObject("rotors").entrySet()){var r=entry.getValue().getAsJsonObject();var pivot=r.getAsJsonArray("pivot_units");for(int angle=0;angle<360;angle+=5){double a=java.lang.Math.toRadians(angle),x=(pivot.get(0).getAsDouble()+12.1*java.lang.Math.cos(a))/16,z=(pivot.get(2).getAsDouble()+12.1*java.lang.Math.sin(a))/16;assertTrue(x>=Dd01VisualPose.MIN_X&&x<=Dd01VisualPose.MAX_X&&z>=Dd01VisualPose.MIN_Z&&z<=Dd01VisualPose.MAX_Z);}}
 }
}
