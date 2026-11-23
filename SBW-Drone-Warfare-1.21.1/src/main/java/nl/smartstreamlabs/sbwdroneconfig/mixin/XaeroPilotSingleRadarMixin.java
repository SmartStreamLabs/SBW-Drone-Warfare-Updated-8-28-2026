package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.PilotMapClient;import net.minecraft.world.entity.Entity;import net.minecraft.client.gui.GuiGraphics;import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo @Mixin(targets="xaero.hud.minimap.radar.render.element.RadarRenderer",remap=false)
public abstract class XaeroPilotSingleRadarMixin {
 @Inject(method="renderSingleEntity",at=@At("HEAD"),cancellable=true,require=0)
 private void pilotMap$single(Entity e,boolean a,boolean b,float partial,boolean c,boolean d,@Coerce Object location,RenderTarget target,GuiGraphics graphics,CallbackInfo ci){if(PilotMapClient.hidden(e))ci.cancel();}
}
