package nl.smartstreamlabs.sbwdroneconfig.mixin;

import nl.smartstreamlabs.sbwdroneconfig.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Only the new ADV-01 source: ordinary vehicles retain their normal armor after this reduction. */
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity",remap=false)
public abstract class Adv01VehicleDamageMixin {
    @ModifyVariable(method="hurt",at=@At("HEAD"),argsOnly=true,ordinal=0,require=1)
    private float adv01$flak(float damage,DamageSource source,float original) {
        return source.getDirectEntity() instanceof Adv01Shell&&!SbwCompat.isDrone((Entity)(Object)this)
            ?damage*Adv01Config.VEHICLE_SCALE.get().floatValue():damage;
    }
}
