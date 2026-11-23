package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.*;import net.minecraft.world.damagesource.DamageSource;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.syncher.*;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;
/** Native multipart damage/selection bounds, without six extra world entities or a huge solid rotor box. */
public final class DeliveryRotorPart extends net.minecraftforge.entity.PartEntity<DeliveryDroneEntity>{
 public DeliveryRotorPart(DeliveryDroneEntity parent){super(parent);refreshDimensions();}
 @Override public EntityDimensions getDimensions(Pose pose){return EntityDimensions.scalable(1.525f,.16f);}
 @Override protected void defineSynchedData(){}
 @Override protected void readAdditionalSaveData(CompoundTag n){}
 @Override protected void addAdditionalSaveData(CompoundTag n){}
 @Override public boolean isPickable(){return !getParent().isRemoved();}
 @Override public boolean hurt(DamageSource source,float amount){return !level().isClientSide&&getParent().hurt(source,amount);}
 @Override public InteractionResult interact(Player player,InteractionHand hand){return getParent().interact(player,hand);}
 @Override public boolean is(Entity e){return e==this||e==getParent();}
 @Override public net.minecraft.world.item.ItemStack getPickResult(){return getParent().getPickResult();}
}
