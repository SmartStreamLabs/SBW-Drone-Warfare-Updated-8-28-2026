package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

public class AntiVehicleMortarPayloadEntity extends Entity {
    private static final String TAG_OWNER_UUID = "sbwdroneconfigMortarOwnerUuid";
    private static final String TAG_SOURCE_DRONE_UUID = "sbwdroneconfigMortarSourceDroneUuid";
    private static final String TAG_ACTIVE_FLIGHT_TICKS = "sbwdroneconfigMortarActiveFlightTicks";
    private static final int MAX_LIFETIME_TICKS = 200;
    private static final double VEHICLE_DAMAGE = 85.0D;
    private static final double ENTITY_DAMAGE = 12.0D;
    private static final double DAMAGE_RADIUS = 7.0D;
    private static final float BLOCK_DAMAGE_RADIUS = 2.0F;

    private UUID ownerUuid;
    private UUID sourceDroneUuid;
    private int activeFlightTicks;
    private boolean detonated;

    public AntiVehicleMortarPayloadEntity(EntityType<? extends AntiVehicleMortarPayloadEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = false;
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ownerUuid = tag.hasUUID(TAG_OWNER_UUID) ? tag.getUUID(TAG_OWNER_UUID) : null;
        this.sourceDroneUuid = tag.hasUUID(TAG_SOURCE_DRONE_UUID) ? tag.getUUID(TAG_SOURCE_DRONE_UUID) : null;
        this.activeFlightTicks = tag.getInt(TAG_ACTIVE_FLIGHT_TICKS);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.putUUID(TAG_OWNER_UUID, this.ownerUuid);
        }
        if (this.sourceDroneUuid != null) {
            tag.putUUID(TAG_SOURCE_DRONE_UUID, this.sourceDroneUuid);
        }
        tag.putInt(TAG_ACTIVE_FLIGHT_TICKS, this.activeFlightTicks);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        this.noPhysics = false;
        setNoGravity(false);
        activeFlightTicks++;

        Vec3 velocity = getDeltaMovement().add(0.0D, -0.055D, 0.0D);
        setDeltaMovement(velocity);
        move(MoverType.SELF, velocity);
        setDeltaMovement(getDeltaMovement().scale(0.985D));

        if (tickCount > 3 && hasImpactEntity()) {
            detonate();
            return;
        }

        if (horizontalCollision || verticalCollision || onGround() || activeFlightTicks > MAX_LIFETIME_TICKS) {
            detonate();
        }
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public void setSourceDroneUuid(UUID sourceDroneUuid) {
        this.sourceDroneUuid = sourceDroneUuid;
    }

    public void releaseFromDrone(Vec3 velocity) {
        this.activeFlightTicks = 0;
        this.noPhysics = false;
        setNoGravity(false);
        setDeltaMovement(velocity);
    }

    private boolean hasImpactEntity() {
        AABB box = getBoundingBox().inflate(0.35D);
        for (Entity entity : level().getEntities(this, box, this::canTriggerImpactWith)) {
            return true;
        }
        return false;
    }

