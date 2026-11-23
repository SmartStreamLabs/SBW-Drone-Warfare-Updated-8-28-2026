package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_DATA;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY_TAG;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.IS_KAMIKAZE;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.MAX_AMMO;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.AMMO;

public class DeliveryDroneEntity extends CubedFpvDroneEntity {
    private static final float DELIVERY_MAX_HEALTH = 40.0F;
    private static final float DELIVERY_COMPONENT_MAX_HEALTH = 20.0F;
    private static final float DELIVERY_INCOMING_DAMAGE_MULTIPLIER = 1.0F;
    private static final EntityDataAccessor<Boolean> HAS_EXTRACTION_CRATE =
            SynchedEntityData.defineId(DeliveryDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MOUNTED_CRATE_OCCUPIED_SLOTS =
            SynchedEntityData.defineId(DeliveryDroneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_SYNCED_DELIVERY_TARGET =
            SynchedEntityData.defineId(DeliveryDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SYNCED_DELIVERY_TARGET_X =
            SynchedEntityData.defineId(DeliveryDroneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SYNCED_DELIVERY_TARGET_Y =
            SynchedEntityData.defineId(DeliveryDroneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SYNCED_DELIVERY_TARGET_Z =
            SynchedEntityData.defineId(DeliveryDroneEntity.class, EntityDataSerializers.INT);
    public static final int MOUNTED_CRATE_SLOT_CAPACITY = 27;
    private static final String TAG_HAS_EXTRACTION_CRATE = "SBWDeliveryDroneHasExtractionCrate";
    private static final String TAG_EXTRACTION_CRATE_DATA = "SBWDeliveryDroneExtractionCrateData";
    private static final String TAG_HAS_TARGET = "SBWDeliveryDroneHasTarget";
    private static final String TAG_TARGET_X = "SBWDeliveryDroneTargetX";
    private static final String TAG_TARGET_Y = "SBWDeliveryDroneTargetY";
    private static final String TAG_TARGET_Z = "SBWDeliveryDroneTargetZ";
    private static final double ARRIVAL_DISTANCE = 1.35D;
    private static final double MAX_AUTOPILOT_SPEED = 0.42D;
    private static final int AUTOPILOT_DEPLOY_SCAN_DEPTH = 48;
    private static final int MANUAL_DEPLOY_SCAN_DEPTH = 128;
    private static final int MANUAL_PICKUP_SCAN_DEPTH = 24;
    private static final int MANUAL_PICKUP_HORIZONTAL_RADIUS = 2;
    private static final float RAD_TO_DEG = 180.0F / (float) Math.PI;

    @Nullable
    private BlockPos deliveryTarget;
    private CompoundTag mountedCrateData = new CompoundTag();

    @Override public java.util.List<Entity> getOrderedPassengers(){return getPassengers();}
    // Cargo is a vanilla passenger, not a native SBW operator seat (this airframe has no seats).
    @Override protected void addPassenger(Entity e){
        if(e instanceof VatagEntity){if(e.getVehicle()!=this)throw new IllegalStateException("Cargo must startRiding first");((nl.smartstreamlabs.sbwdroneconfig.mixin.DeliveryPassengerAccess)(Object)this).sbwdw$cargoPassengers(com.google.common.collect.ImmutableList.of(e));this.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ENTITY_MOUNT,e);return;}super.addPassenger(e);
    }
    @Override protected void removePassenger(Entity e){
        if(e instanceof VatagEntity){if(e.getVehicle()==this)throw new IllegalStateException("Cargo must stopRiding first");((nl.smartstreamlabs.sbwdroneconfig.mixin.DeliveryPassengerAccess)(Object)this).sbwdw$cargoPassengers(com.google.common.collect.ImmutableList.of());this.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ENTITY_DISMOUNT,e);return;}super.removePassenger(e);
    }
    @Override protected boolean canAddPassenger(Entity e){return e instanceof VatagEntity&&!hasMountedExtractionCrate()&&getPassengers().isEmpty();}
    @Override public void positionRider(Entity e,Entity.MoveFunction move){if(e instanceof VatagEntity)DeliveryGroundCargo.position(this,e,move);else super.positionRider(e,move);}
    @Override public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose){return net.minecraft.world.entity.EntityDimensions.scalable(2.5f,2.575f);}
    @Override protected float getEyeHeight(net.minecraft.world.entity.Pose pose,net.minecraft.world.entity.EntityDimensions dimensions){return .2975f;}
    private final DeliveryRotorPart[] rotorParts={new DeliveryRotorPart(this),new DeliveryRotorPart(this),new DeliveryRotorPart(this),new DeliveryRotorPart(this),new DeliveryRotorPart(this),new DeliveryRotorPart(this)};
    @Override public boolean isMultipartEntity(){return true;}
    @Override public DeliveryRotorPart[] getParts(){return rotorParts;}
    @Override public void setId(int id){super.setId(id);if(rotorParts!=null)for(int i=0;i<rotorParts.length;i++)rotorParts[i].setId(id+i+1);}
    private void positionRotors(){var frame=Dd01VisualPose.matrix(getYaw(1),getBodyPitch(1),getRoll(1));for(int i=0;i<6;i++){double angle=Math.toRadians(i*60);var at=frame.transformPosition(new org.joml.Vector3d(Math.cos(angle)*1.9375,36.5/16d,Math.sin(angle)*1.9375));var part=rotorParts[i];part.setPos(getX()+at.x,getY()+at.y-.08,getZ()+at.z);}}
    public DeliveryDroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        this.setHealth(getMaxHealth());this.refreshDimensions();
    }

    @Override
    protected float getDroneMaxHealthLimit() {
        return DELIVERY_MAX_HEALTH;
    }

    @Override
    protected float getDroneComponentMaxHealthLimit() {
        return DELIVERY_COMPONENT_MAX_HEALTH;
    }

    @Override
    protected float getIncomingDamageMultiplier() {
        return DELIVERY_INCOMING_DAMAGE_MULTIPLIER;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HAS_EXTRACTION_CRATE, false);
        this.entityData.define(MOUNTED_CRATE_OCCUPIED_SLOTS, 0);
        this.entityData.define(HAS_SYNCED_DELIVERY_TARGET, false);
        this.entityData.define(SYNCED_DELIVERY_TARGET_X, 0);
        this.entityData.define(SYNCED_DELIVERY_TARGET_Y, 0);
        this.entityData.define(SYNCED_DELIVERY_TARGET_Z, 0);
    }

