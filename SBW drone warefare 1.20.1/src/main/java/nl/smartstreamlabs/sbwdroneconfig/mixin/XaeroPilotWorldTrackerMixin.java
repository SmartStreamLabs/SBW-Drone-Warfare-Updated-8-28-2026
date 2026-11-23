package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.DroneMapCompatClient;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo @Mixin(targets="xaero.map.radar.tracker.PlayerTrackerMapElementReader",remap=false)
public abstract class XaeroPilotWorldTrackerMixin {
 @Inject(method="isHidden(Lxaero/map/radar/tracker/PlayerTrackerMapElement;Lxaero/map/radar/tracker/PlayerTrackerMapElementRenderContext;)Z",at=@At("HEAD"),cancellable=true,require=0)
 private void pilotMap$hidden(@Coerce Object element,@Coerce Object context,CallbackInfoReturnable<Boolean> ci){if(DroneMapCompatClient.hiddenTracker(element))ci.setReturnValue(true);}
}
