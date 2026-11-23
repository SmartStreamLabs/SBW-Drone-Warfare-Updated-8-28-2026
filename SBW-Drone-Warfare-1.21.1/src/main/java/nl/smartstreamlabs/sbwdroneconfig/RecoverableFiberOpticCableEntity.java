package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class RecoverableFiberOpticCableEntity extends Entity {
    private static final String TAG_RECOVERED_SPOOL = "sbwdroneconfigRecoveredFiberSpool";
    private static final EntityDataAccessor<ItemStack> DATA_RECOVERED_SPOOL =
            SynchedEntityData.defineId(RecoverableFiberOpticCableEntity.class, EntityDataSerializers.ITEM_STACK);

    public RecoverableFiberOpticCableEntity(EntityType<? extends RecoverableFiberOpticCableEntity> type, net.minecraft.world.level.Level level) {
        super(type, level);
        this.blocksBuilding = false;
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RECOVERED_SPOOL, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(TAG_RECOVERED_SPOOL)) {
            setRecoveredSpool(ItemStack.parse(registryAccess(), tag.getCompound(TAG_RECOVERED_SPOOL)).orElse(ItemStack.EMPTY));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!getRecoveredSpool().isEmpty()) {
            tag.put(TAG_RECOVERED_SPOOL, getRecoveredSpool().save(registryAccess(), new CompoundTag()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        this.hurtMarked = true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.isCrouching()) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }

        ItemStack recoveredSpool = getRecoveredSpool();
        if (recoveredSpool.isEmpty()) {
            this.discard();
            return InteractionResult.CONSUME;
        }

        ItemHandlerHelper.giveItemToPlayer(player, recoveredSpool.copy());
        setRecoveredSpool(ItemStack.EMPTY);
        this.discard();
        return InteractionResult.sidedSuccess(false);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(0.45F, 0.2F);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public ItemStack getRecoveredSpool() {
        return this.entityData.get(DATA_RECOVERED_SPOOL);
    }

    public void setRecoveredSpool(ItemStack recoveredSpool) {
        this.entityData.set(DATA_RECOVERED_SPOOL, recoveredSpool == null ? ItemStack.EMPTY : recoveredSpool.copy());
    }
}
