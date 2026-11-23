package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class Rd01AssetTest {
    private static JsonElement asset(String name)throws Exception{return JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/"+name)));}
    @Test void completeMeshHasSixIndependentRotorsAndRealPassengerAnchors()throws Exception {
        var bones=asset("geo/rescue_drone_rd01.geo.json").getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");
        Map<String,JsonObject> map=new HashMap<>();int triangles=0;
        for(var raw:bones){var b=raw.getAsJsonObject();assertNull(map.put(b.get("name").getAsString(),b));if(b.has("poly_mesh"))triangles+=b.getAsJsonObject("poly_mesh").getAsJsonArray("polys").size();}
        assertEquals(33,map.size());assertEquals(10556,triangles);
        assertTrue(map.containsKey("passenger_hips_anchor"));assertTrue(map.containsKey("monitor_camera_anchor"));
        assertEquals(14,map.get("passenger_hips_anchor").getAsJsonArray("pivot").get(1).getAsDouble(),1e-6);
        assertFalse(map.keySet().stream().anyMatch(n->n.toLowerCase(Locale.ROOT).contains("avatar")||n.toLowerCase(Locale.ROOT).contains("mannequin")));
        for(int i=1;i<=6;i++){var rotor=map.get("rotor_"+i);assertNotNull(rotor);assertEquals("rd01_root",rotor.get("parent").getAsString());assertEquals(55.6,rotor.getAsJsonArray("pivot").get(1).getAsDouble(),1e-6);}
    }
    @Test void physicsAssetExcludesTheHangingHarnessAndEmptyCameraBones()throws Exception {
        var boxes=asset("rd01_collision.json").getAsJsonArray();assertTrue(boxes.size()>12&&boxes.size()<35);
        for(var raw:boxes){var b=raw.getAsJsonArray();assertEquals(6,b.size());assertTrue(b.get(1).getAsDouble()>=27.49,"harness must not enlarge aircraft damage bounds");for(int a=0;a<3;a++)assertTrue(b.get(a+3).getAsDouble()>b.get(a).getAsDouble());}
    }
    @Test void nativeUnitsAndAlternatingRotorClipsArePreserved()throws Exception {
        var meta=asset("rd01_integration.json").getAsJsonObject();assertEquals(16,meta.get("units_per_block").getAsInt());assertEquals("-Z",meta.get("forward_axis").getAsString());
        var clips=asset("animations/rescue_drone_rd01.animation.json").getAsJsonObject().getAsJsonObject("animations");
        var rotors=clips.getAsJsonObject("animation.rd01.rotors").getAsJsonObject("bones");
        for(int i=1;i<=6;i++)assertEquals(i%2==1?360:-360,rotors.getAsJsonObject("rotor_"+i).getAsJsonObject("rotation").getAsJsonArray("0.5").get(1).getAsInt());
    }
    @Test void nativeSbwSeatIsPresentSoMountingCannotBecomeAOneSidedReference()throws Exception {
        var data=JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/sbwdroneconfig/sbw/vehicles/rescue_drone_rd01.json"))).getAsJsonObject();
        assertEquals("Drone",data.get("Type").getAsString());assertEquals(1,data.getAsJsonArray("Seats").size());
        assertFalse(data.getAsJsonArray("Seats").get(0).getAsJsonObject().get("HidePassenger").getAsBoolean());
    }
    @Test void actualNativeCableMeshEndpointsStayOnFairleadAndEyeDuringWinchingAndSway(){
        for(var eye:List.of(new org.joml.Vector3d(0,41.12,1),new org.joml.Vector3d(0,29.12,1),new org.joml.Vector3d(2,1.12,-1),new org.joml.Vector3d(-3,20,4))){
            var fit=Rd01CablePose.fit(eye);var r=fit.rotation();var o=fit.offset();
            // Reconstruct Geckolib's T(position)*T(pivot)*Rz*Ry*Rx*S*T(-pivot), including its X-position sign.
            var mesh=new org.joml.Matrix4d().translate(o).translate(0,42.7,1).rotateZ(r.z).rotateY(r.y).rotateX(r.x).scale(1,fit.scaleY(),1).translate(0,-42.7,-1);
            assertTrue(mesh.transformPosition(new org.joml.Vector3d(0,42.5,1)).distance(new org.joml.Vector3d(0,42.7,1))<1e-6);
            assertTrue(mesh.transformPosition(new org.joml.Vector3d(0,37,1)).distance(eye)<1e-6);
            assertTrue(fit.scaleY()>0);
        }
    }
}
