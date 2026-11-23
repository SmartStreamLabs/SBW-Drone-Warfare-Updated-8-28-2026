package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.*;
import org.joml.Matrix4f;import org.joml.Vector3d;import org.joml.Vector4f;

/** Crew-only controls/HUD. All driving, mouse aiming, fire and reload remain native SBW inputs. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class Adv01Client {
    private static KeyMapping key(String name,int code){return new KeyMapping("key.sbwdroneconfig.adv01_"+name,code,"key.categories.sbwdroneconfig");}
    public static final KeyMapping AUTO=key("auto",org.lwjgl.glfw.GLFW.GLFW_KEY_G),RADAR=key("radar",org.lwjgl.glfw.GLFW.GLFW_KEY_B),TARGET=key("target",org.lwjgl.glfw.GLFW.GLFW_KEY_Y),CAMERA=key("camera",org.lwjgl.glfw.GLFW.GLFW_KEY_I),ZOOM=key("zoom",org.lwjgl.glfw.GLFW.GLFW_KEY_O),LIGHTS=key("lights",org.lwjgl.glfw.GLFW.GLFW_KEY_L);
    private static final Matrix4f VIEW=new Matrix4f(),PROJECTION=new Matrix4f();
    private static final Vector4f POINT=new Vector4f();
    private static Vec3 camera=Vec3.ZERO;private static int vehicle=-1,zoom=1;private static boolean optics=true,captured;
    private static void discardClicks(){for(var key:new KeyMapping[]{AUTO,RADAR,TARGET,CAMERA,ZOOM,LIGHTS})while(key.consumeClick()){} }
    public static Adv01Entity vehicle(){var m=Minecraft.getInstance();return m.player!=null&&m.player.getVehicle() instanceof Adv01Entity a?a:null;}
    public static boolean lens(){var m=Minecraft.getInstance();return optics&&vehicle()!=null&&m.options.getCameraType().isFirstPerson();}
    public static Vec3 lensPosition(float partial){var a=vehicle();if(a==null)return null;var matrix=a.cameraFrame(a.getSeatIndex(Minecraft.getInstance().player),partial);return Adv01Entity.vec(matrix.transformPosition(new Vector3d(0,0,.06)));}
    public static float[] lensRotation(float partial){var a=vehicle();var d=a.cameraFrame(a.getSeatIndex(Minecraft.getInstance().player),partial).transformDirection(new Vector3d(0,0,1)).normalize();return new float[]{(float)Math.toDegrees(-Math.atan2(d.x,d.z)),(float)Math.toDegrees(-Math.asin(Math.max(-1,Math.min(1,d.y))))};}
    public static void capture(Matrix4f view,Matrix4f projection){if(vehicle()==null||PortableFpvDetectorRenderContext.isOffscreenRendering())return;VIEW.set(view);PROJECTION.set(projection);camera=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();captured=true;}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){
        if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;var a=vehicle();if(a==null&&VatagClient.active()&&VatagClient.vehicle() instanceof Adv01Entity remote)a=remote;var m=Minecraft.getInstance();
        if(a==null){discardClicks();vehicle=-1;zoom=1;optics=true;captured=false;return;}
        if(vehicle!=a.getId()){vehicle=a.getId();zoom=1;optics=true;captured=false;}
        if(m.screen!=null){discardClicks();return;}
        while(AUTO.consumeClick())AddonNetwork.adv01Control(new Adv01Control(a.getId(),0));
        while(RADAR.consumeClick())AddonNetwork.adv01Control(new Adv01Control(a.getId(),1));
        while(TARGET.consumeClick())AddonNetwork.adv01Control(new Adv01Control(a.getId(),2));
        while(LIGHTS.consumeClick())AddonNetwork.adv01Control(new Adv01Control(a.getId(),3));
        while(CAMERA.consumeClick()){optics=!optics;m.player.displayClientMessage(Component.translatable(optics?"adv01.optics":"adv01.cabin"),true);}
        while(ZOOM.consumeClick())zoom=zoom==1?2:zoom==2?4:1;
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void fov(net.minecraftforge.client.event.ViewportEvent.ComputeFov e){if(lens())e.setFOV(e.getFOV()/zoom);}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void hand(net.minecraftforge.client.event.RenderHandEvent e){if(lens())e.setCanceled(true);}
    private static void text(GuiGraphics g,String s,int x,int y,int color){g.drawString(Minecraft.getInstance().font,s,x,y,color);}
    private static String tr(String key){return Component.translatable(key).getString();}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void hud(net.minecraftforge.client.event.RenderGuiEvent.Post event){
        var m=Minecraft.getInstance();var a=vehicle();if(a==null||m.screen!=null||m.options.hideGui)return;
        var g=event.getGuiGraphics();int width=g.guiWidth(),height=g.guiHeight();var gun=a.getGunData("Cannon");
        int x=8,y=8,w=Math.min(246,width-16);g.fill(x-3,y-3,x+w,y+65,0xb0121e1b);
        text(g,"ADV-01 | "+tr(a.automatic()?"adv01.auto":"adv01.manual"),x,y,0xd7e5cc);
        text(g,String.format(java.util.Locale.ROOT,"HP %.0f/%.0f | %.1f km/h",a.getHealth(),a.getMaxHealth(),a.getDeltaMovement().horizontalDistance()*72),x,y+12,0xffffff);
        text(g,tr("adv01.ammo")+": "+(gun==null?0:gun.ammo.get())+" / "+(gun==null?0:gun.get(com.atsuishio.superbwarfare.data.gun.GunProp.MAGAZINE))+(gun!=null&&gun.reloading()?" | "+tr("adv01.reloading"):""),x,y+24,0xffffff);
        text(g,tr("adv01.heat")+": "+a.temperature()+"% | "+tr(a.radar()?"adv01.radar_on":"adv01.radar_off"),x,y+36,a.temperature()>=90?0xff8d68:0xd7e5cc);
        text(g,tr(a.status()),x,y+48,0xffd38a);
        var contacts=a.contacts().getList("Entries",Tag.TAG_COMPOUND);int right=Math.max(8,width-220),row=85,count=0;
        for(int i=0;i<contacts.size();i++){
            var n=contacts.getCompound(i);Entity entity=m.level.getEntity(n.getInt("Id"));
            if(entity!=null&&(!entity.isAlive()||entity.isRemoved()||!entity.getUUID().equals(n.getUUID("UUID"))))continue;
            int relation=n.getInt("Relation"),color=relation==Adv01Targets.HOSTILE?0xffa18b:relation==Adv01Targets.FRIEND?0x8cd5b0:0xaeb9ba;
            String name=m.font.plainSubstrByWidth(n.getString("Name"),125);boolean selected=n.getUUID("UUID").toString().equals(a.selected());
            if(count++<6){text(g,(selected?"> ":"  ")+name,right,row,color);text(g,String.format(java.util.Locale.ROOT,"%.0f m | %+.0f m | %s",n.getDouble("Distance"),n.getDouble("Height"),tr(relation==Adv01Targets.HOSTILE?"adv01.hostile":relation==Adv01Targets.FRIEND?"adv01.friendly":"adv01.unknown")),right,row+10,color);row+=24;}
            if(captured&&n.getBoolean("Visible")){
                Vec3 at=entity==null?new Vec3(n.getDouble("X"),n.getDouble("Y"),n.getDouble("Z")):entity.getPosition(m.getFrameTime()).add(0,entity.getBbHeight()*.5,0);
                POINT.set((float)(at.x-camera.x),(float)(at.y-camera.y),(float)(at.z-camera.z),1);VIEW.transform(POINT);PROJECTION.transform(POINT);
                if(POINT.w>0){float xx=POINT.x/POINT.w,yy=POINT.y/POINT.w,zz=POINT.z/POINT.w;if(Math.abs(xx)<=1&&Math.abs(yy)<=1&&Math.abs(zz)<=1){int sx=Math.round((xx*.5f+.5f)*width),sy=Math.round((.5f-yy*.5f)*height),r=selected?9:6;int c=0xff000000|color;g.fill(sx-r,sy-r,sx-r+4,sy-r+1,c);g.fill(sx-r,sy-r,sx-r+1,sy-r+4,c);g.fill(sx+r-4,sy-r,sx+r,sy-r+1,c);g.fill(sx+r-1,sy-r,sx+r,sy-r+4,c);g.fill(sx-r,sy+r-1,sx-r+4,sy+r,c);g.fill(sx+r-4,sy+r-1,sx+r,sy+r,c);}}
            }
        }
        if(lens()){
            int cx=width/2,cy=height/2;g.fill(cx-15,cy,cx-4,cy+1,0xffc9e7ba);g.fill(cx+4,cy,cx+15,cy+1,0xffc9e7ba);g.fill(cx,cy-15,cx+1,cy-4,0xffc9e7ba);g.fill(cx,cy+4,cx+1,cy+15,0xffc9e7ba);
            text(g,zoom+"x | "+tr(a.getSeatIndex(m.player)==0?"adv01.driver":"adv01.gunner"),cx-30,height-62,0xe3ebdc);
        }
        text(g,AUTO.getTranslatedKeyMessage().getString()+" "+tr("adv01.mode")+" | "+RADAR.getTranslatedKeyMessage().getString()+" radar | "+TARGET.getTranslatedKeyMessage().getString()+" "+tr("adv01.target"),8,height-42,0xe3ebdc);
        text(g,CAMERA.getTranslatedKeyMessage().getString()+" "+tr("adv01.camera")+" | "+ZOOM.getTranslatedKeyMessage().getString()+" zoom | "+LIGHTS.getTranslatedKeyMessage().getString()+" "+tr("adv01.lights"),8,height-30,0xe3ebdc);
    }
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT,bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
    public static final class Keys {
        @net.minecraftforge.eventbus.api.SubscribeEvent public static void registerAdv01ClientKeys(net.minecraftforge.client.event.RegisterKeyMappingsEvent e){for(var k:new KeyMapping[]{AUTO,RADAR,TARGET,CAMERA,ZOOM,LIGHTS})e.register(k);}
    }
    private Adv01Client(){}
}
