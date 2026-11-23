package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import java.util.UUID;

@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class Rd01Client {
    private static UUID token;
    private static Rd01State snapshot;
    private static long sequence;
    private static net.minecraft.client.multiplayer.ClientLevel world;
    private static CameraType original;
    private static net.minecraft.world.entity.Entity camera;
    private static int waiting,returnTicks,view;
    private static float yaw,orbitYaw,orbitPitch=25;
    private static boolean perspectiveHeld;
    private static final KeyMapping RAISE=new KeyMapping("key.sbwdroneconfig.rd01_raise",org.lwjgl.glfw.GLFW.GLFW_KEY_R,"key.categories.sbwdroneconfig");
    private static final KeyMapping LOWER=new KeyMapping("key.sbwdroneconfig.rd01_lower",org.lwjgl.glfw.GLFW.GLFW_KEY_F,"key.categories.sbwdroneconfig");
    private static final KeyMapping RELEASE=new KeyMapping("key.sbwdroneconfig.rd01_release",org.lwjgl.glfw.GLFW.GLFW_KEY_G,"key.categories.sbwdroneconfig");
    /** Reserved winch/release mappings belong exclusively to the active monitor session. */
    public static boolean blocksMapping(KeyMapping key){return active()&&key!=RAISE&&key!=LOWER&&key!=RELEASE&&(key.same(RAISE)||key.same(LOWER)||key.same(RELEASE));}
    public static String osdCamera(){return new String[]{"LENS","FOLLOW","OVERHEAD"}[view];}
    public static boolean active(){return token!=null;}
    public static boolean holdBodyPrediction(){return active()||returnTicks>0;}
    public static Rd01Entity vehicle(){var m=Minecraft.getInstance();return m.level!=null&&snapshot!=null&&m.level.getEntity(snapshot.entity()) instanceof Rd01Entity d?d:null;}
    public static int view(){return view;}
    public static void state(Rd01State s) {
        if(!s.active()){if(s.token().equals(token))clear(false);return;}
        if(s.token().equals(token)){snapshot=s;return;}
        clear(false);var m=Minecraft.getInstance();snapshot=s;token=s.token();sequence=0;world=m.level;original=m.options.getCameraType();m.options.setCameraType(CameraType.FIRST_PERSON);
        waiting=0;view=0;orbitYaw=0;orbitPitch=25;perspectiveHeld=false;var d=vehicle();yaw=d==null?0:d.getYRot();
        for(KeyMapping key:new KeyMapping[]{RAISE,LOWER,RELEASE})while(key.consumeClick()){}
    }
    private static void clear(boolean notify) {
        var m=Minecraft.getInstance();if(active())SbwDroneRangeConfig.LOGGER.info("RD-01 monitor closing: notify={}, focus={}, screen={}, alive={}, monitor={}, vehicle={}",notify,m.isWindowActive(),m.screen,m.player!=null&&m.player.isAlive(),m.player!=null&&SbwCompat.isMonitor(m.player.getMainHandItem()),vehicle()==null?"absent":vehicle().isAlive()+"/wreck="+vehicle().isWreck());if(notify&&active()&&m.getConnection()!=null)AddonNetwork.rd01Input(new Rd01Input(2,token,++sequence,0,yaw));
        returnTicks=active()&&m.player!=null&&m.level==world?100:0;if(returnTicks==0)world=null;
        token=null;snapshot=null;camera=null;perspectiveHeld=false;
        if(m.player!=null)m.setCameraEntity(m.player);if(original!=null)m.options.setCameraType(original);original=null;
    }
    public static void cycleView() {
        if(perspectiveHeld)return;perspectiveHeld=true;view=(view+1)%3;orbitYaw=0;orbitPitch=view==2?85:25;
        var m=Minecraft.getInstance();m.options.setCameraType(CameraType.FIRST_PERSON);
        if(m.player!=null)m.player.displayClientMessage(net.minecraft.network.chat.Component.translatable("rd01.camera."+view),true);
    }
    public static void look(double dx,double dy) {
        var m=Minecraft.getInstance();double s=m.options.sensitivity().get()*.6+.2;float gain=(float)(s*s*s*8*.15);
        if(view==0)yaw=Mth.wrapDegrees(yaw+(float)Mth.clamp(dx,-256,256)*gain);
        else{orbitYaw+=(float)Mth.clamp(dx,-256,256)*gain;orbitPitch=Mth.clamp(orbitPitch+(float)Mth.clamp(dy,-256,256)*gain,5,89);}
    }
    public static Vec3 viewPosition(float p) {
        Rd01Entity d=vehicle();if(d==null)return null;
        if(view==0)return Rd01Pose.lens(d,p);
        Vec3 target=d.getPosition(p).add(0,Rd01Pose.CENTRE_HEIGHT,0);
        float y=Mth.rotLerp(p,d.yRotO,d.getYRot())+orbitYaw;
        return RoverViewCollision.clip(d,target,target.subtract(Vec3.directionFromRotation(orbitPitch,y).scale(view==2?11:8)));
    }
    public static float[] viewAngles(float p) {
        Rd01Entity d=vehicle();
        if(view==0){var m=Rd01Pose.aircraft(d,p);Vec3 a=Rd01Pose.point(m,0,43.8,-17.5),b=Rd01Pose.point(m,0,43.8,-18.5).subtract(a).normalize();return new float[]{(float)Math.toDegrees(-Math.atan2(b.x,b.z)),(float)-Math.toDegrees(Math.asin(Mth.clamp(b.y,-1,1)))};}
        return new float[]{Mth.rotLerp(p,d.yRotO,d.getYRot())+orbitYaw,orbitPitch};
    }
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e) {
        var m=Minecraft.getInstance();if(returnTicks>0&&(m.player==null||m.level!=world||--returnTicks<=0||m.level.hasChunkAt(m.player.blockPosition())))returnTicks=0;
        if(!active()){if(returnTicks==0)world=null;return;}
        if(m.player==null||m.level!=world||!m.player.isAlive()||!m.isWindowActive()||m.screen!=null&&!PilotMapClient.mapOpen(m.screen)||!SbwCompat.isMonitor(m.player.getMainHandItem())){clear(true);return;}
        if(!m.options.keyTogglePerspective.isDown())perspectiveHeld=false;
        var d=vehicle();if(d==null){if(++waiting>200)clear(true);else AddonNetwork.rd01Input(new Rd01Input(1,token,++sequence,0,yaw));return;}
        if(waiting>0)yaw=d.getYRot();waiting=0;
        if(d.isRemoved()||!d.isAlive()||d.isWreck()){clear(true);return;}
        if(PilotMapClient.mapOpen(m.screen)){while(RELEASE.consumeClick()){}AddonNetwork.rd01Input(new Rd01Input(1,token,++sequence,0,yaw));return;}
        while(RELEASE.consumeClick())AddonNetwork.rd01Input(new Rd01Input(3,token,++sequence,0,yaw));
        int keys=0;var o=m.options;
        if(o.keyLeft.isDown())keys|=1;if(o.keyRight.isDown())keys|=2;if(o.keyUp.isDown())keys|=4;if(o.keyDown.isDown())keys|=8;
        if(o.keyJump.isDown())keys|=16;if(o.keyShift.isDown())keys|=32;
        if(RAISE.isDown())keys|=64;if(LOWER.isDown())keys|=128;
        AddonNetwork.rd01Input(new Rd01Input(1,token,++sequence,keys,yaw));
    }
    @net.neoforged.bus.api.SubscribeEvent public static void movement(net.neoforged.neoforge.client.event.MovementInputUpdateEvent e){if(active()){var i=e.getInput();i.forwardImpulse=i.leftImpulse=0;i.jumping=i.shiftKeyDown=false;}}
    @net.neoforged.bus.api.SubscribeEvent public static void hands(net.neoforged.neoforge.client.event.RenderHandEvent e){if(active())e.setCanceled(true);}
    @net.neoforged.bus.api.SubscribeEvent public static void render(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre e) {
        if(!active())return;
        var m=Minecraft.getInstance();var d=vehicle();if(d==null||m.player==null||m.level!=world)return;
        Vec3 p=viewPosition(e.getPartialTick().getGameTimeDeltaPartialTick(false));float[] a=viewAngles(e.getPartialTick().getGameTimeDeltaPartialTick(false));
        if(camera==null)camera=net.minecraft.world.entity.EntityType.MARKER.create(m.level);if(camera==null)return;
        camera.setPos(p.x,p.y-camera.getEyeHeight(),p.z);camera.setYRot(a[0]);camera.setXRot(a[1]);camera.xo=camera.getX();camera.yo=camera.getY();camera.zo=camera.getZ();camera.yRotO=a[0];camera.xRotO=a[1];m.setCameraEntity(camera);
    }
    @net.neoforged.bus.api.SubscribeEvent public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post e) { if(DroneOsdClient.ownsFullView())return;
        if(!active())return;var m=Minecraft.getInstance();var d=vehicle();var g=e.getGuiGraphics();
        g.fill(6,6,470,70,0xb0000000);g.drawString(m.font,"RESCUE DRONE RD-01 | "+(d==null?"CONNECTING":d.occupied()?"PASSENGER ATTACHED":"HARNESS EMPTY"),12,11,0xb5e8b0);
        g.drawString(m.font,"WASD fly | Space rise | Shift descend | Mouse turn | Esc exit",12,24,0xffffff);
        g.drawString(m.font,RAISE.getTranslatedKeyMessage().getString()+" raise/fold | "+LOWER.getTranslatedKeyMessage().getString()+" lower | "+RELEASE.getTranslatedKeyMessage().getString()+" release on ground",12,37,0xffffff);
        g.drawString(m.font,m.options.keyTogglePerspective.getTranslatedKeyMessage().getString()+" camera | "+new String[]{"LENS","FOLLOW","OVERHEAD"}[view]+(d==null?"":String.format(java.util.Locale.ROOT," | cable %.2f m",d.extension(1))),12,50,0xdddddd);
    }
    @net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
    public static final class Keys {@net.neoforged.bus.api.SubscribeEvent public static void registerRd01ClientKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(RAISE);e.register(LOWER);e.register(RELEASE);}}
}
