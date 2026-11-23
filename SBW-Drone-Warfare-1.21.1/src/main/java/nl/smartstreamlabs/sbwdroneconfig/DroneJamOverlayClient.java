package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;import java.util.*;
/** Existing authoritative jammer sync, shared by HUD and video. Distortion itself runs before GUI. */
public final class DroneJamOverlayClient {
    private record State(float progress,boolean lost,long received){}
    private static final Map<UUID,State> STATES=new HashMap<>();private static Object world;private static long ticks;
    public static void init(net.neoforged.bus.api.IEventBus bus){bus.addListener(DroneJamOverlayClient::setup);}
    private static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(DroneJamOverlayClient.class);}
    public static void handleSync(DroneJamSyncMessage message){if(message==null)return;if(message.jamProgress()<=0&&!message.hardJammed()){STATES.remove(message.droneId());return;}if(STATES.size()>=32&&!STATES.containsKey(message.droneId()))STATES.clear();STATES.put(message.droneId(),new State(InterferenceMath.clamp(message.jamProgress()),message.hardJammed(),ticks));}
    public static float getJamProgressFor(UUID id){var s=STATES.get(id);return s!=null&&ticks-s.received<=100?s.progress:0;}
    public static boolean isHardJammed(UUID id){var s=STATES.get(id);return s!=null&&ticks-s.received<=100&&s.lost;}
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){var m=Minecraft.getInstance();if(world!=m.level||m.player==null||!m.player.isAlive()){world=m.level;STATES.clear();ticks=0;}ticks++;STATES.entrySet().removeIf(e->ticks-e.getValue().received>100);}
    private DroneJamOverlayClient(){}
}
