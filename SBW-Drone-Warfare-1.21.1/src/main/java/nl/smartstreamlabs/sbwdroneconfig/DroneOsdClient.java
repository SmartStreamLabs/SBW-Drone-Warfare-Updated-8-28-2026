package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import java.util.*;

@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class DroneOsdClient {
    private static final java.util.Set<String> HIDDEN=java.util.Set.of("crosshair","hotbar","selected_item_name","item_name","experience_bar","experience_level","player_health","armor_level","food_level","air_level","mount_health","jump_meter","vehicle_health");
    private static final Map<UUID,Sample> SAMPLES=new HashMap<>();
    private static Object world;private static long clock;
    static final Orientation MAIN=new Orientation(),PREVIEW=new Orientation();
    static final String UNKNOWN="—";
    private DroneOsdClient(){}
    private static void world(){var m=Minecraft.getInstance();if(world!=m.level){for(var s:SAMPLES.values())s.ground.clear();SAMPLES.clear();world=m.level;MAIN.id=PREVIEW.id=null;}}
    static boolean showsSignalLost(UUID id){var s=SAMPLES.get(id);var d=s==null?null:s.fresh();return DroneOsdConfig.VISIBLE.get()&&d!=null&&d.jammed();}
    public static Entity fullView(){return DroneThermalVisionClient.fullView();}
    public static boolean ownsFullView(){return !PortableFpvDetectorRenderContext.isOffscreenRendering()&&fullView()!=null&&fullView()!=Minecraft.getInstance().player;}
    private static boolean watched(UUID id){
        var m=Minecraft.getInstance();Entity full=fullView();var preview=DroneJammerClient.thermalPreviewTarget();
        return full!=null&&id.equals(full.getUUID())||preview!=null&&id.equals(preview.droneId())||m.player!=null&&SbwCompat.isUsingLinkedMonitor(SbwCompat.getActiveLinkedMonitor(m.player))&&id.equals(SbwCompat.getLinkedDroneUuid(SbwCompat.getActiveLinkedMonitor(m.player)));
    }
    public static void receive(DroneOsdTelemetry data){
        world();var m=Minecraft.getInstance();if(m.level==null||!data.dimension().equals(m.level.dimension().location().toString())||!watched(data.drone()))return;
        if(!SAMPLES.containsKey(data.drone())&&SAMPLES.size()>=2){for(var s:SAMPLES.values())s.ground.clear();SAMPLES.clear();}
        sample(data.drone()).accept(data);
    }
    private static Sample sample(UUID id){return SAMPLES.computeIfAbsent(id,Sample::new);}
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
        world();clock++;
        var m=Minecraft.getInstance();Entity full=fullView();var target=DroneJammerClient.thermalPreviewTarget();Entity preview=m.level==null||target==null?null:m.level.getEntity(target.entityId());
        if(full!=null)sample(full.getUUID()).update(full);
        if(preview!=null&&preview!=full)sample(preview.getUUID()).update(preview);
        var it=SAMPLES.entrySet().iterator();while(it.hasNext()){var entry=it.next();if((full==null||!entry.getKey().equals(full.getUUID()))&&(preview==null||!entry.getKey().equals(preview.getUUID()))){entry.getValue().ground.clear();it.remove();}}
        if(full==null)MAIN.id=null;if(preview==null)PREVIEW.id=null;
    }
    @net.neoforged.bus.api.SubscribeEvent public static void vanilla(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre e){if(ownsFullView()&&e.getName()!=null&&HIDDEN.contains(e.getName().getPath()))e.setCanceled(true);}
    @net.neoforged.bus.api.SubscribeEvent public static void hands(net.neoforged.neoforge.client.event.RenderHandEvent e){if(ownsFullView())e.setCanceled(true);}
    /** Captures the actual world view matrix, after camera effects and custom perspectives. */
    public static void capture(Matrix4f view,Matrix4f projection){
        world();boolean preview=PortableFpvDetectorRenderContext.isOffscreenRendering();var m=Minecraft.getInstance();Entity e=preview?m.getCameraEntity():fullView();
        if(e!=null)(preview?PREVIEW:MAIN).set(e.getUUID(),view,projection,m.gameRenderer.getMainCamera().getPosition());
    }
    @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event){
        var m=Minecraft.getInstance();if(m.screen!=null||m.options.hideGui||!DroneOsdConfig.VISIBLE.get())return;
        Entity e=fullView();if(e==null||e==m.player)return;var s=sample(e.getUUID());if(s.entity==null)s.update(e);
        DroneOsdRenderer.render(event.getGuiGraphics(),s,MAIN,0,0,event.getGuiGraphics().guiWidth(),event.getGuiGraphics().guiHeight(),false);
        if(e instanceof Fl01Entity fl){event.getGuiGraphics().drawString(m.font,"FL-01 | "+(fl.manual()?"MANUAL":"AUTOPILOT")+" | Fuel: "+fl.fuel(),12,90,0xffffc187);if(fl.manual())event.getGuiGraphics().drawString(m.font,"W/S speed | A/D turn | Mouse steer",12,102,0xffc5e4d5);}else if(e instanceof Gad01Entity)event.getGuiGraphics().drawString(m.font,"Hold "+VatagClient.gadDetonateKey()+" for 1s: detonate loaded mines (A/B are cargo bays)",12,90,0xffffc187);else if(e instanceof Wd01Entity)event.getGuiGraphics().drawString(m.font,"Hold "+VatagClient.waterDetonateKey()+" for 1s: self-destruct",12,90,0xffffc187);else if(e instanceof SupplyRoverEntity)event.getGuiGraphics().drawString(m.font,VatagClient.hitchKey()+": couple / uncouple MT-01 (stop and align hitches)",12,90,0xffc5e4d5);
    }
    public static void preview(GuiGraphics g,PortableFpvDetectorTarget target,int x,int y,int w,int h){
        var m=Minecraft.getInstance();if(m.options.hideGui||!DroneOsdConfig.VISIBLE.get()||m.level==null)return;
        Entity e=m.level.getEntity(target.entityId());if(e==null||!target.droneId().equals(e.getUUID()))return;
        var s=sample(e.getUUID());if(s.entity==null)s.update(e);DroneOsdRenderer.render(g,s,PREVIEW,x,y,w,h,true);
    }
    static String perspective(Entity e){var m=Minecraft.getInstance();if(e==VatagClient.vehicle()&&VatagClient.active())return VatagClient.osdCamera();if(e==Rd01Client.vehicle()&&Rd01Client.active())return Rd01Client.osdCamera();if(e==Id01Client.vehicle()&&Id01Client.active())return new String[]{"LENS","FOLLOW","OVERHEAD"}[Id01Client.view()];return m.options.getCameraType().isFirstPerson()?"LENS":m.options.getCameraType().isMirrored()?"FRONT":"FOLLOW";}
    static final class Orientation {
        UUID id;final Matrix4f view=new Matrix4f(),projection=new Matrix4f();Vec3 position=Vec3.ZERO;
        double heading=Double.NaN,pitch,roll;String headingText=UNKNOWN;int lastHeading=-1;
        void set(UUID id,Matrix4f v,Matrix4f p,Vec3 position){
            if(!java.util.Objects.equals(this.id,id)){heading=Double.NaN;lastHeading=-2;headingText=UNKNOWN;}
            this.id=id;view.set(v);projection.set(p);this.position=position;
            if(Math.hypot(v.m02(),v.m22())>.00001)heading=DroneOsdMath.heading(-v.m02(),-v.m22());
            pitch=Math.toDegrees(Math.atan2(v.m12(),Math.hypot(v.m10(),v.m11())));roll=Math.toDegrees(Math.atan2(v.m10(),v.m11()));
            int h=Double.isFinite(heading)?Math.floorMod((int)Math.round(heading),360):-1;
            if(h!=lastHeading){lastHeading=h;headingText=h<0?UNKNOWN:String.format(Locale.ROOT,"%03d° %s",h,DroneOsdMath.cardinal(h));}
        }
    }
    static final class Sample {
        final Entity[] nativeTargets=new Entity[16];int nativeTargetCount;long lastTargets=-100;
        final UUID id;final DroneOsdGround ground=new DroneOsdGround();Entity entity;DroneOsdTelemetry data;
        long received=-100,lastTime=-1,lastGround=-100;Vec3 previous;int bx=Integer.MIN_VALUE,by,bz;
        double speed=Double.NaN,surface=Double.NaN,agl=Double.NaN,distance=Double.NaN;boolean lowEnergy,lowFuel,nearRange;int energyClear,fuelClear;
        String name=UNKNOWN,alt="AGL "+UNKNOWN,velocity="SPD "+UNKNOWN,dist="DIST "+UNKNOWN,power="",shortPower="",coordinates="",fuel="",link="LINK "+UNKNOWN,range="RANGE "+UNKNOWN,mode=UNKNOWN,payload="",health="",bodyHeading="",warning="";
        final String[] xyz={"X —","Y —","Z —"};
        Sample(UUID id){this.id=id;}
        void accept(DroneOsdTelemetry d){data=d;received=clock;if(d.lowEnergy()){lowEnergy=true;energyClear=0;}else if(++energyClear>=3)lowEnergy=false;if(d.lowFuel()){lowFuel=true;fuelClear=0;}else if(++fuelClear>=3)lowFuel=false;}
        DroneOsdTelemetry fresh(){return data!=null&&clock-received<=40?data:null;}
        void update(Entity e){
            entity=e;long t=e.level().getGameTime();Vec3 at=e.position();
            if(previous!=null&&lastTime<t){double dx=at.x-previous.x,dz=at.z-previous.z;speed=Math.hypot(dx,dz)>64?Double.NaN:DroneOsdMath.speed(dx,dz,t-lastTime);}else if(lastTime<0)speed=Double.NaN;
            previous=at;lastTime=t;int x=(int)Math.floor(at.x),y=(int)Math.floor(at.y),z=(int)Math.floor(at.z);
            if(e instanceof Sd01Entity){agl=Double.NaN;}else{if(x!=bx||y!=by||z!=bz||clock-lastGround>=5){surface=ground.surface(e.level(),e instanceof Hgd01Entity?at.add(0,.25,0):at);bx=x;by=y;bz=z;lastGround=clock;}
            agl=Double.isFinite(surface)?Math.max(0,at.y-surface):Double.NaN;}
            name=e.getName().getString().toUpperCase(Locale.ROOT);alt="AGL "+number(agl," m");velocity="SPD "+number(speed," m/s");
            xyz[0]=String.format(Locale.ROOT,"X %.1f",at.x);xyz[1]=String.format(Locale.ROOT,"Y %.1f",at.y);xyz[2]=String.format(Locale.ROOT,"Z %.1f",at.z);
            coordinates=String.format(Locale.ROOT,"X%d Y%d Z%d",x,y,z);
            double hdg=DroneOsdMath.normalize(e.getYRot()+180);bodyHeading=String.format(Locale.ROOT,"DRN %03d° %s",Math.floorMod((int)Math.round(hdg),360),DroneOsdMath.cardinal(hdg));
            health=e instanceof Hgd01Entity h?String.format(Locale.ROOT,"HP %.0f/%.0f",h.getHealth(),h.getMaxHealth()):e instanceof LivingEntity l?String.format(Locale.ROOT,"HP %.0f/%.0f",l.getHealth(),l.getMaxHealth()):"";
            if(clock-lastTargets>=5){nativeTargetCount=0;java.util.Arrays.fill(nativeTargets,null);if(SbwDroneRangeConfig.DRONE_ENTITY_ID.equals(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString()))for(Entity target:com.atsuishio.superbwarfare.tools.SeekTool.seekLivingEntities(e,256,30)){if(nativeTargetCount==16)break;nativeTargets[nativeTargetCount++]=target;}lastTargets=clock;}
            if(DroneSpotlightClient.osdActive(id))health+=" / LIGHT";
            var d=fresh();power=shortPower=fuel=payload=warning="";distance=Double.NaN;mode=UNKNOWN;link="LINK "+UNKNOWN;range="RANGE "+UNKNOWN;
            if(d!=null){
                distance=at.distanceTo(new Vec3(d.originX(),d.originY(),d.originZ()));mode=d.mode();payload=d.payload();
                power=d.capacity()>0&&d.energy()>=0?String.format(Locale.ROOT,"BAT %.0f%% %s/%s FE",Math.max(0,Math.min(100,d.energy()*100.0/d.capacity())),energy(d.energy()),energy(d.capacity())):"";
                shortPower=d.capacity()>0&&d.energy()>=0?String.format(Locale.ROOT,"BAT%.0f%%",Math.max(0,Math.min(100,d.energy()*100.0/d.capacity()))):"";
                fuel=Float.isFinite(d.fuel())?String.format(Locale.ROOT,"FUEL %.0f%%",d.fuel()):"";
                if(!fuel.isEmpty())shortPower=shortPower.isEmpty()?fuel.replace(" ",""):shortPower+" "+fuel.replace(" ","");
                link=d.link()+(d.jammed()?" / LOST":Float.isFinite(d.signal())?String.format(Locale.ROOT," %.0f%%",d.signal()*100):" / CONNECTED");
                range=(d.link().equals("FIBER")?"CABLE LIMIT ":"RANGE ")+number(d.range()," m");
                if(!Double.isFinite(d.range()))nearRange=false;else if(distance>=d.range()*.9)nearRange=true;else if(distance<d.range()*.85)nearRange=false;
                warning=d.jammed()?"SIGNAL LOST":lowEnergy?"LOW BATTERY":lowFuel?"LOW FUEL":nearRange?"NEAR RANGE":"";
            }else warning="TELEMETRY "+UNKNOWN;
            if(e instanceof Sd01Entity sd){alt=Sd01Client.depth(sd);velocity=String.format(Locale.ROOT,"SPD %.1f m/s",sd.telemetrySpeed());}
            dist="DIST "+number(distance," m");
        }
        private static String energy(int n){return n<10000?Integer.toString(n):String.format(Locale.ROOT,n<1000000?"%.1fk":"%.1fM",n/(n<1000000?1000.0:1000000.0));}
        private static String number(double n,String suffix){return Double.isFinite(n)?String.format(Locale.ROOT,"%.1f%s",n,suffix):UNKNOWN;}
    }
}
