package nl.smartstreamlabs.sbwdroneconfig.mixin;

import com.atsuishio.superbwarfare.client.overlay.RenderContext;
import nl.smartstreamlabs.sbwdroneconfig.CubedFpvReferenceGuiOverlay;
import nl.smartstreamlabs.sbwdroneconfig.DeliveryDroneHudOverlay;
import nl.smartstreamlabs.sbwdroneconfig.LucasDroneHudOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** SBW 0.8.9 changed DroneHudOverlay.render to accept RenderContext. */
@Mixin(targets = "com.atsuishio.superbwarfare.client.overlay.DroneHudOverlay")
public abstract class DroneHudOverlay089Mixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sbwdroneconfig$replaceCubedFpvHud(RenderContext context, CallbackInfo callback) {
        if (CubedFpvReferenceGuiOverlay.shouldReplaceDefaultSbwHud()
                || LucasDroneHudOverlay.shouldReplaceDefaultSbwHud()
                || DeliveryDroneHudOverlay.shouldReplaceDefaultSbwHud()) {
            callback.cancel();
        }
    }
}
