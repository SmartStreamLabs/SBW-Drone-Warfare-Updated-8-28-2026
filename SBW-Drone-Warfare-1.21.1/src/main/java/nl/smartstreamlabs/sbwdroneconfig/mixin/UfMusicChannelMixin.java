package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.mojang.blaze3d.audio.Channel;import net.minecraft.client.sounds.AudioStream;import nl.smartstreamlabs.sbwdroneconfig.*;import org.lwjgl.openal.AL10;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=Channel.class,remap=false)
public abstract class UfMusicChannelMixin implements UfMusicChannel {
    @Shadow private AudioStream stream;
    @Shadow @Final private int source;
    @Inject(method="removeProcessedBuffers",at=@At("RETURN")) private void ufMusic$processed(CallbackInfoReturnable<Integer> ci){if(stream instanceof UfMusicStream s)s.processed(ci.getReturnValue());}
    // AL_SEC_OFFSET is measured from the current queue start, including processed buffers still queued.
    @Override public double ufMusic$cursor(){return stream instanceof UfMusicStream s?s.cursor(0,AL10.alGetSourcef(source,org.lwjgl.openal.AL11.AL_SEC_OFFSET)):-1;}
}

