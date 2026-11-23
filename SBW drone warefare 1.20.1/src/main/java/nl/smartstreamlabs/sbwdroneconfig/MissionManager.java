package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Server-thread mission authority. The journal never creates replacement entities on recovery. */
public final class MissionManager {
    public static final String TAG="sbwdroneconfigMission", NO_TICKETS="sbwdroneconfigMissionReleased";
    private static final Map<MinecraftServer,MissionManager> SERVERS=new WeakHashMap<>();
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    public static final class Flight {
        public UUID id,owner;
        public String dimension,kind,state="Departing",reason="";
        public double tx,ty,tz,cruise,x,y,z,previousX,previousY,previousZ,originX,originY,originZ,vx,vy,vz,carryX,carryY,carryZ;
        public long platform;
        public int age,flightTicks,waitTicks,stalled;
        public boolean spawned;
        public double best=Double.MAX_VALUE;
        public transient Entity entity;
        public boolean hold;
        public transient boolean detonating;
        public transient float throttle;
        public transient double climb,avoidY;
        public transient boolean brake;
        public boolean active(){return state.equals("Departing")||state.equals("En route")||state.equals("Waiting for chunks");}
        public Vec3 target(){return new Vec3(tx,ty,tz);}
        public Vec3 position(){return new Vec3(x,y,z);}
    }
    private final MinecraftServer server;
    private final Path path;
    private final LinkedHashMap<UUID,Flight> flights=new LinkedHashMap<>();
    public final MissionTickets tickets=new MissionTickets();
    private int clock;
    private boolean storageFailed;
    private final MissionJournal journal;
    private MissionManager(MinecraftServer server){
        this.server=server;path=server.getWorldPath(LevelResource.ROOT).resolve("data/sbwdrone_missions.json");
        journal=new MissionJournal(path);
        if(Files.exists(path))try{
            var records=JSON.fromJson(Files.readString(path),Flight[].class);
            if(records==null||records.length>4096)throw new IOException("Invalid mission ledger");
            for(var f:records){
                if(f.id==null||f.owner==null||f.dimension==null||ResourceLocation.tryParse(f.dimension)==null||f.kind==null||!Set.of("lucas","geranium","glide","flamingo").contains(f.kind)||f.state==null||f.reason==null||!Set.of("Departing","En route","Waiting for chunks","Reached","Ended","Failed").contains(f.state)||!MissionMath.finite(f.position())||!MissionMath.finite(f.target())||!Double.isFinite(f.cruise)||f.age<0)throw new IOException("Invalid mission record");
                if(f.active()&&!f.spawned){f.state="Ended";f.reason="Preparation interrupted by server restart";}
                flights.put(f.id,f);
            }
        }catch(IOException|com.google.gson.JsonParseException|IllegalArgumentException e){storageFailed=true;SbwDroneRangeConfig.LOGGER.error("Mission ledger could not be read; coordinate launches disabled to prevent duplication",e);}
    }
    public static MissionManager get(MinecraftServer s){return SERVERS.computeIfAbsent(s,MissionManager::new);}
    public static void shutdown(MinecraftServer s){var m=SERVERS.remove(s);if(m!=null){try{m.checkpoint();}finally{m.journal.close();m.tickets.clear();}}}
    public static void stopped(MinecraftServer s){var m=SERVERS.remove(s);if(m!=null){m.journal.close();m.tickets.clear();}}
    public Collection<Flight> flights(){return Collections.unmodifiableCollection(flights.values());}
    public Flight find(UUID id){return id==null?null:flights.get(id);}
    public static boolean tagged(Entity e){return e.getPersistentData().hasUUID(TAG);}
    public static Flight flight(Entity e){return e.level() instanceof ServerLevel l && tagged(e)?get(l.getServer()).find(e.getPersistentData().getUUID(TAG)):null;}
    public static boolean owns(Entity e){var f=flight(e);return f!=null&&f.active();}
    public static boolean powered(Entity e){return owns(e)&&!(e instanceof GlideBombEntity && !(e instanceof Geranium2Entity));}
    public static boolean suppressDestroy(Entity e){var f=flight(e);return f!=null&&f.detonating;}
    public static boolean wasSpawned(ServerLevel l,UUID id){var f=get(l.getServer()).find(id);return f!=null&&f.spawned;}

