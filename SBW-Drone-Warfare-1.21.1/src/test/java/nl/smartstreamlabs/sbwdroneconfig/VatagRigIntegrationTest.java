package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import com.google.gson.*;import java.nio.file.*;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class VatagRigIntegrationTest {
 JsonObject json(String path)throws Exception{return JsonParser.parseString(Files.readString(Path.of("src/main/resources/"+path))).getAsJsonObject();}
 @Test void meshAndNativeWeaponShareTurretAndBarrelPivots()throws Exception{
  var data=json("data/sbwdroneconfig/sbw/vehicles/vatag_ugv_v01.json");var bones=json("assets/sbwdroneconfig/geo/vatag_ugv_v01.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");Map<String,JsonObject> byName=new HashMap<>();for(var b:bones)byName.put(b.getAsJsonObject().get("name").getAsString(),b.getAsJsonObject());assertEquals(41,bones.size());
  var turret=byName.get("turret_yaw").getAsJsonArray("pivot");var barrel=byName.get("gun_pitch").getAsJsonArray("pivot");var tp=data.getAsJsonArray("TurretPos");var bp=data.getAsJsonArray("BarrelPos");for(int i=0;i<3;i++){double sign=i==2?-1:1;assertEquals(turret.get(i).getAsDouble()*sign/16,tp.get(i).getAsDouble(),.000001);assertEquals((barrel.get(i).getAsDouble()-turret.get(i).getAsDouble())*sign/16,bp.get(i).getAsDouble(),.000001);}
  for(String side:List.of("L","R"))for(int i=1;i<=4;i++){assertEquals("suspension_"+side+i,byName.get("steer_"+side+i).get("parent").getAsString());assertEquals("steer_"+side+i,byName.get("wheel_spin_"+side+i).get("parent").getAsString());}
  assertEquals(0,data.getAsJsonArray("Seats").size());assertEquals(8,data.getAsJsonArray("TerrainCompat").size());assertEquals(30,data.getAsJsonObject("Weapons").getAsJsonObject("Cannon").get("Magazine").getAsInt());assertEquals(0,json("assets/sbwdroneconfig/animations/vatag_ugv_v01.animation.json").getAsJsonObject("animations").size());
 }
 @Test void completeMeshFacesKeepValidVertexAndUvIndices()throws Exception{
  var bones=json("assets/sbwdroneconfig/geo/vatag_ugv_v01.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");int faces=0;for(var raw:bones){var bone=raw.getAsJsonObject();if(!bone.has("poly_mesh"))continue;var mesh=bone.getAsJsonObject("poly_mesh");for(var poly:mesh.getAsJsonArray("polys")){var corners=poly.getAsJsonArray();assertTrue(corners.size()==3||corners.size()==4);for(var corner:corners){var v=corner.getAsJsonArray();assertTrue(v.get(0).getAsInt()>=0&&v.get(0).getAsInt()<mesh.getAsJsonArray("positions").size());assertTrue(v.get(2).getAsInt()>=0&&v.get(2).getAsInt()<mesh.getAsJsonArray("uvs").size());}faces++;}}assertTrue(faces>10000,"full supplied mesh must survive conversion");
 }
}
