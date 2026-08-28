package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.world.entity.Entity;
import nl.smartstreamlabs.sbwdroneconfig.DroneMapCompatClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "xaero.map.gui.GuiMap")
public abstract class XaeroWorldMapGuiMapMixin {
    @Shadow(remap = false)
    private Entity player;

    @Inject(method = "drawDotOnMap", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sbwdroneconfig$hideLocalPlayerDotOnWorldMap(CallbackInfo ci) {
        if (DroneMapCompatClient.shouldHideXaeroWorldMapSelfMarker(this.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawArrowOnMap", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sbwdroneconfig$hideLocalPlayerArrowOnWorldMap(CallbackInfo ci) {
        if (DroneMapCompatClient.shouldHideXaeroWorldMapSelfMarker(this.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawFarArrowOnMap", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sbwdroneconfig$hideLocalPlayerFarArrowOnWorldMap(CallbackInfo ci) {
        if (DroneMapCompatClient.shouldHideXaeroWorldMapSelfMarker(this.player)) {
            ci.cancel();
        }
    }
}
