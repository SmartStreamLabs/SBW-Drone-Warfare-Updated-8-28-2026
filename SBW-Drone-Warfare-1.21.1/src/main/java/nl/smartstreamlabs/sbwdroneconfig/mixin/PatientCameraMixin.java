package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=net.minecraft.client.Camera.class,remap=false)
public abstract class PatientCameraMixin {
 @Shadow public abstract net.minecraft.world.phys.Vec3 getPosition();
 @Shadow protected abstract void setPosition(double x,double y,double z);
 @Inject(method="setup",at=@At("TAIL"))
 private void rescue$head(net.minecraft.world.level.BlockGetter level,net.minecraft.world.entity.Entity e,boolean detached,boolean inverse,float partial,CallbackInfo ci){if(e instanceof net.minecraft.world.entity.player.Player&&e.getVehicle() instanceof nl.smartstreamlabs.sbwdroneconfig.Rd01Entity d){var head=nl.smartstreamlabs.sbwdroneconfig.Rd01Pose.head(d,partial);var at=detached?getPosition().add(head.subtract(e.getEyePosition(partial))):head;setPosition(at.x,at.y,at.z);return;}if(e instanceof net.minecraft.world.entity.player.Player&&e.getVehicle() instanceof nl.smartstreamlabs.sbwdroneconfig.MedicalTrailerEntity t){var head=t.patientCamera();var at=detached?getPosition().add(head.subtract(e.getEyePosition(partial))):head;setPosition(at.x,at.y,at.z);}}
}
