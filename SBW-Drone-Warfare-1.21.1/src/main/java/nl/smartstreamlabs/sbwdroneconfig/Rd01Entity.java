package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.*;

/** SBW aerial drone lifecycle and propulsion, with a separately authenticated monitor operator. */
public final class Rd01Entity extends DroneEntity {
    private static final EntityDataAccessor<Float> EXT=SynchedEntityData.defineId(Rd01Entity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SWAY_X=SynchedEntityData.defineId(Rd01Entity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SWAY_Z=SynchedEntityData.defineId(Rd01Entity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> POWERED=SynchedEntityData.defineId(Rd01Entity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> STOW=SynchedEntityData.defineId(Rd01Entity.class,EntityDataSerializers.FLOAT);
    private UUID owner;
    private ServerPlayer operator;
    private Rd01HarnessEntity interaction;
    private int keys,winch;
    private long lastInput;
    private float desiredYaw,oldExt,oldSwayX,oldSwayZ,oldStow=1;
    private boolean geometryReady,forcedExit,suppressDrops,packing;

    public Rd01Entity(EntityType<? extends DroneEntity> type,Level level) {
        super((EntityType<DroneEntity>)(EntityType<?>)type,level);geometryReady=true;setBoundingBox(Rd01Geometry.bounds(this));setHealth(getMaxHealth());
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);builder.define(EXT,0f);builder.define(SWAY_X,0f);builder.define(SWAY_Z,0f);builder.define(POWERED,false);builder.define(STOW,1f);
    }
    @Override public float getMaxHealth(){return VehicleProfiles.health(this,80);}
    public UUID ownerId(){return owner;}
    public ServerPlayer operator(){return operator;}
    public boolean occupied(){return !getPassengers().isEmpty();}
    public boolean forcedExit(){return forcedExit;}
    public boolean permitted(ServerPlayer p){return owner==null||owner.equals(p.getUUID())||p.hasPermissions(2);}
    public void assign(ServerPlayer p){owner=p.getUUID();SbwCompat.assignDroneOwner(this,p);}
    public float extension(float p){return Mth.lerp(p,oldExt,entityData.get(EXT));}
    public float swayX(float p){return Mth.lerp(p,oldSwayX,entityData.get(SWAY_X));}
    public float swayZ(float p){return Mth.lerp(p,oldSwayZ,entityData.get(SWAY_Z));}
    public float stow(float p){return Mth.lerp(p,oldStow,entityData.get(STOW));}
    public float visualPitch(float p){return Mth.clamp(getBodyPitch(p),-12,12);}
    public float visualRoll(float p){return Mth.clamp(getRoll(p),-12,12);}
    public boolean motor(){return entityData.get(POWERED);}
    @Override public boolean engineRunning(){return motor();}
    @Override public float getEngineSoundVolume(){return motor()?.65f:0;}
    @Override public net.minecraft.sounds.SoundEvent getEngineSound(){return AddonSounds.DELIVERY_DRONE_ENGINE.get();}
    public boolean takeControl(ServerPlayer p) {
        if(!permitted(p)||p.isPassenger()||!isAlive()||isWreck()||operator!=null&&operator!=p)return false;
        if(owner==null)assign(p);operator=p;desiredYaw=getYRot();lastInput=level().getGameTime();keys=0;return true;
    }
    public void releaseControl(){operator=null;keys=winch=0;super.processInput((short)0);super.mouseInput(0,0);}
    public void input(ServerPlayer p,int flags,float yaw) {
        if(p!=operator||!Float.isFinite(yaw))return;
        keys=flags&63;winch=(flags&64)!=0?-1:(flags&128)!=0?1:0;
        desiredYaw=Mth.wrapDegrees(yaw);lastInput=level().getGameTime();
    }
    @Override public void processInput(short input){} // native packets may not bypass RD-01 session authority
    @Override public void mouseInput(double x,double y){}

    @Override public void tick() {
        Vec3 replicatedMotion=getDeltaMovement();super.tick();
        // SBW's normal client interpolation remains responsible for pose. Do not run a second gravity simulation.
        if(level().isClientSide())setDeltaMovement(replicatedMotion);
    }

    @Override public void travel() {
        if(level().isClientSide())return;
        boolean stale=operator==null||operator.hasDisconnected()||operator.level()!=level()||!operator.isAlive()||level().getGameTime()-lastInput>12;
        if(stale){keys=winch=0;}
        boolean energy=!AddonConfig.enableBatterySystem()||DroneBatterySystem.currentEnergy(this)>0;
        boolean running=energy&&!isWreck()&&(operator!=null||!onGround()||occupied());
        entityData.set(POWERED,running);
        // Retain SBW DroneEntity's native throttle, propulsive forces and attitude integration.
        Vec3 before=getDeltaMovement();
        super.processInput((short)(running?keys:0));super.mouseInput(0,0);
        super.travel();
        if((keys&12)==0)setBodyXRot(Mth.approach(getBodyPitch(),0,1));
        if((keys&3)==0)setZRot(Mth.approach(getRoll(),0,1));
        if(!stale)setYRot(Mth.approachDegrees(getYRot(),desiredYaw,3));
        // VehicleEntity subtracts computed gravity AFTER travel(), immediately before move().
        // Assist and acceleration limits operate on physical velocity, not on throttle minus an unknown later force.
        double gravity=computed().getGravity();
        Vec3 nativeVelocity=getDeltaMovement().add(0,-gravity,0);
        double horizontal=nativeVelocity.horizontalDistance();
        if(horizontal>Rd01Config.SPEED.get())nativeVelocity=nativeVelocity.multiply(Rd01Config.SPEED.get()/horizontal,1,Rd01Config.SPEED.get()/horizontal);
        double y=Mth.clamp(nativeVelocity.y,-Rd01Config.CLIMB.get(),Rd01Config.CLIMB.get());
        // Cargo flight assist brakes residual tilt thrust and arrests vertical drift on key release.
        if(running&&(keys&15)==0)nativeVelocity=nativeVelocity.multiply(.72,1,.72);
        if(running&&(keys&48)==0)y=before.y*.65;
        if(!running)y=Math.max(-.22,before.y-.025);
        Vec3 desired=new Vec3(nativeVelocity.x,y,nativeVelocity.z),change=desired.subtract(before);
        double accel=Rd01Config.ACCELERATION.get();if(change.length()>accel)change=change.normalize().scale(accel);
        Vec3 velocity=before.add(change);
        if(occupied()&&velocity.y<0&&!Rd01Geometry.harnessClear(this,extension(1),velocity))velocity=new Vec3(velocity.x,0,velocity.z);
        if(!Rd01Geometry.routeLoaded(this,velocity)){velocity=Vec3.ZERO;keys=winch=0;}
        setDeltaMovement(velocity.add(0,gravity,0));setXRot(0);
    }

    @Override public void baseTick() {
        oldExt=entityData.get(EXT);oldSwayX=entityData.get(SWAY_X);oldSwayZ=entityData.get(SWAY_Z);oldStow=entityData.get(STOW);
        if(geometryReady)setBoundingBox(Rd01Geometry.bounds(this));
        super.baseTick();
        if(geometryReady)setBoundingBox(Rd01Geometry.bounds(this));
        if(level().isClientSide()||isRemoved())return;
        DroneBatterySystem.onDroneBaseTick(this);
        if(!isAlive()||isWreck()){emergencyRelease();if(operator!=null)Rd01Sessions.stop(operator);return;}
        if(occupied()&&getPassengers().stream().anyMatch(p->!p.isAlive()||p.level()!=level()||p instanceof ServerPlayer s&&s.hasDisconnected()))emergencyRelease();
        float extension=entityData.get(EXT);
        if(winch!=0&&motor()&&(stow(1)==0||winch<0)) {
            float next=Mth.clamp(extension+winch*Rd01Config.WINCH.get().floatValue(),0,2.5f);
            if(Rd01Geometry.harnessClear(this,next,Vec3.ZERO))entityData.set(EXT,next);
            else if(operator!=null&&tickCount%20==0)operator.displayClientMessage(Component.translatable("rd01.winch_blocked"),true);
        }
        float folded=entityData.get(STOW),targetFold=!occupied()&&winch<0&&extension(1)==0?1:winch>0||occupied()?0:folded;
        float nextFold=Mth.approach(folded,targetFold,.04f);
        if(nextFold!=folded&&Rd01Geometry.harnessClear(this,extension(1),nextFold,Vec3.ZERO))entityData.set(STOW,nextFold);
        // One replicated sway state drives the real passenger and the harness on every client.
        Vec3 local=getDeltaMovement().yRot((float)Math.toRadians(getYRot()));
        float sx=(float)Mth.clamp(local.z*.12,-.045,.045),sz=(float)Mth.clamp(-local.x*.12,-.045,.045);
        if(!Rd01Geometry.harnessClear(this,extension(1),Vec3.ZERO)){sx=sz=0;}
        entityData.set(SWAY_X,Mth.lerp(.18f,entityData.get(SWAY_X),sx));entityData.set(SWAY_Z,Mth.lerp(.18f,entityData.get(SWAY_Z),sz));
        if(interaction==null||interaction.isRemoved()) {
            interaction=AddonEntities.RD01_HARNESS.get().create(level());
            if(interaction!=null){interaction.attach(this);level().addFreshEntity(interaction);}
        }
        if(interaction!=null)interaction.follow();
        if(operator!=null||occupied())ChunkTicketManager.touchDrone(this,1);else ChunkTicketManager.releaseDrone(this);
    }
    @Override public boolean canAddPassenger(Entity e){return e instanceof ServerPlayer p&&p!=operator&&p.isAlive()&&!p.isSpectator()&&!p.isPassenger()&&getPassengers().isEmpty()&&!isWreck()&&isAlive();}
    public InteractionResult board(Player p) {
        if(level().isClientSide())return InteractionResult.SUCCESS;
        Vec3 anchor=Rd01Pose.point(Rd01Pose.harness(this,1),0,18,-4);
        boolean valid=p.level()==level()&&canAddPassenger(p)&&stow(1)==0&&getDeltaMovement().length()<.055&&p.getEyePosition().distanceToSqr(anchor)<Rd01Config.PICKUP.get()*Rd01Config.PICKUP.get()&&Rd01Geometry.harnessClear(this,extension(1),Vec3.ZERO);
        if(!valid){p.displayClientMessage(Component.translatable("rd01.board_failed"),true);return InteractionResult.FAIL;}
        if(p.startRiding(this)){
            if(hasPassenger(p)){p.fallDistance=0;p.displayClientMessage(Component.translatable("rd01.boarded"),true);return InteractionResult.CONSUME;}
            // A data pack removing SBW's required seat must never leave a one-sided player->vehicle reference.
            forcedExit=true;try{p.stopRiding();}finally{forcedExit=false;}
        }
        return InteractionResult.FAIL;
    }
    @Override public void positionRider(Entity p,MoveFunction move) {
        if(hasPassenger(p)){Vec3 at=Rd01Pose.feet(this,1);move.accept(p,at.x,at.y,at.z);p.fallDistance=0;}
    }
    public Optional<Vec3> safeExit(LivingEntity p) {
        Vec3 feet=Rd01Pose.feet(this,1);
        for(double x:new double[]{0,-.8,.8})for(double z:new double[]{0,-.8,.8})for(int dy=1;dy>=-1;dy--) {
            BlockPos ground=BlockPos.containing(feet.add(x,dy,z)).below();
            Vec3 at=new Vec3(feet.x+x,ground.getY()+1.01,feet.z+z);
            if(Math.abs(at.y-feet.y)>.8||!level().hasChunkAt(ground)||!level().getWorldBorder().isWithinBounds(ground))continue;
            if(level().getBlockState(ground).isFaceSturdy(level(),ground,Direction.UP)&&level().noCollision(p,p.getDimensions(Pose.STANDING).makeBoundingBox(at))&&!level().containsAnyLiquid(p.getDimensions(Pose.STANDING).makeBoundingBox(at)))return Optional.of(at);
        }
        return Optional.empty();
    }
    public boolean releasePassenger() {
        if(level().isClientSide()||getDeltaMovement().length()>.06)return false;
        if(getFirstPassenger() instanceof LivingEntity p&&safeExit(p).isPresent()){p.stopRiding();p.fallDistance=0;return true;}
        if(operator!=null)operator.displayClientMessage(Component.translatable("rd01.no_ground"),true);
        return false;
    }
    public void emergencyRelease() {
        if(level().isClientSide()||!occupied())return;
        forcedExit=true;
        try {for(Entity e:List.copyOf(getPassengers())){e.stopRiding();e.fallDistance=0;if(e instanceof LivingEntity living)living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,200,0,false,true));}}
        finally {forcedExit=false;}
    }
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity p){return safeExit(p).orElse(Rd01Pose.feet(this,1));}
    @Override public Vec3 getCameraPosition(float partial,Player viewer,boolean first,boolean zoom){return viewer.getVehicle()==this?Rd01Pose.head(this,partial):super.getCameraPosition(partial,viewer,first,zoom);}
    @Override public Vec2 getCameraRotation(float partial,Player viewer,boolean first,boolean zoom){return viewer.getVehicle()==this?new Vec2(viewer.getViewYRot(partial),viewer.getViewXRot(partial)):super.getCameraRotation(partial,viewer,first,zoom);}
    @Override public boolean canCollideWith(Entity e){return !hasPassenger(e)&&!(e instanceof Rd01HarnessEntity)&&super.canCollideWith(e);}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public void hitEntityCrash(Player p,Entity e){if(!hasPassenger(e)&&e!=interaction)super.hitEntityCrash(p,e);}
    @Override public InteractionResult interact(Player p,InteractionHand hand) {
        if(isRemoved())return InteractionResult.FAIL;
        ItemStack stack=p.getItemInHand(hand);
        if(SbwCompat.isMonitor(stack)) {
            if(p instanceof ServerPlayer sp&&permitted(sp)&&p!=getFirstPassenger()) {
                if(owner==null)assign(sp);VatagMonitorTag.link(stack,getUUID(),blockPosition(),level().dimension().location().toString());Rd01Sessions.mark(stack);
                sp.displayClientMessage(Component.translatable("rd01.linked"),true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if(p.isShiftKeyDown()&&stack.is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR)) {
            if(!(p instanceof ServerPlayer sp)||!permitted(sp)||occupied()||!onGround()||getDeltaMovement().length()>.03){if(!level().isClientSide())p.displayClientMessage(Component.translatable("rd01.pack_blocked"),true);return InteractionResult.FAIL;}
            if(operator!=null)Rd01Sessions.stop(operator);
            CompoundTag n=new CompoundTag();saveWithoutId(n);for(String key:new String[]{"UUID","Pos","Motion","Rotation","Passengers"})n.remove(key);
            ItemStack item=new ItemStack(AddonItems.RD01.get());var saved=AddonStackTags.getOrCreate(item);saved.put("Rd01Saved",n);AddonStackTags.save(item,saved);
            net.neoforged.neoforge.items.ItemHandlerHelper.giveItemToPlayer(p,item);packing=true;discard();return InteractionResult.CONSUME;
        }
        if(stack.isEmpty()&&!p.isShiftKeyDown())return board(p);
        // No native pickup, gun or payload installation route can bypass the occupied check.
        return InteractionResult.PASS;
    }
    @Override public ItemStack getPickResult(){return new ItemStack(AddonItems.RD01.get());}
    @Override protected AABB makeBoundingBox(){return geometryReady?Rd01Geometry.bounds(this):super.makeBoundingBox();}
    @Override public AABB getBoundingBoxForCulling(){return Rd01Geometry.bounds(this).minmax(new AABB(Rd01Pose.hips(this,1),Rd01Pose.hips(this,1)).inflate(1.3,1.8,1.3));}
    @Override public void move(MoverType type,Vec3 wanted) {
        if(level().isClientSide()&&type==MoverType.SELF)return;
        if(!geometryReady){super.move(type,wanted);return;}
        Vec3 allowed=Rd01Geometry.collide(this,wanted);boolean prior=noPhysics;noPhysics=true;
        try{super.move(type,allowed);}finally{noPhysics=prior;}
        horizontalCollision=Math.abs(allowed.x-wanted.x)>1e-7||Math.abs(allowed.z-wanted.z)>1e-7;
        verticalCollision=Math.abs(allowed.y-wanted.y)>1e-7;setOnGround(wanted.y<=0&&Rd01Geometry.grounded(this));
        if(verticalCollision)setDeltaMovement(getDeltaMovement().x,0,getDeltaMovement().z);
    }
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float amount) {
        Entity direct=s.getDirectEntity();
        if(direct instanceof net.minecraft.world.entity.projectile.Projectile shot&&!s.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)&&!Rd01Geometry.ray(this,new Vec3(shot.xo,shot.yo,shot.zo),shot.position().add(shot.getDeltaMovement())))return false;
        if(direct instanceof Player p&&direct==s.getEntity()&&!Rd01Geometry.ray(this,p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(6))))return false;
        return super.hurt(s,amount);
    }
    @Override public void destroy(){if(level().isClientSide())return;emergencyRelease();if(operator!=null)Rd01Sessions.stop(operator);super.destroy();}
    @Override public net.minecraft.world.entity.item.ItemEntity spawnAtLocation(ItemStack stack,float offset){return suppressDrops?null:super.spawnAtLocation(stack,offset);}
    @Override public void remove(RemovalReason reason) {
        if(isRemoved())return;
        if(!level().isClientSide()){emergencyRelease();if(operator!=null)Rd01Sessions.stop(operator);if(interaction!=null)interaction.discard();ChunkTicketManager.releaseDrone(this);}
        suppressDrops=packing||!reason.shouldDestroy();try{super.remove(reason);}finally{suppressDrops=false;}
    }
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("Rd01Owner",owner);n.putFloat("Rd01Extension",entityData.get(EXT));n.putFloat("Rd01Stow",entityData.get(STOW));}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("Rd01Owner")?n.getUUID("Rd01Owner"):null;float value=n.contains("Rd01Extension")?n.getFloat("Rd01Extension"):0;entityData.set(EXT,Float.isFinite(value)?Mth.clamp(value,0,2.5f):0);oldExt=entityData.get(EXT);float fold=n.contains("Rd01Stow")?n.getFloat("Rd01Stow"):1;entityData.set(STOW,Float.isFinite(fold)?Mth.clamp(fold,0,1):1);oldStow=entityData.get(STOW);operator=null;keys=winch=0;entityData.set(SWAY_X,0f);entityData.set(SWAY_Z,0f);entityData.set(POWERED,false);}
}
