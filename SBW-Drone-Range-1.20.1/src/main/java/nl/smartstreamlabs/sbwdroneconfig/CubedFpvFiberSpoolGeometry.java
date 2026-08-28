package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

final class CubedFpvFiberSpoolGeometry {
    static final String MODEL_PART = "fiber_spool_module";
    static final String ENCLOSED_SPOOL_HOUSING_PART = "enclosed_spool_housing";
    static final String LEFT_SPOOL_CAP_PART = "left_spool_cap";
    static final String RIGHT_SPOOL_CAP_PART = "right_spool_cap";
    static final String CENTER_HUB_LEFT_PART = "center_hub_left";
    static final String CENTER_HUB_RIGHT_PART = "center_hub_right";
    static final String UPPER_MOUNT_PART = "upper_mount";
    static final String PROTECTIVE_FRAME_LEFT_PART = "protective_frame_left";
    static final String PROTECTIVE_FRAME_RIGHT_PART = "protective_frame_right";
    static final String PROTECTIVE_FRAME_BOTTOM_PART = "protective_frame_bottom";
    static final String REAR_CABLE_OUTLET_PART = "rear_cable_outlet";
    static final String ORANGE_CONNECTOR_PART = "orange_connector";
    static final String FIBER_CABLE_EXIT_POINT = "fiber_cable_exit";

    static final int HOUSING_SEGMENTS = 8;
    static final double POD_CENTER_X = 0.000D;
    static final double POD_CENTER_Y = -0.018D;
    static final double MAIN_HOUSING_MIN_Z = -0.062D;
    static final double MAIN_HOUSING_MAX_Z = 0.062D;
    static final double MAIN_HOUSING_RADIUS_X = 0.025D;
    static final double MAIN_HOUSING_RADIUS_Y = 0.020D;
    static final double LEFT_SPOOL_CAP_MIN_Z = -0.080D;
    static final double LEFT_SPOOL_CAP_MAX_Z = -0.062D;
    static final double RIGHT_SPOOL_CAP_MIN_Z = 0.062D;
    static final double RIGHT_SPOOL_CAP_MAX_Z = 0.080D;
    static final double SPOOL_CAP_RADIUS_X = 0.027D;
    static final double SPOOL_CAP_RADIUS_Y = 0.023D;
    static final double SIDE_GROOVE_RADIUS_X = 0.017D;
    static final double SIDE_GROOVE_RADIUS_Y = 0.014D;
    static final double SIDE_HUB_RADIUS_X = 0.006D;
    static final double SIDE_HUB_RADIUS_Y = 0.005D;

    static final Vec3 UPPER_MOUNT_MIN = new Vec3(-0.020D, 0.001D, -0.043D);
    static final Vec3 UPPER_MOUNT_MAX = new Vec3(0.020D, 0.008D, 0.043D);
    static final Vec3 LEFT_CLAMP_MIN = new Vec3(-0.020D, -0.009D, -0.052D);
    static final Vec3 LEFT_CLAMP_MAX = new Vec3(0.006D, 0.001D, -0.042D);
    static final Vec3 RIGHT_CLAMP_MIN = new Vec3(-0.020D, -0.009D, 0.042D);
    static final Vec3 RIGHT_CLAMP_MAX = new Vec3(0.006D, 0.001D, 0.052D);

