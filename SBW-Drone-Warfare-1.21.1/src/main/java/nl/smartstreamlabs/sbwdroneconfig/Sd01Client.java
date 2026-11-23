package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import net.minecraft.client.gui.GuiGraphics;import net.minecraft.network.chat.Component;import java.util.*;
/** Configured keys use the existing authenticated VatagInput sequence/token. No client movement. */
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class Sd01Client {
 private static final KeyMapping SONAR=new KeyMapping("key.sbwdroneconfig.sd01_sonar",org.lwjgl.glfw.GLFW.GLFW_KEY_K,"key.categories.sbwdroneconfig"),LIGHT=new KeyMapping("key.sbwdroneconfig.sd01_lights",org.lwjgl.glfw.GLFW.GLFW_KEY_L,"key.categories.sbwdroneconfig"),BRAKE=new KeyMapping("key.sbwdroneconfig.sd01_brake",org.lwjgl.glfw.GLFW.GLFW_KEY_X,"key.categories.sbwdroneconfig");
 private static boolean sonarHeld,lightHeld;private static String lastSonar="";private static float[] distances=new float[15];
 public static void reset(){sonarHeld=lightHeld=false;lastSonar="";KeyMapping.releaseAll();}
 public static boolean lampKey(int key,int scan){return LIGHT.matches(key,scan);}
 public static int keys(Minecraft m){return (m.options.keyUp.isDown()?1:0)|(m.options.keyDown.isDown()?2:0)|(m.options.keyLeft.isDown()?4:0)|(m.options.keyRight.isDown()?8:0)|(m.options.keyShift.isDown()?16:0)|(BRAKE.isDown()?32:0)|(m.options.keyJump.isDown()?64:0);}
 /** Returns 5/6 once per press; never repeats a pulse/toggle every tick while held. */
 public static int command(){var m=Minecraft.getInstance();boolean a=VatagClient.bindingDown(m,SONAR),b=VatagClient.bindingDown(m,LIGHT);int action=a&&!sonarHeld?5:b&&!lightHeld?6:0;sonarHeld=a;lightHeld=b;return action;}
 public static String depth(Sd01Entity d){return Float.isFinite(d.depth())?String.format(Locale.ROOT,"DEPTH %.1f m",d.depth()):"DEPTH --";}
 public static void hud(GuiGraphics g,Sd01Entity d,int w,int h){var m=Minecraft.getInstance();int x=14,y=Math.max(86,h-220);g.drawString(m.font,"LIGHT "+(d.lights()?"ON":"OFF")+" | "+(d.submerged()?"SUBMERGED":"NOT SUBMERGED"),x,y,0xffd1e6df);g.drawString(m.font,"WASD | Jump up | Sneak down",x,y+12,0xffd1e6df);g.drawString(m.font,BRAKE.getTranslatedKeyMessage().getString()+" brake | "+SONAR.getTranslatedKeyMessage().getString()+" sonar | "+LIGHT.getTranslatedKeyMessage().getString()+" lights",x,y+24,0xffd1e6df);g.drawString(m.font,"Hold "+VatagClient.waterDetonateKey()+" 1s: self-destruct",x,y+48,0xffffc187);g.drawString(m.font,"SONAR "+Sd01Config.SONAR_RANGE.get()+" m | "+(d.sonarWait()>0?String.format(Locale.ROOT,"%.1fs",d.sonarWait()/20d):"READY"),x,y+36,0xffd1e6df);
  if(d.level().getGameTime()-d.pulse()>160)return;if(!lastSonar.equals(d.sonar())){lastSonar=d.sonar();distances=Sd01Sonar.decode(lastSonar);}float range=distances[0];if(range<=0)return;int cx=x+44,cy=y+91,radius=29;g.fill(cx-radius-4,cy-radius-4,cx+radius+5,cy+radius+5,0x90080d10);for(int i=0;i<12;i++){double a=i*Math.PI/6;int px=cx+(int)(Math.sin(a)*radius),py=cy-(int)(Math.cos(a)*radius);g.fill(px-1,py-1,px+1,py+1,0xff526c68);float dist=distances[i+1];if(dist>=0){double t=Math.min(1,dist/range);px=cx+(int)(Math.sin(a)*radius*t);py=cy-(int)(Math.cos(a)*radius*t);g.fill(px-2,py-2,px+2,py+2,0xffa8dfc5);}}g.fill(cx-1,cy-1,cx+2,cy+2,0xffeeeeee);g.drawString(m.font,"SONAR "+(int)range+" m",x+83,cy-15,0xffbce1d5);g.drawString(m.font,"FLOOR "+number(distances[13]),x+83,cy-3,0xffbce1d5);g.drawString(m.font,"CEILING "+number(distances[14]),x+83,cy+9,0xffbce1d5);g.drawString(m.font,"COOLDOWN "+String.format(Locale.ROOT,"%.1fs",d.sonarWait()/20d),x+83,cy+21,0xffbce1d5);
 }
 private static String number(float f){return f<0?"--":String.format(Locale.ROOT,"%.1f m",f);}
 public static void init(net.neoforged.bus.api.IEventBus bus){bus.addListener(Sd01Client::registerKeys);}
 private static void registerKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(SONAR);e.register(LIGHT);e.register(BRAKE);}
 @net.neoforged.bus.api.SubscribeEvent public static void dvtRoll(net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles e){var m=Minecraft.getInstance();if(DvtClient.capturing()&&m.getCameraEntity()instanceof Sd01Entity sd)e.setRoll(VatagAim.roll(sd.cameraFrame((float)e.getPartialTick())));}
 private Sd01Client(){}
}
