package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class DeliveryDroneModel extends GeoModel<DeliveryDroneEntity> {
    private static final String[] PROPELLER_BONES = {
            "delivery_propeller_0",
            "delivery_propeller_1",
            "delivery_propeller_2",
            "delivery_propeller_3"
    };
    private static final float[] PROPELLER_DIRECTIONS = {1.0F, -1.0F, -1.0F, 1.0F};
    private static final Map<Entity, PropellerSpinState> SPIN_STATES = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public ResourceLocation getModelResource(DeliveryDroneEntity animatable) {
        return DeliveryDroneDefinition.model();
    }

    @Override
    public ResourceLocation getTextureResource(DeliveryDroneEntity animatable) {
        return DeliveryDroneDefinition.texture();
    }

    @Override
    public ResourceLocation getAnimationResource(DeliveryDroneEntity animatable) {
        return DeliveryDroneDefinition.animation();
    }

    @Override
    public void setCustomAnimations(DeliveryDroneEntity drone, long instanceId, AnimationState<DeliveryDroneEntity> animationState) {
        super.setCustomAnimations(drone, instanceId, animationState);

        PropellerSpinState spinState = SPIN_STATES.computeIfAbsent(drone, ignored -> new PropellerSpinState());
        float partialTick = animationState == null ? 0.0F : animationState.getPartialTick();
        float angle = spinState.update(drone.tickCount + partialTick, targetSpeed(drone));

        for (int index = 0; index < PROPELLER_BONES.length; index++) {
            CoreGeoBone bone = getAnimationProcessor().getBone(PROPELLER_BONES[index]);
            if (bone != null) {
                bone.setRotY(angle * PROPELLER_DIRECTIONS[index]);
            }
        }
    }

    private static float targetSpeed(DeliveryDroneEntity drone) {
        if (isFlying(drone)) {
            return 2.35F;
        }
        if (isActive(drone)) {
            return 0.75F;
        }
        return 0.0F;
    }

    private static boolean isActive(DeliveryDroneEntity drone) {
        return getController(drone) != null
                || hasInput(drone)
                || drone.getDeltaMovement().lengthSqr() > 1.0E-5D
                || drone.engineRunning();
    }

    private static boolean isFlying(DeliveryDroneEntity drone) {
        return !drone.onGround()
                || Math.abs(drone.getDeltaMovement().y) > 0.02D
                || drone.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D
                || hasInput(drone);
    }

    private static boolean hasInput(DeliveryDroneEntity drone) {
        return invokeBooleanGetter(drone, "forwardInputDown")
                || invokeBooleanGetter(drone, "backInputDown")
                || invokeBooleanGetter(drone, "leftInputDown")
                || invokeBooleanGetter(drone, "rightInputDown")
                || invokeBooleanGetter(drone, "upInputDown")
                || invokeBooleanGetter(drone, "downInputDown");
    }

    private static Object getController(DeliveryDroneEntity drone) {
        try {
            Method method = drone.getClass().getMethod("getController");
            return method.invoke(drone);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static boolean invokeBooleanGetter(DeliveryDroneEntity drone, String methodName) {
        try {
            Method method = drone.getClass().getMethod(methodName);
            Object result = method.invoke(drone);
            return result instanceof Boolean value && value;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static final class PropellerSpinState {
        private static final float ACCELERATION_PER_TICK = 0.18F;
        private static final float DECELERATION_PER_TICK = 0.13F;
        private float lastRenderTick = Float.NaN;
        private float speed;
        private float angle;

        private float update(float renderTick, float targetSpeed) {
            float deltaTicks = Float.isNaN(lastRenderTick) ? 0.0F : Math.max(0.0F, Math.min(renderTick - lastRenderTick, 4.0F));
            lastRenderTick = renderTick;

            float step = (targetSpeed > speed ? ACCELERATION_PER_TICK : DECELERATION_PER_TICK) * deltaTicks;
            if (speed < targetSpeed) {
                speed = Math.min(targetSpeed, speed + step);
            } else if (speed > targetSpeed) {
                speed = Math.max(targetSpeed, speed - step);
            }

            angle += speed * deltaTicks;
            float twoPi = (float) (Math.PI * 2.0D);
            if (angle > twoPi || angle < -twoPi) {
                angle %= twoPi;
            }
            return angle;
        }
    }
}