    @Override
    public void baseTick() {
        super.baseTick();positionRotors();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            tickDeliveryAutopilot(serverLevel);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean cargoAction = isCargoAction(player);

        if (cargoAction && (hasMountedExtractionCrate()||DeliveryGroundCargo.loaded(this))) {
            if (!this.level().isClientSide()) {
                tryDeployMountedExtractionCrate(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (!cargoAction && stack.is(AddonItems.EXTRACTION_CRATE.get())) {
            if (!this.level().isClientSide()) {
                attachExtractionCrate(player, stack);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (cargoAction && (stack.isEmpty() || stack.is(ModTags.Items.TOOLS_CROWBAR))) {
            if (!this.level().isClientSide()) {
                pickupDeliveryDrone(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.interact(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(TAG_HAS_EXTRACTION_CRATE, hasMountedExtractionCrate());
        if (hasMountedExtractionCrate() && !mountedCrateData.isEmpty()) {
            tag.put(TAG_EXTRACTION_CRATE_DATA, mountedCrateData.copy());
        }
        if (deliveryTarget != null) {
            tag.putBoolean(TAG_HAS_TARGET, true);
            tag.putInt(TAG_TARGET_X, deliveryTarget.getX());
            tag.putInt(TAG_TARGET_Y, deliveryTarget.getY());
            tag.putInt(TAG_TARGET_Z, deliveryTarget.getZ());
        } else {
            tag.putBoolean(TAG_HAS_TARGET, false);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        mountedCrateData = tag.contains(TAG_EXTRACTION_CRATE_DATA, Tag.TAG_COMPOUND)
                ? tag.getCompound(TAG_EXTRACTION_CRATE_DATA).copy()
                : new CompoundTag();
        setMountedExtractionCrate(tag.getBoolean(TAG_HAS_EXTRACTION_CRATE));
        if (tag.getBoolean(TAG_HAS_TARGET)) {
            setDeliveryTarget(new BlockPos(tag.getInt(TAG_TARGET_X), tag.getInt(TAG_TARGET_Y), tag.getInt(TAG_TARGET_Z)));
        } else {
            clearDeliveryTarget();
        }
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return new ItemStack(AddonItems.DELIVERY_DRONE.get());
    }

    public boolean hasMountedExtractionCrate() {
        return this.entityData.get(HAS_EXTRACTION_CRATE);
    }

    public int getMountedCargoOccupiedSlots() {
        return hasMountedExtractionCrate()
                ? Mth.clamp(this.entityData.get(MOUNTED_CRATE_OCCUPIED_SLOTS), 0, MOUNTED_CRATE_SLOT_CAPACITY)
                : 0;
    }

    public int getMountedCargoSlotCapacity() {
        return MOUNTED_CRATE_SLOT_CAPACITY;
    }

    public static boolean isCargoAction(Player player) {
        return player.isCrouching() || player.isShiftKeyDown();
    }

    public boolean tryDeployMountedExtractionCrate(Player player) {
        if(DeliveryGroundCargo.loaded(this))return DeliveryGroundCargo.release(this,player);
        if (!hasMountedExtractionCrate()) {
            return false;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            return deployMountedCrate(serverLevel, null, player);
        }
        return false;
    }

    public boolean tryPickupExtractionCrate(Player player) {
        if(DeliveryGroundCargo.loaded(this))return false;if(DeliveryGroundCargo.pickup(this,player))return true;
        if (hasMountedExtractionCrate()) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_already_attached"), true);
            return false;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            return pickupExtractionCrate(serverLevel, player);
        }
        return false;
    }

    public void setDeliveryTarget(@Nullable BlockPos target) {
        this.deliveryTarget = target;
        syncDeliveryTarget(target);
    }

    @Nullable
    public BlockPos getDeliveryTarget() {
        return deliveryTarget;
    }

    @Nullable
    public BlockPos getSyncedDeliveryTarget() {
        if (!this.entityData.get(HAS_SYNCED_DELIVERY_TARGET)) {
            return null;
        }
        return new BlockPos(
                this.entityData.get(SYNCED_DELIVERY_TARGET_X),
                this.entityData.get(SYNCED_DELIVERY_TARGET_Y),
                this.entityData.get(SYNCED_DELIVERY_TARGET_Z)
        );
    }

    public void clearDeliveryTarget() {
        setDeliveryTarget(null);
    }

    private void attachExtractionCrate(Player player, ItemStack stack) {
        if (hasMountedExtractionCrate()) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_already_attached"), true);
            return;
        }

        if(DeliveryGroundCargo.loaded(this)){player.displayClientMessage(Component.translatable("delivery.robot_unload_first"),true);return;}
        mountedCrateData = crateDataFromStack(stack);
        setMountedExtractionCrate(true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_attached"), true);
    }

    private void setMountedExtractionCrate(boolean mounted) {
        this.entityData.set(HAS_EXTRACTION_CRATE, mounted);
        this.entityData.set(MOUNTED_CRATE_OCCUPIED_SLOTS, mounted ? countOccupiedCrateSlots(mountedCrateData) : 0);
    }

    private void clearMountedExtractionCrate() {
        mountedCrateData = new CompoundTag();
        setMountedExtractionCrate(false);
    }

    private void pickupDeliveryDrone(Player player) {
        if(DeliveryGroundCargo.loaded(this)){player.displayClientMessage(Component.translatable("delivery.robot_unload_first"),true);return;}
        ItemHandlerHelper.giveItemToPlayer(player, MoistureSystem.pack(this, new ItemStack(AddonItems.DELIVERY_DRONE.get())));

        if (hasMountedExtractionCrate()) {
            ItemHandlerHelper.giveItemToPlayer(player, createMountedCrateStack());
            clearMountedExtractionCrate();
        }

        returnInheritedPayloadIfPresent(player);
        clearDeliveryTarget();

        player.getInventory().items.stream()
                .filter(SbwCompat::isMonitor)
                .forEach(itemStack -> SbwCompat.unlinkMonitorFromDrone(itemStack, player, this));

        if (this.level() instanceof ServerLevel serverLevel) {
            SbwCompat.disconnectControllersForDrone(serverLevel, this);
        }

        this.discard();
    }

    private void returnInheritedPayloadIfPresent(Player player) {
        if (SbwCompat.getDroneCurrentItem(this).isEmpty() || this.entityData.get(AMMO) <= 0) {
            return;
        }

        ItemStack payload = SbwCompat.getDroneCurrentItem(this).copy();
        payload.setCount(1);
        ItemHandlerHelper.giveItemToPlayer(player, payload);
        clearInheritedPayload();
    }

    private void clearInheritedPayload() {
        SbwCompat.setDroneCurrentItem(this, ItemStack.EMPTY);
        this.entityData.set(AMMO, 0);
        this.entityData.set(MAX_AMMO, 1);
        this.entityData.set(IS_KAMIKAZE, false);
        this.entityData.set(DISPLAY_ENTITY, "");
        this.entityData.set(DISPLAY_ENTITY_TAG, new CompoundTag());
        this.entityData.set(DISPLAY_DATA, List.of(1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F));
    }

    private void tickDeliveryAutopilot(ServerLevel level) {
        if (deliveryTarget == null) {
            return;
        }

        ServerPlayer controller = SbwCompat.findLinkedMonitorController(level, this);
        if (controller != null && SbwCompat.isUsingLinkedMonitorForDrone(controller, this)) {
            return;
        }

        if (!level.hasChunkAt(deliveryTarget)) {
            return;
        }

        Vec3 targetCenter = Vec3.atCenterOf(deliveryTarget).add(0.0D, 2.0D, 0.0D);
        Vec3 toTarget = targetCenter.subtract(this.position());
        double distance = toTarget.length();
        if (distance <= ARRIVAL_DISTANCE) {
            this.setDeltaMovement(Vec3.ZERO);
            if (hasMountedExtractionCrate()) {
                deployMountedCrate(level, deliveryTarget, controller);
            }
            clearDeliveryTarget();
            return;
        }

        Vec3 desired = toTarget.normalize().scale(Mth.clamp(distance * 0.06D, 0.08D, MAX_AUTOPILOT_SPEED));
        Vec3 nextMovement = this.getDeltaMovement().scale(0.72D).add(desired.scale(0.28D));
        this.setDeltaMovement(nextMovement);
        this.hasImpulse = true;

        if (nextMovement.horizontalDistanceSqr() > 1.0E-6D) {
            this.setYRot((float) (Mth.atan2(-nextMovement.x, nextMovement.z) * RAD_TO_DEG));
        }
    }

    private boolean deployMountedCrate(ServerLevel level, @Nullable BlockPos requestedTarget, @Nullable Player feedbackPlayer) {
        BlockPos placementPos = findCratePlacementPos(level, requestedTarget);
        ServerPlayer controller = SbwCompat.findLinkedMonitorController(level, this);

        if (placementPos == null) {
            if (controller != null) {
                controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_deploy_blocked"), true);
            } else if (feedbackPlayer != null) {
                feedbackPlayer.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_deploy_blocked"), true);
            }
            return false;
        }

        Direction facing = Direction.fromYRot(this.getYRot()).getOpposite();
        BlockState state = AddonBlocks.EXTRACTION_CRATE.get()
                .defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing);

        if (level.setBlock(placementPos, state, 3)) {
            if (level.getBlockEntity(placementPos) instanceof ExtractionCrateBlockEntity crate) {
                crate.loadContentsOnly(mountedCrateData.copy());
                DroneInventoryContainer droneInventory = new DroneInventoryContainer(this);
                if (crate.extractAllFromDrone(droneInventory)) {
                    droneInventory.saveToDrone();
                }
                crate.setChanged();
            }

            clearMountedExtractionCrate();
            level.playSound(null, placementPos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
            if (controller != null) {
                controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_deployed"), true);
            } else if (feedbackPlayer != null) {
                feedbackPlayer.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_deployed"), true);
            }
            return true;
        }

        return false;
    }

    @Nullable
    private BlockPos findCratePlacementPos(ServerLevel level, @Nullable BlockPos requestedTarget) {
        BlockPos anchor = requestedTarget;
        boolean manualDeploy = anchor == null;
        if (manualDeploy) {
            anchor = BlockPos.containing(this.getX(), this.getBoundingBox().minY - 0.25D, this.getZ());
        }

        if (anchor == null) {
            return null;
        }

        int topY = Math.min(level.getMaxBuildHeight() - 1, anchor.getY());
        int bottomY = Math.max(level.getMinBuildHeight() + 1, topY - (manualDeploy ? MANUAL_DEPLOY_SCAN_DEPTH : AUTOPILOT_DEPLOY_SCAN_DEPTH));
        for (int y = topY; y >= bottomY; y--) {
            BlockPos candidate = new BlockPos(anchor.getX(), y, anchor.getZ());
            BlockPos below = candidate.below();
            if (!level.hasChunkAt(candidate)) {
                return null;
            }

            BlockState existing = level.getBlockState(candidate);
            BlockState support = level.getBlockState(below);
            if ((existing.isAir() || existing.canBeReplaced()) && support.isFaceSturdy(level, below, Direction.UP)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean pickupExtractionCrate(ServerLevel level, Player feedbackPlayer) {
        BlockPos cratePos = findPickupCratePos(level);
        if (cratePos == null || !(level.getBlockEntity(cratePos) instanceof ExtractionCrateBlockEntity crate)) {
            feedbackPlayer.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_pickup_blocked"), true);
            return false;
        }

        BlockState crateState = level.getBlockState(cratePos);
        CompoundTag crateContents = crate.saveContentsOnly();
        crate.loadContentsOnly(new CompoundTag());
        if (!level.setBlock(cratePos, Blocks.AIR.defaultBlockState(), 3)) {
            crate.loadContentsOnly(crateContents.copy());
            feedbackPlayer.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_pickup_blocked"), true);
            return false;
        }

        mountedCrateData = crateContents;
        setMountedExtractionCrate(true);
        level.playSound(null, cratePos, crateState.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.55F, 1.15F);
        feedbackPlayer.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_picked_up"), true);
        return true;
    }

    @Nullable
    private BlockPos findPickupCratePos(ServerLevel level) {
        BlockPos anchor = BlockPos.containing(this.getX(), this.getBoundingBox().minY - 0.25D, this.getZ());
        int topY = Math.min(level.getMaxBuildHeight() - 1, anchor.getY());
        int bottomY = Math.max(level.getMinBuildHeight(), topY - MANUAL_PICKUP_SCAN_DEPTH);
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (int y = topY; y >= bottomY; y--) {
            for (int x = anchor.getX() - MANUAL_PICKUP_HORIZONTAL_RADIUS; x <= anchor.getX() + MANUAL_PICKUP_HORIZONTAL_RADIUS; x++) {
                for (int z = anchor.getZ() - MANUAL_PICKUP_HORIZONTAL_RADIUS; z <= anchor.getZ() + MANUAL_PICKUP_HORIZONTAL_RADIUS; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (!level.hasChunkAt(candidate) || !level.getBlockState(candidate).is(AddonBlocks.EXTRACTION_CRATE.get())) {
                        continue;
                    }

                    double distance = this.distanceToSqr(candidate.getX() + 0.5D, candidate.getY() + 0.5D, candidate.getZ() + 0.5D);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestPos = candidate;
                    }
                }
            }
        }

        return bestPos;
    }

    private static CompoundTag crateDataFromStack(ItemStack stack) {
        CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
        return blockEntityTag == null ? new CompoundTag() : blockEntityTag.copy();
    }

    private ItemStack createMountedCrateStack() {
        ItemStack stack = new ItemStack(AddonItems.EXTRACTION_CRATE.get());
        if (!mountedCrateData.isEmpty()) {
            stack.getOrCreateTag().put("BlockEntityTag", mountedCrateData.copy());
        }
        return stack;
    }

    private void syncDeliveryTarget(@Nullable BlockPos target) {
        this.entityData.set(HAS_SYNCED_DELIVERY_TARGET, target != null);
        if (target != null) {
            this.entityData.set(SYNCED_DELIVERY_TARGET_X, target.getX());
            this.entityData.set(SYNCED_DELIVERY_TARGET_Y, target.getY());
            this.entityData.set(SYNCED_DELIVERY_TARGET_Z, target.getZ());
        }
    }

    private static int countOccupiedCrateSlots(CompoundTag crateData) {
        if (crateData == null || !crateData.contains("Items", Tag.TAG_LIST)) {
            return 0;
        }
        return Mth.clamp(crateData.getList("Items", Tag.TAG_COMPOUND).size(), 0, MOUNTED_CRATE_SLOT_CAPACITY);
    }
}
