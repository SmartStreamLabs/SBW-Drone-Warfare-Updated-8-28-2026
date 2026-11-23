package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.sounds.*;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=SoundManager.class)
public interface UfMusicManagerAccess {@Accessor("soundEngine")SoundEngine ufMusic$engine();}
