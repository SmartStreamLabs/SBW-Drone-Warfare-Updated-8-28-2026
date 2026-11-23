package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Server-thread leases live in the active window, never in saved NBT or a global cache. */
public final class PlatformInventory {
    private PlatformInventory(){}
    private static final Map<Entity,Boolean> ACTIVE=new WeakHashMap<>();
    static void opened(Entity d){ACTIVE.put(d,Boolean.TRUE);}
    static void closed(Entity d){ACTIVE.remove(d);}
    public static boolean canReceive(Player p,ItemStack item){if(p.getInventory().getFreeSlot()>=0)return true;for(var s:p.getInventory().items)if(ItemStack.isSameItemSameTags(s,item)&&s.getCount()+item.getCount()<=Math.min(s.getMaxStackSize(),p.getInventory().getMaxStackSize()))return true;return false;}
    static Level level(Object h){return h instanceof BlockEntity b?b.getLevel():((Entity)h).level();}
    public static boolean editing(Object h){
        if(h==null||!(level(h) instanceof ServerLevel l))return false;
        for(var p:l.getServer().getPlayerList().getPlayers())if(p.containerMenu instanceof PlatformInventoryMenu m&&m.editor!=null&&m.editor.host==h)return true;
        return false;
    }
    public static boolean editingDrone(Entity e){
        return !e.level().isClientSide&&ACTIVE.containsKey(e);
    }
    public static boolean guard(Object h,Player p){
        if(!editing(h))return false;
        if(p!=null)p.displayClientMessage(Component.literal("Drone inventory is being edited. Close it before launching or unloading."),true);
        return true;
    }
    public static void closeFor(Object h){
        if(!(level(h) instanceof ServerLevel l))return;
        for(var p:List.copyOf(l.getServer().getPlayerList().getPlayers()))if(p.containerMenu instanceof PlatformInventoryMenu m&&m.editor!=null&&m.editor.host==h)p.closeContainer();
    }
    static ItemStack cargo(Object h,int bay){return h instanceof MissionPlatform m?m.drone():h instanceof Ndc01Entity n&&bay>=0&&bay<18?n.cargo(bay):ItemStack.EMPTY;}
    static Entity live(Object h,int bay){return h instanceof HxPlatformBlockEntity x?x.drone():h instanceof Dc01Entity c&&bay>=0&&bay<3?c.drone(bay):null;}
    static UUID identity(Object h,int bay){var e=live(h,bay);return h instanceof MissionPlatform m?m.loadedId():h instanceof Ndc01Entity n&&bay>=0&&bay<18?n.loadId(bay):e==null?null:e.getUUID();}
    static boolean idle(Object h){
        if(h instanceof LucasPlatformBlockEntity p)return p.phase()==LucasPlatformBlockEntity.Phase.READY;
        if(h instanceof GeraniumPlatformBlockEntity p)return p.phase()==GeraniumPlatformBlockEntity.Phase.READY;
        if(h instanceof GlidePlatformBlockEntity p)return p.phase()==GlidePlatformBlockEntity.Phase.READY;
        if(h instanceof Fl01PlatformBlockEntity p)return p.phase()==Fl01PlatformBlockEntity.Phase.READY;
        if(h instanceof HxPlatformBlockEntity p)return p.phase()!=HxPlatformBlockEntity.Phase.LAUNCHING;
        if(h instanceof Ndc01Entity n)return !n.busy();
        return h instanceof Dc01Entity c&&c.stopped();
    }
    static boolean access(Object h,ServerPlayer p){
        if(!p.isAlive()||p.hasDisconnected()||p.isSpectator()||p.level()!=level(h)||InjuryServer.cannotOperate(p))return false;
        if(h instanceof BlockEntity b&& (b.isRemoved()||!p.level().hasChunkAt(b.getBlockPos())||p.level().getBlockEntity(b.getBlockPos())!=b))return false;
        if(h instanceof Entity e&&(!e.isAlive()||e.isRemoved()))return false;
        return h instanceof MissionPlatform m?m.canUse(p):h instanceof HxPlatformBlockEntity x?x.canUse(p):h instanceof Ndc01Entity n?n.canUse(p):h instanceof Dc01Entity c&&c.permitted(p)&&(p.distanceToSqr(c)<=64||(VatagSessions.controlling(p,c)||CarrierFleet.access(p,c)));
    }
    public static void open(Object host,int bay,Player who){
        if(!(who instanceof ServerPlayer p)||!access(host,p))return;
        if(identity(host,bay)==null){PlatformMonitor.message(p,"Load a drone before opening Drone Inventory.");return;}
        if(!idle(host)||guard(host,p)){PlatformMonitor.message(p,"Platform busy: close the inventory or wait for the platform to finish.");return;}
        Editor e=new Editor(host,bay,p);if(e.drone==null||e.drone.isRemoved()||!e.drone.isAlive()||e.drone instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&v.isWreck()||!Hg01Payload.allowed(p,e.drone)){PlatformMonitor.message(p,"Cannot open this drone: invalid data or access denied.");return;}
        // Existing entity windows must close before this window can edit the same inventory.
        for(var other:List.copyOf(p.server.getPlayerList().getPlayers()))if(other!=p&&other.containerMenu.slots.stream().anyMatch(s->s.container instanceof DroneInventoryContainer c&&c.boundDrone()==e.drone)){PlatformMonitor.message(p,"Another player is editing this drone.");return;}
        net.minecraftforge.network.NetworkHooks.openScreen(p,new SimpleMenuProvider((id,inv,w)->new PlatformInventoryMenu(id,inv,e),Component.literal("Drone Inventory")),b->b.writeBoolean(e.payload()));
        if(p.containerMenu instanceof PlatformInventoryMenu m&&m.editor==e)opened(e.drone);
    }
    static CompoundTag saved(ItemStack s){if(s.isEmpty())return new CompoundTag();var n=PlatformMonitor.tag(s);return n.getCompound(s.is(AddonItems.ID01.get())?"Id01Saved":"EntityTag");}
    /** A packed drone has exactly one owner for inventory and mounted payload. */
    public static ItemStack packDrone(Entity drone,ItemStack item){
        var data=new CompoundTag();drone.saveWithoutId(data);for(String key:new String[]{"UUID","Pos","Motion","Rotation","Passengers","Leash","Dimension"})data.remove(key);
        if(data.contains("Item",10)&&data.getCompound("Item").isEmpty())data.remove("Item");
        var n=PlatformMonitor.tag(item);var prior=n.getCompound("EntityTag");prior.merge(data);n.put("EntityTag",prior);PlatformMonitor.save(item,n);return item;
    }
    public static Entity hydrate(Entity e,CompoundTag source){
        if(e==null)return null;
        if(!source.isEmpty()){
            var n=source.copy();for(String k:new String[]{"UUID","Pos","Motion","Rotation","Passengers","Leash","Dimension"})n.remove(k);
            var defaults=new CompoundTag();e.saveWithoutId(defaults);defaults.merge(n);
            if(defaults.contains("Item",10)&&defaults.getCompound("Item").isEmpty())defaults.remove("Item");
            e.load(defaults);
        }
        return e;
    }
    public static boolean batteryReady(MissionPlatform p){if(!AddonConfig.enableBatterySystem()||p instanceof Fl01PlatformBlockEntity)return true;var d=saved(p.drone()).getCompound("ForgeData");return !d.contains("sbwdroneconfigCurrentEnergy")||d.getInt("sbwdroneconfigCurrentEnergy")>0;}
    public static ContainerData status(Object h,IntSupplier bay){
        return h==null?new SimpleContainerData(5):new ContainerData(){
            public int get(int i){int s=bay.getAsInt();var id=identity(h,s);if(id==null)return 0;if(i==0)return id==null?0:editing(h)||!idle(h)?2:1;
                var e=live(h,s);CompoundTag data=e==null?saved(cargo(h,s)).getCompound("ForgeData"):e.getPersistentData();
                int cap=data.contains("sbwdroneconfigMaxEnergy")?data.getInt("sbwdroneconfigMaxEnergy"):h instanceof HxPlatformBlockEntity?Hx2Config.CAPACITY.get():AddonConfig.maxEnergy();
                int energy=data.contains("sbwdroneconfigCurrentEnergy")?data.getInt("sbwdroneconfigCurrentEnergy"):cap;
                int value=i<=2?energy:cap;return i%2==1?value&65535:value>>>16;
            }
            public void set(int i,int value){}public int getCount(){return 5;}
        };
    }
    static final class Editor {
        final Object host;final int bay;final UUID id;final ItemStack item;final Entity drone;final ServerPlayer player;final boolean stored;
        boolean closed;
        Editor(Object h,int s,ServerPlayer p){host=h;bay=s;id=identity(h,s);item=cargo(h,s);player=p;Entity e=live(h,s);stored=e==null;
            if(e==null){
                if(h instanceof LucasPlatformBlockEntity b)e=b.createDrone();
                else if(h instanceof GeraniumPlatformBlockEntity b)e=b.createDrone();
                else if(h instanceof GlidePlatformBlockEntity b)e=b.createDrone();
                else if(h instanceof Fl01PlatformBlockEntity b)e=b.createDrone();
                else if(h instanceof Ndc01Entity n)e=n.inventoryDrone(s,p);
            }
            drone=e;
        }
        boolean payload(){return drone!=null&&drone.getClass()==CubedFpvDroneEntity.class;}
        boolean valid(){return !closed&&access(host,player)&&idle(host)&&Objects.equals(id,identity(host,bay))&&(!stored||cargo(host,bay)==item)&&drone!=null&&drone.isAlive()&&!drone.isRemoved()&&(!(drone instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v)||!v.isWreck());}
        void save(){
            if(!valid())return;
            if(stored){var data=new CompoundTag();drone.saveWithoutId(data);if(data.contains("Item",10)&&data.getCompound("Item").isEmpty())data.remove("Item");for(String k:new String[]{"UUID","Pos","Motion","Rotation","Passengers","Leash","Dimension"})data.remove(k);
                var n=PlatformMonitor.tag(item);n.put(drone instanceof Id01Entity?"Id01Saved":"EntityTag",data);
                if(drone instanceof Fl01Entity f){n.putInt("Fuel",f.fuel());n.putBoolean("Pink",f.pink());}
                PlatformMonitor.save(item,n);
            }
            if(host instanceof BlockEntity b)b.setChanged();
            if(host instanceof Ndc01Entity n)n.inventoryChanged(bay);
        }
        void refreshHost(){if(host instanceof MissionPlatform m)m.missionSync();}
        void back(){
            if(!access(host,player))return;
            if(host instanceof LucasPlatformBlockEntity b)b.open(player);
            else if(host instanceof GeraniumPlatformBlockEntity b)b.open(player);
            else if(host instanceof GlidePlatformBlockEntity b)b.open(player);
            else if(host instanceof Fl01PlatformBlockEntity b)b.open(player);
            else if(host instanceof HxPlatformBlockEntity b)HxPlatformMenu.open(player,b);
            else if(host instanceof Ndc01Entity n)n.openBays(player);
            else if(host instanceof Dc01Entity c)c.openDocks(player);
        }
    }
}
