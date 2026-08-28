package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

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

    public DeliveryDroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        this.setHealth(DELIVERY_MAX_HEALTH);
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_EXTRACTION_CRATE, false);
        builder.define(MOUNTED_CRATE_OCCUPIED_SLOTS, 0);
        builder.define(HAS_SYNCED_DELIVERY_TARGET, false);
        builder.define(SYNCED_DELIVERY_TARGET_X, 0);
        builder.define(SYNCED_DELIVERY_TARGET_Y, 0);
        builder.define(SYNCED_DELIVERY_TARGET_Z, 0);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            tickDeliveryAutopilot(serverLevel);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean cargoAction = isCargoAction(player);

        if (cargoAction && hasMountedExtractionCrate()) {
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
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean(TAG_HAS_EXTRACTION_CRATE, hasMountedExtractionCrate());
        if (hasMountedExtractionCrate() && !mountedCrateData.isEmpty()) {
            compound.put(TAG_EXTRACTION_CRATE_DATA, mountedCrateData.copy());
        }
        if (deliveryTarget != null) {
            compound.putBoolean(TAG_HAS_TARGET, true);
            compound.putInt(TAG_TARGET_X, deliveryTarget.getX());
            compound.putInt(TAG_TARGET_Y, deliveryTarget.getY());
            compound.putInt(TAG_TARGET_Z, deliveryTarget.getZ());
        } else {
            compound.putBoolean(TAG_HAS_TARGET, false);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        mountedCrateData = compound.contains(TAG_EXTRACTION_CRATE_DATA, Tag.TAG_COMPOUND)
                ? compound.getCompound(TAG_EXTRACTION_CRATE_DATA).copy()
                : new CompoundTag();
        setMountedExtractionCrate(compound.getBoolean(TAG_HAS_EXTRACTION_CRATE));
        if (compound.getBoolean(TAG_HAS_TARGET)) {
            setDeliveryTarget(new BlockPos(compound.getInt(TAG_TARGET_X), compound.getInt(TAG_TARGET_Y), compound.getInt(TAG_TARGET_Z)));
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
        if (!hasMountedExtractionCrate()) {
            return false;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            return deployMountedCrate(serverLevel, null, player);
        }
        return false;
    }

    public boolean tryPickupExtractionCrate(Player player) {
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
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(AddonItems.DELIVERY_DRONE.get()));

        if (hasMountedExtractionCrate()) {
            ItemHandlerHelper.giveItemToPlayer(player, createMountedCrateStack());
            clearMountedExtractionCrate();
        }

        for (int index = 0; index < SbwCompat.getDroneAmmo(this); index++) {
            if (!this.getCurrentItem().isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, this.getCurrentItem().copy());
            }
        }
        clearDeliveryTarget();

        player.getInventory().items.stream()
                .filter(SbwCompat::isMonitor)
                .forEach(itemStack -> SbwCompat.unlinkMonitorFromDrone(itemStack, player, this));

        if (this.level() instanceof ServerLevel serverLevel) {
            SbwCompat.disconnectControllersForDrone(serverLevel, this);
        }

        this.discard();
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
                crate.loadContentsOnly(mountedCrateData.copy(), level.registryAccess());
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
        CompoundTag crateContents = crate.saveContentsOnly(level.registryAccess());
        crate.loadContentsOnly(new CompoundTag(), level.registryAccess());
        if (!level.setBlock(cratePos, Blocks.AIR.defaultBlockState(), 3)) {
            crate.loadContentsOnly(crateContents.copy(), level.registryAccess());
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
        return stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
    }

    private ItemStack createMountedCrateStack() {
        ItemStack stack = new ItemStack(AddonItems.EXTRACTION_CRATE.get());
        if (!mountedCrateData.isEmpty()) {
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(mountedCrateData.copy()));
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
