package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.UUID;

public class FiberOpticCableSegmentEntity extends Entity {
    private static final String TAG_DRONE_UUID = "sbwdroneconfigFiberCableDroneUuid";
    private static final String TAG_SEGMENT_INDEX = "sbwdroneconfigFiberCableSegmentIndex";
    private static final String TAG_SEGMENT_HEALTH = "sbwdroneconfigFiberCableSegmentHealth";
    private static final String TAG_DETACHED = "sbwdroneconfigFiberCableDetached";
    private static final String TAG_FORMER_DRONE_UUID = "sbwdroneconfigFiberCableFormerDroneUuid";
    private static final String TAG_DETACHED_AGE = "sbwdroneconfigFiberCableDetachedAge";
    private static final String TAG_DETACHED_LIFETIME_TICKS = "sbwdroneconfigFiberCableDetachedLifetimeTicks";
    private static final String TAG_DETACHED_EXPLOSION_IMMUNITY_TICKS = "sbwdroneconfigFiberCableDetachedExplosionImmunityTicks";
    private static final String TAG_SEGMENT_DIRECTION_X = "sbwdroneconfigFiberCableSegmentDirectionX";
    private static final String TAG_SEGMENT_DIRECTION_Y = "sbwdroneconfigFiberCableSegmentDirectionY";
    private static final String TAG_SEGMENT_DIRECTION_Z = "sbwdroneconfigFiberCableSegmentDirectionZ";
    private static final EntityDataAccessor<Float> DATA_SEGMENT_LENGTH =
            SynchedEntityData.defineId(FiberOpticCableSegmentEntity.class, EntityDataSerializers.FLOAT);

    private UUID droneUuid;
    private UUID formerDroneUuid;
    private int segmentIndex;
    private float segmentHealth = (float) AddonConfig.fiberCableSegmentHealth();
    private boolean detached;
    private int detachedAge;
    private int detachedLifetimeTicks = -1;
    private int detachedExplosionImmunityTicks;
    private float lastAppliedHitboxSize = -1.0F;
    private Vec3 segmentDirection = new Vec3(0.0D, 0.0D, 1.0D);
    private String lastDamageSourceId = "none";
    private String lastDirectEntityType = "none";
    private String lastDirectEntityUuid = "none";
    private String lastAttackerType = "none";
    private String lastAttackerUuid = "none";
    private float lastDamageAmount = 0.0F;

    public FiberOpticCableSegmentEntity(EntityType<? extends FiberOpticCableSegmentEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SEGMENT_LENGTH, 0.25F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(TAG_DRONE_UUID)) {
            this.droneUuid = tag.getUUID(TAG_DRONE_UUID);
        } else {
            this.droneUuid = null;
        }
        if (tag.hasUUID(TAG_FORMER_DRONE_UUID)) {
            this.formerDroneUuid = tag.getUUID(TAG_FORMER_DRONE_UUID);
        } else {
            this.formerDroneUuid = null;
        }
        this.segmentIndex = tag.getInt(TAG_SEGMENT_INDEX);
        this.segmentHealth = tag.contains(TAG_SEGMENT_HEALTH)
                ? tag.getFloat(TAG_SEGMENT_HEALTH)
                : (float) AddonConfig.fiberCableSegmentHealth();
        this.detached = tag.getBoolean(TAG_DETACHED);
        this.detachedAge = tag.getInt(TAG_DETACHED_AGE);
        this.detachedLifetimeTicks = tag.contains(TAG_DETACHED_LIFETIME_TICKS)
                ? tag.getInt(TAG_DETACHED_LIFETIME_TICKS)
                : -1;
        this.detachedExplosionImmunityTicks = tag.getInt(TAG_DETACHED_EXPLOSION_IMMUNITY_TICKS);
        if (tag.contains(TAG_SEGMENT_DIRECTION_X)
                && tag.contains(TAG_SEGMENT_DIRECTION_Y)
                && tag.contains(TAG_SEGMENT_DIRECTION_Z)) {
            Vec3 savedDirection = new Vec3(
                    tag.getDouble(TAG_SEGMENT_DIRECTION_X),
                    tag.getDouble(TAG_SEGMENT_DIRECTION_Y),
                    tag.getDouble(TAG_SEGMENT_DIRECTION_Z)
            );
            if (savedDirection.lengthSqr() > 1.0E-6D) {
                this.segmentDirection = savedDirection.normalize();
            }
        }
        this.entityData.set(DATA_SEGMENT_LENGTH, Math.max(0.18F, tag.getFloat("SegmentLength")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.droneUuid != null) {
            tag.putUUID(TAG_DRONE_UUID, this.droneUuid);
        }
        if (this.formerDroneUuid != null) {
            tag.putUUID(TAG_FORMER_DRONE_UUID, this.formerDroneUuid);
        }
        tag.putInt(TAG_SEGMENT_INDEX, this.segmentIndex);
        tag.putFloat(TAG_SEGMENT_HEALTH, this.segmentHealth);
        tag.putBoolean(TAG_DETACHED, this.detached);
        tag.putInt(TAG_DETACHED_AGE, this.detachedAge);
        tag.putInt(TAG_DETACHED_LIFETIME_TICKS, this.detachedLifetimeTicks);
        tag.putInt(TAG_DETACHED_EXPLOSION_IMMUNITY_TICKS, this.detachedExplosionImmunityTicks);
        Vec3 direction = getSegmentDirection();
        tag.putDouble(TAG_SEGMENT_DIRECTION_X, direction.x);
        tag.putDouble(TAG_SEGMENT_DIRECTION_Y, direction.y);
        tag.putDouble(TAG_SEGMENT_DIRECTION_Z, direction.z);
        tag.putFloat("SegmentLength", this.getSegmentLength());
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);
        refreshHitboxIfNeeded();
        this.setBoundingBox(makeBoundingBox());

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (isDetached()) {
            settleDetachedOnGroundDuringCrashWindow();
            tickDetachedLifetime();
            tickDetachedExplosionImmunity();
            return;
        }

