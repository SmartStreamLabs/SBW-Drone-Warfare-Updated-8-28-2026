package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile;
import com.atsuishio.superbwarfare.entity.projectile.RpgRocketTBGEntity;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModEntities;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.AMMO;

public class BayraktarTb2Entity extends LucasDroneEntity {
    private static final String[] MISSILE_ITEM_IDS = {
            "superbwarfare:rpg_rocket_tbg",
            "superbwarfare:rpg_rocket_yasin_105_tbg"
    };
    private static final String TAG_WAYPOINT_SET = "sbwdroneconfigBayraktarWaypointSet";
    private static final String TAG_WAYPOINT_X = "sbwdroneconfigBayraktarWaypointX";
    private static final String TAG_WAYPOINT_Y = "sbwdroneconfigBayraktarWaypointY";
    private static final String TAG_WAYPOINT_Z = "sbwdroneconfigBayraktarWaypointZ";
    private static final double MISSILE_FORWARD_OFFSET = 1.85D;
    private static final double MISSILE_DOWN_OFFSET = -0.12D;
    private static final float MISSILE_SPEED = 2.35F;


    // Purely visual inventory snapshots. These do not add or limit weapon slots.
    private static final java.util.List<net.minecraft.network.syncher.EntityDataAccessor<ItemStack>> BT01_VISUAL_PAYLOADS = java.util.List.of(
            net.minecraft.network.syncher.SynchedEntityData.defineId(BayraktarTb2Entity.class, net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK),
            net.minecraft.network.syncher.SynchedEntityData.defineId(BayraktarTb2Entity.class, net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK),
            net.minecraft.network.syncher.SynchedEntityData.defineId(BayraktarTb2Entity.class, net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK),
            net.minecraft.network.syncher.SynchedEntityData.defineId(BayraktarTb2Entity.class, net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK));
    private CompoundTag bt01LastVisualInventory;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        for (var visual : BT01_VISUAL_PAYLOADS) this.entityData.define(visual, ItemStack.EMPTY);
    }

    public BayraktarTb2Entity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void baseTick() {
        if (!this.level().isClientSide() && SbwCompat.getDroneFire(this)) {
            tryFireMissile();
            SbwCompat.setDroneFire(this, false);
        }

        super.baseTick();
        if (!this.level().isClientSide()) bt01SyncPayloadVisuals();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult refuelResult = LucasFuelSystem.refuel(this, player, hand);
        if (refuelResult.consumesAction()) {
            return refuelResult;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && (stack.isEmpty() || stack.is(ModTags.Items.TOOLS_CROWBAR))) {
            if (!this.level().isClientSide()) {
                pickupBayraktar(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.interact(player, hand);
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return new ItemStack(AddonItems.UKRAINIAN_BAYRAKTAR_TB2.get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        BlockPos waypoint = getWaypoint();
        if (waypoint != null) {
            compound.putBoolean(TAG_WAYPOINT_SET, true);
            compound.putInt(TAG_WAYPOINT_X, waypoint.getX());
            compound.putInt(TAG_WAYPOINT_Y, waypoint.getY());
            compound.putInt(TAG_WAYPOINT_Z, waypoint.getZ());
        } else {
            compound.putBoolean(TAG_WAYPOINT_SET, false);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.getBoolean(TAG_WAYPOINT_SET)
                && compound.contains(TAG_WAYPOINT_X, Tag.TAG_INT)
                && compound.contains(TAG_WAYPOINT_Y, Tag.TAG_INT)
                && compound.contains(TAG_WAYPOINT_Z, Tag.TAG_INT)) {
            setWaypoint(new BlockPos(
                    compound.getInt(TAG_WAYPOINT_X),
                    compound.getInt(TAG_WAYPOINT_Y),
                    compound.getInt(TAG_WAYPOINT_Z)
            ));
        } else {
            clearWaypoint();
        }
    }

    public void setWaypoint(BlockPos waypoint) {
        CompoundTag data = this.getPersistentData();
        data.putBoolean(TAG_WAYPOINT_SET, true);
        data.putInt(TAG_WAYPOINT_X, waypoint.getX());
        data.putInt(TAG_WAYPOINT_Y, waypoint.getY());
        data.putInt(TAG_WAYPOINT_Z, waypoint.getZ());
    }

    public void clearWaypoint() {
        CompoundTag data = this.getPersistentData();
        data.putBoolean(TAG_WAYPOINT_SET, false);
        data.remove(TAG_WAYPOINT_X);
        data.remove(TAG_WAYPOINT_Y);
        data.remove(TAG_WAYPOINT_Z);
    }

    public @Nullable BlockPos getWaypoint() {
        CompoundTag data = this.getPersistentData();
        if (!data.getBoolean(TAG_WAYPOINT_SET)
                || !data.contains(TAG_WAYPOINT_X, Tag.TAG_INT)
                || !data.contains(TAG_WAYPOINT_Y, Tag.TAG_INT)
                || !data.contains(TAG_WAYPOINT_Z, Tag.TAG_INT)) {
            return null;
        }
        return new BlockPos(data.getInt(TAG_WAYPOINT_X), data.getInt(TAG_WAYPOINT_Y), data.getInt(TAG_WAYPOINT_Z));
    }

    private boolean tryFireMissile() {
        Player controller = SbwCompat.getDroneControllerPlayer(this);
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }

        DroneInventoryContainer inventory = new DroneInventoryContainer(this);
        int missileSlot = findMissileSlot(inventory);
        if (missileSlot < 0) {
            if (controller != null) {
                controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.bayraktar_no_missiles"), true);
            }
            return false;
        }

        Player owner = SbwCompat.getDroneControllerPlayer(this);
        Vec3 forward = getMissileForwardVector();
        Vec3 spawnPos = this.position()
                .add(forward.scale(MISSILE_FORWARD_OFFSET))
                .add(0.0D, MISSILE_DOWN_OFFSET, 0.0D);

        RpgRocketTBGEntity missile = new RpgRocketTBGEntity(ModEntities.RPG_ROCKET_TBG.get(), level);
        missile.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, this.getYRot(), this.getXRot());
        missile.setYRot(this.getYRot());
        missile.setXRot(this.getXRot());
        FastThrowableProjectile projectile = missile;
        if (owner != null) {
            projectile.setOwner(owner);
        }
        missile.shoot(forward.x, forward.y, forward.z, MISSILE_SPEED, 0.35F);
        missile.setDeltaMovement(missile.getDeltaMovement().add(this.getDeltaMovement().scale(0.35D)));

        ItemStack missileStack = inventory.getItem(missileSlot);
        String launchedPayload=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(missileStack.getItem()).toString();
        missileStack.shrink(1);
        inventory.setItem(missileSlot, missileStack.isEmpty() ? ItemStack.EMPTY : missileStack);
        inventory.saveToDrone();

        DroneExplosionVfx.markPayload(missile);DroneAttackDamage.mark(missile,this,launchedPayload);
        level.addFreshEntity(missile);
        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, SoundEvents.DISPENSER_LAUNCH, SoundSource.NEUTRAL, 1.0F, 0.8F);
        if (controller != null) {
            controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.bayraktar_missiles", countMissiles(inventory)), true);
        }
        return true;
    }

    private Vec3 getMissileForwardVector() {
        Vec3 forward = Vec3.directionFromRotation(this.getXRot(), this.getYRot());
        if (forward.lengthSqr() < 1.0E-6D) {
            forward = this.getLookAngle();
        }
        return forward.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : forward.normalize();
    }

    private int findMissileSlot(DroneInventoryContainer inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            for (String itemId : MISSILE_ITEM_IDS) {
                if (stackMatches(stack, itemId)) {
                    return slot;
                }
            }
        }
        return -1;
    }

    public int osdMissileCount(){return countMissiles(new DroneInventoryContainer(this));}

    private int countMissiles(DroneInventoryContainer inventory) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            for (String itemId : MISSILE_ITEM_IDS) {
                if (stackMatches(stack, itemId)) {
                    count += stack.getCount();
                    break;
                }
            }
        }
        return count;
    }

    private static boolean stackMatches(ItemStack stack, String itemId) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && itemId.equals(key.toString());
    }


    /** Read-only display state; the original inventory/weapon code below remains authoritative. */
    public ItemStack bt01VisualPayload(int socket) { return this.entityData.get(BT01_VISUAL_PAYLOADS.get(socket)); }

    private void bt01SetVisualPayload(int socket, ItemStack display) {
        var key = BT01_VISUAL_PAYLOADS.get(socket);
        if (!ItemStack.matches(this.entityData.get(key), display)) this.entityData.set(key, display);
    }

    private void bt01SyncPayloadVisuals() {
        CompoundTag stored = this.getPersistentData().contains("sbwdroneconfigDroneInventory", Tag.TAG_COMPOUND)
                ? this.getPersistentData().getCompound("sbwdroneconfigDroneInventory") : null;
        if (stored == bt01LastVisualInventory) return;
        bt01LastVisualInventory = stored;
        DroneInventoryContainer inventory = new DroneInventoryContainer(this);
        int socket = 0;
        for (int slot = 0; slot < inventory.getContainerSize() && socket < BT01_VISUAL_PAYLOADS.size(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            for (String itemId : MISSILE_ITEM_IDS) {
                if (stackMatches(stack, itemId)) {
                    for (int n = 0; n < stack.getCount() && socket < BT01_VISUAL_PAYLOADS.size(); n++) {
                        ItemStack display = stack.copy(); display.setCount(1);
                        bt01SetVisualPayload(socket++, display);
                    }
                    break;
                }
            }
        }
        while (socket < BT01_VISUAL_PAYLOADS.size()) bt01SetVisualPayload(socket++, ItemStack.EMPTY);
    }

    private void pickupBayraktar(Player player) {
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(AddonItems.UKRAINIAN_BAYRAKTAR_TB2.get()));

        for (int index = 0; index < this.entityData.get(AMMO); index++) {
            if (!SbwCompat.getDroneCurrentItem(this).isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, SbwCompat.getDroneCurrentItem(this).copy());
            }
        }

        player.getInventory().items.stream()
                .filter(SbwCompat::isMonitor)
                .forEach(itemStack -> SbwCompat.unlinkMonitorFromDrone(itemStack, player, this));

        if (this.level() instanceof ServerLevel serverLevel) {
            SbwCompat.disconnectControllersForDrone(serverLevel, this);
        }

        this.discard();
    }
}
