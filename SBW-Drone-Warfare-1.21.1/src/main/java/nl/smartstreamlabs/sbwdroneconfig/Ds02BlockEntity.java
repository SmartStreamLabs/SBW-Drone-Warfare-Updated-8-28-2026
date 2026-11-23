package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashMap;
import java.util.UUID;

/** Uses the compact siren's exact owner/whitelist/detection rules, with its own alarm lifecycle. */
public class Ds02BlockEntity extends DroneDetectionSirenBlockEntity {
    private boolean enabled=true;
    private int remaining;
    private long alarmStart,doorStart;
    private float doorFrom;
    private boolean doorOpen;
    private double audibleRange=96;
    private final HashMap<UUID,Long> viewers=new HashMap<>();
    public Ds02BlockEntity(BlockPos p,BlockState s){super(AddonBlockEntities.DS02.get(),p,s);}
    public boolean enabled(){return enabled;}
    public boolean alarming(){return enabled && getBlockState().getValue(Ds02Block.ACTIVE);}
    public long alarmStart(){return alarmStart;}
    public double audibleRange(){return audibleRange;}
    public float door(float partial){
        double elapsed=level==null?0:Math.max(0,level.getGameTime()+partial-doorStart);
        float t=(float)Math.min(1,elapsed/15.0);return doorFrom+((doorOpen?1:0)-doorFrom)*t;
    }
    public void control(ServerPlayer p,int action){
        if(level==null || !canConfigure(p) || p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))>64)return;
        if(action==0)viewers.put(p.getUUID(),level.getGameTime()+60);
        else if(action==1)viewers.remove(p.getUUID());
        else if(action==2){enabled=!enabled;if(!enabled){remaining=0;alarm(false);}changed();}
        updateDoor();
    }
    private void updateDoor(){
        boolean next=!viewers.isEmpty();if(next==doorOpen)return;
        doorFrom=door(0);doorStart=level.getGameTime();doorOpen=next;changed();
    }
    private void changed(){setChanged();if(level!=null && !level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    private void alarm(boolean active){
        if(level==null || level.isClientSide || alarming()==active && getBlockState().getValue(Ds02Block.ACTIVE)==active)return;
        alarmStart=active?level.getGameTime():0;
        level.setBlock(worldPosition,getBlockState().setValue(Ds02Block.ACTIVE,active),3);
        level.updateNeighborsAt(worldPosition,getBlockState().getBlock());changed();
    }
    @Override public void refreshDetectionState(ServerLevel l){
        boolean detected=enabled && AddonConfig.enableDroneSiren() && hasDetectedDrone(l,worldPosition);
        remaining=detected?Math.max(1,AddonConfig.droneSirenCooldownTicks()):0;alarm(detected);
    }
    public static void serverTick(Level level,BlockPos p,BlockState s,Ds02BlockEntity be){
        if(!(level instanceof ServerLevel l))return;
        long now=l.getGameTime();
        if(be.audibleRange!=Ds02Config.RANGE.get()){be.audibleRange=Ds02Config.RANGE.get();be.changed();}
        if(!be.enabled || !AddonConfig.enableDroneSiren()){be.remaining=0;be.alarm(false);}
        else {
            if(Math.floorMod(now+p.asLong(),10)==0 && be.hasDetectedDrone(l,p))be.remaining=Math.max(1,AddonConfig.droneSirenCooldownTicks());
            if(be.remaining>0){be.remaining--;be.alarm(true);}else be.alarm(false);
        }
        if(now%10==0){
            be.viewers.entrySet().removeIf(e->{var v=l.getServer().getPlayerList().getPlayer(e.getKey());return e.getValue()<now || v==null || v.level()!=l || !be.canConfigure(v) || v.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(p))>64;});
            be.updateDoor();
        }
    }
    // SAVE_START
    @Override protected void saveAdditional(CompoundTag t,net.minecraft.core.HolderLookup.Provider registries){
        super.saveAdditional(t,registries);t.putBoolean("DS02Enabled",enabled);t.putInt("DS02Remaining",remaining);t.putLong("DS02AlarmStart",alarmStart);
    }
    @Override protected void loadAdditional(CompoundTag t,net.minecraft.core.HolderLookup.Provider registries){
        super.loadAdditional(t,registries);enabled=!t.contains("DS02Enabled") || t.getBoolean("DS02Enabled");remaining=t.getInt("DS02Remaining");alarmStart=t.getLong("DS02AlarmStart");
        if(t.contains("DS02DoorStart")){doorStart=t.getLong("DS02DoorStart");doorOpen=t.getBoolean("DS02DoorOpen");doorFrom=t.getFloat("DS02DoorFrom");audibleRange=t.getDouble("DS02Range");}
    }
    @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){
        CompoundTag t=super.getUpdateTag(registries);t.putBoolean("DS02Enabled",enabled);t.putInt("DS02Remaining",remaining);t.putLong("DS02AlarmStart",alarmStart);
        t.putLong("DS02DoorStart",doorStart);t.putBoolean("DS02DoorOpen",doorOpen);t.putFloat("DS02DoorFrom",doorFrom);t.putDouble("DS02Range",audibleRange);return t;
    }
    // SAVE_END
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(worldPosition).inflate(1,0,1).expandTowards(0,4,0);}
}
