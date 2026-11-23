package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import nl.smartstreamlabs.sbwdroneconfig.Hx2Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** HX owns bounded flight leases; SBW's independent keep-alive must not pin parked HX drones. */
@Mixin(value=VehicleEntity.class,remap=false)
public abstract class Hx2ChunkMixin {
 @Inject(method="keepChunkLoaded(Lnet/minecraft/world/phys/Vec3;)V",at=@At("HEAD"),cancellable=true,require=1)
 private void hx2$boundedLeases(Vec3 position,CallbackInfo ci){if((Object)this instanceof Hx2Entity)ci.cancel();}
}
