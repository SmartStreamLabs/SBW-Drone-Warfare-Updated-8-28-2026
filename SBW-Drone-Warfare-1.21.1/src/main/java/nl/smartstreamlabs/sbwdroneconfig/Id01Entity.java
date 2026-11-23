package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.UUID;

/** VTOL controller inside SBW's flying vehicle lifecycle. SBW owns movement/collision and replication. */
public class Id01Entity extends DroneEntity {
    public enum Mode { STANDBY, LAUNCHING, MANUAL, EN_ROUTE, INTERCEPTING, TARGET_LOST, RETURNING, LANDING, DISABLED }
    private static final EntityDataAccessor<Integer> MODE=SynchedEntityData.defineId(Id01Entity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ENERGY=SynchedEntityData.defineId(Id01Entity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TILT=SynchedEntityData.defineId(Id01Entity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BODY_PITCH=SynchedEntityData.defineId(Id01Entity.class,EntityDataSerializers.FLOAT);
    private UUID owner,target;
    private boolean geometryReady;
    private Vec3 home;
    private ServerPlayer operator;
    private long lastInput;
    private int buttons,flightTicks,stateTicks;
    private float desiredYaw,oldTilt=90,oldPosePitch;
    private boolean spent,blastBlocked;
    void explosionBlocked(boolean blocked){blastBlocked=blocked;}
    private Vec3 previousNose,previousTarget;
    private UUID sampledTarget;

    public Id01Entity(EntityType<? extends DroneEntity> type,Level level){super((EntityType<DroneEntity>)(EntityType<?>)type,level);geometryReady=true;setBoundingBox(Id01Geometry.bounds(this));}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(MODE,0);builder.define(ENERGY,0);builder.define(TILT,90f);builder.define(BODY_PITCH,0f);}
    public Mode mode(){return Mode.values()[Mth.clamp(entityData.get(MODE),0,Mode.values().length-1)];}
    private void mode(Mode m){if(mode()!=m){entityData.set(MODE,m.ordinal());stateTicks=0;}}
    public float tilt(float p){return Mth.lerp(p,oldTilt,entityData.get(TILT));}
    public float posePitch(float p){return Mth.lerp(p,oldPosePitch,entityData.get(BODY_PITCH));}
    public String status(){return mode()==Mode.STANDBY?"READY":mode()==Mode.LAUNCHING?"LAUNCHING":mode()==Mode.DISABLED?"ENDED":"INTERCEPTING";}
    @Override public boolean engineRunning(){return motor();}
    @Override public float getEngineSoundVolume(){return motor()?.7f:0;}
    @Override public net.minecraft.sounds.SoundEvent getEngineSound(){return AddonSounds.CUBED_FPV_DRONE_ENGINE.get();}
    public boolean motor(){return mode()!=Mode.STANDBY&&mode()!=Mode.DISABLED&&energy()>0;}
    public int energy(){return level().isClientSide()?entityData.get(ENERGY):AddonConfig.enableBatterySystem()?DroneBatterySystem.currentEnergy(this):AddonConfig.maxEnergy();}
    private int reserve(){double distance=home==null?0:home.subtract(position()).horizontalDistance();double needed=distance/(Id01Config.SPEED.get()*.65)*AddonConfig.energyUsePerTick()+100*AddonConfig.energyUsePerTick();return (int)Math.min(AddonConfig.maxEnergy()*.8,Math.max(Id01Config.RESERVE.get(),needed));}
    public UUID targetId(){return target;}
    public static boolean contactPair(Entity a,Entity b){return a instanceof Id01Entity left&&left.armedFor(b)||b instanceof Id01Entity right&&right.armedFor(a);}
    private boolean armedFor(Entity other){return target!=null&&other!=null&&target.equals(other.getUUID())&&(mode()==Mode.EN_ROUTE||mode()==Mode.INTERCEPTING)&&Id01Radar.hostile(this,other);}
    @Override public boolean canBeCollidedWith(){return false;} // Native OBB support uses the component query, not the broad hull.
    @Override public boolean canCollideWith(Entity other){return !contactPair(this,other)&&super.canCollideWith(other);}
    public UUID ownerId(){return owner;}
    public Vec3 home(){return home;}
    public void relocateHome(){home=position();}
    public ServerPlayer operator(){return operator;}
    public boolean permitted(ServerPlayer p){return owner==null||owner.equals(p.getUUID())||p.hasPermissions(2);}
    public void carrierOwner(UUID id,String name){if(owner==null){owner=id;SbwCompat.assignDroneOwner(this,id,name);home=position();}}
    public boolean carrierLaunch(UUID goal){if(!(level()instanceof ServerLevel l)||mode()!=Mode.STANDBY||spent||!Id01Radar.hostile(this,l.getEntity(goal))||energy()<=Math.min(Id01Config.RESERVE.get(),AddonConfig.maxEnergy()*.8))return false;target=goal;home=position();flightTicks=0;sampledTarget=null;getPersistentData().remove(MissionManager.NO_TICKETS);mode(Mode.LAUNCHING);return true;}
    public void assign(ServerPlayer p){owner=p.getUUID();SbwCompat.assignDroneOwner(this,p);home=position();}
    public boolean takeControl(ServerPlayer p){if(!permitted(p)||spent||isWreck()||operator!=null&&operator!=p)return false;if(owner==null)assign(p);operator=p;lastInput=level().getGameTime();desiredYaw=getYRot();buttons=0;return true;}
    public void releaseControl(){operator=null;buttons=0;}
    public void input(ServerPlayer p,int keys,float yaw){if(p==operator)lastInput=level().getGameTime();} // observation heartbeat only
    public void loadingHeartbeat(ServerPlayer p){input(p,0,getYRot());}
    public boolean launch(){
        if(!(level() instanceof ServerLevel launchLevel)||target==null||!Id01Radar.hostile(this,launchLevel.getEntity(target))||mode()!=Mode.STANDBY||spent||energy()<=Math.min(Id01Config.RESERVE.get(),AddonConfig.maxEnergy()*.8)||!onGround())return false;
        var box=getBoundingBox().expandTowards(0,5,0);
        if(!level().hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))||!Id01Geometry.parts(this).stream().allMatch(b->level().noCollision(this,b.expandTowards(0,5,0))))return false;
        home=position();flightTicks=0;sampledTarget=null;getPersistentData().remove(MissionManager.NO_TICKETS);mode(Mode.LAUNCHING);return true;
    }
    public boolean intercept(UUID id){
        if(!(level() instanceof ServerLevel l)||mode()==Mode.DISABLED||spent)return false;
        Entity t=l.getEntity(id);if(!Id01Radar.hostile(this,t)||t.distanceToSqr(this)>Id01Config.RANGE.get()*Id01Config.RANGE.get())return false;
        target=id;if(mode()==Mode.STANDBY&&!launch()){target=null;return false;}
        target=id;sampledTarget=null;if(mode()!=Mode.LAUNCHING)mode(Mode.EN_ROUTE);return true;
    }
    @Override public void processInput(short keys){} // refuse native SBW movement packets for this entity only
    @Override public void mouseInput(double x,double y){} // refuse native SBW mouse packets
    public void manual(){} // legacy binary entry point deliberately cannot grant piloting
    public void requestReturn(){if(mode()!=Mode.STANDBY)terminate();}
    @Override public void baseTick(){
        oldTilt=entityData.get(TILT);oldPosePitch=entityData.get(BODY_PITCH);
        if(geometryReady)setBoundingBox(Id01Geometry.bounds(this));
        super.baseTick();
        if(geometryReady)setBoundingBox(Id01Geometry.bounds(this));
        if(level().isClientSide()||isRemoved())return;
        DroneBatterySystem.onDroneBaseTick(this);entityData.set(ENERGY,energy());
        if(target!=null&&level() instanceof ServerLevel l){
            Entity t=l.getEntity(target);
            if(!Id01Radar.hostile(this,t)){terminate();}
            else {
                Vec3 nose=Id01Pose.nose(this),center=t.getBoundingBox().getCenter();
                if(target.equals(sampledTarget)&&previousNose!=null&&previousTarget!=null&&
                    (mode()==Mode.EN_ROUTE||mode()==Mode.INTERCEPTING)&&
                    Id01Pose.contact(previousNose,nose,previousTarget,t.getBoundingBox())){detonate(t);return;}
                previousNose=nose;previousTarget=center;sampledTarget=target;
            }
        }
        if(mode()==Mode.STANDBY||mode()==Mode.DISABLED){getPersistentData().putBoolean(MissionManager.NO_TICKETS,true);ChunkTicketManager.releaseDrone(this);}
        else ChunkTicketManager.touchDrone(this,1);
    }
    @Override public void travel(){
        if(level().isClientSide())return;
        stateTicks++;
        if(operator!=null&&(operator.hasDisconnected()||!operator.isAlive()||operator.level()!=level()||level().getGameTime()-lastInput>25))Id01Sessions.stop(operator);
        if(mode()==Mode.STANDBY){setDeltaMovement(0,-.04,0);entityData.set(TILT,90f);entityData.set(BODY_PITCH,0f);return;}
        if(mode()==Mode.DISABLED){
            if(onGround()){setDeltaMovement(0,-.04,0);}else{setDeltaMovement(Vec3.directionFromRotation(0,getYRot()).scale(Id01Config.MIN_SPEED.get()*.5).add(0,Math.max(-.6,getDeltaMovement().y-.04),0));}
            if(stateTicks>150)ChunkTicketManager.releaseDrone(this);return;
        }
        flightTicks++;DroneJammerSystem.beforeDroneTravel(this);
        Entity t=target==null?null:((ServerLevel)level()).getEntity(target);
        if(!Id01Radar.hostile(this,t)||energy()<=0||flightTicks>Id01Config.DURATION.get()||DroneJammerSystem.isJammed(this)){terminate();return;}
        if(mode()==Mode.LAUNCHING){
            double rise=getY()-home.y;if(rise>=3.5)entityData.set(TILT,Mth.approach(entityData.get(TILT),0,2.25f));
            double blend=1-entityData.get(TILT)/90.0;
            setDeltaMovement(Vec3.directionFromRotation(0,getYRot()).scale(Id01Config.MIN_SPEED.get()*blend).add(0,.22*(1-blend)+.03,0));
            if(entityData.get(TILT)==0)mode(Mode.INTERCEPTING);return;
        }
        mode(Mode.INTERCEPTING);entityData.set(TILT,0f);
        Vec3 delta=t.getBoundingBox().getCenter().subtract(Id01Pose.nose(this));float heading=(float)Math.toDegrees(-Math.atan2(delta.x,delta.z));
        setYRot(Mth.approachDegrees(getYRot(),heading,Id01Config.TURN.get().floatValue()));
        float pitch=(float)-Math.toDegrees(Math.atan2(delta.y,Math.max(.01,delta.horizontalDistance())));
        Vec3 ahead=Vec3.directionFromRotation(0,getYRot()).scale(4);
        if(level().getBlockCollisions(this,getBoundingBox().expandTowards(ahead)).iterator().hasNext())pitch=-30;
        entityData.set(BODY_PITCH,Mth.approachDegrees(entityData.get(BODY_PITCH),Mth.clamp(pitch,-30,30),2));
        Vec3 desired=Vec3.directionFromRotation(entityData.get(BODY_PITCH),getYRot()).scale(Math.max(Id01Config.MIN_SPEED.get(),Id01Config.SPEED.get()));
        Vec3 change=desired.subtract(getDeltaMovement());if(change.length()>Id01Config.ACCELERATION.get())change=change.normalize().scale(Id01Config.ACCELERATION.get());
        Vec3 velocity=getDeltaMovement().add(change);double minimum=Id01Config.MIN_SPEED.get();if(velocity.length()<minimum)velocity=desired.normalize().scale(minimum);
        if(!level().hasChunkAt(BlockPos.containing(position().add(velocity.scale(3))))){terminate();return;}
        setDeltaMovement(velocity);setXRot(0);setBodyXRot(0);setZRot(0);
        if(horizontalCollision){terminate();}
    }
    public void vc01Detonate(Vec3 at){
        if(spent||!(level() instanceof ServerLevel l))return;
        spent=true;getPersistentData().putBoolean("Id01Spent",true);target=null;
        if(operator!=null)Id01Sessions.stop(operator);ChunkTicketManager.releaseDrone(this);
        float power=Id01Config.BLAST.get().floatValue();DroneExplosionVfx.detonate(l,at,power);
        try(var scope=DroneExplosionVfx.explosionScope(l,this,at.x,at.y,at.z,power,true)){l.explode(this,at.x,at.y,at.z,power,AddonConfig.enableDroneCrashExplosionDamage()?Level.ExplosionInteraction.TNT:Level.ExplosionInteraction.NONE);}
    }
    public void terminate(){target=null;sampledTarget=null;buttons=0;mode(Mode.DISABLED);}
    private void detonate(Entity t){
        if(spent||!(level() instanceof ServerLevel l)||!Id01Radar.hostile(this,t))return;
        spent=true;getPersistentData().putBoolean("Id01Spent",true);target=null;
        Vec3 at=Id01Pose.nose(this);float power=Id01Config.BLAST.get().floatValue();
        // Interceptor is consumed before damage callbacks can re-enter its controller.
        if(operator!=null)Id01Sessions.stop(operator);ChunkTicketManager.releaseDrone(this);
        DroneExplosionVfx.detonate(l,at,power);
        blastBlocked=false;
        try(var scope=DroneExplosionVfx.explosionScope(l,this,at.x,at.y,at.z,power,true)){
            l.explode(this,at.x,at.y,at.z,power,AddonConfig.enableDroneCrashExplosionDamage()?Level.ExplosionInteraction.TNT:Level.ExplosionInteraction.NONE);
        }
        if(!blastBlocked&&!t.isRemoved()){
            var vehicle=(com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)t;
            // Damage can make SBW report isAlive=false before native wreck cleanup. The target
            // was validated before this very blast; a dead target must still be removed once.
            boolean allowed=vehicle.getHealth()<=0||vehicle.isWreck();
            if(!allowed&&Id01Radar.hostile(this,t)){
                float healthBefore=vehicle.getHealth();
                boolean reported=t.hurt(damageSources().explosion(this,l.getServer().getPlayerList().getPlayer(owner)),Math.max(Id01Config.IMPACT.get().floatValue(),vehicle.getMaxHealth()*4));
                // SBW applies onHurt, then returns Entity.hurt (false). Observe actual permitted
                // damage as well; never delete an immune/protected target whose health did not change.
                allowed=reported||vehicle.getHealth()<healthBefore;
            }
            if(allowed){
                MissionManager.destroyed(t);
                SbwCompat.disconnectControllersForDrone(l,t);
                FiberOpticLinkSystem.handleDroneCrashRecovery(l,t);
                // Native KILLED removal drops native inventory once, without triggering a second kamikaze blast.
                t.remove(RemovalReason.KILLED);
            }
        }
        discard();
    }
    /** Non-contact failure is deliberately inert; cleanup never calls the native kamikaze destroy path. */
    @Override public void destroy(){if(level().isClientSide())return;Sk01Deaths.destroyed(this);target=null;mode(Mode.DISABLED);buttons=0;if(operator!=null)Id01Sessions.stop(operator);ChunkTicketManager.releaseDrone(this);}
    @Override public void hitEntityCrash(Player player,Entity entity){}
    @Override public InteractionResult interact(Player p,InteractionHand hand){
        ItemStack stack=p.getItemInHand(hand);
        if(SbwCompat.isMonitor(stack)){
            if(p instanceof ServerPlayer sp&&permitted(sp)){if(owner==null)assign(sp);VatagMonitorTag.link(stack,getUUID(),blockPosition(),level().dimension().location().toString());Id01Sessions.mark(stack);sp.displayClientMessage(Component.literal("ID-01 linked. Observation only; R selects and confirms a hostile target."),true);}
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if(p instanceof ServerPlayer sp&&!permitted(sp))return InteractionResult.FAIL;
        if(p.isShiftKeyDown()&&stack.is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR)){
            if(mode()!=Mode.STANDBY&&mode()!=Mode.DISABLED){if(!level().isClientSide())p.displayClientMessage(Component.literal("ID-01: land before recovery."),true);return InteractionResult.FAIL;}
            if(!level().isClientSide()){
                if(operator!=null)Id01Sessions.stop(operator);
                CompoundTag n=new CompoundTag();saveWithoutId(n);n.remove("UUID");n.remove("Pos");n.remove("Motion");n.remove("Rotation");
                ItemStack item=new ItemStack(AddonItems.ID01.get());var itemTag=AddonStackTags.getOrCreate(item);itemTag.put("Id01Saved",n);AddonStackTags.save(item,itemTag);
                net.neoforged.neoforge.items.ItemHandlerHelper.giveItemToPlayer(p,item);discard();
            }return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return InteractionResult.PASS;
    }
    @Override protected AABB makeBoundingBox(){return geometryReady?Id01Geometry.bounds(this):super.makeBoundingBox();}
    @Override public void move(MoverType type,Vec3 requested){
        if(!geometryReady){super.move(type,requested);return;}
        Vec3 allowed=Id01Geometry.collide(this,requested);boolean prior=noPhysics;noPhysics=true;try{super.move(type,allowed);}finally{noPhysics=prior;}
        horizontalCollision=Math.abs(allowed.x-requested.x)>1e-7||Math.abs(allowed.z-requested.z)>1e-7;verticalCollision=Math.abs(allowed.y-requested.y)>1e-7;setOnGround(verticalCollision&&requested.y<0);
        if(verticalCollision)setDeltaMovement(getDeltaMovement().x,0,getDeltaMovement().z);
    }
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount){
        var direct=source.getDirectEntity();
        if(direct instanceof net.minecraft.world.entity.projectile.Projectile shot&&!source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)){Vec3 end=shot.position().add(shot.getDeltaMovement());Vec3 start=new Vec3(shot.xo,shot.yo,shot.zo);if(!Id01Geometry.ray(this,start,end))return false;}
        else if(source.getEntity() instanceof Player attacker&&direct==attacker&&!source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)){Vec3 start=attacker.getEyePosition();if(!Id01Geometry.ray(this,start,start.add(attacker.getLookAngle().scale(6))))return false;}
        return super.hurt(source,amount);
    }
    @Override public ItemStack getPickResult(){return new ItemStack(AddonItems.ID01.get());}
    @Override public AABB getBoundingBoxForCulling(){return getBoundingBox().inflate(2);}
    private boolean suppressRemovalDrops;
    @Override public net.minecraft.world.entity.item.ItemEntity spawnAtLocation(ItemStack stack,float offset){return suppressRemovalDrops?null:super.spawnAtLocation(stack,offset);}
    @Override public void remove(RemovalReason reason){if(isRemoved())return;if(!level().isClientSide()){if(operator!=null)Id01Sessions.stop(operator);ChunkTicketManager.releaseDrone(this);}suppressRemovalDrops=!reason.shouldDestroy();try{super.remove(reason);}finally{suppressRemovalDrops=false;}}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("Id01Owner",owner);if(home!=null){n.putDouble("Id01HomeX",home.x);n.putDouble("Id01HomeY",home.y);n.putDouble("Id01HomeZ",home.z);}n.putInt("Id01Mode",mode().ordinal());n.putInt("Id01FlightTicks",flightTicks);n.putFloat("Id01Tilt",entityData.get(TILT));n.putBoolean("Id01Spent",spent);if(target!=null)n.putUUID("Id01Target",target);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("Id01Owner")?n.getUUID("Id01Owner"):null;home=n.contains("Id01HomeX")?new Vec3(n.getDouble("Id01HomeX"),n.getDouble("Id01HomeY"),n.getDouble("Id01HomeZ")):null;if(home!=null&&!MissionMath.finite(home))home=null;spent=n.getBoolean("Id01Spent");target=null;operator=null;buttons=0;flightTicks=n.getInt("Id01FlightTicks");int saved=n.getInt("Id01Mode");mode(saved==0&&!spent?Mode.STANDBY:Mode.DISABLED);entityData.set(TILT,Mth.clamp(n.getFloat("Id01Tilt"),0,90));oldTilt=entityData.get(TILT);oldPosePitch=0;getPersistentData().remove(MissionManager.NO_TICKETS);}
}
