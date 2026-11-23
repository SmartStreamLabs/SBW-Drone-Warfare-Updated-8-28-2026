package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import com.google.gson.*;import java.nio.file.*;import java.util.*;import org.joml.Matrix4d;import org.joml.Vector3d;
class Hb01VisualTest {
 @Test void completeMeshRetainsAllPartsAndSixMetadataPivots()throws Exception{
  var g=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/geo/heavy_bomber_drone.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();var map=new HashMap<String,JsonObject>();int faces=0;double minY=1e9;
  for(var v:g.getAsJsonArray("bones")){var b=v.getAsJsonObject();map.put(b.get("name").getAsString(),b);if(b.has("poly_mesh")){var mesh=b.getAsJsonObject("poly_mesh");faces+=mesh.getAsJsonArray("polys").size();var p=mesh.getAsJsonArray("positions");for(int i=1;i<p.size();i+=3)minY=Math.min(minY,p.get(i).getAsDouble());}}
  assertEquals(30,map.size());assertEquals(13528,faces);assertEquals(0,minY,1e-8);assertFalse(map.keySet().stream().anyMatch(n->n.contains("PREVIEW_ONLY")));
  var meta=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/hb01_integration.json"))).getAsJsonObject();
  for(int i=1;i<=6;i++){var r=map.get("rotor_"+i);var pivot=r.getAsJsonArray("pivot");var supplied=meta.getAsJsonObject("rotors").getAsJsonObject("rotor_"+i).getAsJsonArray("pivot_units");assertEquals(-supplied.get(0).getAsDouble(),pivot.get(0).getAsDouble(),1e-8);assertEquals(13.75,pivot.get(1).getAsDouble(),1e-8);assertEquals(supplied.get(2).getAsDouble(),pivot.get(2).getAsDouble(),1e-8);assertNotEquals("rotor_"+i,map.get("arm_"+i).get("parent").getAsString());}
 }
 @Test void nativeFeetBasisAndAircraftCenterUseOneRigidTransform(){
  for(float yaw:new float[]{0,45,90,180,270})for(float pitch:new float[]{-30,0,30})for(float roll:new float[]{-45,0,45}){
   var m=Hb01VisualPose.matrix(yaw,pitch,roll);assertTrue(m.transformPosition(new Vector3d(0,11.5/16,0)).distance(new Vector3d(0,11.5/16,0))<1e-8);assertEquals(1,m.determinant3x3(),1e-8);
   if(pitch==0&&roll==0)assertEquals(0,m.transformPosition(new Vector3d(1,0,1)).y,1e-8);
   if(roll==0)assertEquals(-Math.sin(Math.toRadians(pitch)),m.transformDirection(new Vector3d(0,0,-1)).y,1e-8);
  }
 }
 @Test void fullRotorSweepStaysInsideVisualBounds()throws Exception{
  var meta=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/hb01_integration.json"))).getAsJsonObject();
  for(var entry:meta.getAsJsonObject("rotors").entrySet()){var p=entry.getValue().getAsJsonObject().getAsJsonArray("pivot_units");double radius=meta.getAsJsonObject("rotor_actual_swept_radius_units").get(entry.getKey()).getAsDouble();for(int angle=0;angle<360;angle++){double a=Math.toRadians(angle),x=(p.get(0).getAsDouble()+radius*Math.cos(a))/16,z=(p.get(2).getAsDouble()+radius*Math.sin(a))/16;assertTrue(x>=Hb01VisualPose.MIN_X&&x<=Hb01VisualPose.MAX_X&&z>=Hb01VisualPose.MIN_Z&&z<=Hb01VisualPose.MAX_Z);}}
 }
 @Test void payloadsAreUniformlyFittedByTheirTopNotTheirCenter(){
  for(double[] bounds:new double[][]{{-.4,-.1,-.4,.4,.1,.4},{-2,-.15,-.15,2,.15,.15},{-.1,-.1,-2,.1,.1,2},{-.5,-.5,0,.5,.5,0}}){
   var fit=Hb01PayloadFit.fromCenteredBounds(bounds[0],bounds[1],bounds[2],bounds[3],bounds[4],bounds[5]);assertTrue(fit.width()<=Hb01PayloadFit.MAX_WIDTH+1e-8&&fit.height()<=Hb01PayloadFit.MAX_HEIGHT+1e-8&&fit.depth()<=Hb01PayloadFit.MAX_DEPTH+1e-8);assertEquals(Math.pow(fit.scale(),3),fit.matrix().determinant3x3(),1e-8);
   double top=-1e9;for(int i=0;i<8;i++){var v=fit.matrix().transformPosition(new Vector3d(bounds[(i&1)==0?0:3],bounds[(i&2)==0?1:4],bounds[(i&4)==0?2:5]));top=Math.max(top,v.y);assertTrue(v.y<=1e-8&&v.y>=-Hb01PayloadFit.MAX_HEIGHT-1e-8);}assertEquals(0,top,1e-8);
  }
 }
 @Test void socketLoadsShareAttitudeKeepFeetClearAndCannotOverlapEachOther(){
  for(float yaw:new float[]{0,90,180,270})for(float pitch:new float[]{-20,0,20})for(float roll:new float[]{-30,0,30}){
   var body=Hb01VisualPose.matrix(yaw,pitch,roll);var fit=Hb01PayloadFit.fromCenteredBounds(-.5,-.5,-.5,.5,.5,.5);
   for(int slot=0;slot<4;slot++){var a=Hb01VisualPose.socket(slot);assertEquals(6.9/16,a.y,1e-8);assertTrue(a.y-Hb01PayloadFit.MAX_HEIGHT>0);var load=new Matrix4d(body).translate(a).mul(fit.matrix());var top=fit.matrix().transformPosition(new Vector3d(0,.5,0));var actual=new Matrix4d(body).invert().transformPosition(load.transformPosition(new Vector3d(0,.5,0)));assertTrue(actual.distance(new Vector3d(a).add(top))<1e-8);}
   assertTrue(Math.abs(Hb01VisualPose.socket(1).x-Hb01VisualPose.socket(0).x)>Hb01PayloadFit.MAX_WIDTH);assertTrue(Math.abs(Hb01VisualPose.socket(2).z-Hb01VisualPose.socket(0).z)>Hb01PayloadFit.MAX_DEPTH);
  }
 }
}
