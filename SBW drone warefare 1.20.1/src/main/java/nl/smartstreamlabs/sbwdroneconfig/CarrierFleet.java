package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import java.util.*;

/** The home link is durable. Native monitor/session fields describe only the active target. */
public final class CarrierFleet {
    public static final String HOME="CarrierHome";
    private static final Map<UUID,Active> ACTIVE=new HashMap<>();
    private static final Map<UUID,Pending> PENDING=new HashMap<>();
    private static final Map<UUID,Long> OPEN_RATE=new HashMap<>();
    private static final TicketType<UUID> LOAD=TicketType.create("carrier_fleet_switch",Comparator.comparing(UUID::toString),100);
    private record Active(UUID home,UUID target,String dimension){}
    private record Pending(UUID home,UUID target,ServerLevel level,ChunkPos chunk,long started,int window){}
    public static UUID home(ItemStack monitor){if(!SbwCompat.isMonitor(monitor))return null;var n=PlatformMonitor.tag(monitor);return SbwCompat.isMonitor(monitor)&&n.hasUUID(HOME)?n.getUUID(HOME):null;}
    public static boolean carrier(Entity e){return e instanceof Dc01Entity||e instanceof Ndc01Entity;}
    public static boolean bind(ServerPlayer p,Entity carrier){
        if(!carrier(carrier)||!(carrier instanceof VatagEntity v)||!v.permitted(p)||!SbwCompat.isMonitor(p.getMainHandItem()))return false;
        CarrierFleetData.get(p.serverLevel()).register(carrier,carrier.getUUID(),true);
        var n=PlatformMonitor.tag(p.getMainHandItem());n.putUUID(HOME,carrier.getUUID());n.putString("CarrierDimension",carrier.level().dimension().location().toString());n.putLong("CarrierPosition",carrier.blockPosition().asLong());PlatformMonitor.save(p.getMainHandItem(),n);
        return true;
    }
    public static boolean access(ServerPlayer p,Entity carrier){return carrier(carrier)&&carrier.getUUID().equals(home(p.getMainHandItem()))&&carrier instanceof VatagEntity v&&v.permitted(p)&&p.level()==carrier.level()&&inRange(p,carrier.position())&&!DroneJamStateManager.isHardJammed(carrier)&&!DroneJammerSystem.isJammed(carrier);}
    public static void observeCarrierSession(ServerPlayer p,VatagEntity v){if(home(p.getMainHandItem())==null)bind(p,v);if(v.getUUID().equals(home(p.getMainHandItem()))){CarrierFleetData.get(p.serverLevel()).register(v,v.getUUID(),true);ACTIVE.put(p.getUUID(),new Active(v.getUUID(),v.getUUID(),p.level().dimension().location().toString()));}}
    public static void migrate(ServerPlayer p){if(!SbwCompat.isMonitor(p.getMainHandItem()))return;
        if(home(p.getMainHandItem())!=null)return;
        var n=PlatformMonitor.tag(p.getMainHandItem());UUID id=n.hasUUID("NdcCarrier")?n.getUUID("NdcCarrier"):VatagMonitorTag.linked(p.getMainHandItem());
        if(id!=null&&carrier(p.serverLevel().getEntity(id))){bind(p,p.serverLevel().getEntity(id));return;}
        if(id!=null&&(n.hasUUID("NdcCarrier")||n.getBoolean("DC01CarrierMonitor"))){String dim=n.hasUUID("NdcCarrier")?n.getString("NdcDimension"):n.getString("VatagDimension");String key=n.hasUUID("NdcCarrier")?"NdcReturnPosition":"VatagPosition";if(!dim.isBlank()&&n.contains(key)){CarrierFleetData.get(p.serverLevel()).rememberHome(id,dim,BlockPos.of(n.getLong(key)),n.hasUUID("NdcCarrier")?"NDC-01":"DC-01");n.putUUID(HOME,id);n.putString("CarrierDimension",dim);n.putLong("CarrierPosition",n.getLong(key));PlatformMonitor.save(p.getMainHandItem(),n);}}
    }
    private static boolean inRange(ServerPlayer p,net.minecraft.world.phys.Vec3 pos){return p.position().distanceToSqr(pos)<=Math.pow(AddonConfig.droneMaxRange(),2);}
    public static Entity entity(ServerPlayer p,UUID id){return id==null?null:p.serverLevel().getEntity(id);}
    public static boolean member(ServerPlayer p,UUID home,UUID id){var e=CarrierFleetData.get(p.serverLevel()).entry(id);return home.equals(id)||e!=null&&home.equals(e.home);}
    public static final String[] STATUS={"Available","Controlling","Chunk unloaded","Destroyed / unavailable","Out of range","Another dimension","Signal jammed","No energy / fuel","Another pilot","FPV goggles required","Access denied","Coordinate mission active","Inventory being edited","Launch in progress","Operator unavailable","Server session limit","Docked: use Launch & Fly"};
    public static int status(ServerPlayer p,CarrierFleetData.Entry record){
        if(record==null||record.destroyed)return 3;
        if(!record.dimension.equals(p.level().dimension().location().toString()))return 5;
        var e=entity(p,record.id);
        if(e==null)return inRange(p,net.minecraft.world.phys.Vec3.atCenterOf(record.position))?2:4;
        return reason(p,e);
    }
    public static int reason(ServerPlayer p,Entity e){return reason(p,e,false);}
    private static int reason(ServerPlayer p,Entity e,boolean launching){
        if(!p.isAlive()||p.isSpectator()||p.hasDisconnected()||InjuryServer.cannotOperate(p)||!SbwCompat.isMonitor(p.getMainHandItem()))return 14;
        if(e==null||!e.isAlive()||e.isRemoved()||e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&v.isWreck())return 3;
        if(e.level()!=p.level())return 5;
        if(e instanceof VatagEntity v?!v.permitted(p):e instanceof Id01Entity i?!i.permitted(p):!Dc01Docking.owned(p,e))return 10;
        if(!launching&&e.isPassenger())return 16;
        if(!inRange(p,e.position()))return 4;
        if(DroneJamStateManager.isHardJammed(e)||DroneJammerSystem.isJammed(e))return 6;
        if(e instanceof VatagEntity v){if(v.operator()!=null&&v.operator()!=p)return 8;if(PlatformInventory.editing(v))return 12;if(v instanceof Ndc01Entity n&&n.busy())return 13;if(v.getEnergy()<=0)return 7;}
        else {if(!SbwCompat.hasEquippedFpvGoggles(p))return 9;if(AddonConfig.enableBatterySystem()&&DroneBatterySystem.isOutOfEnergyState(e))return 7;}
        if(e instanceof VatagEntity&&!VatagSessions.fleetSlot(p)||e instanceof Id01Entity&&!Id01Sessions.fleetSlot(p))return 15;
        if(MissionManager.owns(e))return 11;
        for(var other:p.serverLevel().getServer().getPlayerList().getPlayers())if(other!=p&&(VatagSessions.controlling(other,e)||Id01Sessions.controlling(other,e)||SbwCompat.isUsingLinkedMonitorForDrone(other,e)))return 8;
        return controlling(p,e)?1:0;
    }
    public static boolean controlling(ServerPlayer p,Entity e){return VatagSessions.controlling(p,e)||Id01Sessions.controlling(p,e)||SbwCompat.isUsingLinkedMonitorForDrone(p,e);}
    public static void message(ServerPlayer p,String text){PlatformMonitor.message(p,text);}
    public static void open(ServerPlayer p,int page){if(!SbwCompat.isMonitor(p.getMainHandItem())){message(p,"Hold your carrier monitor to open the fleet.");return;}long now=p.serverLevel().getGameTime();if(now-OPEN_RATE.getOrDefault(p.getUUID(),-10L)<3)return;OPEN_RATE.put(p.getUUID(),now);var a=ACTIVE.get(p.getUUID());var current=a==null?SbwCompat.findActiveLinkedDrone(p):entity(p,a.target);if(current!=null){SbwCompat.resetDroneInput(current);CubedFpvInputState.clear(current);}migrate(p);UUID home=home(p.getMainHandItem());if(home==null){message(p,"Link this monitor to DC-01 or NDC-01 first.");return;}CarrierFleetMenu.open(p,home,page);}
    public static void request(ServerPlayer p,UUID id){
        migrate(p);UUID home=home(p.getMainHandItem());if(home==null||!member(p,home,id)){message(p,"This drone does not belong to the linked carrier.");return;}
        var current=ACTIVE.get(p.getUUID());if(current!=null){var old=entity(p,current.target);if(old instanceof Ndc01Entity n&&n.busy()){message(p,STATUS[13]);return;}}
        var source=entity(p,home);if(source instanceof Ndc01Entity n&&n.busy()){message(p,STATUS[13]);return;}cancelPending(p);
        Entity target=entity(p,id);
        if(target!=null){switchTo(p,home,target);return;}
        var record=CarrierFleetData.get(p.serverLevel()).entry(id);int state=status(p,record);
        if(state!=2){message(p,STATUS[state]);return;}
        if(PENDING.size()>=4&&!PENDING.containsKey(p.getUUID())){message(p,STATUS[15]);return;}if(!p.serverLevel().getWorldBorder().isWithinBounds(record.position)){message(p,"Last vehicle position is outside the world border.");return;}
        cancelPending(p);var chunk=new ChunkPos(record.position);p.serverLevel().getChunkSource().addRegionTicket(LOAD,chunk,2,p.getUUID(),true);PENDING.put(p.getUUID(),new Pending(home,id,p.serverLevel(),chunk,p.serverLevel().getGameTime(),p.containerMenu.containerId));message(p,"Loading the selected vehicle. Your current connection is retained.");
    }
    private static void releaseNative(ServerPlayer p,Entity old){
        if(old==null)return;if(carrier(old)&&old instanceof VatagEntity vehicle){vehicle.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);vehicle.setDeltaRot(0);vehicle.setPower(0);}
        CubedFpvInputState.clear(old);SbwCompat.resetDroneInput(old);if(old instanceof DroneEntity)SbwCompat.setDroneFire(old,false);
        if(old instanceof DroneEntity d&&p.getStringUUID().equals(d.getEntityData().get(DroneEntity.CONTROLLER)))d.getEntityData().set(DroneEntity.CONTROLLER,"");
        if(SbwCompat.isUsingLinkedMonitorForDrone(p,old))SbwCompat.stopUsingMonitor(p.getMainHandItem());
    }
    public static boolean switchTo(ServerPlayer p,UUID home,Entity target){return switchTo(p,home,target,true);}
    private static boolean switchTo(ServerPlayer p,UUID home,Entity target,boolean restore){
        if(!home.equals(home(p.getMainHandItem()))||!member(p,home,target.getUUID())||home.equals(target.getUUID())&&!carrier(target))return false;
        int denied=reason(p,target);if(denied!=0&&denied!=1){message(p,STATUS[denied]);return false;}
        cancelPending(p);if(denied==1){ACTIVE.put(p.getUUID(),new Active(home,target.getUUID(),p.level().dimension().location().toString()));p.closeContainer();return true;}
        var previous=ACTIVE.get(p.getUUID());Entity old=previous==null?SbwCompat.findActiveLinkedDrone(p):entity(p,previous.target);releaseNative(p,old);
        Rd01Sessions.stop(p);Id01Sessions.stop(p);
        var monitor=p.getMainHandItem();
        if(target instanceof VatagEntity){
            VatagSessions.stop(p);VatagMonitorTag.link(monitor,target.getUUID(),target.blockPosition(),target.level().dimension().location().toString());VatagSessions.start(p,target.getUUID());
        }else if(target instanceof Id01Entity){
            VatagSessions.stop(p);VatagMonitorTag.link(monitor,target.getUUID(),target.blockPosition(),target.level().dimension().location().toString());Id01Sessions.mark(monitor);Id01Sessions.start(p,target.getUUID());
        }else if(target instanceof DroneEntity drone){
            SbwCompat.ensureDroneOwner(target,p);drone.getEntityData().set(DroneEntity.LINKED,true);drone.getEntityData().set(DroneEntity.CONTROLLER,p.getStringUUID());SbwCompat.linkMonitorToDrone(monitor,target,true);
            var tag=PlatformMonitor.tag(monitor);tag.putUUID("PadPilotSession",UUID.randomUUID());tag.putDouble("PosX",target.getX());tag.putDouble("PosY",target.getY());tag.putDouble("PosZ",target.getZ());PlatformMonitor.save(monitor,tag);VatagSessions.handoff(p,target);
        }else return false;
        if(!controlling(p,target)){message(p,"The selected vehicle refused control. Carrier link retained.");if(restore&&old!=null&&old!=target&&old.isAlive())switchTo(p,home,old,false);return false;}
        ACTIVE.put(p.getUUID(),new Active(home,target.getUUID(),p.level().dimension().location().toString()));CarrierFleetData.get(p.serverLevel()).refresh(target);p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();p.containerMenu.broadcastChanges();p.closeContainer();return true;
    }
    public static boolean prepareLaunch(ServerPlayer p,Entity source,Entity drone){UUID home=home(p.getMainHandItem());if(home==null){if(!bind(p,source))return false;home=source.getUUID();}if(!home.equals(source.getUUID())){message(p,"This monitor belongs to another carrier. Link it explicitly before launching here.");return false;}int state=reason(p,drone,true);if(state!=0&&state!=1){message(p,STATUS[state]);return false;}return true;}
    public static boolean launched(ServerPlayer p,Entity carrier,Entity drone){
        UUID home=home(p.getMainHandItem());if(home==null){if(!bind(p,carrier))return false;home=carrier.getUUID();}
        if(!home.equals(carrier.getUUID())){message(p,"This monitor belongs to another carrier. Link it explicitly before launching here.");return false;}
        CarrierFleetData.get(p.serverLevel()).register(carrier,home,true);var roster=CarrierFleetData.get(p.serverLevel());boolean fresh=roster.entry(drone.getUUID())==null;roster.register(drone,home,false);
        boolean success=switchTo(p,home,drone);if(!success&&fresh)roster.forget(drone.getUUID());return success;
    }
    public static String leaving(ServerPlayer p){
        var a=ACTIVE.get(p.getUUID());var e=a==null?SbwCompat.findActiveLinkedDrone(p):entity(p,a.target);
        if(e instanceof VatagEntity)return "On switch: the carrier brakes and remains stopped.";
        if(e instanceof Id01Entity)return "On switch: ID-01 keeps its current native flight / mission mode.";
        return "On switch: inputs stop. The drone may drift or descend and uses battery. No automatic return is started.";
    }
    public static void cancelPending(ServerPlayer p){var v=PENDING.remove(p.getUUID());if(v!=null)v.level.getChunkSource().removeRegionTicket(LOAD,v.chunk,2,p.getUUID(),true);}
    public static void stopCurrent(ServerPlayer p){cancelPending(p);var active=ACTIVE.remove(p.getUUID());releaseNative(p,active==null?SbwCompat.findActiveLinkedDrone(p):entity(p,active.target));Rd01Sessions.stop(p);Id01Sessions.stop(p);VatagSessions.stop(p);p.inventoryMenu.broadcastChanges();}
    public static void login(ServerPlayer p){for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(!SbwCompat.isMonitor(stack))continue;var tag=PlatformMonitor.tag(stack);if(SbwCompat.isMonitor(stack)&&(tag.hasUUID(HOME)||tag.hasUUID("NdcCarrier"))){tag.putBoolean(SbwCompat.TAG_USING,false);tag.remove("PadPilotSession");PlatformMonitor.save(stack,tag);}}p.inventoryMenu.broadcastChanges();}
    public static void logout(ServerPlayer p){cancelPending(p);var active=ACTIVE.remove(p.getUUID());if(active!=null){var id=net.minecraft.resources.ResourceLocation.tryParse(active.dimension);var level=id==null?null:p.getServer().getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,id));if(level!=null){var old=level.getEntity(active.target);releaseNative(p,old);if(old!=null&&VatagSessions.controlling(p,old))VatagSessions.stop(p);if(old!=null&&Id01Sessions.controlling(p,old))Id01Sessions.stop(p);}}OPEN_RATE.remove(p.getUUID());}
    public static void shutdown(net.minecraft.server.MinecraftServer server){for(var id:List.copyOf(ACTIVE.keySet())){var p=server.getPlayerList().getPlayer(id);if(p!=null)logout(p);}for(var pair:PENDING.entrySet()){var v=pair.getValue();v.level.getChunkSource().removeRegionTicket(LOAD,v.chunk,2,pair.getKey(),true);}PENDING.clear();ACTIVE.clear();OPEN_RATE.clear();}
    public static void tick(net.minecraft.server.MinecraftServer server){
        for(var pair:List.copyOf(PENDING.entrySet())){
            var p=server.getPlayerList().getPlayer(pair.getKey());var v=pair.getValue();
            if(p==null){v.level.getChunkSource().removeRegionTicket(LOAD,v.chunk,2,pair.getKey(),true);PENDING.remove(pair.getKey());continue;}
            if(p.containerMenu.containerId!=v.window||p.serverLevel()!=v.level||!v.home.equals(home(p.getMainHandItem()))||!p.isAlive()||p.hasDisconnected()){cancelPending(p);continue;}
            Entity target=v.level.getEntity(v.target);
            if(target!=null){cancelPending(p);switchTo(p,v.home,target);}
            else if(v.level.getGameTime()-v.started>=80){cancelPending(p);message(p,"Vehicle could not be loaded at its last known position. Carrier link retained.");}
        }
        if(server.getTickCount()%10!=0)return;
        for(var pair:List.copyOf(ACTIVE.entrySet())){
            var p=server.getPlayerList().getPlayer(pair.getKey());var a=pair.getValue();
            if(p==null){ACTIVE.remove(pair.getKey());continue;}
            if(!p.isAlive()||p.hasDisconnected()||!a.dimension.equals(p.level().dimension().location().toString())||!a.home.equals(home(p.getMainHandItem()))){logout(p);continue;}
            var target=entity(p,a.target);var data=CarrierFleetData.get(p.serverLevel());var homeEntity=entity(p,a.home);if(homeEntity!=null)data.refresh(homeEntity);
            if(target!=null&&target.isAlive()&&!target.isRemoved()){
                data.refresh(target);int state=reason(p,target);if(controlling(p,target)&&(state==0||state==1||state==12||state==13))continue;
                if(state==0||state==1){releaseNative(p,target);ACTIVE.remove(pair.getKey());continue;}
            }
            ACTIVE.remove(pair.getKey());releaseNative(p,target);if(a.target.equals(SbwCompat.getLinkedDroneUuid(p.getMainHandItem())))SbwCompat.stopUsingMonitor(p.getMainHandItem());Rd01Sessions.stop(p);Id01Sessions.stop(p);VatagSessions.stop(p);p.inventoryMenu.broadcastChanges();if(a.target.equals(a.home))continue;
            if(homeEntity!=null&&reason(p,homeEntity)==0){message(p,"Drone connection ended. Returning to carrier.");switchTo(p,a.home,homeEntity);}
            else if(homeEntity==null&&status(p,data.entry(a.home))==2){message(p,"Drone connection ended. Loading carrier for return.");request(p,a.home);}else message(p,"Drone connection ended. Carrier unavailable; its link is retained. Open the fleet with F7.");
        }
    }
    private CarrierFleet(){}
}
