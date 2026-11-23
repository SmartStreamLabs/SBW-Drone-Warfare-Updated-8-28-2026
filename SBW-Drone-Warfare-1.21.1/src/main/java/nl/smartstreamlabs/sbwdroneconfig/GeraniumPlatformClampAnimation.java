package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Samples the actual supplied linear release clip. No per-frame parsing or asset lookup. */
final class GeraniumPlatformClampAnimation {
    static final int TICKS,LID_TICKS;
    static final float LEFT_RADIANS,RIGHT_RADIANS,LID_BASE_X,LID_CLOSE_X;
    static {
        String path="/assets/sbwdroneconfig/animations/geranium2_launch_platform_lp02.animation.json";
        try(var stream=GeraniumPlatformClampAnimation.class.getResourceAsStream(path)) {
            if(stream==null)throw new IllegalStateException("Missing LP-02 clamp animation");
            var animations=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations");var clip=animations.getAsJsonObject("animation.geranium_platform.release_clamps");var lid=animations.getAsJsonObject("animation.geranium_platform.close_console");LID_TICKS=(int)Math.ceil(lid.get("animation_length").getAsDouble()*20);LID_BASE_X=(float)Math.toRadians(GeraniumPlatformLayout.CONSOLE_LID_X);LID_CLOSE_X=(float)Math.toRadians(lid.getAsJsonObject("bones").getAsJsonObject("console_lid").getAsJsonObject("rotation").getAsJsonArray("0.7").get(0).getAsFloat());
            TICKS=(int)Math.ceil(clip.get("animation_length").getAsDouble()*20);
            var bones=clip.getAsJsonObject("bones");
            LEFT_RADIANS=endRotation(bones.getAsJsonObject("clamp_left"));
            RIGHT_RADIANS=endRotation(bones.getAsJsonObject("clamp_right"));
            if(TICKS<1 || TICKS>200)throw new IllegalStateException("Invalid LP-02 clamp duration");
        } catch(java.io.IOException failure){throw new ExceptionInInitializerError(failure);}
    }
    private static float endRotation(com.google.gson.JsonObject bone){
        var rotation=bone.getAsJsonObject("rotation");
        var end=rotation.entrySet().stream().max(java.util.Comparator.comparingDouble(e->Double.parseDouble(e.getKey()))).orElseThrow().getValue().getAsJsonArray();
        return (float)Math.toRadians(end.get(2).getAsDouble());
    }
}
