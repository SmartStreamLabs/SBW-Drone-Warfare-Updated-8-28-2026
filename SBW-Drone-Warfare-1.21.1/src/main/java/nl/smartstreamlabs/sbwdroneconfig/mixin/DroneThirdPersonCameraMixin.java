package nl.smartstreamlabs.sbwdroneconfig.mixin;

import nl.smartstreamlabs.sbwdroneconfig.DroneClientViewContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class DroneThirdPersonCameraMixin {
    private static final double DRONE_THIRD_PERSON_DISTANCE = 96.0D;
    private static final double DRONE_THIRD_PERSON_HEIGHT = 8.0D;
    private static final double DRONE_THIRD_PERSON_TARGET_HEIGHT = 2.4D;

    @Shadow(remap = false)
    protected abstract void setPosition(double x, double y, double z);

    @Shadow(remap = false)
    public abstract float getYRot();

    @Shadow(remap = false)
    public abstract float getXRot();

    @Inject(method = "setup", at = @At("TAIL"), require = 0, remap = false)
    private void sbwdronewarfare$moveDroneThirdPersonCamera(BlockGetter level, Entity cameraEntity, boolean detached, boolean mirror, float partialTick, CallbackInfo ci) {
        if (DroneClientViewContext.isReadOnlyCameraInterceptActive()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.options.getCameraType()==net.minecraft.client.CameraType.FIRST_PERSON)return;
        Entity drone = DroneClientViewContext.activeLucasDrone(minecraft);
        if (drone == null || !drone.isAlive()) {
            return;
        }

        double targetHeight = Math.max(DRONE_THIRD_PERSON_TARGET_HEIGHT, drone.getBbHeight() * 0.6D);
        Vec3 target = drone.getPosition(partialTick).add(0.0D, targetHeight, 0.0D);
        float yaw = this.getYRot();
        float pitch = this.getXRot();
        float yawRadians = yaw * Mth.DEG_TO_RAD;
        float pitchRadians = pitch * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(
                -Mth.sin(yawRadians) * Mth.cos(pitchRadians),
                -Mth.sin(pitchRadians),
                Mth.cos(yawRadians) * Mth.cos(pitchRadians)
        ).normalize();
        Vec3 position = target.subtract(forward.scale(DRONE_THIRD_PERSON_DISTANCE)).add(0.0D, DRONE_THIRD_PERSON_HEIGHT, 0.0D);
        this.setPosition(position.x, position.y, position.z);
    }
}
