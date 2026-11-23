package nl.smartstreamlabs.sbwdroneconfig.mixin;

import nl.smartstreamlabs.sbwdroneconfig.DroneMapCompatClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "xaero.common.minimap.render.MinimapRenderer")
public abstract class XaeroMinimapRendererMixin {
    @Inject(method = "drawArrow", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sbwdroneconfig$hideLocalPlayerMarkerOnMinimap(CallbackInfo ci) {
        if (DroneMapCompatClient.shouldHideXaeroMinimapSelfMarker()) {
            ci.cancel();
        }
    }
}
