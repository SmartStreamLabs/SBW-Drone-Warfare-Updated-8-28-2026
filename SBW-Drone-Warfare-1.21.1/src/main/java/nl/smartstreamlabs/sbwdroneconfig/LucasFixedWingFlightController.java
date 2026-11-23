package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.projectile.ProjectileEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

final class LucasFixedWingFlightController {
    private static final double IDLE_THROTTLE_DECAY = 0.008D;
    private static final double THROTTLE_STEP = 0.090D;
    private static final double BRAKE_THROTTLE_STEP = 0.18D;
    private static final double BRAKE_STOP_SPEED = 0.045D;
    private static final double AIR_VERTICAL_DAMPING = 0.84D;
    private static final double BASE_DESCENT = 0.014D;
    private static final double TAKEOFF_THROTTLE_FLOOR = 0.35D;
    private static final double MAX_CLIMB_SPEED = 0.42D;
    private static final double MAX_DESCENT_SPEED = -0.72D;
    private static final double DESCEND_DIVE_MULTIPLIER = 4.0D;
    private static final double MIN_DIVE_DESCENT_SPEED = 0.24D;
    private static final float MAX_PITCH_UP = -16.0F;
    private static final float MAX_PITCH_DOWN = 16.0F;
    private static final float MOUSE_YAW_SCALE = 0.22F;
    private static final float MOUSE_PITCH_SCALE = 0.14F;
    private static final float PITCH_RETURN_TO_LEVEL = 0.12F;
    private static final float CRASH_SWEEP_WIDTH = 0.85F;
    private static final double CRASH_SWEEP_HEIGHT = 0.35D;

    private LucasFixedWingFlightController() {
    }

