package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.world.phys.*;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.server.level.ServerPlayer;import net.minecraft.core.BlockPos;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.syncher.*;import net.minecraft.network.chat.Component;import java.util.*;
public class MedicalTrailerEntity extends GeoVehicleEntity implements RescueVehicle {
 /** Physics origin lies on the real wheel axle; supplied model coordinates are translated once. */
 public static final double AXLE_Z=7.0/16;
 private static final EntityDataAccessor<String> LINK=SynchedEntityData.defineId(MedicalTrailerEntity.class,EntityDataSerializers.STRING);
 private float rails,oldRails,leg,oldLeg,latch,oldLatch;
 private UUID owner;private BlockPos lastPartner=BlockPos.ZERO;public boolean forcedExit;
 public MedicalTrailerEntity(EntityType<? extends MedicalTrailerEntity> t,Level l){super(t,l);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(LINK,"");}
 public com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity unit(){return this;}public UUID partner(){String id=entityData.get(LINK);return id.isEmpty()?null:UUID.fromString(id);}public void partner(UUID id,BlockPos last){entityData.set(LINK,id==null?"":id.toString());lastPartner=last;}public BlockPos lastPartnerPosition(){return lastPartner;}public boolean occupied(){return !getPassengers().isEmpty();}public boolean permitted(ServerPlayer p){return owner==null||owner.equals(p.getUUID())||p.hasPermissions(2);}
 @Override public void move(MoverType type,Vec3 movement){if(!HitchJoint.propose(this,type,movement))super.move(type,movement);}
 /** Articulate between completed physics poses too: linear position/angle interpolation is not a rigid joint. */
 public Vec3 hitchRenderOffset(float partial){
  if(!level().isClientSide||partial>=1||partner()==null)return Vec3.ZERO;
  var peer=RescueCoupling.other(this);if(!(peer instanceof SupplyRoverEntity r)||!HitchJoint.paired(r,this))return Vec3.ZERO;
  // Only interpolate an already-solved physical joint; never hide an unsolved tick or missing peer.
  var physical=transformPosition(super.getVehicleTransform(1),0,.65625,2.25+AXLE_Z);
  if(r.hitch().distanceTo(new Vec3(physical.x,physical.y,physical.z))>.08)return Vec3.ZERO;
  var a=r.transformPosition(r.getVehicleTransform(partial),0,.65625,-2.15625);
  var b=transformPosition(super.getVehicleTransform(partial),0,.65625,2.25+AXLE_Z);
  return new Vec3(a.x-b.x,a.y-b.y,a.z-b.z);
 }
 @Override public org.joml.Matrix4d getVehicleTransform(float partial){var matrix=super.getVehicleTransform(partial);var offset=hitchRenderOffset(partial);return matrix.setTranslation(matrix.m30()+offset.x,matrix.m31()+offset.y,matrix.m32()+offset.z);}
 @Override public boolean canCollideWith(Entity other){return !HitchJoint.paired(this,other)&&super.canCollideWith(other);}
 @Override public void baseTick(){super.baseTick();if(level().isClientSide){oldRails=rails;oldLeg=leg;oldLatch=latch;rails=net.minecraft.util.Mth.approach(rails,occupied()?0:1,1f/12);leg=net.minecraft.util.Mth.approach(leg,partner()!=null?1:0,.1f);latch=net.minecraft.util.Mth.approach(latch,partner()==null?1:0,1f/6);}if(!level().isClientSide){if((isWreck()||!isAlive())&&occupied()){forcedExit=true;ejectPassengers();forcedExit=false;}RescueCoupling.tick(this);}}
 // A passive wheeled trailer has no FE storage; native wheel physics still queries these accessors.
 @Override public int getEnergy(){return 0;}@Override public int getMaxEnergy(){return 0;}@Override public void setEnergy(int ignored){}@Override public void consumeEnergy(int ignored){}
 @Override public void travel(){setForwardInputDown(false);setBackInputDown(false);setLeftInputDown(false);setRightInputDown(false);setFireInputDown(false);setPower(0);setUpInputDown(partner()==null);super.travel();}
 @Override public boolean canAddPassenger(Entity e){return e instanceof Player&&getPassengers().isEmpty()&&!isWreck()&&isAlive();}
 @Override public void positionRider(Entity e,MoveFunction move){if(hasPassenger(e)){Vec3 at=RescueCoupling.point(this,0,1.0875,.875-(1.501+.25)*.9375+AXLE_Z);move.accept(e,at.x,at.y,at.z);}}
 public void claim(ServerPlayer p){if(owner==null&&permitted(p))owner=p.getUUID();}
 @Override public net.minecraft.world.phys.AABB getBoundingBoxForCulling(){return super.getBoundingBoxForCulling().inflate(2,.5,2);}
 public float railPose(float p){return net.minecraft.util.Mth.lerp(p,oldRails,rails);}public float legPose(float p){return net.minecraft.util.Mth.lerp(p,oldLeg,leg);}public float latchPose(float p){return net.minecraft.util.Mth.lerp(p,oldLatch,latch);}
 public Vec3 patientCamera(){return RescueCoupling.point(this,0,1.34,.875+AXLE_Z);}
 public Optional<Vec3> safeExit(LivingEntity p){for(double x:new double[]{-1.55,1.55,-2,2}){Vec3 at=RescueCoupling.point(this,x,.08,.125+AXLE_Z);for(int dy=1;dy>=-2;dy--){Vec3 pos=at.add(0,dy,0);AABB box=p.getDimensions(Pose.STANDING).makeBoundingBox(pos);BlockPos below=BlockPos.containing(pos).below();if(level().getWorldBorder().isWithinBounds(BlockPos.containing(pos))&&level().hasChunkAt(below)&&level().getBlockState(below).isFaceSturdy(level(),below,net.minecraft.core.Direction.UP)&&level().noCollision(p,box)&&!level().containsAnyLiquid(box))return Optional.of(pos);}}return Optional.empty();}
 @Override public Vec3 getDismountLocationForPassenger(LivingEntity p){return safeExit(p).orElseGet(()->super.getDismountLocationForPassenger(p));}
 @Override public InteractionResult interact(Player p,InteractionHand h){if(isRemoved())return InteractionResult.FAIL;if(level().isClientSide)return InteractionResult.SUCCESS;if(!(p instanceof ServerPlayer sp)||p.isSpectator())return InteractionResult.FAIL;if(p.getItemInHand(h).isEmpty()&&!p.isShiftKeyDown()&&canAddPassenger(p)&&p.distanceToSqr(this)<36&&getDeltaMovement().horizontalDistance()<.04){if(owner==null)owner=p.getUUID();p.startRiding(this);return InteractionResult.CONSUME;}if(!permitted(sp))return InteractionResult.FAIL;var tool=RescueCoupling.tool(sp,this,h);if(tool!=null)return tool;if(SbwCompat.isMonitor(p.getItemInHand(h))){sp.displayClientMessage(Component.literal("MT-01: passive trailer. Control the Supply Rover."),true);return InteractionResult.CONSUME;}if(p.getItemInHand(h).isEmpty()&&!p.isShiftKeyDown()){if(owner==null)owner=p.getUUID();if(canAddPassenger(p)&&p.distanceToSqr(this)<36&&getDeltaMovement().horizontalDistance()<.04){p.startRiding(this);return InteractionResult.CONSUME;}sp.displayClientMessage(Component.literal("MT-01: stop the trailer and check the stretcher is empty."),true);return InteractionResult.FAIL;}return super.interact(p,h);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("MedicalOwner",owner);RescueCoupling.save(this,n);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("MedicalOwner")?n.getUUID("MedicalOwner"):null;RescueCoupling.load(this,n);}
 @Override public void remove(RemovalReason why){if(!level().isClientSide&&why.shouldDestroy()){forcedExit=true;ejectPassengers();RescueCoupling.detach(this);}if(!level().isClientSide)RescueCoupling.forget(getUUID());super.remove(why);}
}