    private boolean canTriggerImpactWith(Entity entity) {
        if (entity == null || !entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        UUID uuid = entity.getUUID();
        return !uuid.equals(ownerUuid) && !uuid.equals(sourceDroneUuid);
    }

    private void detonate() {
        if (detonated || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        detonated = true;

        AABB damageBox = getBoundingBox().inflate(DAMAGE_RADIUS);
        for (Entity target : serverLevel.getEntities(this, damageBox, this::canDamage)) {
            boolean vehicleLike = isVehicleLike(target);
            float damage = (float) (vehicleLike ? VEHICLE_DAMAGE : ENTITY_DAMAGE);
            if (damage > 0.0F) {
                target.hurt(damageSources().explosion(this, this), damage);
                if (vehicleLike) {
                    applyVehicleDirectDamage(target, damage);
                }
            }
        }

        serverLevel.explode(this, getX(), getY(), getZ(), BLOCK_DAMAGE_RADIUS, Level.ExplosionInteraction.TNT);
        discard();
    }

    private boolean canDamage(Entity entity) {
        return canTriggerImpactWith(entity);
    }

    private static boolean isVehicleLike(Entity entity) {
        if (!canBeVehicleTarget(entity)) {
            return false;
        }

        String combined = combinedEntityIdAndClass(entity);
        boolean looksLikeVehicle = combined.contains("superbwarfare")
                && (combined.contains("vehicle")
                || combined.contains("tank")
                || combined.contains("apc")
                || combined.contains("mrap")
                || combined.contains("truck")
                || combined.contains("car")
                || combined.contains("plane")
                || combined.contains("aircraft")
                || combined.contains("helicopter")
                || combined.contains("artillery")
                || combined.contains("cannon")
                || combined.contains("boat")
                || combined.contains("lav"));
        return looksLikeVehicle || hasVehicleHealthApi(entity);
    }

    private static boolean hasVehicleHealthApi(Entity entity) {
        if (!canBeVehicleTarget(entity) || !combinedEntityIdAndClass(entity).contains("superbwarfare")) {
            return false;
        }
        return findNoArgNumberMethod(entity.getClass(), "getHealth", "getHp", "getHP", "getVehicleHealth", "getEntityHealth") != null
                && findNumericSetterMethod(entity.getClass(), "setHealth", "setHp", "setHP", "setVehicleHealth", "setEntityHealth") != null;
    }

    private static boolean applyVehicleDirectDamage(Entity entity, double damage) {
        if (!hasVehicleHealthApi(entity) || damage <= 0.0D) {
            return false;
        }

        Method getter = findNoArgNumberMethod(entity.getClass(), "getHealth", "getHp", "getHP", "getVehicleHealth", "getEntityHealth");
        Method setter = findNumericSetterMethod(entity.getClass(), "setHealth", "setHp", "setHP", "setVehicleHealth", "setEntityHealth");
        if (getter == null || setter == null) {
            return false;
        }

        try {
            Object value = getter.invoke(entity);
            if (!(value instanceof Number number)) {
                return false;
            }
            invokeNumericSetter(entity, setter, Math.max(0.0D, number.doubleValue() - damage));
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean canBeVehicleTarget(Entity entity) {
        return entity != null && !(entity instanceof Player) && !(entity instanceof LivingEntity) && !SbwCompat.isDrone(entity);
    }

    private static String combinedEntityIdAndClass(Entity entity) {
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String id = typeId == null ? "" : typeId.toString().toLowerCase(Locale.ROOT);
        String className = entity.getClass().getName().toLowerCase(Locale.ROOT);
        return id + " " + className;
    }

    private static Method findNoArgNumberMethod(Class<?> type, String... names) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && isNamed(method, names) && isNumericType(method.getReturnType())) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }

    private static Method findNumericSetterMethod(Class<?> type, String... names) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 1 && isNamed(method, names) && isNumericType(method.getParameterTypes()[0])) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }

    private static boolean isNamed(Method method, String... names) {
        for (String name : names) {
            if (method.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNumericType(Class<?> type) {
        return type == float.class
                || type == double.class
                || type == int.class
                || type == long.class
                || type == Float.class
                || type == Double.class
                || type == Integer.class
                || type == Long.class
                || Number.class.isAssignableFrom(type);
    }

    private static void invokeNumericSetter(Entity entity, Method setter, double value) throws ReflectiveOperationException {
        Class<?> type = setter.getParameterTypes()[0];
        if (type == float.class || type == Float.class) {
            setter.invoke(entity, (float) value);
        } else if (type == double.class || type == Double.class) {
            setter.invoke(entity, value);
        } else if (type == int.class || type == Integer.class) {
            setter.invoke(entity, (int) Math.ceil(value));
        } else if (type == long.class || type == Long.class) {
            setter.invoke(entity, (long) Math.ceil(value));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