    static void travel(LucasDroneEntity drone) {
        Player controller = SbwCompat.getDroneController(drone) instanceof Player player ? player : null;
        boolean activelyControlled = MissionManager.powered(drone) || controller != null && SbwCompat.isUsingLinkedMonitorForDrone(controller, drone);
        boolean poweredFlight = LucasFuelSystem.canUsePoweredFlight(drone);
        boolean braking = activelyControlled && isBrakeInput(drone);
        if(MissionManager.owns(drone))braking=MissionManager.braking(drone);

        if (activelyControlled && poweredFlight && !braking && (!MissionManager.owns(drone)||MissionManager.flight(drone).flightTicks<12)) {
            if (drone.beginLucasTakeoffAssist()) {
                drone.setLucasThrottle(Math.max(drone.getLucasThrottle(), (float) TAKEOFF_THROTTLE_FLOOR));
            }
        } else {
            drone.endLucasTakeoffAssist();
        }
        boolean takeoffAssistActive = drone.hasLucasTakeoffAssist();

        float throttle = updateThrottle(drone, activelyControlled && poweredFlight);
        if (LucasFuelSystem.isOutOfFuel(drone)) {
            throttle = 0.0F;
            takeoffAssistActive = false;
        }
        if (takeoffAssistActive) {
            throttle = Math.max(throttle, (float) TAKEOFF_THROTTLE_FLOOR);
            drone.tickLucasTakeoffAssist();
        }
        drone.setLucasThrottle(throttle);
        drone.getEntityData().set(VehicleEntity.POWER, throttle);

        double minSpeed = Math.max(AddonConfig.lucasMinSpeed(), 0.85D);
        double maxSpeed = Math.max(AddonConfig.lucasMaxSpeed(), 3.20D);
        double targetSpeed = braking ? 0.0D : resolveTargetSpeed(throttle, minSpeed, maxSpeed);
        if (takeoffAssistActive) {
            targetSpeed = Math.max(targetSpeed, Math.max(AddonConfig.lucasTakeoffForwardBoost(), 1.15D));
        }

        Vec3 currentMotion = drone.getDeltaMovement();
        double currentSpeed = currentMotion.horizontalDistance();
        float inputControlAuthority = AddonConfig.lucasStallEnabled() && !takeoffAssistActive
                ? 1.0F - (computeStallSeverity(currentSpeed, AddonConfig.lucasMinLiftSpeed()) * 0.45F)
                : 1.0F;

        updateOrientation(drone, inputControlAuthority, currentSpeed, maxSpeed);

        Vec3 desiredMotion = directionFromRotation(drone.getYRot(), drone.getXRot()).scale(targetSpeed);
        double response = Math.max(AddonConfig.lucasAcceleration(), 0.085D) * (activelyControlled ? 1.0D : 0.55D);
        Vec3 blendedMotion = currentMotion.add(desiredMotion.subtract(currentMotion).scale(response));

        double drag = Math.max(AddonConfig.lucasDrag(), 0.992D);
        Vec3 draggedMotion = blendedMotion.multiply(drag, 1.0D, drag);
        if (drone.onGround()) {
            double groundDrag = LucasFlightPhysicsMath.computeGroundDragForThrottle(throttle, true);
            draggedMotion = draggedMotion.multiply(groundDrag, 1.0D, groundDrag);
        }
        double brakeDrag = LucasFlightPhysicsMath.computeBrakeDrag(braking, drone.onGround());
        draggedMotion = draggedMotion.multiply(brakeDrag, 1.0D, brakeDrag);
        if (braking && throttle <= 0.02F && draggedMotion.horizontalDistance() < BRAKE_STOP_SPEED) {
            draggedMotion = new Vec3(0.0D, draggedMotion.y, 0.0D);
        }

        double blendedSpeed = draggedMotion.horizontalDistance();
        float updatedStallSeverity = AddonConfig.lucasStallEnabled() && !takeoffAssistActive
                ? computeStallSeverity(blendedSpeed, AddonConfig.lucasMinLiftSpeed())
                : 0.0F;
        float controlAuthority = 1.0F - (updatedStallSeverity * 0.45F);
        drone.setLucasStalling(updatedStallSeverity > 0.01F);
        drone.setLucasAirspeed((float) blendedSpeed);

        double liftBlend = Mth.clamp((throttle - 0.25D) / 0.75D, 0.0D, 1.0D);
        double assistedLift = activelyControlled && throttle > 0.25F
                ? Math.max(AddonConfig.lucasLiftStrength(), 0.060D) * Mth.lerp(liftBlend, 0.65D, 1.0D)
                : 0.0D;
        double pitchRadians = Math.toRadians(drone.getXRot());
        double pitchLift = -Math.sin(pitchRadians) * Math.max(blendedSpeed, targetSpeed) * 0.022D * controlAuthority;
        double climbCommand = 0.0D;
        if(MissionManager.powered(drone))climbCommand=MissionManager.climb(drone);
        else if (activelyControlled && poweredFlight) {
            if (drone.upInputDown()) {
                climbCommand += AddonConfig.lucasClimbSpeed();
            }
            if (drone.downInputDown()) {
                climbCommand -= Math.max(AddonConfig.lucasDescendSpeed() * DESCEND_DIVE_MULTIPLIER, MIN_DIVE_DESCENT_SPEED);
            }
        }
        double lowThrottleSink = LucasFlightPhysicsMath.computeThrottleSink(throttle);
        double stallDrop = AddonConfig.lucasStallEnabled() ? AddonConfig.lucasStallGravity() * updatedStallSeverity : 0.0D;
        double takeoffUpBoost = takeoffAssistActive ? Math.max(AddonConfig.lucasTakeoffUpBoost(), 0.24D) : 0.0D;
        double verticalVelocity = (currentMotion.y * AIR_VERTICAL_DAMPING)
                + assistedLift
                + pitchLift
                + climbCommand
                + takeoffUpBoost
                - BASE_DESCENT
                - lowThrottleSink
                - stallDrop;
        if (LucasFuelSystem.isOutOfFuel(drone)) {
            verticalVelocity -= 0.045D;
        }

        if (drone.onGround()) {
            if (activelyControlled && (takeoffAssistActive || throttle > 0.25F)) {
                double launchFloor = Math.max(0.035D, (assistedLift * 0.70D) + climbCommand + takeoffUpBoost);
                verticalVelocity = Math.max(verticalVelocity, launchFloor);
            } else {
                verticalVelocity = Math.max(-0.03D, verticalVelocity * 0.45D);
            }
        }

        Vec3 finalMotion = new Vec3(
                draggedMotion.x,
                Mth.clamp(verticalVelocity, MAX_DESCENT_SPEED, MAX_CLIMB_SPEED),
                draggedMotion.z
        );
        drone.setDeltaMovement(finalMotion);
        drone.setBodyXRot(drone.getXRot());
        drone.hurtMarked = true;

        applyCrashSweep(drone, controller);
    }

    static float clampThrottleNormalized(float throttle) {
        return LucasFlightPhysicsMath.clampThrottleNormalized(throttle);
    }

    static double resolveTargetSpeed(float throttleNormalized, double minSpeed, double maxSpeed) {
        return LucasFlightPhysicsMath.resolveTargetSpeed(throttleNormalized, minSpeed, maxSpeed);
    }

