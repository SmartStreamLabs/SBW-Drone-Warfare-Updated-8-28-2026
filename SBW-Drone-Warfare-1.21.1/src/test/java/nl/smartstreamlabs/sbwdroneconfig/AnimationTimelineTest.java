package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class AnimationTimelineTest {
    @Test void animationKeyframesAreChronologicalForGeckoLib() throws Exception {
        var root=Path.of("src/main/resources/assets/sbwdroneconfig/animations");
        try(var paths=Files.list(root)) {
            for(var path:paths.filter(p->p.toString().endsWith(".json")).toList()) {
                var animations=JsonParser.parseString(Files.readString(path)).getAsJsonObject().getAsJsonObject("animations");
                for(var animation:animations.entrySet()) {
                    var bones=animation.getValue().getAsJsonObject().getAsJsonObject("bones");
                    if(bones==null)continue;
                    for(var bone:bones.entrySet())for(var channel:bone.getValue().getAsJsonObject().entrySet()) {
                        if(!channel.getValue().isJsonObject())continue;
                        var frames=channel.getValue().getAsJsonObject();
                        double previous=-1;
                        for(var frame:frames.entrySet()) {
                            double timestamp;
                            try{timestamp=Double.parseDouble(frame.getKey());}catch(NumberFormatException ignored){continue;}
                            assertTrue(Double.isFinite(timestamp)&&timestamp>previous,
                                path.getFileName()+" / "+animation.getKey()+" / "+bone.getKey()+" / "+channel.getKey()+" / "+frame.getKey());
                            previous=timestamp;
                        }
                    }
                }
            }
        }
    }

    @Test void jammerPackingStillRetractsFromDeployedPose() throws Exception {
        var json=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/animations/mobile_jammer_truck_mj03.animation.json"))).getAsJsonObject();
        var pack=json.getAsJsonObject("animations").getAsJsonObject("animation.mj03.pack");
        assertEquals(3,pack.get("animation_length").getAsInt());
        assertEquals("hold_on_last_frame",pack.get("loop").getAsString());
        var lift=pack.getAsJsonObject("bones").getAsJsonObject("panel_lift").getAsJsonObject("position");
        assertEquals(12,lift.getAsJsonArray("0.0").get(1).getAsInt());
        assertEquals(0,lift.getAsJsonArray("3.0").get(1).getAsInt());
        var pitch=pack.getAsJsonObject("bones").getAsJsonObject("panel_pitch").getAsJsonObject("rotation");
        assertEquals(0,pitch.getAsJsonArray("0.0").get(0).getAsInt());
        assertEquals(-75,pitch.getAsJsonArray("3.0").get(0).getAsInt());
    }
}
