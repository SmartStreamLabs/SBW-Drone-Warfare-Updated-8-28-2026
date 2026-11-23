package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.Dil01Item;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** The native Javelin-ammo artwork is not the DIL launcher. Its own HUD uses the correct asset. */
@Mixin(value={com.atsuishio.superbwarfare.client.overlay.AmmoBarOverlay.class,com.atsuishio.superbwarfare.client.overlay.AmmoCountOverlay.class},remap=false)
public abstract class Dil01AmmoHudMixin {
 @Inject(method="render",at=@At("HEAD"),cancellable=true)
 private void dil01$hud(CallbackInfo cir){var p=Minecraft.getInstance().player;if(p!=null&&Dil01Item.is(p.getMainHandItem()))cir.cancel();}
}
