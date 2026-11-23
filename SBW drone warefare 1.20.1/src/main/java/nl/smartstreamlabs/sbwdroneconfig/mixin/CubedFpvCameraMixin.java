package nl.smartstreamlabs.sbwdroneconfig.mixin;

import nl.smartstreamlabs.sbwdroneconfig.CubedFpvCameraViewMath;
import nl.smartstreamlabs.sbwdroneconfig.CubedFpvDroneEntity;
import nl.smartstreamlabs.sbwdroneconfig.DroneClientViewContext;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CubedFpvCameraMixin {
    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Inject(method = "setup", at = @At("TAIL"), require = 0)
    private void sbwdronewarfare$moveCubedFpvCamera(BlockGetter level, Entity entity, boolean detached, boolean mirror, float partialTick, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.getCameraType() != CameraType.FIRST_PERSON) {
            return;
        }

        CubedFpvDroneEntity drone = DroneClientViewContext.activeCubedFpvDrone(minecraft);
        if (drone == null || !drone.isAlive()) {
            return;
        }

        this.setPosition(CubedFpvCameraViewMath.fpvCameraPosition(drone, partialTick));
    }
}
