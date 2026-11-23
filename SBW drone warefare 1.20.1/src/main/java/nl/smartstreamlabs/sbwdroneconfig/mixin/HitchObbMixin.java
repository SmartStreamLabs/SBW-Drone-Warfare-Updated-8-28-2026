package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Native support/crush queries also use this OBB predicate, separately from vanilla collision. */
@Mixin(value=com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.class,remap=false)
public abstract class HitchObbMixin {
 @Inject(method="isInObb(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;)Z",at=@At("HEAD"),cancellable=true)
 private void rescue$jointInternal(net.minecraft.world.entity.Entity other,net.minecraft.world.phys.Vec3 velocity,CallbackInfoReturnable<Boolean> ci){if((Object)this instanceof nl.smartstreamlabs.sbwdroneconfig.Id01Entity id&&!nl.smartstreamlabs.sbwdroneconfig.Id01Geometry.parts(id).stream().anyMatch(b->b.intersects(other.getBoundingBox().expandTowards(velocity)))){ci.setReturnValue(false);return;}if(nl.smartstreamlabs.sbwdroneconfig.Dc01Docking.sameCarrier((net.minecraft.world.entity.Entity)(Object)this,other)||nl.smartstreamlabs.sbwdroneconfig.HitchJoint.paired((net.minecraft.world.entity.Entity)(Object)this,other)||nl.smartstreamlabs.sbwdroneconfig.Id01Entity.contactPair((net.minecraft.world.entity.Entity)(Object)this,other))ci.setReturnValue(false);}
 @Inject(method="getVehicleTransform(F)Lorg/joml/Matrix4d;",at=@At("HEAD"),cancellable=true)
 private void dc01$mountedTransform(float partial,CallbackInfoReturnable<org.joml.Matrix4d> ci){var e=(net.minecraft.world.entity.Entity)(Object)this;var c=nl.smartstreamlabs.sbwdroneconfig.Dc01Docking.carrier(e);if(c!=null&&c.slot(e)>=0)ci.setReturnValue(c.dockMatrix(c.slot(e),partial));}
}
