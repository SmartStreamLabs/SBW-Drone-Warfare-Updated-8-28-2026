package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.data.gun.GunData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.*;
import net.minecraft.world.phys.*;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import java.util.*;

/** Native manned SBW tracked vehicle: driver at index 0, gunner at index 1. */
public class Adv01Entity extends VatagEntity {
    private static final EntityDataAccessor<Boolean> AUTO=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RADAR=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LIGHTS=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<CompoundTag> CONTACTS=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<String> TARGET=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> STATUS=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Long> SHOT=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> HEAT=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HATCH=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SERVICE=SynchedEntityData.defineId(Adv01Entity.class,EntityDataSerializers.BOOLEAN);
    private UUID owner,lastShooter;private String ownerName="",overrides="";
    private final ArrayList<UUID> candidates=new ArrayList<>();
    private long nextShot,lastShot=-100,lastAction=-100;private int hatchTicks,serviceTicks;
    private boolean retrieving;private float hatch,oldHatch,service,oldService;
    private Vec3 visualLast;private float visualYaw;private double left,right,oldLeft,oldRight;

    public Adv01Entity(EntityType<? extends Adv01Entity> type,Level level){super(type,level);setTurretXRot(-35);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){
        super.defineSynchedData(b);b.define(AUTO,false);b.define(RADAR,true);b.define(LIGHTS,false);
        b.define(CONTACTS,new CompoundTag());b.define(TARGET,"");b.define(STATUS,"adv01.ready");
        b.define(SHOT,-100L);b.define(HEAT,0);b.define(HATCH,false);b.define(SERVICE,false);
    }
    @Override public boolean permitted(ServerPlayer p){return canConfigure(p);}
    @Override public boolean takeControl(ServerPlayer p){return getPassengers().isEmpty()&&super.takeControl(p);}
    @Override protected boolean nativeCrewControls(){return !remotelyControlled();}
    @Override protected boolean remoteAimAllowed(){return !automatic();}
    @Override protected boolean remoteFireAllowed(){return !automatic()&&ready();}
    @Override protected org.joml.Matrix4d remoteAimFrame(){return getVehicleTransform(1);}
    @Override protected float minimumAimPitch(){return -70;}
    @Override protected float maximumAimPitch(){return 0;}
    @Override public Vec3 camera(boolean aim,float partial){return camera(aim?1:0,partial);}
    public UUID owner(){return owner;}public String ownerName(){return ownerName;}
    public boolean automatic(){return entityData.get(AUTO);}public boolean radar(){return entityData.get(RADAR);}
    public boolean lights(){return entityData.get(LIGHTS)&&getEnergy()>0&&!isWreck();}
    public int temperature(){return entityData.get(HEAT);}public String status(){return entityData.get(STATUS);}
    public CompoundTag contacts(){return entityData.get(CONTACTS);}public String selected(){return entityData.get(TARGET);}
    public float hatch(float p){return Mth.lerp(p,oldHatch,hatch);}public float service(float p){return Mth.lerp(p,oldService,service);}
    public long shotTime(){return entityData.get(SHOT);}
    public double trackTravel(boolean side,float p){return side?oldLeft+(left-oldLeft)*p:oldRight+(right-oldRight)*p;}
    public boolean canConfigure(ServerPlayer p){return owner==null||owner.equals(p.getUUID())||p.hasPermissions(2);}
    public LivingEntity gunner(){if(operator()!=null)return operator();var e=getNthEntity(1);return e instanceof LivingEntity l?l:null;}
    public boolean recentShotBy(Entity e){return e!=null&&level().getGameTime()-lastShot<=6&&(e==this||e.getUUID().equals(lastShooter));}
    private void status(String key){if(!status().equals(key))entityData.set(STATUS,key);}
    public void control(ServerPlayer p,int action){
        if(p.getVehicle()!=this&&!VatagSessions.controlling(p,this)||!p.isAlive()||isWreck()||level().getGameTime()-lastAction<4)return;
        if(action==0){if(!canConfigure(p)){p.displayClientMessage(Component.translatable("adv01.owner_only"),true);return;}entityData.set(AUTO,!automatic());}
        else if(action==1){if(getSeatIndex(p)!=1&&!canConfigure(p))return;entityData.set(RADAR,!radar());if(!radar()){candidates.clear();entityData.set(CONTACTS,new CompoundTag());entityData.set(TARGET,"");}}
        else if(action==2){if(getSeatIndex(p)!=1&&!canConfigure(p))return;cycleTarget();}
        else if(action==3){if(!canConfigure(p))return;entityData.set(LIGHTS,!entityData.get(LIGHTS));}
        else return;
        lastAction=level().getGameTime();p.displayClientMessage(Component.translatable(automatic()?"adv01.auto":"adv01.manual"),true);
    }
    private void cycleTarget(){
        if(candidates.isEmpty()){entityData.set(TARGET,"");return;}
        int at=-1;for(int i=0;i<candidates.size();i++)if(candidates.get(i).toString().equals(selected()))at=i;
        entityData.set(TARGET,candidates.get((at+1)%candidates.size()).toString());
    }
    private Entity selectedEntity(){
        if(!(level() instanceof ServerLevel l)||selected().isEmpty())return null;
        try{return l.getEntity(UUID.fromString(selected()));}catch(IllegalArgumentException invalid){return null;}
    }
    private boolean valid(Entity e,double range){return Adv01Targets.drone(e)&&e.level()==level()&&distanceToSqr(e)<=range*range;}
    private void scan(){
        var l=(ServerLevel)level();double range=Adv01Config.RADAR_RANGE.get();
        var found=l.getEntities(this,new AABB(position(),position()).inflate(range),e->valid(e,range));
        found.sort(Comparator.comparingDouble(this::distanceToSqr));candidates.clear();ListTag entries=new ListTag();
        Vec3 origin=camera(1,1);
        for(var e:found){
            boolean clear=Adv01Targets.clear(l,this,origin,e.getBoundingBox().getCenter());
            if(Adv01Config.RADAR_LOS.get()&&!clear)continue;
            candidates.add(e.getUUID());var n=new CompoundTag();n.putInt("Id",e.getId());n.putUUID("UUID",e.getUUID());
            n.putString("Name",e.getName().getString());n.putDouble("X",e.getX());n.putDouble("Y",e.getBoundingBox().getCenter().y);n.putDouble("Z",e.getZ());
            n.putDouble("Distance",distanceTo(e));n.putDouble("Height",e.getY()-getY());n.putInt("Relation",Adv01Targets.relation(l,owner,ownerName,e));n.putBoolean("Visible",clear);entries.add(n);
            if(candidates.size()>=32)break;
        }
        var n=new CompoundTag();n.put("Entries",entries);n.putLong("Tick",level().getGameTime());entityData.set(CONTACTS,n);
        if(selectedEntity()==null||!candidates.contains(selectedEntity().getUUID()))entityData.set(TARGET,"");
    }
    private Entity automaticTarget(){
        var l=(ServerLevel)level();Entity selected=selectedEntity();double range=Math.min(Adv01Config.FIRE_RANGE.get(),Adv01Config.RADAR_RANGE.get());
        if(valid(selected,range)&&Adv01Targets.relation(l,owner,ownerName,selected)==Adv01Targets.HOSTILE)return selected;
        for(var id:candidates){var e=l.getEntity(id);if(valid(e,range)&&Adv01Targets.relation(l,owner,ownerName,e)==Adv01Targets.HOSTILE){entityData.set(TARGET,id.toString());return e;}}
        entityData.set(TARGET,"");return null;
    }
    private Vec3 aimPoint(Entity target){
        double ticks=Math.min(16,getShootPos("Cannon",1).distanceTo(target.getBoundingBox().getCenter())/18);
        return target.getBoundingBox().getCenter().add(target.getDeltaMovement().scale(ticks));
    }
    @Override public void adjustTurretAngle(){
        // Native SBW calls this only with an occupied gunner seat. Automatic aiming runs once
        // from our server baseTick instead, including when the vehicle has no gunner.
        if(automatic()||isWreck())return;
        var actor=gunner();if(actor==null)return;
        // SBW's default servo subtracts yaw error for its own -yaw turret matrix.
        // ADV's supplied rig and physical barrel both use +yaw. Convert the native
        // player's world look into that frame once, instead of feeding the old servo.
        Vec3 origin=vec(getBarrelTransform(1).transformPosition(new org.joml.Vector3d()));
        Vec3 direction=getBoundingBox().getCenter().add(actor.getViewVector(1).scale(512)).subtract(origin).normalize();
        float yaw=(float)Math.toDegrees(-Math.atan2(direction.x,direction.z)),pitch=(float)Math.toDegrees(-Math.asin(Mth.clamp(direction.y,-1,1)));
        var local=VatagAim.local(getVehicleTransform(1),yaw,pitch);
        float damage=getEntityData().get(com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.TURRET_DAMAGED)?.2f:1;
        setTurretYRot(Mth.approachDegrees(getTurretYRot(),Mth.clamp(local.x,-getTurretMaxYaw(),-getTurretMinYaw()),getTurretTurnYSpeed()*damage));
        setTurretXRot(Mth.approach(getTurretXRot(),Mth.clamp(local.y,-getTurretMaxPitch(),-getTurretMinPitch()),getTurretTurnXSpeed()*damage));
    }
    private void trackAutomatically(Entity target){
        var direction=aimPoint(target).subtract(vec(getBarrelTransform(1).transformPosition(new org.joml.Vector3d()))).normalize();
        float yaw=(float)Math.toDegrees(-Math.atan2(direction.x,direction.z));
        float pitch=(float)Math.toDegrees(-Math.asin(Mth.clamp(direction.y,-1,1)));
        var local=VatagAim.local(getVehicleTransform(1),yaw,pitch);
        setTurretYRot(Mth.approachDegrees(getTurretYRot(),local.x,4));
        setTurretXRot(Mth.approach(getTurretXRot(),Mth.clamp(local.y,-70,0),3));
    }
    private boolean ready(){
        var g=getGunData("Cannon");
        if(isWreck()||!isAlive()||getEnergy()<=0){status("adv01.no_energy");return false;}
        if(serviceTicks>0){status("adv01.service");return false;}
        if(level() instanceof ServerLevel l&&Adv01Shell.activeCount(l)>=Adv01Config.MAX_SHELLS.get()){status("adv01.capacity");return false;}
        if(g==null){status("adv01.no_ammo");return false;}
        if(g.reloading()){status("adv01.reloading");return false;}
        if(g.ammo.get()<=0){status("adv01.no_ammo");return false;}
        if(g.overHeat.get()||g.heat.get()>=100){status("adv01.overheated");return false;}
        if(level().getGameTime()<nextShot)return false;
        var pivot=vec(getBarrelTransform(1).transformPosition(new org.joml.Vector3d()));
        if(level() instanceof ServerLevel l&&!Adv01Targets.clear(l,this,pivot,getShootPos("Cannon",1))){status("adv01.blocked");return false;}
        return true;
    }
    @Override public boolean canShoot(LivingEntity actor){return !automatic()&&actor!=null&&actor==gunner()&&ready()&&(actor==operator()||super.canShoot(actor));}
    @Override public void afterShoot(GunData data,Vec3 direction){
        super.afterShoot(data,direction);nextShot=level().getGameTime()+Math.max(2,(int)Math.ceil(1200d/Adv01Config.RPM.get()));
        lastShot=level().getGameTime();var actor=automatic()&&level() instanceof ServerLevel l?l.getServer().getPlayerList().getPlayer(owner):gunner();
        lastShooter=actor==null?null:actor.getUUID();if(!level().isClientSide){entityData.set(SHOT,lastShot);consumeEnergy(100);status("adv01.firing");}
    }
    @Override public void baseTick(){
        super.baseTick();oldHatch=hatch;oldService=service;hatch=Mth.approach(hatch,entityData.get(HATCH)?1:0,.08f);service=Mth.approach(service,entityData.get(SERVICE)?1:0,.05f);
        if(level().isClientSide){oldLeft=left;oldRight=right;if(visualLast!=null){var d=position().subtract(visualLast);if(d.lengthSqr()<16){var f=getVehicleTransform(1).transformDirection(new org.joml.Vector3d(0,0,1)).normalize();double along=d.dot(vec(f)),turn=Mth.wrapDegrees(getYRot()-visualYaw)*Mth.DEG_TO_RAD*1.3;left+=along+turn;right+=along-turn;}}visualLast=position();visualYaw=getYRot();return;}
        if(isWreck()||!isAlive()){entityData.set(AUTO,false);entityData.set(RADAR,false);entityData.set(CONTACTS,new CompoundTag());candidates.clear();return;}
        entityData.set(HATCH,hatchTicks-- >0);entityData.set(SERVICE,serviceTicks-- >0);
        var gun=getGunData("Cannon");
        if(gun!=null){if(overrides.isEmpty()||tickCount%40==0){String config=Adv01Config.gunOverrides();if(!config.equals(overrides)){gun.propertyOverrideString.set(config);overrides=config;}}if(tickCount%4==0)entityData.set(HEAT,(int)Math.round(gun.heat.get()));}
        if(getEnergy()<=0&&!candidates.isEmpty()){candidates.clear();entityData.set(CONTACTS,new CompoundTag());entityData.set(TARGET,"");}
        if(radar()&&getEnergy()>0&&Math.floorMod(level().getGameTime()+getId(),Adv01Config.SCAN_TICKS.get())==0){consumeEnergy(30);scan();}
        if(automatic()){
            if(!radar()){status("adv01.radar_off");return;}
            var l=(ServerLevel)level();var actor=owner==null?null:l.getServer().getPlayerList().getPlayer(owner);
            if(actor==null||!actor.isAlive()){status("adv01.owner_unavailable");return;}
            var target=automaticTarget();if(target==null){boolean distant=false;for(var id:candidates){var c=l.getEntity(id);if(valid(c,Adv01Config.RADAR_RANGE.get())&&Adv01Targets.relation(l,owner,ownerName,c)==Adv01Targets.HOSTILE){distant=true;break;}}status(distant?"adv01.out_of_range":"adv01.no_target");return;}
            trackAutomatically(target);
            if(!Adv01Targets.clear(l,this,getShootPos("Cannon",1),aimPoint(target))){status("adv01.blocked");return;}
            var direction=aimPoint(target).subtract(getShootPos("Cannon",1)).normalize();
            if(getShootVec("Cannon",1).normalize().dot(direction)<.999){status("adv01.tracking");return;}
            if(gun!=null&&gun.ammo.get()<=0&&!gun.reloading()&&gun.hasBackupAmmo(this))gun.startReload();
            if(ready()&&gun!=null&&gun.canShoot(this))Adv01Native.fire(this,actor);
        }else if(level().getGameTime()-lastShot>5&&ready())status(radar()?"adv01.ready":"adv01.radar_off");
    }
    static Vec3 vec(org.joml.Vector3d p){return new Vec3(p.x,p.y,p.z);}
    @Override public org.joml.Matrix4d getTurretTransform(float p){return Adv01Frames.turret(getVehicleTransform(p),Mth.rotLerp(p,getTurretYRotO(),getTurretYRot()));}
    @Override public org.joml.Matrix4d getBarrelTransform(float p){return Adv01Frames.gun(getTurretTransform(p),Mth.lerp(p,getTurretXRotO(),getTurretXRot()));}
    @Override public Vec3 getShootPos(String weapon,float p){return vec(Adv01Frames.muzzle(getBarrelTransform(p)));}
    @Override public Vec3 getShootVec(String weapon,float p){return vec(getBarrelTransform(p).transformDirection(new org.joml.Vector3d(0,0,1)).normalize());}
    @Override public Vec3 getShootPos(Entity actor,float p){return getShootPos("Cannon",p);}
    @Override public Vec3 getShootVec(Entity actor,float p){return getShootVec("Cannon",p);}
    public org.joml.Matrix4d cameraFrame(int seat,float p){return seat==1?Adv01Frames.sensor(getTurretTransform(p),Mth.lerp(p,getTurretXRotO(),getTurretXRot())):Adv01Frames.drive(getVehicleTransform(p));}
    public Vec3 camera(int seat,float p){return vec(cameraFrame(seat,p).transformPosition(new org.joml.Vector3d()));}
    @Override public Vec3 getCameraPos(Entity passenger,float p){return camera(getSeatIndex(passenger),p);}
    @Override public int getTurretControllerIndex(){return 1;}
    @Override public InteractionResult interact(Player p,InteractionHand hand){
        if(isRemoved()||retrieving)return InteractionResult.FAIL;
        if(!level().isClientSide&&owner==null){owner=p.getUUID();ownerName=p.getScoreboardName();}
        if(p.isShiftKeyDown()&&p.getItemInHand(hand).is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR)){
            if(level().isClientSide)return InteractionResult.SUCCESS;
            if(!(p instanceof ServerPlayer sp)||!canConfigure(sp)||!getPassengers().isEmpty()||getDeltaMovement().horizontalDistance()>.015){p.displayClientMessage(Component.translatable("adv01.pack_blocked"),true);return InteractionResult.FAIL;}
            entityData.set(AUTO,false);entityData.set(RADAR,false);retrieving=true;try{return super.interact(p,hand);}finally{retrieving=false;}
        }
        if(p.isShiftKeyDown()&&p.getItemInHand(hand).isEmpty()){
            if(p instanceof ServerPlayer sp&&canConfigure(sp)){serviceTicks=80;openMenu(p);}return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if(p.getItemInHand(hand).isEmpty()){if(remotelyControlled())return InteractionResult.FAIL;hatchTicks=50;}
        if(SbwCompat.isMonitor(p.getItemInHand(hand))||p.isShiftKeyDown())return super.interact(p,hand);
        return crewInteract(p,hand);
    }
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity p){
        hatchTicks=50;
        for(double x:new double[]{2.15,-2.15,2.8,-2.8})for(double z:new double[]{1.2,0,-2}){
            var point=vec(getVehicleTransform(1).transformPosition(new org.joml.Vector3d(x,.05,z)));
            for(int y=0;y<3;y++){var at=point.add(0,y,0);var box=p.getDimensions(Pose.STANDING).makeBoundingBox(at);if(level().hasChunkAt(net.minecraft.core.BlockPos.containing(at))&&level().noCollision(p,box)&&!level().noCollision(p,box.move(0,-.2,0)))return at;}
        }
        return super.dismount(p);
    }
    @Override public Vec3 getDismountLocationForIndex(LivingEntity p,int i){return getDismountLocationForPassenger(p);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("Adv01Owner",owner);n.putString("Adv01OwnerName",ownerName);n.putBoolean("Adv01Auto",automatic());n.putBoolean("Adv01Radar",radar());n.putBoolean("Adv01Lights",entityData.get(LIGHTS));}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("Adv01Owner")?n.getUUID("Adv01Owner"):null;ownerName=n.getString("Adv01OwnerName");entityData.set(AUTO,n.getBoolean("Adv01Auto"));entityData.set(RADAR,!n.contains("Adv01Radar")||n.getBoolean("Adv01Radar"));entityData.set(LIGHTS,n.getBoolean("Adv01Lights"));candidates.clear();entityData.set(CONTACTS,new CompoundTag());entityData.set(TARGET,"");overrides="";}
    @Override public AABB getBoundingBoxForCulling(){return new AABB(getX()-7,getY()-.5,getZ()-7,getX()+7,getY()+8,getZ()+7);}
}
