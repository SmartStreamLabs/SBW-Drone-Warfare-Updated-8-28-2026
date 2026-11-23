package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.DroneMapCompatClient;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo @Mixin(targets="xaero.hud.minimap.player.tracker.PlayerTrackerMinimapElementReader",remap=false)
public abstract class XaeroPilotMinimapTrackerMixin {
 @Inject(method="isHidden(Lxaero/hud/minimap/player/tracker/PlayerTrackerMinimapElement;Lxaero/hud/minimap/player/tracker/PlayerTrackerMinimapElementRenderContext;)Z",at=@At("HEAD"),cancellable=true,require=0)
 private void pilotMap$hidden(@Coerce Object element,@Coerce Object context,CallbackInfoReturnable<Boolean> ci){if(DroneMapCompatClient.hiddenTracker(element))ci.setReturnValue(true);}
}