    static final Vec3 LEFT_FRAME_UPRIGHT_MIN = new Vec3(-0.032D, -0.046D, -0.090D);
    static final Vec3 LEFT_FRAME_UPRIGHT_MAX = new Vec3(-0.026D, -0.007D, -0.084D);
    static final Vec3 RIGHT_FRAME_UPRIGHT_MIN = new Vec3(-0.032D, -0.046D, 0.084D);
    static final Vec3 RIGHT_FRAME_UPRIGHT_MAX = new Vec3(-0.026D, -0.007D, 0.090D);
    static final Vec3 LEFT_FRAME_BOTTOM_RAIL_MIN = new Vec3(-0.032D, -0.051D, -0.090D);
    static final Vec3 LEFT_FRAME_BOTTOM_RAIL_MAX = new Vec3(0.022D, -0.045D, -0.084D);
    static final Vec3 RIGHT_FRAME_BOTTOM_RAIL_MIN = new Vec3(-0.032D, -0.051D, 0.084D);
    static final Vec3 RIGHT_FRAME_BOTTOM_RAIL_MAX = new Vec3(0.022D, -0.045D, 0.090D);
    static final Vec3 BOTTOM_FRAME_REAR_CROSSBAR_MIN = new Vec3(-0.032D, -0.051D, -0.090D);
    static final Vec3 BOTTOM_FRAME_REAR_CROSSBAR_MAX = new Vec3(-0.026D, -0.045D, 0.090D);
    static final Vec3 BOTTOM_FRAME_FRONT_CROSSBAR_MIN = new Vec3(0.016D, -0.051D, -0.090D);
    static final Vec3 BOTTOM_FRAME_FRONT_CROSSBAR_MAX = new Vec3(0.022D, -0.045D, 0.090D);
    static final Vec3 LEFT_FRAME_SHOULDER_MIN = new Vec3(0.006D, -0.013D, -0.090D);
    static final Vec3 LEFT_FRAME_SHOULDER_MAX = new Vec3(0.022D, -0.007D, -0.084D);
    static final Vec3 RIGHT_FRAME_SHOULDER_MIN = new Vec3(0.006D, -0.013D, 0.084D);
    static final Vec3 RIGHT_FRAME_SHOULDER_MAX = new Vec3(0.022D, -0.007D, 0.090D);

    static final Vec3 OUTLET_MIN = new Vec3(-0.036D, -0.034D, -0.018D);
    static final Vec3 OUTLET_MAX = new Vec3(-0.021D, -0.022D, 0.018D);
    static final Vec3 CONNECTOR_MIN = new Vec3(-0.057D, -0.040D, -0.011D);
    static final Vec3 CONNECTOR_MAX = new Vec3(-0.036D, -0.027D, 0.011D);
    static final Vec3 CONNECTOR_CABLE_ORIGIN_LOCAL = new Vec3(-0.057D, -0.033D, 0.000D);
    static final Vec3 FIBER_CABLE_EXIT = new Vec3(-0.089D, -0.045D, 0.000D);
    static final Vec3 CABLE_EXIT_LOCAL = FIBER_CABLE_EXIT;

    private CubedFpvFiberSpoolGeometry() {
    }

    static Vec3 cableExitWorldPosition(CubedFpvDroneEntity drone, float partialTicks) {
        return modelOffsetWorldPosition(drone, partialTicks, CABLE_EXIT_LOCAL);
    }

    static Vec3 cableExitWorldPosition(CubedFpvDroneEntity drone) {
        return modelOffsetWorldPosition(drone, 1.0F, CABLE_EXIT_LOCAL);
    }

    private static Vec3 modelOffsetWorldPosition(CubedFpvDroneEntity drone, float partialTicks, Vec3 modelOffset) {
        Vec3 base = drone.getPosition(partialTicks).add(0.0D, CubedFpvDroneRenderMath.attachmentBaseYOffset(), 0.0D);
        Vector3f offset = transformModelOffset(drone, partialTicks, modelOffset);
        return base.add(offset.x(), offset.y(), offset.z());
    }

    private static Vector3f transformModelOffset(CubedFpvDroneEntity drone, float partialTicks, Vec3 modelOffset) {
        float scale = CubedFpvDroneRenderMath.droneVisualScale();
        Vector3f offset = new Vector3f(
                (float) (modelOffset.x * scale),
                (float) (modelOffset.y * scale),
                (float) (modelOffset.z * scale)
        );
        new Quaternionf()
                .rotateY(CubedFpvDroneRenderMath.renderYawDegrees(drone.getYaw(partialTicks)) * Mth.DEG_TO_RAD)
                .rotateX(CubedFpvDroneRenderMath.renderBodyPitchDegrees(drone.getBodyPitch(partialTicks)) * Mth.DEG_TO_RAD)
                .rotateZ(CubedFpvDroneRenderMath.renderRollDegrees(drone.getRoll(partialTicks)) * Mth.DEG_TO_RAD)
                .transform(offset);

        return offset;
    }
}
