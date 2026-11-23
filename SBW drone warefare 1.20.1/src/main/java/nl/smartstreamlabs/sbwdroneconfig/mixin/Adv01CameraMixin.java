package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import nl.smartstreamlabs.sbwdroneconfig.Adv01Client;
@Mixin(value=net.minecraft.client.Camera.class,priority=1200)
public abstract class Adv01CameraMixin {
    @Shadow protected abstract void setPosition(double x,double y,double z);
    @Shadow protected abstract void setRotation(float yaw,float pitch);
    @Inject(method="setup",at=@At("TAIL"))
    private void adv01$lens(net.minecraft.world.level.BlockGetter level,net.minecraft.world.entity.Entity entity,boolean detached,boolean inverse,float partial,CallbackInfo ci){
        if(detached||!Adv01Client.lens())return;var p=Adv01Client.lensPosition(partial);var r=Adv01Client.lensRotation(partial);if(p!=null){setPosition(p.x,p.y,p.z);setRotation(r[0],r[1]);}
    }
}