    public static String validate(MissionPlatform p,String xs,String ys,String zs,String cs){
        if(!(p.block().getLevel() instanceof ServerLevel l))return "Server unavailable";
        if(p.drone().isEmpty())return "Load a suitable vehicle first";
        if(p.missionKind().equals("flamingo")&&MissionConfig.RADIUS.get()!=1)return "FL-01 requires mission chunkRadius = 1 for its full airframe";
        try {
            double x=Double.parseDouble(xs.trim()),y=Double.parseDouble(ys.trim()),z=Double.parseDouble(zs.trim());
            double c=cs.isBlank()?Math.max(p.missionMount().y+20,y):Double.parseDouble(cs.trim());
            var target=new Vec3(x,y,z);
            if(!MissionMath.finite(target)||!Double.isFinite(c))return "Coordinates must be finite numbers";
            if(Math.abs(x)>29999984||Math.abs(z)>29999984||!l.getWorldBorder().isWithinBounds(BlockPos.containing(target)))return "Target outside world border";
            if(y<l.getMinBuildHeight()+1||y>=l.getMaxBuildHeight()-2||c<l.getMinBuildHeight()+1||c>=l.getMaxBuildHeight()-2)return "Target/cruise outside build height";
            if(p.missionMount().distanceTo(target)>MissionConfig.DISTANCE.get())return String.format(java.util.Locale.ROOT,"Target is %.0f blocks away; server mission limit is %.0f blocks",p.missionMount().distanceTo(target),MissionConfig.DISTANCE.get());
            if(p.missionMount().distanceToSqr(target)<16)return "Target must be at least 4 blocks from the rail";
            if(p.missionKind().equals("glide")){
                if(!cs.isBlank())return "Unpowered Glide Bomb has no cruise altitude";
                if(!MissionMath.glideReachable(p.missionMount(),GlidePlatformLayout.forward(p.facing()),target))return "Unreachable with existing unpowered Glide Bomb trajectory";
            }
            return "";
        }catch(NumberFormatException e){return "Enter complete numeric X, Y and Z coordinates";}
    }
    public boolean reserve(MissionPlatform p,net.minecraft.world.entity.player.Player owner,UUID id){
        var d=p.mission();
        String error=validate(p,d.x,d.y,d.z,d.cruise);
        if(storageFailed)error="Mission journal unavailable";
        if(flights.values().stream().filter(Flight::active).count()>=MissionConfig.MAX_MISSIONS.get())error="Server mission limit reached";
        var old=find(d.last);if(old!=null&&old.active())error="This platform already has an active mission";
        if(!error.isEmpty()||!d.valid){d.message=error.isEmpty()?"Apply valid coordinates first":error;p.missionSync();return false;}
        var f=new Flight();f.id=id;f.owner=owner.getUUID();f.dimension=p.block().getLevel().dimension().location().toString();f.kind=p.missionKind();f.platform=p.block().getBlockPos().asLong();
        f.tx=d.target.x;f.ty=d.target.y;f.tz=d.target.z;f.cruise=d.cruise.isBlank()?Math.max(p.missionMount().y+20,f.ty):d.altitude;
        var at=p.missionMount();f.x=f.previousX=f.originX=at.x;f.y=f.previousY=f.originY=at.y;f.z=f.previousZ=f.originZ=at.z;
        flights.put(id,f);d.last=id;
        if(!checkpoint()){flights.remove(id);d.message="Could not save mission journal";p.missionSync();return false;}
        tickets.follow(id,(ServerLevel)p.block().getLevel(),at,Vec3.ZERO);p.missionRefresh();return true;
    }
    public boolean prepared(UUID id){var f=find(id);if(f==null||!f.active())return false;boolean ready=tickets.ready(id);if(!ready)f.state="Waiting for chunks";return ready;}
    /** Journal commit precedes adding the entity, preventing stale platform copies after recovery. */
    public boolean bind(UUID id,Entity e){var f=find(id);if(f==null||!f.active())return false;f.spawned=true;f.entity=e;e.getPersistentData().putUUID(TAG,id);if(!checkpoint()){f.spawned=false;f.entity=null;return false;}return true;}
    public static void spawnRejected(Entity e){var f=flight(e);if(f!=null){f.spawned=false;get(((ServerLevel)e.level()).getServer()).end(f,"Failed","Entity spawn rejected; vehicle retained in platform");}}
    public void unload(ServerLevel l){for(var f:new ArrayList<>(flights.values()))if(f.active()&&f.dimension.equals(l.dimension().location().toString()))end(f,"Failed","Dimension unloaded");}
    public boolean cancel(UUID id,net.minecraft.world.entity.player.Player player){var f=find(id);if(f==null||!f.active()||!f.owner.equals(player.getUUID())&&!player.hasPermissions(2))return false;end(f,"Ended","Cancelled by operator");return true;}
    public void abortPreparation(UUID id){var f=find(id);if(f!=null&&f.active()&&!f.spawned)end(f,"Ended","Launch preparation interrupted");}
    private void end(Flight f,String state,String reason){
        f.state=state;f.reason=reason;tickets.release(f.id);
        if(f.entity!=null && !state.equals("Reached")){f.entity.getPersistentData().remove(TAG);f.entity.getPersistentData().putBoolean(NO_TICKETS,true);}
        checkpoint();
    }
    public static void destroyed(Entity e){var f=flight(e);if(f!=null&&f.active())get(((ServerLevel)e.level()).getServer()).end(f,"Failed","Vehicle destroyed or intercepted");}
    public void removed(Entity e){var f=flight(e);if(f==null||!f.active())return;if(e.getRemovalReason()!=null&&e.getRemovalReason().shouldDestroy())end(f,"Failed","Vehicle removed");else {f.entity=null;checkpoint();}}
    public void joined(Entity e){
        if(!tagged(e))return;var f=flight(e);
        if(f==null || !f.spawned || !f.id.equals(e.getUUID()) || !f.dimension.equals(e.level().dimension().location().toString()) || !f.active()) {e.discard();return;}
        if(f.entity!=null&&f.entity!=e&&!f.entity.isRemoved()){e.discard();return;}
        f.entity=e;
    }
    private ServerLevel level(Flight f){return server.getLevel(ResourceKey.create(Registries.DIMENSION,new ResourceLocation(f.dimension)));}
    public void tick(){
        clock++;
        if(!storageFailed && journal.failure()!=null)storageFailure(journal.failure());
        int permitted=MissionConfig.MAX_MISSIONS.get();
        for(var f:new ArrayList<>(flights.values()))if(f.active()){
            if(permitted--<=0){end(f,"Failed","Server simultaneous mission limit reduced");continue;}
            if(storageFailed){end(f,"Failed","Mission storage unavailable");continue;}
            var l=level(f);if(l==null){end(f,"Failed","Dimension unavailable");continue;}
            // Region tickets keep the bounded chunks ticking, but vanilla can still
            // suspend the whole entity loop after 300 ticks without a player.
            // Keep that loop awake only while a real mission owns its moving window.
            // This public vanilla API requests no additional chunks.
            l.resetEmptyTime();
            if(++f.age>MissionConfig.DURATION.get()){end(f,"Failed","Mission duration exceeded");continue;}
            if(!MissionConfig.OFFLINE.get()&&server.getPlayerList().getPlayer(f.owner)==null){end(f,"Ended","Owner offline (server policy)");continue;}
            if(!f.spawned){
                if(l.hasChunkAt(BlockPos.of(f.platform))){var be=l.getBlockEntity(BlockPos.of(f.platform));if(!(be instanceof MissionPlatform p)||p.mission().last==null||!p.mission().last.equals(f.id)){end(f,"Failed","Launch platform removed");continue;}}
                if(f.age>MissionConfig.WAIT.get()+80){end(f,"Failed","Launch preparation timed out");continue;}
                continue;
            }
            if(f.entity==null||f.entity.isRemoved())f.entity=l.getEntity(f.id);
            if(f.entity!=null){
                var e=f.entity;
                if(!e.isAlive()){end(f,"Failed","Vehicle no longer alive");continue;}
                if(!MissionMath.finite(e.position())||!MissionMath.finite(e.getDeltaMovement())){end(f,"Failed","Invalid flight state");continue;}
                if(e.level()!=l){end(f,"Failed","Vehicle changed dimension");continue;}
                var controller=SbwCompat.getDroneControllerPlayer(e);
                if(!(e instanceof Fl01Entity)&&controller!=null&&controller.getUUID().equals(f.owner)&&SbwCompat.isUsingLinkedMonitorForDrone(controller,e)){end(f,"Ended","Manual control resumed");e.getPersistentData().remove(NO_TICKETS);continue;}
                Vec3 at=e.position(),old=f.position();
                f.previousX=f.x;f.previousY=f.y;f.previousZ=f.z;f.x=at.x;f.y=at.y;f.z=at.z;
                f.flightTicks++;
                if(f.flightTicks>8 && MissionMath.closest(old,at,f.target()).distanceToSqr(f.target())<=1.5625){
                    // Durable tombstone first: recovery can never repeat this detonation.
                    f.state="Reached";f.reason="Target reached (1.25 block segment tolerance)";
                    if(!checkpoint()){f.state="Failed";f.reason="Journal write failed; detonation refused";tickets.release(f.id);e.getPersistentData().remove(TAG);e.getPersistentData().putBoolean(NO_TICKETS,true);continue;}
                    tickets.release(f.id);f.detonating=true;
                    try {if(e instanceof Fl01Entity fl)fl.detonate(e.position());else{DroneCrashExplosionSystem.handleDroneDestroy(l,e);e.discard();}}finally{f.detonating=false;}
                    continue;
                }
                double dist=at.distanceTo(f.target());
                if(dist<f.best-.25){f.best=dist;f.stalled=0;}else if(!f.hold&&++f.stalled>400){end(f,"Failed","No progress toward target");continue;}
                if(at.distanceTo(new Vec3(f.originX,f.originY,f.originZ))>MissionConfig.DISTANCE.get()+32){end(f,"Failed","Mission range exceeded");continue;}
                // Keep the prefetch direction while paused. A zero hold velocity must not
                // alternate the window across a border and repeatedly cancel its own loads.
                if(!f.hold && e.getDeltaMovement().horizontalDistanceSqr()>.0025){f.vx=e.getDeltaMovement().x;f.vy=e.getDeltaMovement().y;f.vz=e.getDeltaMovement().z;}
                if(!f.hold)rememberMotion(f,e);
                if(e instanceof Fl01Entity fl&&f.hold)tickets.follow(f.id,l,fl.prefetchAnchor(),Vec3.ZERO);else tickets.follow(f.id,l,at,new Vec3(f.vx,f.vy,f.vz));
            }else tickets.follow(f.id,l,f.position(),new Vec3(f.vx,f.vy,f.vz));
            boolean ready=f.entity==null||f.entity instanceof Fl01Entity&&f.hold?tickets.ready(f.id):tickets.flightReady(f.entity,new Vec3(f.vx,f.vy,f.vz),f.hold?new Vec3(f.carryX,f.carryY,f.carryZ):f.entity.getDeltaMovement());
            if(!ready||f.entity==null){f.hold=true;f.state="Waiting for chunks";if(++f.waitTicks>MissionConfig.WAIT.get())end(f,"Failed",f.entity==null?"Saved vehicle missing; no replacement spawned":"Chunk loading timed out");}
            else {if(f.hold&&f.entity!=null){f.entity.setDeltaMovement(f.carryX,f.carryY,f.carryZ);f.entity.hurtMarked=true;}f.hold=false;f.waitTicks=0;f.state=f.flightTicks<12?"Departing":"En route";}
        }
        tickets.tick();
        if(clock%100==0 && !flights.isEmpty())savePeriodic();
    }
    private byte[] snapshot(){
        while(flights.size()>256){var victim=flights.values().stream().filter(f->!f.active()).findFirst();if(victim.isEmpty())break;flights.remove(victim.get().id);}
        return JSON.toJson(flights.values()).getBytes(StandardCharsets.UTF_8);
    }
    private void storageFailure(IOException e){
        storageFailed=true;tickets.clear();
        SbwDroneRangeConfig.LOGGER.error("Mission journal write failed; autonomous flight disabled",e);
    }
    private void savePeriodic(){
        if(storageFailed)return;
        try{journal.saveAsync(snapshot());}catch(IOException e){storageFailure(e);}
    }
    /** Durability barrier for launch reservations, spawn commits and terminal tombstones. */
    public boolean checkpoint(){
        if(storageFailed)return false;
        try{journal.saveAndWait(snapshot());return true;}catch(IOException e){storageFailure(e);return false;}
    }
    public static boolean beforeTravel(Entity e){
        var f=flight(e);if(f==null||!f.active())return false;
        if(get(((ServerLevel)e.level()).getServer()).storageFailed){f.hold=true;return true;}
        if(DroneJammerSystem.isJammed(e)){get(((ServerLevel)e.level()).getServer()).end(f,"Failed","Guidance jammed");return false;}
        if(e instanceof LucasDroneEntity d && !LucasFuelSystem.canUsePoweredFlight(d)||e instanceof Geranium2Entity g && !Geranium2FuelSystem.canUsePoweredFlight(g)){get(((ServerLevel)e.level()).getServer()).end(f,"Failed","Fuel exhausted");return false;}
        if(f.hold)return true;
        if(!powered(e))return false;
        var delta=f.target().subtract(e.position());double horizontal=delta.horizontalDistance();
        double desiredY=horizontal>Math.max(32,Math.abs(e.getY()-f.ty)*6)?f.cruise:f.ty;
        if(f.flightTicks>20 && e.tickCount%4==0){
            // Only inspect collision shapes inside the ready moving window. No terrain generation here.
            Vec3 direction=e.getDeltaMovement().horizontalDistanceSqr()>1e-5?e.getDeltaMovement().normalize():delta.normalize();
            var box=e.getBoundingBox().inflate(.25).expandTowards(direction.scale(10));
            if(!e.level().hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){rememberMotion(f,e);f.vx=direction.x;f.vy=direction.y;f.vz=direction.z;get(((ServerLevel)e.level()).getServer()).tickets.follow(f.id,(ServerLevel)e.level(),e.position(),direction);f.hold=true;return true;}
            if(!e.level().noCollision(e,box)){f.avoidY=Math.min(e.level().getMaxBuildHeight()-3,Math.max(desiredY,e.getY()+10));}
            else f.avoidY=0;
        }
        f.brake=f.avoidY>0;desiredY=Math.max(desiredY,f.avoidY);
        double dy=desiredY-e.getY();
        f.climb=Math.max(-Math.max(AddonConfig.lucasDescendSpeed()*4,.24),Math.min(AddonConfig.lucasClimbSpeed(),dy*.07-e.getDeltaMovement().y*.5+.025));
        f.throttle=horizontal<12?.30F:horizontal<40?.48F:.70F;
        if(f.flightTicks>12){
            float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));
            float turn=net.minecraft.util.Mth.clamp(net.minecraft.util.Mth.wrapDegrees(yaw-e.getYRot()),-(float)AddonConfig.lucasTurnRate(),(float)AddonConfig.lucasTurnRate());
            e.setYRot(e.getYRot()+turn);e.setXRot((float)Math.max(-16,Math.min(16,-dy*1.2)));
        }
        return false;
    }
    private static void rememberMotion(Flight f,Entity e){f.carryX=e.getDeltaMovement().x;f.carryY=e.getDeltaMovement().y;f.carryZ=e.getDeltaMovement().z;}
    public static void afterTravel(Entity e){var f=flight(e);if(f!=null&&f.active()&&f.hold){e.setDeltaMovement(0,e instanceof GlideBombEntity?.06:0,0);e.hurtMarked=true;}}
    public static float throttle(Entity e){var f=flight(e);return f==null?0:f.throttle;}
    public static double climb(Entity e){var f=flight(e);return f==null?0:f.climb;}
    public static boolean braking(Entity e){var f=flight(e);return f!=null&&f.brake;}
}
