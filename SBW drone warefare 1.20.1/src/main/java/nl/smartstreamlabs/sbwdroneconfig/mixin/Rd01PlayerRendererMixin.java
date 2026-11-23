package nl.smartstreamlabs.sbwdroneconfig.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import nl.smartstreamlabs.sbwdroneconfig.*;

@Mixin(value=net.minecraft.client.renderer.entity.player.PlayerRenderer.class,priority=1100)
public abstract class Rd01PlayerRendererMixin {
    @Inject(method="setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",at=@At("HEAD"),cancellable=true)
    private void rd01$seat(net.minecraft.client.player.AbstractClientPlayer p,com.mojang.blaze3d.vertex.PoseStack pose,float age,float yaw,float partial,CallbackInfo c) {
        if(p.getVehicle() instanceof Rd01Entity d) {
            var target=Rd01Pose.hips(d,partial).subtract(p.getPosition(partial));pose.translate(target.x,target.y,target.z);
            pose.mulPose(new org.joml.Quaternionf(Rd01Pose.harness(d,partial).getNormalizedRotation(new org.joml.Quaterniond())));
            pose.translate(0,-Rd01Pose.PLAYER_HIP,0);c.cancel();
        }
    }
}
