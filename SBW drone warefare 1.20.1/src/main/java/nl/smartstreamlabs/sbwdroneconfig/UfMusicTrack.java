package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.*;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;

/** One fixed user-selected recording, bundled for native positional streaming. */
public final class UfMusicTrack extends SimpleJsonResourceReloadListener {
    public static final String FILE="assets/sbwdroneconfig/sounds/music/flag_music.ogg";
    public static final ResourceLocation AUDIO=new ResourceLocation("sbwdroneconfig","sounds/music/flag_music.ogg");
    public static volatile long duration;
    public static volatile String title="Flag Music";
    public static boolean ready(){return duration>0;}
    public UfMusicTrack(){super(new Gson(),"uf01_music");}
    @Override protected void apply(Map<ResourceLocation,JsonElement> data,ResourceManager resources,ProfilerFiller profiler){
        duration=0;title="Flag Music";
        try {var e=data.get(new ResourceLocation("sbwdroneconfig","track"));
            if(e!=null){var j=e.getAsJsonObject();long ms=j.get("duration_ms").getAsLong();
                if(j.get("enabled").getAsBoolean()&&ms>=1000&&ms<=1_800_000){
                    String s=j.get("title").getAsString();if(!s.isBlank()&&s.length()<=160){title=s;duration=ms;}
                }
            }
        }catch(RuntimeException e){SbwDroneRangeConfig.LOGGER.warn("Invalid UF01 music descriptor; playback disabled",e);}
        UfMusicServer.trackChanged();
    }
}
