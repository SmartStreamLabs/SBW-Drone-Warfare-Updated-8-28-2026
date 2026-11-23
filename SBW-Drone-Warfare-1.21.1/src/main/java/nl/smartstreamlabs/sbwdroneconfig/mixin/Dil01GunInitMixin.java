package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.atsuishio.superbwarfare.event.GunEventHandler;import com.atsuishio.superbwarfare.data.gun.GunData;import nl.smartstreamlabs.sbwdroneconfig.Dil01Item;import net.minecraft.world.entity.Entity;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Initialize through SBW without its optional free creative-mode magazine, only for DIL. */
@Mixin(value=GunEventHandler.class,remap=false)
public abstract class Dil01GunInitMixin {
 @Inject(method="init",at=@At("HEAD"))private void dil01$empty(Entity holder,GunData data,CallbackInfo ci){if(data.item instanceof Dil01Item&&!data.initialized())data.initialize();}
}
