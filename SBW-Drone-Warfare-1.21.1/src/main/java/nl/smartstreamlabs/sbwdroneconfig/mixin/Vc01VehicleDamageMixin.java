package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import nl.smartstreamlabs.sbwdroneconfig.Vc01Damage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** Both exact SBW distributions store computedAmount before setCrash. Ordinal 1
 * identifies the second FLOAT local, regardless of its different local-slot index.
 * This runs AFTER SBW armor and BEFORE component/onHurt damage, including bypass sources.
 */
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity",remap=false)
public abstract class Vc01VehicleDamageMixin {
 @ModifyVariable(method="hurt",ordinal=1,at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setCrash(Z)V"),require=1)
 private float vc01$damage(float computed,DamageSource source,float amount){return nl.smartstreamlabs.sbwdroneconfig.VehicleProfiles.incoming((Entity)(Object)this,Vc01Damage.filter((Entity)(Object)this,source,computed,amount));}
}
