package nl.smartstreamlabs.sbwdroneconfig.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import nl.smartstreamlabs.sbwdroneconfig.*;

@Mixin(Entity.class)
public abstract class Rd01PassengerBodyMixin {
    @Unique private boolean rd01$mounted;
    @Unique private Rd01Entity rd01$drone(){var e=(Entity)(Object)this;return e instanceof net.minecraft.world.entity.player.Player&&e.getVehicle() instanceof Rd01Entity d?d:null;}
    @Inject(method="makeBoundingBox",at=@At("HEAD"),cancellable=true)
    private void rd01$bounds(CallbackInfoReturnable<AABB> c){var d=rd01$drone();if(d!=null)c.setReturnValue(Rd01Geometry.passengerBounds(d));}
    @Inject(method="getEyePosition()Lnet/minecraft/world/phys/Vec3;",at=@At("HEAD"),cancellable=true)
    private void rd01$eye(CallbackInfoReturnable<Vec3> c){var d=rd01$drone();if(d!=null)c.setReturnValue(Rd01Pose.head(d,1));}
    @Inject(method="getEyePosition(F)Lnet/minecraft/world/phys/Vec3;",at=@At("HEAD"),cancellable=true)
    private void rd01$eyePartial(float partial,CallbackInfoReturnable<Vec3> c){var d=rd01$drone();if(d!=null)c.setReturnValue(Rd01Pose.head(d,partial));}
    @Inject(method="getEyeHeight()F",at=@At("HEAD"),cancellable=true)
    private void rd01$height(CallbackInfoReturnable<Float> c){var d=rd01$drone();if(d!=null)c.setReturnValue((float)(Rd01Pose.head(d,1).y-((Entity)(Object)this).getY()));}
    @Inject(method="stopRiding",at=@At("HEAD")) private void rd01$remember(CallbackInfo c){rd01$mounted=rd01$drone()!=null;}
    @Inject(method="stopRiding",at=@At("RETURN")) private void rd01$reset(CallbackInfo c){if(rd01$mounted&&rd01$drone()==null){var e=(Entity)(Object)this;e.setBoundingBox(e.getDimensions(e.getPose()).makeBoundingBox(e.position()));}rd01$mounted=false;}
}
