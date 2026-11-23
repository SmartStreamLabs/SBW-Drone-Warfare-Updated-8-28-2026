package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.PilotMapClient;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo @Mixin(targets="xaero.hud.minimap.radar.render.element.RadarElementReader",remap=false)
public abstract class XaeroPilotRadarMixin {
 @Inject(method="isHidden(Lnet/minecraft/world/entity/Entity;Lxaero/hud/minimap/radar/render/element/RadarRenderContext;)Z",at=@At("HEAD"),cancellable=true,require=0)
 private void pilotMap$hidden(Entity e,@Coerce Object context,CallbackInfoReturnable<Boolean> ci){if(PilotMapClient.hidden(e))ci.setReturnValue(true);}
 @Inject(method="isInteractable(Lxaero/hud/minimap/element/render/MinimapElementRenderLocation;Lnet/minecraft/world/entity/Entity;)Z",at=@At("HEAD"),cancellable=true,require=0)
 private void pilotMap$interactive(@Coerce Object location,Entity e,CallbackInfoReturnable<Boolean> ci){if(PilotMapClient.hidden(e))ci.setReturnValue(false);}
}
