package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.renderer.LevelRenderer;
import nl.smartstreamlabs.sbwdroneconfig.WhiteHotRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LevelRenderer.class)
public abstract class WhiteHotWorldMixin {
    @Inject(method="renderLevel",at=@At("HEAD")) private void drone$thermalBegin(CallbackInfo ci){WhiteHotRender.begin();}
    @Inject(method="renderLevel",at=@At("RETURN")) private void drone$thermalEnd(CallbackInfo ci){WhiteHotRender.end();}
}
