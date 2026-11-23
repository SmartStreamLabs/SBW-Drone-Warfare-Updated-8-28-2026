package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import org.lwjgl.glfw.GLFW;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class PlatformPilotClient {
 public static CameraType originalCamera(){return wasUsing?previous:null;}
 private static final KeyMapping KEY=new KeyMapping("key.sbwdroneconfig.platform_pilot",GLFW.GLFW_KEY_C,"key.categories.sbwdroneconfig");private static boolean down,wasUsing;private static CameraType previous;private static java.util.UUID lastSession;
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event){if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
  var m=Minecraft.getInstance();if(m.player==null||m.level==null){if(previous!=null)m.options.setCameraType(previous);down=wasUsing=false;previous=null;lastSession=null;return;}
  boolean pressed=VatagClient.bindingDown(m,KEY);var s=m.player.getMainHandItem();boolean monitor=SbwCompat.isMonitor(s);var n=monitor?s.getOrCreateTag():new net.minecraft.nbt.CompoundTag();
  boolean naval=n.hasUUID("NdcCarrier");boolean eligible=(n.hasUUID(CarrierFleet.HOME)&&SbwCompat.isUsingLinkedMonitor(s))||naval||n.hasUUID("PadLoad");if(!eligible&&SbwCompat.isLinkedMonitor(s))for(var e:m.level.entitiesForRendering())if(e.getStringUUID().equals(n.getString(SbwCompat.TAG_LINKED_DRONE))&&PlatformMonitor.eligible(e)){eligible=true;break;}
  if(pressed&&!down&&m.screen==null&&m.getOverlay()==null&&m.isWindowActive()&&monitor&&eligible&&(!SbwCompat.isUsingLinkedMonitor(s)||naval&&m.options.keyShift.isDown()))AddonNetwork.platformPilot(naval&&m.options.keyShift.isDown());down=pressed;
  boolean using=eligible&&n.hasUUID("PadPilotSession")&&SbwCompat.isUsingLinkedMonitor(s);
  if(using&&!n.getUUID("PadPilotSession").equals(lastSession)){lastSession=n.getUUID("PadPilotSession");if(!wasUsing)previous=m.options.getCameraType();com.atsuishio.superbwarfare.event.ClientEventHandler.lastCameraType=previous;m.options.setCameraType(CameraType.FIRST_PERSON);}
  if(!using&&wasUsing&&previous!=null){if(VatagClient.active()||Id01Client.active())m.options.setCameraType(CameraType.FIRST_PERSON);else m.options.setCameraType(previous);previous=null;}wasUsing=using;
 }
 @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,value=net.minecraftforge.api.distmarker.Dist.CLIENT) public static final class Keys { @net.minecraftforge.eventbus.api.SubscribeEvent public static void registerPlatformPilotClientKeys(net.minecraftforge.client.event.RegisterKeyMappingsEvent event){event.register(KEY);} }
}
