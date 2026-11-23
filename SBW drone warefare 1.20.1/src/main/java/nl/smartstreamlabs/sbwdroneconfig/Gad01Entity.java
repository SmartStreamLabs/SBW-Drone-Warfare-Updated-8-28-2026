package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.world.phys.*;import net.minecraft.world.*;import net.minecraft.world.item.*;import net.minecraft.world.entity.player.Player;import net.minecraft.server.level.*;import net.minecraft.network.chat.Component;import net.minecraft.network.syncher.*;import net.minecraft.nbt.CompoundTag;
/** One native SBW wheel vehicle. Native inventory is the sole source of payload stacks. */
public class Gad01Entity extends VatagEntity {
 private static final java.util.List<EntityDataAccessor<ItemStack>> MINES=java.util.stream.IntStream.range(0,2).mapToObj(i->SynchedEntityData.defineId(Gad01Entity.class,EntityDataSerializers.ITEM_STACK)).toList();
 private static final EntityDataAccessor<Integer> ARM=SynchedEntityData.defineId(Gad01Entity.class,EntityDataSerializers.INT);
 private long lm01NextDeploy;
 public boolean deployLandmine(ServerPlayer p){if(level().getGameTime()<lm01NextDeploy||!Lm01Placement.deploy(this,p,2))return false;lm01NextDeploy=level().getGameTime()+13;return true;}
 private boolean settled,suppressDrops;private long lastHold=-100;private Vec3 lastVisual;private float lastYaw;private double left,right,oldLeft,oldRight;
 public Gad01Entity(EntityType<? extends Gad01Entity> type,Level l){super(type,l);}
 @Override protected void defineSynchedData(){super.defineSynchedData();for(var key:MINES)entityData.define(key,ItemStack.EMPTY);entityData.define(ARM,0);}
 @Override public int getContainerSize(){return 3;}
 @Override public int getMaxStackSize(){return 1;}
 @Override public boolean canPlaceItem(int slot,ItemStack stack){return !settled&&slot>=0&&slot<3&&getItem(slot).isEmpty()&&(slot==2?MineRoverEntity.battery(stack):MineRoverEntity.suitable(stack));}
 public ItemStack removeItemNoUpdate(int slot){return removeItem(slot,Integer.MAX_VALUE);}
 public ItemStack displayedMine(int slot){return entityData.get(MINES.get(slot));}
 public int mineCount(){int count=0;for(int i=0;i<2;i++)if(MineRoverEntity.suitable(displayedMine(i)))count++;return count;}
 public int armTicks(){return entityData.get(ARM);}
 private void syncMines(){for(int i=0;i<2;i++){var s=getItem(i);if(!ItemStack.matches(s,displayedMine(i)))entityData.set(MINES.get(i),s.copy());}}
 @Override public void setChanged(){super.setChanged();if(!level().isClientSide)syncMines();}
 @Override public double getTargetSpeed(){return super.getTargetSpeed()*Gad01Config.SPEED.get();}
 @Override public float getStepHeight(){return Gad01Config.STEP.get().floatValue();}
 @Override public float maxUpStep(){return Gad01Config.STEP.get().floatValue();}
 @Override public void processInput(short keys){} // no native passenger/pilot bypass
 @Override public int getTurretControllerIndex(){return -1;}
 @Override protected boolean remoteFireAllowed(){return false;}
 @Override public boolean canAddPassenger(Entity e){return false;}
 @Override public void travel(){if(!level().isClientSide)super.travel();}
 @Override public void move(MoverType type,Vec3 delta){if(!level().isClientSide)super.move(type,delta);}
 @Override public void input(ServerPlayer p,int flags,float yaw,float pitch){
  if(operator()!=p)return;boolean jam=DroneJamStateManager.getJamProgress(this)>=AddonConfig.controlInterferenceThreshold();
  super.input(p,jam||getEnergy()<=0?64:flags&15|flags&64,yaw,0);
  long now=level().getGameTime();if((flags&16)==0||!canDetonate(p)){entityData.set(ARM,0);lastHold=-100;return;}
  int ticks=now-lastHold<=2?armTicks()+1:1;lastHold=now;entityData.set(ARM,Math.min(20,ticks));if(ticks>=20)detonate(p);
 }
 public boolean canDetonate(ServerPlayer p){return p!=null&&operator()==p&&VatagSessions.controlling(p,this)&&permitted(p)&&p.isAlive()&&!p.hasDisconnected()&&!settled&&!isRemoved()&&isAlive()&&!isWreck()&&getEnergy()>0&&mineCount()>0&&!DroneJammerSystem.isJammed(this);}
 @Override public void releaseControl(){super.releaseControl();lastHold=-100;entityData.set(ARM,0);}
 @Override public boolean takeControl(ServerPlayer p){return !settled&&!DroneJamStateManager.isHardJammed(this)&&super.takeControl(p);}
 public boolean detonate(ServerPlayer p){
  if(level().isClientSide||!canDetonate(p))return false;
  float damage=0,radius=0;int count=0;String payload="";
  for(int i=0;i<2;i++){var s=getItem(i);if(!MineRoverEntity.suitable(s))continue;boolean tm=s.getItem() instanceof com.atsuishio.superbwarfare.item.projectile.Tm62Item;damage+=Lm01Payload.damage(s);radius=Math.max(radius,Lm01Payload.radius(s));count++;payload=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()).toString();}
  if(count==0)return false;damage=damage/count*(count==1?Gad01Config.ONE_DAMAGE:Gad01Config.TWO_DAMAGE).get().floatValue();if(count==2)radius*=Gad01Config.TWO_RADIUS.get().floatValue();
  // Commit once before callbacks or explosion events can re-enter. No deployed mine entities.
  settled=true;for(int i=0;i<3;i++)removeItemNoUpdate(i);syncMines();DroneAttackDamage.mark(this,this,payload);closeViewers();VatagSessions.stop(p);ChunkTicketManager.releaseDrone(this);discard();
  try(var ignored=DroneAttackDamage.scope(this)){new com.atsuishio.superbwarfare.tools.CustomExplosion.Builder(this).directSource(this).source(this).attacker(p).position(position()).damage(damage).radius(radius).keepBlock().fireTime(0).explode();}return true;
 }
 private void closeViewers(){if(level() instanceof ServerLevel l)for(var p:l.getServer().getPlayerList().getPlayers())if(p.containerMenu instanceof Gad01Menu menu&&menu.vehicleId==getId())p.closeContainer();}
 private void dropContents(){if(settled)return;settled=true;for(int i=0;i<3;i++){var stack=removeItemNoUpdate(i);if(!stack.isEmpty())super.spawnAtLocation(stack.copy(),0);}syncMines();}
 @Override public void destroy(){if(level().isClientSide)return;Sk01Deaths.destroyed(this);dropContents();discard();}
 @Override public net.minecraft.world.entity.item.ItemEntity spawnAtLocation(ItemStack s,float offset){return suppressDrops?null:super.spawnAtLocation(s,offset);}
 @Override public void remove(RemovalReason why){if(isRemoved())return;if(!level().isClientSide){if(why.shouldDestroy())dropContents();if(operator()!=null)VatagSessions.stop(operator());closeViewers();ChunkTicketManager.releaseDrone(this);}suppressDrops=true;try{super.remove(why);}finally{suppressDrops=false;}}
 public boolean pack(ServerPlayer p){if(!permitted(p)||settled||getDeltaMovement().lengthSqr()>.0004){p.displayClientMessage(Component.translatable("gad01.pack_stopped"),true);return false;}if(operator()!=null)VatagSessions.stop(operator());closeViewers();var data=new CompoundTag();saveWithoutId(data);for(String k:new String[]{"UUID","Pos","Motion","Rotation","Passengers"})data.remove(k);var item=com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(this);if(!p.getInventory().add(item)&&p.drop(item,false)==null)return false;settled=true;for(int i=0;i<3;i++)removeItemNoUpdate(i);discard();return true;}
 @Override public InteractionResult interact(Player p,InteractionHand hand){if(isRemoved())return InteractionResult.FAIL;var held=p.getItemInHand(hand);if(p instanceof ServerPlayer sp){if(!permitted(sp))return InteractionResult.FAIL;if(p.isShiftKeyDown()&&held.is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR))return pack(sp)?InteractionResult.CONSUME:InteractionResult.FAIL;if(held.isEmpty()||MineRoverEntity.suitable(held)||MineRoverEntity.battery(held)){if(operator()!=null)return InteractionResult.FAIL;claimOwner(sp);SbwCompat.ensureDroneOwner(this,sp);net.minecraftforge.network.NetworkHooks.openScreen(sp,new SimpleMenuProvider((id,inv,who)->new Gad01Menu(id,inv,this),Component.translatable("entity.sbwdroneconfig.ground_assault_drone_gad01")),buf->buf.writeInt(getId()));return InteractionResult.CONSUME;}if(SbwCompat.isMonitor(held)){SbwCompat.ensureDroneOwner(this,sp);}}
  return held.isEmpty()?InteractionResult.sidedSuccess(level().isClientSide):super.interact(p,hand);
 }
 @Override public Vec3 camera(boolean aim,float partial){var v=getVehicleTransform(partial).transformPosition(new org.joml.Vector3d(0,.4,.625));return new Vec3(v.x,v.y,v.z);}
 public double wheelTravel(boolean isLeft,float p){return isLeft?oldLeft+(left-oldLeft)*p:oldRight+(right-oldRight)*p;}
 @Override public void baseTick(){super.baseTick();if(!level().isClientSide){syncMines();if(armTicks()>0&&level().getGameTime()-lastHold>2)entityData.set(ARM,0);return;}oldLeft=left;oldRight=right;if(lastVisual!=null){Vec3 delta=position().subtract(lastVisual);if(delta.lengthSqr()<9){var f=getVehicleTransform(1).transformDirection(new org.joml.Vector3d(0,0,1));double distance=delta.dot(new Vec3(f.x,f.y,f.z)),turn=net.minecraft.util.Mth.wrapDegrees(getYRot()-lastYaw)*Math.PI/180*.49;left+=distance+turn;right+=distance-turn;}}lastVisual=position();lastYaw=getYRot();}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putBoolean("GadCargoSettled",settled);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);settled=n.getBoolean("GadCargoSettled");entityData.set(ARM,0);lastHold=-100;syncMines();}
 @Override public AABB getBoundingBoxForCulling(){return getBoundingBox().inflate(.6,1.4,.6);}
}
