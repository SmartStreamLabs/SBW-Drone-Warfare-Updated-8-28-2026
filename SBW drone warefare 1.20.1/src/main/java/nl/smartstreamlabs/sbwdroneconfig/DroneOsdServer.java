package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import java.util.*;

/** Read-only adapters. No inventory writes, control changes, chunk requests or world entity scans. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class DroneOsdServer {
    private record Preview(UUID entity,long tick,float signal){}
    private static final Map<UUID,Preview> PREVIEWS=new HashMap<>();
    public static void preview(ServerPlayer p,List<PortableFpvDetectorTarget> targets,UUID selected){
        var t=targets.stream().filter(v->v.droneId().equals(selected)).findFirst().orElse(null);
        if(t==null)PREVIEWS.remove(p.getUUID());else PREVIEWS.put(p.getUUID(),new Preview(t.droneId(),p.serverLevel().getGameTime(),(float)t.signalStrength()));
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.PlayerTickEvent event){
        if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END||!(event.player instanceof ServerPlayer p)||p.tickCount%5!=0)return;
        var monitor=SbwCompat.getActiveLinkedMonitor(p);UUID id=SbwCompat.getLinkedDroneUuid(monitor);
        if(id==null)id=VatagMonitorTag.linked(p.getMainHandItem());
        Entity e=id==null?null:p.serverLevel().getEntity(id);
        if(e!=null&&(VatagSessions.controlling(p,e)||Rd01Sessions.controlling(p,e)||Id01Sessions.controlling(p,e)||SbwCompat.isUsingLinkedMonitorForDrone(p,e)))send(p,e,false,Float.NaN);
        var preview=PREVIEWS.get(p.getUUID());
        if(preview!=null){
            if(!DroneJammerItem.isActiveJammerHeld(p)||p.serverLevel().getGameTime()-preview.tick>Math.max(20,AddonConfig.portableFpvDetectorScanIntervalTicks()+10)){PREVIEWS.remove(p.getUUID());return;}
            Entity remote=p.serverLevel().getEntity(preview.entity);
            if(remote!=null&&remote!=e)send(p,remote,true,preview.signal);
        }
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){PREVIEWS.remove(e.getEntity().getUUID());CameraInterferenceStatus.remove(e.getEntity().getUUID());}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppedEvent e){PREVIEWS.clear();CameraInterferenceStatus.clear();}
    private static void send(ServerPlayer p,Entity e,boolean preview,float signal){if(e.isAlive()&&!e.isRemoved()&&e.level()==p.level()){AddonNetwork.sendDroneOsd(p,collect(p,e,preview,signal));CameraInterferenceStatus.send(p,e);}}
    public static DroneOsdTelemetry collect(ServerPlayer p,Entity e,boolean preview,float signal){
        boolean custom=e instanceof VatagEntity||e instanceof Rd01Entity||e instanceof Id01Entity;
        boolean fiber=FiberOpticLinkSystem.isFiberSessionActive(e);
        Vec3 captured=PlayerDroneAnchorManager.getCapturedOriginalPosition(p),origin=captured==null?p.position():captured;
        double range=preview?AddonConfig.portableFpvDetectorRange():custom?AddonConfig.droneMaxRange():captured!=null?Double.NaN:DroneWeatherEffects.getEffectiveControlRange(p,e,AddonConfig.droneMaxRange());
        if(e instanceof Hx2Entity)range=DroneWeatherEffects.getEffectiveControlRange(p,e,Math.min(AddonConfig.droneMaxRange(),Hx2Config.RANGE.get()));
        if(fiber){var n=e.getPersistentData();origin=n.contains(FiberOpticLinkSystem.TAG_FIBER_ANCHOR_X)&&n.contains(FiberOpticLinkSystem.TAG_FIBER_ANCHOR_Y)&&n.contains(FiberOpticLinkSystem.TAG_FIBER_ANCHOR_Z)?new Vec3(FiberOpticLinkSystem.getAnchorX(e),FiberOpticLinkSystem.getAnchorY(e),FiberOpticLinkSystem.getAnchorZ(e)):new Vec3(Double.NaN,Double.NaN,Double.NaN);range=AddonConfig.fiberOpticCableLength();}
        if(e instanceof Sd01Entity)range=Sd01Entity.radio();
        int energy=-1,max=-1;var n=e.getPersistentData();
        if(e instanceof VatagEntity v){energy=v.displayEnergy();max=v.getMaxEnergy();}
        else if(AddonConfig.enableBatterySystem()&&n.contains("sbwdroneconfigCurrentEnergy")&&n.contains("sbwdroneconfigMaxEnergy")){energy=n.getInt("sbwdroneconfigCurrentEnergy");max=n.getInt("sbwdroneconfigMaxEnergy");}
        float fuel=e instanceof LucasDroneEntity l?l.getLucasFuelPercent():e instanceof Geranium2Entity g?g.getGeraniumFuelPercent():Float.NaN;
        boolean lowFuel=AddonConfig.lucasMaxFuel()>0&&AddonConfig.lucasLowFuelWarningPercent()>0&&(e instanceof LucasDroneEntity l&&l.getLucasFuel()>0||e instanceof Geranium2Entity g&&g.getGeraniumFuel()>0)&&Float.isFinite(fuel)&&fuel<=AddonConfig.lucasLowFuelWarningPercent();
        int target=-1;if(e instanceof Id01Entity i&&i.targetId()!=null){Entity t=p.serverLevel().getEntity(i.targetId());if(t!=null)target=t.getId();}
        return new DroneOsdTelemetry(e.getUUID(),e.getId(),e.level().dimension().location().toString(),mode(e),payload(e),e instanceof Sd01Entity?"SUB LINK":fiber?"FIBER":preview?"RX RF":"RF",origin.x,origin.y,origin.z,energy,max,fuel,max>0&&energy>=0&&energy<=AddonConfig.lowEnergyThreshold(),lowFuel,!fiber&&(!(e instanceof Sd01Entity)||Sd01Config.JAMMERS.get())&&DroneJamStateManager.isHardJammed(e),range,target,signal,markers(p));
    }
    private static List<Integer> markers(ServerPlayer p){
        var stack=p.getOffhandItem();String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();if(!key.equals("artillery_indicator")&&!key.equals("firing_parameters"))return List.of();
        var tags=(stack.hasTag()?stack.getTag():new net.minecraft.nbt.CompoundTag()).getList("Cannons",10);var ids=new ArrayList<Integer>();for(int i=0;i<Math.min(16,tags.size());i++){String id=tags.getCompound(i).getString("UUID");try{Entity e=p.serverLevel().getEntity(UUID.fromString(id));if(e!=null)ids.add(e.getId());}catch(IllegalArgumentException ignored){/* malformed existing item data is not a marker */}}return ids;
    }
    public static String mode(Entity e){
        if(e instanceof Fl01Entity fl)return fl.manual()?"MANUAL / FL-01":"AUTO / FL-01";
        if(e instanceof Sd01Entity d)return d.getEnergy()<=0?"NO POWER / "+Sd01Config.EMPTY.get().name():d.remotelyControlled()?"MANUAL / SUBMARINE":"FAIL-SAFE / "+Sd01Config.LOST.get().name();
        if(e instanceof Hx2Entity h)return h.mount()!=null?"DOCKED / HX-2":h.flying()?"MANUAL / HX-2":"IDLE / HX-2";
        if(e instanceof Id01Entity i)return "AUTO / "+i.status();
        var flight=MissionManager.flight(e);if(flight!=null&&flight.active())return flight.hold||flight.state.equals("Waiting for chunks")?"AUTO / HOLD":"AUTO / COORD";
        if(e instanceof DeliveryDroneEntity d&&d.getSyncedDeliveryTarget()!=null)return "AUTO / DELIVERY";
        if(e instanceof BayraktarTb2Entity b&&b.getWaypoint()!=null)return "AUTO / WAYPOINT";
        if(e instanceof Qd01Entity q)return q.remotelyControlled()?"MANUAL / QUADRUPED":"IDLE / QUADRUPED";
        if(e instanceof Wd01Entity w)return w.remotelyControlled()?"MANUAL / WATER":"IDLE / WATER";
        if(e instanceof Dc01Entity c)return c.operator()!=null?"MANUAL / CARRIER":"IDLE / CARRIER";
        if(e instanceof VatagEntity v)return v.operator()!=null?"MANUAL / DRIVE":"IDLE";
        if(e instanceof Rd01Entity r)return r.operator()!=null?"MANUAL / RESCUE":"IDLE";
        return SbwCompat.getDroneControllerUuid(e)!=null?"MANUAL":"IDLE";
    }
    public static String payload(Entity e){
        if(e instanceof Sd01Entity d)return "UNARMED / LIGHT "+(d.lights()?"ON":"OFF");
        if(e instanceof Hx2Entity)return "FIXED PAYLOAD";
        if(e instanceof Wd01Entity w)return w.floating()?"AFLOAT / UNARMED":"GROUNDED / UNARMED";
        if(e instanceof Dc01Entity c)return "DOCKS "+Integer.bitCount(c.mask())+" / "+(c.mask()==0?"EMPTY":(c.mask()&1)!=0?"CENTER":"SMALL PAIR");
        if(e instanceof MineRoverEntity m)return "MINES "+m.mineCount()+"/6";
        if(e instanceof SupplyRoverEntity s)return "CARGO "+s.cargoSlots()+"/"+s.getContainerSize()+" | MT-01 "+(s.partner()==null?"UNCOUPLED":"COUPLED");
        if(e instanceof Gad01Entity g)return "MINES "+(g.displayedMine(0).isEmpty()?"A EMPTY":"A LOADED")+" / "+(g.displayedMine(1).isEmpty()?"B EMPTY":"B LOADED")+" | "+(g.mineCount()==0?"NO PAYLOAD":g.armTicks()>0?"ARMING "+g.armTicks()+"/20":g.getEnergy()<=0||DroneJammerSystem.isJammed(g)?"DETONATION BLOCKED":"HOLD DETONATE");
        if(e instanceof Hgd01Entity h)return h.weaponName()+" | "+(h.weapon()==0?"FE "+h.displayEnergy():"AMMO "+h.magazine()+(h.reloading()?" / RELOAD":""));
        if(e instanceof VatagEntity v)return "AMMO "+v.magazine()+(v.reloading()?" / RELOAD":"");
        if(e instanceof Rd01Entity r)return (r.occupied()?"HARNESS OCCUPIED":"HARNESS EMPTY")+String.format(Locale.ROOT," / %.2f m",r.extension(1));
        if(e instanceof Id01Entity)return "INTERCEPTOR";
        if(e instanceof DeliveryDroneEntity d)return DeliveryGroundCargo.loaded(d)?"CARGO GROUND ROBOT | U pickup / Y deploy":d.hasMountedExtractionCrate()?"CARGO "+d.getMountedCargoOccupiedSlots()+"/"+d.getMountedCargoSlotCapacity():"CARGO EMPTY";
        if(e instanceof HeavyBomberDroneEntity h){int grenades=0;var inventory=new DroneInventoryContainer(h);for(int i=0;i<inventory.getContainerSize();i++)if(Hg01Payload.is(inventory.getItem(i)))grenades+=inventory.getItem(i).getCount();return grenades>0?"HG-01 x"+grenades:"BOMBS "+h.getBombsRemaining();}
        if(e instanceof BayraktarTb2Entity b)return "MISSILES "+b.osdMissileCount();
        if(e instanceof Geranium2Entity||e instanceof GlideBombEntity)return "FIXED PAYLOAD";
        if(e instanceof CubedFpvDroneEntity f&&Hg01Payload.is(SbwCompat.getDroneCurrentItem(f)))return "HG-01 x1";
        if(e instanceof DroneEntity){var item=SbwCompat.getDroneCurrentItem(e);int ammo=e.getEntityData().get(DroneEntity.AMMO);return item.isEmpty()||ammo<=0?"PAYLOAD EMPTY":"PAYLOAD "+Math.max(0,ammo);}
        return "";
    }
}
