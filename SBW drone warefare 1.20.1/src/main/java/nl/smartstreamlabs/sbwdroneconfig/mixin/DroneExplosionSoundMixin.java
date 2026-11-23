package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import nl.smartstreamlabs.sbwdroneconfig.ExplosionAudioGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientLevel.class)
public abstract class DroneExplosionSoundMixin {
    @Inject(method="playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V",at=@At("HEAD"),cancellable=true,require=1)
    private void onlyOwn(double x,double y,double z,SoundEvent sound,SoundSource category,float volume,float pitch,boolean delay,CallbackInfo ci) {
        if(ExplosionAudioGate.muted && sound==SoundEvents.GENERIC_EXPLODE){ci.cancel();if(Boolean.getBoolean("sbw.audio.trace"))System.out.println("AUDIO_SUPPRESSED_OWN_VANILLA");}
    }
}
