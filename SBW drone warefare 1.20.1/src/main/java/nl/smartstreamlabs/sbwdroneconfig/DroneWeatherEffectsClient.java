package nl.smartstreamlabs.sbwdroneconfig;
/** Render only the watched existing feed; rain is server-authored and never taken from the player's weather. */
public final class DroneWeatherEffectsClient {
 private static final net.minecraft.client.KeyMapping WIPE=new net.minecraft.client.KeyMapping("key.sbwdroneconfig.wipe_drone_camera",com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,org.lwjgl.glfw.GLFW.GLFW_KEY_X,"key.categories.sbwdroneconfig");
 public static void init(net.minecraftforge.eventbus.api.IEventBus bus){bus.addListener(DroneWeatherEffectsClient::setup);bus.addListener(DroneWeatherEffectsClient::keys);}
 private static void keys(net.minecraftforge.client.event.RegisterKeyMappingsEvent e){e.register(WIPE);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;while(WIPE.consumeClick()){var watched=DroneOsdClient.fullView();if(watched!=null)watched.getPersistentData().putLong("DwClientWipeTime",watched.level().getGameTime());}}
 private static void setup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e){net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneWeatherEffectsClient.class);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void hud(net.minecraftforge.client.event.RenderGuiEvent.Post e){var mc=net.minecraft.client.Minecraft.getInstance();if(mc.screen==null&&!mc.options.hideGui)MoistureClient.draw(e.getGuiGraphics(),DroneOsdClient.fullView(),0,0,e.getGuiGraphics().guiWidth(),e.getGuiGraphics().guiHeight());}
 private DroneWeatherEffectsClient(){}
}
