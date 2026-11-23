package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.*;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;import net.minecraft.core.*;import net.minecraft.world.item.ItemStack;import net.minecraft.network.syncher.*;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.network.chat.Component;
/** MR-01 uses native SBW inventory, wheel physics and BLU-43 entities, never a replacement mine. */
public class MineRoverEntity extends VatagEntity {
 private static final EntityDataAccessor<Integer> LOAD=SynchedEntityData.defineId(MineRoverEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Long> DEPLOY=SynchedEntityData.defineId(MineRoverEntity.class,EntityDataSerializers.LONG);
 private static final EntityDataAccessor<Boolean> SERVICE=SynchedEntityData.defineId(MineRoverEntity.class,EntityDataSerializers.BOOLEAN);
 private float service,oldService;private boolean held;private long nextDeploy;private final java.util.Map<ServerPlayer,net.minecraft.world.inventory.AbstractContainerMenu> viewers=new java.util.HashMap<>();
 public MineRoverEntity(EntityType<? extends MineRoverEntity> type,Level level){super(type,level);}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(LOAD,0);entityData.define(DEPLOY,-100L);entityData.define(SERVICE,false);}
 // SBW Final's remove() drops inventory even for UNLOADED_TO_CHUNK. Keep unload non-destructive.
 private boolean suppressRemovalDrops;
 @Override public void remove(RemovalReason reason){if(isRemoved())return;if(!level().isClientSide&&operator()!=null)VatagSessions.stop(operator());suppressRemovalDrops=!reason.shouldDestroy();try{super.remove(reason);}finally{suppressRemovalDrops=false;}}
 @Override public net.minecraft.world.entity.item.ItemEntity spawnAtLocation(ItemStack stack,float offset){return suppressRemovalDrops?null:super.spawnAtLocation(stack,offset);}
 public static boolean suitable(ItemStack s){return s.is(AddonItems.LM01.get())||s.getItem() instanceof com.atsuishio.superbwarfare.item.projectile.Blu43MineItem||s.getItem() instanceof com.atsuishio.superbwarfare.item.projectile.Tm62Item;}
 public static boolean battery(ItemStack stack){return stack.getItem() instanceof com.atsuishio.superbwarfare.item.material.BatteryItem;}
 @Override public int getMaxStackSize(){return 1;}
 @Override public boolean canPlaceItem(int slot,ItemStack stack){return slot>=0&&slot<=6&&getItem(slot).isEmpty()&&(slot==6?battery(stack):suitable(stack));}
 public boolean loaded(int slot){return mineKind(slot)!=0;}
 public int mineKind(int slot){return (entityData.get(LOAD)>>(slot*2))&3;}
 public int mineCount(){int count=0;for(int i=0;i<6;i++)if(mineKind(i)!=0)count++;return count;}
 public boolean serviceOpen(){return entityData.get(SERVICE);}
 public float servicePose(float partial){return net.minecraft.util.Mth.lerp(partial,oldService,service);}
 public float gate(float partial){float age=level().getGameTime()+partial-entityData.get(DEPLOY);return age<0||age>13?0:(float)Math.sin(Math.PI*age/13);}
 @Override public Vec3 camera(boolean aim,float partial){var v=transformPosition(getVehicleTransform(partial),0,.85,1.3375);return new Vec3(v.x,v.y,v.z);}
 @Override public void releaseControl(){super.releaseControl();held=false;}
 @Override public void input(ServerPlayer p,int flags,float yaw,float pitch){if(operator()!=p)return;boolean pressed=(flags&16)!=0;super.input(p,flags&~48,yaw,0);if(pressed&&!held)deploy(p);held=pressed;}
 public boolean deploy(ServerPlayer p){
  if(level().isClientSide||operator()!=p||!VatagSessions.controlling(p,this)||!permitted(p)||isWreck()||!isAlive())return false;
  long now=level().getGameTime();if(now<nextDeploy)return false;nextDeploy=now+13;
  if(getDeltaMovement().horizontalDistance()>.04){notice(p,"Stop before deploying a mine.");return false;}
  if(java.util.stream.IntStream.range(0,6).anyMatch(i->getItem(i).is(AddonItems.LM01.get()))){boolean placed=Lm01Placement.deploy(this,p,6);if(placed){entityData.set(DEPLOY,now);syncLoad();}else notice(p,"No free supported ground behind the rack. Move the rover forward to lay more mines.");return placed;}
  if(getDeltaMovement().horizontalDistance()>.04){notice(p,"Stop before deploying a mine.");return false;}
  int slot=-1;for(int i=0;i<6;i++)if(suitable(getItem(i))){slot=i;break;}if(slot<0){notice(p,"No LM-01, TM-62 or BLU-43 mines loaded.");return false;}
  // Supplied deploy anchor, extended beyond the native vehicle collision envelope.
  var anchor=transformPosition(getVehicleTransform(1),0,15.236777274921637/16,27.128810753898275/16);
  var direction=getVehicleTransform(1).transformDirection(new org.joml.Vector3d(0,0,1));Vec3 at=new Vec3(anchor.x,anchor.y,anchor.z).add(direction.x*.6,0,direction.z*.6);
  if(!level().hasChunkAt(BlockPos.containing(at))||!level().getWorldBorder().isWithinBounds(BlockPos.containing(at)))return false;
  var hit=level().clip(new ClipContext(at.add(0,1,0),at.add(0,-2.5,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
  if(hit.getType()!=HitResult.Type.BLOCK||hit.getDirection()!=Direction.UP||!level().getFluidState(hit.getBlockPos().above()).isEmpty()){notice(p,"Mine placement blocked: level solid ground required.");return false;}
  Entity mine=getItem(slot).getItem() instanceof com.atsuishio.superbwarfare.item.projectile.Tm62Item?new com.atsuishio.superbwarfare.entity.projectile.Tm62Entity(p,level(),false):new com.atsuishio.superbwarfare.entity.projectile.Blu43Entity(p,level());Vec3 pos=hit.getLocation().add(0,.02,0);mine.moveTo(pos.x,pos.y,pos.z,getYRot(),0);mine.setDeltaMovement(Vec3.ZERO);
  if(isInObb(mine,pos)||!level().noCollision(mine,mine.getBoundingBox())||!level().getEntities(this,mine.getBoundingBox().inflate(.2),e->e!=mine&&e.isPickable()).isEmpty()){notice(p,"Mine placement blocked: clear the deployment area.");return false;}
  if(!level().addFreshEntity(mine))return false;
  removeItem(slot,1);setChanged();entityData.set(DEPLOY,now);syncLoad();return true;
 }
 private void notice(ServerPlayer p,String message){p.displayClientMessage(Component.literal("MR-01: "+message),true);}
 private void syncLoad(){int mask=0;for(int i=0;i<6;i++)if(suitable(getItem(i)))mask|=(getItem(i).is(AddonItems.LM01.get())?3:getItem(i).getItem() instanceof com.atsuishio.superbwarfare.item.projectile.Tm62Item?2:1)<<(i*2);if(mask!=entityData.get(LOAD)&&tickCount>5){var sound=net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.tryParse("superbwarfare:mortar_load"));if(sound!=null)level().playSound(null,getX(),getY(),getZ(),sound,net.minecraft.sounds.SoundSource.BLOCKS,.25f,1); }entityData.set(LOAD,mask);}
 @Override public void baseTick(){super.baseTick();if(level().isClientSide){oldService=service;service=net.minecraft.util.Mth.approach(service,serviceOpen()?1:0,.1f);}if(!level().isClientSide){syncLoad();viewers.entrySet().removeIf(e->e.getKey().hasDisconnected()||e.getKey().containerMenu!=e.getValue()||e.getKey().distanceToSqr(this)>64);entityData.set(SERVICE,!viewers.isEmpty());}}
 @Override public InteractionResult interact(Player p,InteractionHand hand){if(p instanceof ServerPlayer sp&&p.getItemInHand(hand).isEmpty()&&!p.isShiftKeyDown()){if(!permitted(sp)||p.distanceToSqr(this)>64||operator()!=null)return InteractionResult.FAIL;claimOwner(sp);openCustomInventoryScreen(sp);viewers.put(sp,sp.containerMenu);notice(sp,"Slots 1-6: LM-01 / TM-62 / BLU-43 mines. Slot 7: charged SBW battery.");return InteractionResult.CONSUME;}return super.interact(p,hand);}
}
