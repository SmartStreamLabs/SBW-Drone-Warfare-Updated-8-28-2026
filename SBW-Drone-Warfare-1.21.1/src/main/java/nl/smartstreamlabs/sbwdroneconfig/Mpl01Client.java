package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import net.minecraft.network.chat.Component;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class Mpl01Client{
 private static KeyMapping key(String s,int code){return new KeyMapping("key.sbwdroneconfig.mpl01_"+s,code,"key.categories.sbwdroneconfig");}
 public static final KeyMapping CONSOLE=key("console",org.lwjgl.glfw.GLFW.GLFW_KEY_I),DEPLOY=key("deploy",org.lwjgl.glfw.GLFW.GLFW_KEY_G),PARK=key("park",org.lwjgl.glfw.GLFW.GLFW_KEY_P);
 private static void send(Mpl01Entity e,int action){AddonNetwork.mpl01Action(new Mpl01Action(e.getId(),action,0,new java.util.UUID(0,0)));}
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){var m=Minecraft.getInstance();var v=m.player!=null&&m.player.getVehicle()instanceof Mpl01Entity a?a:null;if(v==null||m.screen!=null){while(CONSOLE.consumeClick()){}while(DEPLOY.consumeClick()){}while(PARK.consumeClick()){}return;}while(CONSOLE.consumeClick())m.setScreen(new Mpl01Screen(v));while(DEPLOY.consumeClick())send(v,0);while(PARK.consumeClick())send(v,1);}
 @net.neoforged.bus.api.SubscribeEvent public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post e){var m=Minecraft.getInstance();if(m.player==null||m.screen!=null||m.options.hideGui||!(m.player.getVehicle()instanceof Mpl01Entity v))return;var g=e.getGuiGraphics();int y=8;g.fill(5,y-5,370,y+47,0xb0182522);g.drawString(m.font,"MPL-01 | "+v.phase().name()+" | "+v.status(),10,y,0xffe0e6d1);g.drawString(m.font,"PM-01: "+Integer.bitCount(v.ammoMask())+" / 4 | Selected: "+(v.selectedSlot()+1)+" | "+(v.parked()?"PARK BRAKE":"DRIVE"),10,y+13,0xffc0d2cb);g.drawString(m.font,CONSOLE.getTranslatedKeyMessage().getString()+" console | "+DEPLOY.getTranslatedKeyMessage().getString()+" deploy | "+PARK.getTranslatedKeyMessage().getString()+" park",10,y+26,0xffc0d2cb);}
 @net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
 public static final class Keys{@net.neoforged.bus.api.SubscribeEvent public static void registerMpl01ClientKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(CONSOLE);e.register(DEPLOY);e.register(PARK);}}
 private Mpl01Client(){}
}