    static float computeStallSeverity(double speed, double minLiftSpeed) {
        return LucasFlightPhysicsMath.computeStallSeverity(speed, minLiftSpeed);
    }

    private static float updateThrottle(LucasDroneEntity drone, boolean activelyControlled) {
        if(MissionManager.owns(drone))return MissionManager.powered(drone)?MissionManager.throttle(drone):0;
        float throttle = drone.getLucasThrottle();
        boolean throttleUp = drone.forwardInputDown();
        boolean throttleDown = drone.backInputDown();
        if (throttleUp) {
            throttle += THROTTLE_STEP;
        }
        if (throttleDown) {
            throttle -= isBrakeInput(drone) ? BRAKE_THROTTLE_STEP : THROTTLE_STEP;
        }
        if (!activelyControlled) {
            throttle -= IDLE_THROTTLE_DECAY;
        }
        return clampThrottleNormalized(throttle);
    }

    private static boolean isBrakeInput(LucasDroneEntity drone) {
        return drone.backInputDown() && !drone.forwardInputDown();
    }

    private static void updateOrientation(LucasDroneEntity drone, float controlAuthority, double currentSpeed, double maxSpeed) {
        if(MissionManager.powered(drone))return;
        float yawInput = 0.0F;
        if (drone.leftInputDown()) {
            yawInput += 1.0F;
        }
        if (drone.rightInputDown()) {
            yawInput -= 1.0F;
        }

        float speedFactor = (float) Mth.clamp(currentSpeed / Math.max(0.001D, maxSpeed), 0.25D, 1.0D);
        float yawDelta = (yawInput * (float) AddonConfig.lucasTurnRate()) + (drone.getMouseMoveSpeedX() * MOUSE_YAW_SCALE);
        float leveledPitch = Mth.lerp(PITCH_RETURN_TO_LEVEL, drone.getXRot(), 0.0F);
        float pitchDelta = drone.getMouseMoveSpeedY() * MOUSE_PITCH_SCALE * (float) AddonConfig.lucasPitchRate();

        drone.setYRot(drone.getYRot() + (yawDelta * speedFactor * controlAuthority));
        drone.setXRot(Mth.clamp(leveledPitch + (pitchDelta * controlAuthority), MAX_PITCH_UP, MAX_PITCH_DOWN));

        float targetRoll = Mth.clamp(
                -((yawInput * 0.8F) + (drone.getMouseMoveSpeedX() * 0.18F)) * (float) AddonConfig.lucasRollVisualAmount() * speedFactor * controlAuthority,
                -(float) AddonConfig.lucasRollVisualAmount(),
                (float) AddonConfig.lucasRollVisualAmount()
        );
        drone.setZRot(Mth.lerp(0.18F, drone.getRoll(), targetRoll));
    }

    private static Vec3 directionFromRotation(float yawDegrees, float pitchDegrees) {
        float yawRadians = yawDegrees * ((float) Math.PI / 180F);
        float pitchRadians = pitchDegrees * ((float) Math.PI / 180F);
        double x = -Mth.sin(yawRadians) * Mth.cos(pitchRadians);
        double y = -Mth.sin(pitchRadians);
        double z = Mth.cos(yawRadians) * Mth.cos(pitchRadians);
        return new Vec3(x, y, z).normalize();
    }

    private static void applyCrashSweep(LucasDroneEntity drone, Player controller) {
        AABB aabb = AABB.ofSize(drone.getEyePosition(), CRASH_SWEEP_WIDTH, CRASH_SWEEP_HEIGHT, CRASH_SWEEP_WIDTH);
        for (Entity target : drone.level().getEntitiesOfClass(Entity.class, aabb, entity -> true)) {
            if (drone == target || target == null) {
                continue;
            }
            if (target instanceof ItemEntity
                    || target instanceof Projectile
                    || target instanceof ProjectileEntity
                    || isIgnoredSbwCrashSweepEntity(target)
                    || target.getType().is(ModTags.EntityTypes.DECOY)
                    || target instanceof AreaEffectCloud) {
                continue;
            }
            drone.hitEntityCrash(controller, target);
        }
    }

    private static boolean isIgnoredSbwCrashSweepEntity(Entity entity) {
        String className = entity.getClass().getName().toLowerCase(Locale.ROOT);
        String typeId = String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())).toLowerCase(Locale.ROOT);
        return className.contains("laserentity")
                || className.contains("c4entity")
                || typeId.contains("laser")
                || typeId.contains("c4");
    }
}
