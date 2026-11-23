package nl.smartstreamlabs.sbwdroneconfig.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.phys.Vec3;
import nl.smartstreamlabs.sbwdroneconfig.*;

/** Runs after Camera's vanilla context initialization but before SBW's cancellable vehicle-camera hook. */
@Mixin(value=net.minecraft.client.Camera.class,priority=1200,remap=false)
public abstract class Rd01PassengerCameraMixin {
    @Shadow protected abstract void setPosition(double x,double y,double z);
    @Shadow protected abstract void setRotation(float yaw,float pitch);
    @Inject(method="setup",at=@At(value="FIELD",target="Lnet/minecraft/client/Camera;partialTickTime:F",opcode=181,shift=At.Shift.AFTER),cancellable=true)
    private void rd01$passenger(net.minecraft.world.level.BlockGetter level,net.minecraft.world.entity.Entity entity,boolean detached,boolean inverse,float partial,CallbackInfo c) {
        if(Rd01Client.active()&&Rd01Client.vehicle()!=null){
            var at=Rd01Client.viewPosition(partial);var angles=Rd01Client.viewAngles(partial);
            setRotation(angles[0],angles[1]);setPosition(at.x,at.y,at.z);c.cancel();return;
        }
        if(!(entity instanceof net.minecraft.world.entity.player.Player p)||!(p.getVehicle() instanceof Rd01Entity d))return;
        float yaw=p.getViewYRot(partial)-(float)com.atsuishio.superbwarfare.event.ClientMouseHandler.freeCameraYaw;
        float pitch=net.minecraft.util.Mth.clamp(p.getViewXRot(partial)+(float)com.atsuishio.superbwarfare.event.ClientMouseHandler.freeCameraPitch,-89,89);
        if(detached&&inverse){yaw+=180;pitch=-pitch;}
        Vec3 head=Rd01Pose.head(d,partial),at=head;
        if(detached)at=RoverViewCollision.clip(d,head,head.subtract(Vec3.directionFromRotation(pitch,yaw).scale(4)));
        setRotation(yaw,pitch);setPosition(at.x,at.y,at.z);c.cancel();
    }
}
