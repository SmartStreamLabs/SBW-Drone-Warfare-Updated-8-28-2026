package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import java.util.*;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class Id01Client {
    private static UUID token;private static Id01State snapshot;private static long sequence;
    private static net.minecraft.client.multiplayer.ClientLevel world;private static CameraType original;
    private static net.minecraft.world.entity.Entity camera;private static int waiting,returnTicks,view;
    private static float yaw,orbitYaw,orbitPitch=30;private static boolean perspectiveHeld,launchArmed;
    private static final KeyMapping LAUNCH=new KeyMapping("key.sbwdroneconfig.id01_launch",org.lwjgl.glfw.GLFW.GLFW_KEY_L,"key.categories.sbwdroneconfig");
    private static final KeyMapping RADAR=new KeyMapping("key.sbwdroneconfig.id01_radar",org.lwjgl.glfw.GLFW.GLFW_KEY_R,"key.categories.sbwdroneconfig");
    private static final KeyMapping RETURN=new KeyMapping("key.sbwdroneconfig.id01_return",org.lwjgl.glfw.GLFW.GLFW_KEY_H,"key.categories.sbwdroneconfig");
    private static final KeyMapping MANUAL=new KeyMapping("key.sbwdroneconfig.id01_manual",org.lwjgl.glfw.GLFW.GLFW_KEY_M,"key.categories.sbwdroneconfig");
    public static boolean active(){return token!=null;}
    public static boolean holdBodyPrediction(){return active()||returnTicks>0;}
    public static Id01Entity vehicle(){var m=Minecraft.getInstance();return m.level!=null&&snapshot!=null&&m.level.getEntity(snapshot.entity()) instanceof Id01Entity d?d:null;}
    public static Id01State snapshot(){return snapshot;}
    public static int view(){return view;}
    public static void state(Id01State s){
        if(!s.active()){if(s.token().equals(token))clear(false);return;}
        if(s.token().equals(token)){snapshot=s;return;}
        var nativeCamera=PlatformPilotClient.originalCamera();clear(false);var m=Minecraft.getInstance();snapshot=s;token=s.token();sequence=0;world=m.level;original=nativeCamera==null?m.options.getCameraType():nativeCamera;m.options.setCameraType(CameraType.FIRST_PERSON);waiting=0;view=0;orbitYaw=0;orbitPitch=30;perspectiveHeld=false;launchArmed=false;var d=vehicle();yaw=d==null?0:d.getYRot();
        // Do not turn an old advancements-key press or a held key into a new launch.
        for(var key:new KeyMapping[]{LAUNCH,RADAR,RETURN,MANUAL})while(key.consumeClick()){}
    }
    public static void action(int action,UUID target){if(active())AddonNetwork.id01Input(new Id01Input(action,token,++sequence,0,yaw,target==null?Id01Input.NONE:target));}
    private static void clear(boolean notify){
        var m=Minecraft.getInstance();if(notify&&active()&&m.getConnection()!=null)action(2,null);
        returnTicks=active()&&m.player!=null&&m.level==world?100:0;if(returnTicks==0)world=null;token=null;snapshot=null;camera=null;
        if(m.player!=null)m.setCameraEntity(m.player);if(original!=null)m.options.setCameraType(original);original=null;
        if(m.screen instanceof Id01RadarScreen)m.setScreen(null);
    }
    public static void cycleView(){if(perspectiveHeld)return;perspectiveHeld=true;view=(view+1)%3;orbitYaw=0;orbitPitch=view==2?78:30;var m=Minecraft.getInstance();m.options.setCameraType(CameraType.FIRST_PERSON);if(m.player!=null)m.player.displayClientMessage(net.minecraft.network.chat.Component.literal("ID-01: "+new String[]{"Lens","Follow","Overhead"}[view]),true);}
    public static void look(double dx,double dy){
        var m=Minecraft.getInstance();double f=m.options.sensitivity().get()*.6+.2;float gain=(float)(f*f*f*8*.15);
        if(view!=0){orbitYaw+=(float)dx*gain;orbitPitch=Mth.clamp(orbitPitch+(float)dy*gain,5,89);}
    }
    public static Vec3 viewPosition(float partial){var d=vehicle();if(d==null)return null;if(view==0)return Id01Pose.lens(d,partial);Vec3 target=d.getPosition(partial).add(0,Id01Pose.ROOT_HEIGHT,0);float y=Mth.rotLerp(partial,d.yRotO,d.getYRot())+orbitYaw;return RoverViewCollision.clip(d,target,target.subtract(Vec3.directionFromRotation(orbitPitch,y).scale(view==2?12:7)));}
    public static float[] viewAngles(float partial){var d=vehicle();if(view==0){Vec3 dir=Id01Pose.direction(d,partial,new Vec3(0,0,1)).normalize();return new float[]{(float)Math.toDegrees(-Math.atan2(dir.x,dir.z)),(float)-Math.toDegrees(Math.asin(Mth.clamp(dir.y,-1,1)))};}return new float[]{Mth.rotLerp(partial,d.yRotO,d.getYRot())+orbitYaw,orbitPitch};}
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
        var m=Minecraft.getInstance();if(returnTicks>0&&(m.player==null||m.level!=world||--returnTicks<=0||m.level.hasChunkAt(m.player.blockPosition())))returnTicks=0;
        if(!active()){if(returnTicks==0)world=null;return;}
        if(m.player==null||m.level!=world||!m.player.isAlive()||!m.isWindowActive()||m.screen!=null&&!(m.screen instanceof Id01RadarScreen)&&!(m.screen instanceof CarrierFleetScreen)||!SbwCompat.isMonitor(m.player.getMainHandItem())){clear(true);return;}
        m.options.setCameraType(CameraType.FIRST_PERSON);if(!m.options.keyTogglePerspective.isDown())perspectiveHeld=false;
        var d=vehicle();if(d==null){if(++waiting>200)clear(true);else AddonNetwork.id01Input(new Id01Input(1,token,++sequence,0,yaw,Id01Input.NONE));return;}
        if(waiting>0)yaw=d.getYRot();waiting=0;if(d.isRemoved()||!d.isAlive()||d.isWreck()){clear(true);return;}
        if(!LAUNCH.isDown())launchArmed=true;
        while(LAUNCH.consumeClick()){}
        while(RETURN.consumeClick())action(6,null);while(MANUAL.consumeClick()){}
        while(RADAR.consumeClick()){action(4,null);m.setScreen(new Id01RadarScreen());}
        int keys=0; // observation never supplies flight controls
        AddonNetwork.id01Input(new Id01Input(1,token,++sequence,keys,yaw,Id01Input.NONE));
    }
    @net.neoforged.bus.api.SubscribeEvent public static void movement(net.neoforged.neoforge.client.event.MovementInputUpdateEvent e){if(!active())return;var i=e.getInput();i.forwardImpulse=0;i.leftImpulse=0;i.jumping=false;i.shiftKeyDown=false;}
    @net.neoforged.bus.api.SubscribeEvent public static void hands(net.neoforged.neoforge.client.event.RenderHandEvent e){if(active())e.setCanceled(true);}
    @net.neoforged.bus.api.SubscribeEvent public static void render(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre e){if(!active())return;
        var m=Minecraft.getInstance();var d=vehicle();if(d==null||m.player==null||m.level!=world)return;float partial=Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);Vec3 at=viewPosition(partial);float[] a=viewAngles(partial);
        if(camera==null)camera=net.minecraft.world.entity.EntityType.MARKER.create(m.level);if(camera==null)return;camera.setPos(at.x,at.y-camera.getEyeHeight(),at.z);camera.setYRot(a[0]);camera.setXRot(a[1]);camera.xo=camera.getX();camera.yo=camera.getY();camera.zo=camera.getZ();camera.yRotO=a[0];camera.xRotO=a[1];m.setCameraEntity(camera);
    }
    @net.neoforged.bus.api.SubscribeEvent public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post e){ if(DroneOsdClient.ownsFullView())return;
        if(!active())return;var m=Minecraft.getInstance();var d=vehicle();var g=e.getGuiGraphics();float scale=Math.min(1,m.getWindow().getGuiScaledWidth()/650f);g.pose().pushPose();g.pose().scale(scale,scale,1);g.fill(6,6,405,80,0xb0000000);
        g.drawString(m.font,"ID-01 | "+(d==null?"CONNECTING":d.status()),12,11,0x97dca4);
        if(d!=null)g.drawString(m.font,String.format(Locale.ROOT,"%.1f km/h | FE %,d | CONNECTED | %s",d.getDeltaMovement().length()*72,d.energy(),new String[]{"LENS","FOLLOW","OVERHEAD"}[view]),12,24,0xffffff);
        g.drawString(m.font,snapshot.target().isEmpty()?"TARGET: none":"TARGET: "+snapshot.target()+String.format(Locale.ROOT,"  %.0f m",snapshot.distance()),12,37,0xffffff);
        g.drawString(m.font,"AUTOMATIC | "+RADAR.getTranslatedKeyMessage().getString()+" select target and launch",12,50,0xdddddd);
        g.drawString(m.font,RETURN.getTranslatedKeyMessage().getString()+" end mission | "+m.options.keyTogglePerspective.getTranslatedKeyMessage().getString()+" camera | Esc exit",12,63,0xdddddd);g.pose().popPose();
    }
    @net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
    public static final class Keys {@net.neoforged.bus.api.SubscribeEvent public static void registerId01ClientKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(RADAR);e.register(RETURN);}}
}
