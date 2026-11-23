package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.*;

/** Native SBW wheel vehicle. Deployment does not replace driving, seats, damage or energy. */
public class Mj03Entity extends GeoVehicleEntity {
    public static final int DRIVE=0,DEPLOYING=1,DEPLOYED=2,PACKING=3,TRANSITION_TICKS=60,RANGE=12000;
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(Mj03Entity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> FROM=SynchedEntityData.defineId(Mj03Entity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> SINCE=SynchedEntityData.defineId(Mj03Entity.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> LIGHTS=SynchedEntityData.defineId(Mj03Entity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> AZIMUTH=SynchedEntityData.defineId(Mj03Entity.class,EntityDataSerializers.FLOAT);
    private boolean retrieving;
    private UUID owner;
    private final Set<String> trusted=new LinkedHashSet<>();
    public Mj03Entity(EntityType<? extends Mj03Entity> type,Level level){super(type,level);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(PHASE,DRIVE);entityData.define(FROM,0f);entityData.define(SINCE,0L);entityData.define(LIGHTS,false);entityData.define(AZIMUTH,0f);}
    public boolean lights(){return entityData.get(LIGHTS) && getEnergy()>0 && !isWreck();}
    public int phase(){return entityData.get(PHASE);}
    public float deployment(float partial){
        float from=entityData.get(FROM);if(phase()==DRIVE)return 0;if(phase()==DEPLOYED)return 1;
        float target=phase()==DEPLOYING?1:0;float elapsed=(level().getGameTime()+partial-entityData.get(SINCE))/TRANSITION_TICKS;
        return from+(target-from)*net.minecraft.util.Mth.clamp(elapsed,0,1);
    }
    public float azimuth(float partial){
        float elapsed=(float)(level().getGameTime()+partial-entityData.get(SINCE));
        if(phase()==DEPLOYED)return (float)Math.toRadians(net.minecraft.util.Mth.wrapDegrees((float)Math.toDegrees(entityData.get(AZIMUTH)+elapsed*.012f)));
        if(phase()==PACKING)return entityData.get(AZIMUTH)*(1-net.minecraft.util.Mth.clamp(elapsed/TRANSITION_TICKS,0,1));
        return 0;
    }
    private void transition(int state){float current=deployment(0),angle=azimuth(0);entityData.set(FROM,current);entityData.set(AZIMUTH,angle);entityData.set(SINCE,level().getGameTime());entityData.set(PHASE,state);}
    public boolean operational(){return phase()==DEPLOYED && onGround() && isAlive() && !isRemoved() && !isWreck() && getEnergy()>=Mj03Config.ENERGY_PER_TICK.get() && AddonConfig.enableDroneJammer();}
    public UUID owner(){return owner;}
    public Set<String> trusted(){return Collections.unmodifiableSet(trusted);}
    public void owner(UUID id){owner=id;}
    public boolean canConfigure(ServerPlayer p){return owner==null || owner.equals(p.getUUID()) || p.hasPermissions(2);}
    public boolean control(ServerPlayer p,int action,String name){
        if(level().isClientSide || !canConfigure(p) || p.level()!=level() || p.distanceToSqr(this)>100)return false;
        if(action==3){entityData.set(LIGHTS,!entityData.get(LIGHTS));return true;}
        if(action==0){
            if(phase()==DEPLOYING || phase()==DEPLOYED){transition(PACKING);return true;}
            if(phase()!=DRIVE || getDeltaMovement().horizontalDistance()>.015 || !onGround() || Math.abs(getXRot())>4 || Math.abs(getRoll())>4 || !clearance()){
                p.displayClientMessage(Component.translatable("mj03.cannot_deploy"),true);return false;
            }
            if(getEnergy()<Mj03Config.ENERGY_PER_TICK.get()*TRANSITION_TICKS){p.displayClientMessage(Component.translatable("mj03.no_energy"),true);return false;}
            transition(DEPLOYING);return true;
        }
        String value=AddonConfig.normalizeName(name==null?"":name.trim(),AddonConfig.blacklistMatchCaseInsensitive());
        if(value.isEmpty() || value.length()>32)return false;
        if(action==1 && trusted.size()<128)return trusted.add(value);
        return action==2 && trusted.remove(value);
    }
    private Vec3 local(double x,double y,double z){return position().add(new Vec3(x,y,z).yRot((float)Math.toRadians(-getYRot())));}
    public boolean clearance(){
        if(!onGround())return false;
        for(double x:new double[]{-1.9,1.9})for(double z:new double[]{-2.5,1.55}){
            BlockPos support=BlockPos.containing(local(x,-.08,z));
            if(!level().hasChunkAt(support) || !level().getBlockState(support).isFaceSturdy(level(),support,Direction.UP))return false;
            Vec3 at=local(x,.65,z),base=local(Math.copySign(1.15,x),.65,z);
            AABB swept=new AABB(Math.min(base.x,at.x)-.35,getY()+.05,Math.min(base.z,at.z)-.35,Math.max(base.x,at.x)+.35,getY()+1.8,Math.max(base.z,at.z)+.35);
            if(!level().noCollision(this,swept))return false;
        }
        // Raised rotating panel and its complete swept box; local geometry is otherwise inside the SBW hull.
        Vec3 top=local(0,4.45,-.5);AABB panel=new AABB(top.x-1.65,getY()+3.4,top.z-1.65,top.x+1.65,getY()+5.3,top.z+1.65);
        return level().hasChunksAt(BlockPos.containing(panel.minX,panel.minY,panel.minZ),BlockPos.containing(panel.maxX,panel.maxY,panel.maxZ)) && level().noCollision(this,panel);
    }
    @Override public InteractionResult interact(Player p,InteractionHand hand){
        if(isRemoved())return InteractionResult.FAIL;
        if(!level().isClientSide && owner==null && p instanceof ServerPlayer sp)owner=sp.getUUID();
        if(p.isShiftKeyDown() && p.getItemInHand(hand).is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR)){
            if(level().isClientSide)return InteractionResult.SUCCESS;
            if(isRemoved() || retrieving)return InteractionResult.FAIL;
            if(!(p instanceof ServerPlayer sp))return InteractionResult.FAIL;
            if(!canConfigure(sp)){sp.displayClientMessage(Component.translatable("mj03.not_owner"),true);return InteractionResult.FAIL;}
            if(!getPassengers().isEmpty() || isWreck()){sp.displayClientMessage(Component.translatable("mj03.retrieve_blocked"),true);return InteractionResult.FAIL;}
            if(phase()!=DRIVE){if(phase()!=PACKING)transition(PACKING);sp.displayClientMessage(Component.translatable("mj03.retrieve_packing"),true);return InteractionResult.SUCCESS;}
            if(getDeltaMovement().horizontalDistance()>.015){sp.displayClientMessage(Component.translatable("mj03.retrieve_stopped"),true);return InteractionResult.FAIL;}
            retrieving=true;
            try{var result=super.interact(p,hand);if(isRemoved())Mj03JammerRegistry.leave(this);return result;}
            finally{retrieving=false;}
        }
        if(p.isShiftKeyDown() && hand==InteractionHand.MAIN_HAND){
            if(p instanceof ServerPlayer sp){if(canConfigure(sp))AddonNetwork.openMj03(sp,new Mj03OpenMessage(getId(),List.copyOf(trusted)));else sp.displayClientMessage(Component.translatable("mj03.not_owner"),true);}
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.interact(p,hand);
    }
    @Override public void travel(){
        if(phase()!=DRIVE){setForwardInputDown(false);setBackInputDown(false);setLeftInputDown(false);setRightInputDown(false);setUpInputDown(true);setPower(0);setDeltaRot(0);setDeltaMovement(0,getDeltaMovement().y,0);}
        super.travel();
    }
    @Override public void baseTick(){
        super.baseTick();
        if(level().isClientSide)return;
        if(phase()!=DRIVE && tickCount%10==0 && (!clearance() || getEnergy()<Mj03Config.ENERGY_PER_TICK.get()) && phase()!=PACKING)transition(PACKING);
        if((phase()==DEPLOYING || phase()==PACKING) && level().getGameTime()-entityData.get(SINCE)>=TRANSITION_TICKS)transition(phase()==DEPLOYING?DEPLOYED:DRIVE);
        if(operational())consumeEnergy(Mj03Config.ENERGY_PER_TICK.get());
        if(decoyInputDown())horn();
    }
    @Override public void addAdditionalSaveData(CompoundTag t){
        super.addAdditionalSaveData(t);if(owner!=null)t.putUUID("MJ03Owner",owner);
        t.putBoolean("MJ03Lights",entityData.get(LIGHTS));
        t.putFloat("MJ03Azimuth",azimuth(0));
        t.putInt("MJ03Phase",phase());t.putFloat("MJ03Progress",deployment(0));t.putString("MJ03Trusted",String.join("\n",trusted));
    }
    @Override public void readAdditionalSaveData(CompoundTag t){
        super.readAdditionalSaveData(t);owner=t.hasUUID("MJ03Owner")?t.getUUID("MJ03Owner"):null;
        entityData.set(LIGHTS,t.getBoolean("MJ03Lights"));
        entityData.set(AZIMUTH,t.getFloat("MJ03Azimuth"));
        int state=net.minecraft.util.Mth.clamp(t.getInt("MJ03Phase"),DRIVE,PACKING);entityData.set(PHASE,state);entityData.set(FROM,net.minecraft.util.Mth.clamp(t.getFloat("MJ03Progress"),0,1));entityData.set(SINCE,level().getGameTime());
        trusted.clear();for(String s:t.getString("MJ03Trusted").split("\\R")){String n=AddonConfig.normalizeName(s,AddonConfig.blacklistMatchCaseInsensitive());if(!n.isEmpty() && n.length()<=32 && trusted.size()<128)trusted.add(n);}
    }
    @Override public AABB getBoundingBoxForCulling(){return getBoundingBox().inflate(2,5,4);}
}
