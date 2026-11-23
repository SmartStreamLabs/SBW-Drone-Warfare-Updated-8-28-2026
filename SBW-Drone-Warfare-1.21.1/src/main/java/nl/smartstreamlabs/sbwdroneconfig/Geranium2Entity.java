package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class Geranium2Entity extends GlideBombEntity {
    private static final EntityDataAccessor<Float> GERANIUM_FUEL = SynchedEntityData.defineId(Geranium2Entity.class, EntityDataSerializers.FLOAT);
    private static final String TAG_GERANIUM_FUEL = "sbwdroneconfigGeranium2Fuel";
    private static final EntityDataAccessor<java.util.Optional<net.minecraft.core.BlockPos>> LP02_MOUNT=SynchedEntityData.defineId(Geranium2Entity.class,EntityDataSerializers.OPTIONAL_BLOCK_POS);
    public boolean isLp02Fit(){return this.entityData.get(LP02_MOUNT).isPresent();}
    public void setLp02Mount(net.minecraft.core.BlockPos anchor){this.entityData.set(LP02_MOUNT,java.util.Optional.of(anchor.immutable()));}
    public boolean isLp02PropellerSafe(){var a=this.entityData.get(LP02_MOUNT);if(a.isEmpty())return true;var p=a.get();return ((net.minecraft.world.entity.Entity)this).distanceToSqr(p.getX()+.5,p.getY()+.5,p.getZ()+.5)>36;}
    private static final float GERANIUM_MAX_HEALTH = 12.0F;
    private static final float GERANIUM_COMPONENT_MAX_HEALTH = 6.0F;
    private static final float GERANIUM_INCOMING_DAMAGE_MULTIPLIER = 1.0F;

    public Geranium2Entity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        this.setHealth(getMaxHealth());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GERANIUM_FUEL, 0.0F).define(LP02_MOUNT,java.util.Optional.empty());
    }

    @Override
    protected float getDroneMaxHealthLimit() {
        return GERANIUM_MAX_HEALTH;
    }

    @Override
    protected float getDroneComponentMaxHealthLimit() {
        return GERANIUM_COMPONENT_MAX_HEALTH;
    }

    @Override
    protected float getIncomingDamageMultiplier() {
        return GERANIUM_INCOMING_DAMAGE_MULTIPLIER;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult refuelResult = Geranium2FuelSystem.refuel(this, player, hand);
        if (refuelResult.consumesAction()) {
            return refuelResult;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && (stack.isEmpty() || stack.is(ModTags.Items.TOOLS_CROWBAR))) {
            if (!this.level().isClientSide()) {
                pickupGeranium(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.interact(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.entityData.get(LP02_MOUNT).ifPresent(p->tag.putLong("sbwdroneconfigLp02Mount",p.asLong()));
        tag.putFloat(TAG_GERANIUM_FUEL, getGeraniumFuel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(LP02_MOUNT,tag.contains("sbwdroneconfigLp02Mount")?java.util.Optional.of(net.minecraft.core.BlockPos.of(tag.getLong("sbwdroneconfigLp02Mount"))):java.util.Optional.empty());
        if (tag.contains(TAG_GERANIUM_FUEL)) {
            setGeraniumFuel(tag.getFloat(TAG_GERANIUM_FUEL));
        }
    }

    @Override
    public void travel() {
        Geranium2FuelSystem.beforeGeraniumTravel(this);
        super.travel();
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return new ItemStack(AddonItems.GERANIUM_2.get());
    }

    public float getGeraniumFuel() {
        return this.entityData.get(GERANIUM_FUEL);
    }

    public void setGeraniumFuel(float fuel) {
        this.entityData.set(GERANIUM_FUEL, Math.max(0.0F, Math.min(fuel, (float) AddonConfig.lucasMaxFuel())));
    }

    public int getGeraniumFuelPercent() {
        int maxFuel = AddonConfig.lucasMaxFuel();
        if (maxFuel <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(100, Math.round((getGeraniumFuel() / (float) maxFuel) * 100.0F)));
    }

    private void pickupGeranium(Player player) {
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(AddonItems.GERANIUM_2.get()));

        player.getInventory().items.stream()
                .filter(SbwCompat::isMonitor)
                .forEach(itemStack -> SbwCompat.unlinkMonitorFromDrone(itemStack, player, this));

        if (this.level() instanceof ServerLevel serverLevel) {
            SbwCompat.disconnectControllersForDrone(serverLevel, this);
        }

        this.discard();
    }
}