        Entity drone = getTrackedDrone(serverLevel);
        if (drone == null || !drone.isAlive() || !FiberOpticLinkSystem.isFiberSessionActive(drone)) {
            if (AddonConfig.keepDetachedFiberCableOnDroneDeath()) {
                detachFromDroneDeath();
            } else {
                this.discard();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) {
            return true;
        }
        if (!(this.level() instanceof ServerLevel serverLevel) || !canTakeDamageFrom(serverLevel, source)) {
            return false;
        }

        float appliedDamage = Math.max(0.1F, amount);
        recordDamageContext(source, appliedDamage);
        this.segmentHealth -= appliedDamage;

        if (AddonConfig.debugFiberCableSegments()) {
            SbwDroneRangeConfig.LOGGER.info(
                    "Fiber cable segment damaged drone={} segment={} source={} amount={} remainingHealth={}",
                    this.droneUuid,
                    this.segmentIndex,
                    source.getMsgId(),
                    String.format(java.util.Locale.ROOT, "%.2f", appliedDamage),
                    String.format(java.util.Locale.ROOT, "%.2f", this.segmentHealth)
            );
        }

        if (this.segmentHealth <= 0.0F) {
            if (AddonConfig.fiberCableBreaksWhenAnySegmentDestroyed()) {
                FiberOpticLinkSystem.breakCableFromSegment(serverLevel, this);
            } else {
                this.discard();
            }
        }

        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity drone = getTrackedDrone(serverLevel);
            if (drone != null && FiberOpticLinkSystem.isProtectedCableBreaker(serverLevel, drone, attacker)) {
                return true;
            }
        }
        return super.skipAttackInteraction(attacker);
    }

    @Override
    public float getPickRadius() {
        return (float) Math.max(0.35D, AddonConfig.fiberCableSegmentHitboxSize());
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float hitboxSize = (float) Math.max(0.1D, AddonConfig.fiberCableSegmentHitboxSize());
        return EntityDimensions.scalable(hitboxSize, hitboxSize);
    }

    public void applySegmentPose(UUID ownerDroneUuid, int newSegmentIndex, FiberOpticCableSegmentLayout.SegmentPose pose) {
        this.droneUuid = ownerDroneUuid;
        this.formerDroneUuid = null;
        this.detached = false;
        this.detachedAge = 0;
        this.detachedLifetimeTicks = -1;
        this.detachedExplosionImmunityTicks = 0;
        this.segmentIndex = newSegmentIndex;
        this.segmentHealth = Math.min(this.segmentHealth, (float) AddonConfig.fiberCableSegmentHealth());
        this.entityData.set(DATA_SEGMENT_LENGTH, Math.max(0.18F, pose.length()));
        this.segmentDirection = pose.direction();

        Vec3 center = pose.center();
        Vec3 direction = pose.direction();
        float yaw = (float) Math.toDegrees(Mth.atan2(direction.x, direction.z));
        float horizontalDistance = (float) Math.sqrt((direction.x * direction.x) + (direction.z * direction.z));
        float pitch = (float) -Math.toDegrees(Mth.atan2(direction.y, horizontalDistance));

        this.moveTo(center.x, center.y, center.z, yaw, pitch);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
        this.setBoundingBox(makeBoundingBox());
    }

    public UUID getDroneUuid() {
        return this.droneUuid;
    }

    public UUID getFormerDroneUuid() {
        return this.formerDroneUuid;
    }

    public int getSegmentIndex() {
        return this.segmentIndex;
    }

    public float getSegmentLength() {
        return this.entityData.get(DATA_SEGMENT_LENGTH);
    }

    public boolean isDetached() {
        return this.detached;
    }

    public void detachFromDroneDeath() {
        this.detached = true;
        this.formerDroneUuid = this.droneUuid;
        this.droneUuid = null;
        this.detachedAge = 0;
        this.detachedLifetimeTicks = AddonConfig.detachedFiberCableLifetimeTicks();
        this.detachedExplosionImmunityTicks = 60;
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.noPhysics = true;
        refreshHitboxIfNeeded();
        this.setBoundingBox(makeBoundingBox());
        this.hurtMarked = true;
    }

    public void detachToGround(int lifetimeTicks) {
        this.detached = true;
        this.detachedAge = 0;
        this.detachedLifetimeTicks = lifetimeTicks;

        Vec3 groundedPosition = snapDetachedPositionToGround(this.level(), this.position());
        Vec3 horizontalDirection = flattenDirection(this.segmentDirection);
        float yaw = (float) Math.toDegrees(Mth.atan2(horizontalDirection.x, horizontalDirection.z));

        this.segmentDirection = horizontalDirection;
        this.moveTo(groundedPosition.x, groundedPosition.y, groundedPosition.z, yaw, 0.0F);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setXRot(0.0F);
        this.yRotO = yaw;
        this.xRotO = 0.0F;
        this.setBoundingBox(makeBoundingBox());
        this.hurtMarked = true;
    }

    public String describeLastDamageContext() {
        return "source=" + this.lastDamageSourceId
                + ", amount=" + String.format(java.util.Locale.ROOT, "%.2f", this.lastDamageAmount)
                + ", directType=" + this.lastDirectEntityType
                + ", directUuid=" + this.lastDirectEntityUuid
                + ", attackerType=" + this.lastAttackerType
                + ", attackerUuid=" + this.lastAttackerUuid;
    }

    @Override
    protected AABB makeBoundingBox() {
        Vec3 direction = getSegmentDirection();
        double halfLength = Math.max(0.09D, this.getSegmentLength() * 0.5D);
        Vec3 center = this.position();
        Vec3 start = center.subtract(direction.scale(halfLength));
        Vec3 end = center.add(direction.scale(halfLength));
        double halfThickness = Math.max(0.08D, AddonConfig.fiberCableSegmentHitboxSize() * 0.5D);

        return new AABB(
                Math.min(start.x, end.x) - halfThickness,
                Math.min(start.y, end.y) - halfThickness,
                Math.min(start.z, end.z) - halfThickness,
                Math.max(start.x, end.x) + halfThickness,
                Math.max(start.y, end.y) + halfThickness,
                Math.max(start.z, end.z) + halfThickness
        );
    }

    private boolean canTakeDamageFrom(ServerLevel level, DamageSource source) {
        Entity drone = getTrackedDrone(level);
        Entity responsibleAttacker = FiberOpticLinkSystem.resolveResponsibleCableAttacker(source);
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            if (this.detached && this.detachedExplosionImmunityTicks > 0) {
                return false;
            }
            if (drone != null && FiberOpticLinkSystem.isProtectedCableBreaker(level, drone, responsibleAttacker)) {
                return false;
            }
            return AddonConfig.fiberCableCanBeDamagedByExplosions();
        }

        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof Projectile) {
            if (drone != null && FiberOpticLinkSystem.isProtectedProjectileCableBreaker(level, drone, responsibleAttacker)) {
                return false;
            }
            return responsibleAttacker != null && AddonConfig.fiberCableCanBeDamagedByProjectiles();
        }

        if (drone != null && FiberOpticLinkSystem.isProtectedCableBreaker(level, drone, responsibleAttacker)) {
            return false;
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof Player) {
            return AddonConfig.fiberCableCanBeDamagedByPlayers();
        }

        return false;
    }

    private void recordDamageContext(DamageSource source, float appliedDamage) {
        this.lastDamageSourceId = source != null ? source.getMsgId() : "none";
        this.lastDamageAmount = appliedDamage;

        Entity directEntity = source != null ? source.getDirectEntity() : null;
        this.lastDirectEntityType = describeEntityType(directEntity);
        this.lastDirectEntityUuid = directEntity != null ? directEntity.getUUID().toString() : "none";

        Entity attacker = source != null ? FiberOpticLinkSystem.resolveResponsibleCableAttacker(source) : null;
        this.lastAttackerType = describeEntityType(attacker);
        this.lastAttackerUuid = attacker != null ? attacker.getUUID().toString() : "none";
    }

    private static String describeEntityType(Entity entity) {
        if (entity == null) {
            return "none";
        }

        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null ? key.toString() : entity.getClass().getName();
    }

    private Entity getTrackedDrone(ServerLevel level) {
        if (this.droneUuid == null) {
            return null;
        }
        Entity entity = level.getEntity(this.droneUuid);
        return entity != null && SbwCompat.isDrone(entity) ? entity : null;
    }

    private void tickDetachedLifetime() {
        if (this.detachedLifetimeTicks < 0) {
            return;
        }

        this.detachedAge++;
        if (this.detachedAge >= this.detachedLifetimeTicks) {
            this.discard();
        }
    }

    private void tickDetachedExplosionImmunity() {
        if (this.detachedExplosionImmunityTicks > 0) {
            this.detachedExplosionImmunityTicks--;
        }
    }

    private void settleDetachedOnGroundDuringCrashWindow() {
        if (this.detachedExplosionImmunityTicks <= 0) {
            return;
        }

        Vec3 groundedPosition = snapDetachedPositionToGround(this.level(), this.position());
        if (groundedPosition.distanceToSqr(this.position()) <= 1.0E-6D) {
            return;
        }

        this.moveTo(groundedPosition.x, groundedPosition.y, groundedPosition.z, this.getYRot(), this.getXRot());
        this.setYRot(this.getYRot());
        this.setYHeadRot(this.getYRot());
        this.setXRot(this.getXRot());
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.noPhysics = true;
        refreshHitboxIfNeeded();
        this.setBoundingBox(makeBoundingBox());
        this.hurtMarked = true;
    }

    private static Vec3 flattenDirection(Vec3 direction) {
        Vec3 horizontal = direction == null ? Vec3.ZERO : new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.lengthSqr() <= 1.0E-6D) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }
        return horizontal.normalize();
    }

    private static Vec3 snapDetachedPositionToGround(Level level, Vec3 position) {
        BlockPos.MutableBlockPos cursor = BlockPos.containing(position).mutable();
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        int startY = Math.max(minY, Math.min(maxY, (int) Math.ceil(position.y + 8.0D)));

        for (int y = startY; y >= minY; y--) {
            cursor.set(position.x, y, position.z);
            VoxelShape shape = level.getBlockState(cursor).getCollisionShape(level, cursor);
            if (shape.isEmpty()) {
                continue;
            }

            double top = cursor.getY() + shape.max(Direction.Axis.Y);
            return new Vec3(position.x, top + 0.04D, position.z);
        }

        if (startY < maxY) {
            for (int y = maxY; y > startY; y--) {
                cursor.set(position.x, y, position.z);
                VoxelShape shape = level.getBlockState(cursor).getCollisionShape(level, cursor);
                if (shape.isEmpty()) {
                    continue;
                }

                double top = cursor.getY() + shape.max(Direction.Axis.Y);
                return new Vec3(position.x, top + 0.04D, position.z);
            }
        }

        return position;
    }

    private void refreshHitboxIfNeeded() {
        float desiredHitboxSize = (float) Math.max(0.1D, AddonConfig.fiberCableSegmentHitboxSize());
        if (Math.abs(desiredHitboxSize - this.lastAppliedHitboxSize) > 1.0E-4F) {
            this.lastAppliedHitboxSize = desiredHitboxSize;
            this.refreshDimensions();
            this.setBoundingBox(makeBoundingBox());
        }
    }

    private Vec3 getSegmentDirection() {
        if (this.segmentDirection != null && this.segmentDirection.lengthSqr() > 1.0E-6D) {
            return this.segmentDirection.normalize();
        }

        float yawRadians = (float) Math.toRadians(this.getYRot());
        float pitchRadians = (float) Math.toRadians(this.getXRot());
        double x = -Mth.sin(yawRadians) * Mth.cos(pitchRadians);
        double y = -Mth.sin(pitchRadians);
        double z = Mth.cos(yawRadians) * Mth.cos(pitchRadians);
        Vec3 derived = new Vec3(x, y, z);
        return derived.lengthSqr() > 1.0E-6D ? derived.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
    }
}
