package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class CubedFpvCameraViewMath {
    public static final double CAMERA_LOCAL_X = 0.0D;
    public static final double CAMERA_LOCAL_Y = 0.42D;
    public static final double CAMERA_LOCAL_Z = 0.34D;
    public static final double MIN_FPV_FOV = 105.0D;
    public static final double MAX_FPV_FOV = 115.0D;
    public static final double DEFAULT_FPV_FOV = 112.0D;

    private CubedFpvCameraViewMath() {
    }

    public static Vec3 fpvCameraPosition(CubedFpvDroneEntity drone, float partialTick) {
        Vec3 base = drone.getPosition(partialTick);
        Vector3f local = new Vector3f((float) CAMERA_LOCAL_X, (float) CAMERA_LOCAL_Y, (float) CAMERA_LOCAL_Z);
        local.rotate(Axis.YP.rotationDegrees(-Mth.rotLerp(partialTick, drone.yRotO, drone.getYRot())));
        return base.add(local.x(), local.y(), local.z());
    }

    public static double clampFov(double configuredFov) {
        return Mth.clamp(configuredFov, MIN_FPV_FOV, MAX_FPV_FOV);
    }
}
