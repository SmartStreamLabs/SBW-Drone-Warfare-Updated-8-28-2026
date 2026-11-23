package nl.smartstreamlabs.sbwdroneconfig.mixin;
import java.util.Map;import net.minecraft.client.sounds.*;import net.minecraft.client.resources.sounds.SoundInstance;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=SoundEngine.class)
public interface UfMusicEngineAccess {@Accessor("instanceToChannel")Map<SoundInstance,ChannelAccess.ChannelHandle> ufMusic$channels();}
