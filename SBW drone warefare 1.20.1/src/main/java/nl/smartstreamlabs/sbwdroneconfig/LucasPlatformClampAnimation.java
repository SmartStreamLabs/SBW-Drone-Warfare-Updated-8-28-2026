package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Samples the actual supplied linear release clip. No per-frame parsing or asset lookup. */
final class LucasPlatformClampAnimation {
    static final int TICKS;
    static final float LEFT_RADIANS,RIGHT_RADIANS;
    static {
        String path="/assets/sbwdroneconfig/animations/lucas_launch_platform_lp01.animation.json";
        try(var stream=LucasPlatformClampAnimation.class.getResourceAsStream(path)) {
            if(stream==null)throw new IllegalStateException("Missing LP-01 clamp animation");
            var clip=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations").getAsJsonObject("animation.lucas_platform.release_clamps");
            TICKS=(int)Math.ceil(clip.get("animation_length").getAsDouble()*20);
            var bones=clip.getAsJsonObject("bones");
            LEFT_RADIANS=endRotation(bones.getAsJsonObject("clamp_left"));
            RIGHT_RADIANS=endRotation(bones.getAsJsonObject("clamp_right"));
            if(TICKS<1 || TICKS>200)throw new IllegalStateException("Invalid LP-01 clamp duration");
        } catch(java.io.IOException failure){throw new ExceptionInInitializerError(failure);}
    }
    private static float endRotation(com.google.gson.JsonObject bone){
        var rotation=bone.getAsJsonObject("rotation");
        var end=rotation.entrySet().stream().max(java.util.Comparator.comparingDouble(e->Double.parseDouble(e.getKey()))).orElseThrow().getValue().getAsJsonArray();
        return (float)Math.toRadians(end.get(2).getAsDouble());
    }
}
