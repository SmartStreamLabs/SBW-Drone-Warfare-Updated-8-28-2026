package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import com.google.gson.*;import java.nio.file.*;import java.util.*;import org.joml.Matrix4d;import org.joml.Vector3d;
class Bt01VisualTest {
 private JsonArray bones()throws Exception{return JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/geo/ukrainian_bayraktar_tb2.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");}
 private static double v(JsonArray a,int i){return a.get(i).getAsDouble();}
 @Test void completeCleanMeshHasExactNativeDimensionsAfterTailBindTransforms()throws Exception{
  var bs=bones();assertEquals(37,bs.size());var matrices=new HashMap<String,Matrix4d>();int triangles=0;Vector3d lo=new Vector3d(1e9),hi=new Vector3d(-1e9);
  for(var value:bs){var b=value.getAsJsonObject();String name=b.get("name").getAsString();assertFalse(name.contains("reference_rack"));var p=b.getAsJsonArray("pivot");var r=b.getAsJsonArray("rotation");var m=b.has("parent")?new Matrix4d(matrices.get(b.get("parent").getAsString())):new Matrix4d();double x=-v(p,0),y=v(p,1),z=v(p,2);m.translate(x,y,z).rotateZ(Math.toRadians(v(r,2))).rotateY(Math.toRadians(-v(r,1))).rotateX(Math.toRadians(-v(r,0))).translate(-x,-y,-z);matrices.put(name,m);
   if(b.has("poly_mesh")){var mesh=b.getAsJsonObject("poly_mesh");triangles+=mesh.getAsJsonArray("polys").size();var pos=mesh.getAsJsonArray("positions");for(int i=0;i<pos.size();i+=3){var point=m.transformPosition(new Vector3d(-v(pos,i),v(pos,i+1),v(pos,i+2)));lo.min(point);hi.max(point);}}
  }
  assertEquals(23676,triangles);assertEquals(-96,lo.x,1e-6);assertEquals(96,hi.x,1e-6);assertEquals(-52,lo.z,1e-6);assertEquals(52,hi.z,1e-6);assertEquals(0,lo.y,1e-6);assertEquals(35.2,hi.y,1e-6);
 }
 @Test void propellerAndDiagonalTailKeepMetadataHierarchy()throws Exception{
  var map=new HashMap<String,JsonObject>();for(var b:bones())map.put(b.getAsJsonObject().get("name").getAsString(),b.getAsJsonObject());
  var prop=map.get("propeller").getAsJsonArray("pivot");assertEquals(0,v(prop,0),1e-9);assertEquals(21,v(prop,1));assertEquals(30.6,v(prop,2));assertNotEquals("propeller",map.get("engine_cowl").get("parent").getAsString());
  for(String side:new String[]{"L","R"}){var basis=map.get("tail_hinge_basis_"+side);assertEquals("tail_hinge_basis_"+side,map.get("ruddervator_"+side).get("parent").getAsString());assertEquals(side.equals("L")?36.1932073055647:143.8067926944353,v(basis.getAsJsonArray("rotation"),2),1e-9);assertEquals(0,v(map.get("ruddervator_"+side).getAsJsonArray("rotation"),0),1e-9);}
 }
 @Test void modelBasisIsRigidAndCullingContainsEveryAttitude(){
  for(float yaw:new float[]{0,45,90,180,270})for(float pitch:new float[]{-45,0,45})for(float roll:new float[]{-80,0,80}){var m=Bt01VisualPose.matrix(yaw,pitch,roll);var center=new Vector3d(0,1.25,0);assertTrue(m.transformPosition(new Vector3d(center)).distance(center)<1e-8);assertEquals(1,m.determinant3x3(),1e-8);for(int i=0;i<8;i++){var p=m.transformPosition(new Vector3d((i&1)==0?-6:6,(i&2)==0?0:2.2,(i&4)==0?-3.25:3.25));assertTrue(p.distance(center)<Bt01VisualPose.CULL_RADIUS);}if(pitch==0&&roll==0)assertEquals(0,m.transformPosition(new Vector3d(1,0,1)).y,1e-8);}
 }
 @Test void wheelsRollWithActualGroundTravelAndIgnoreRepeatedFrames(){
  var w=new Bt01WheelMotion();w.update(1,0,0,0,true);w.update(2,0,.2125,0,true);assertEquals(-1,w.main(),1e-9);double front=w.front();for(int i=0;i<120;i++)w.update(2,0,.2125,0,true);assertEquals(-1,w.main(),1e-9);assertEquals(front,w.front());w.update(3,0,.2125,10,true);assertTrue(w.steering()<0);w.update(4,0,4,10,false);assertEquals(-1,w.main(),1e-9);w.update(5,0,5,10,true);assertEquals(-1,w.main(),1e-9);
 }
 @Test void visualPayloadsMeetTopAnchorsWithUniformScaling(){
  for(double[] b:new double[][]{{-.5,-.5,-.03,.5,.5,.03},{-2,-.1,-.1,2,.1,.1}}){var fit=Bt01PayloadFit.fromCenteredBounds(b[0],b[1],b[2],b[3],b[4],b[5]);assertTrue(fit.width()<=.44+1e-8&&fit.height()<=.55+1e-8&&fit.depth()<=1.5+1e-8);double top=-1e9;for(int i=0;i<8;i++){var p=fit.matrix().transformPosition(new Vector3d(b[(i&1)==0?0:3],b[(i&2)==0?1:4],b[(i&4)==0?2:5]));top=Math.max(top,p.y);}assertEquals(0,top,1e-8);assertEquals(Math.pow(fit.scale(),3),fit.matrix().determinant3x3(),1e-8);}
  assertTrue(Math.abs(Bt01VisualPose.socket(0).x-Bt01VisualPose.socket(1).x)>.44);assertEquals(1,Bt01VisualPose.socket(3).y);
 }
}
