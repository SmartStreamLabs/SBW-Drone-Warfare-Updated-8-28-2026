package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Connection-scoped server snapshot. No item polling, entity mutation or disk configuration. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class PilotMapClient {
    private static final PilotMapState state = new PilotMapState();
    private static void sync() { state.connection(Minecraft.getInstance().getConnection()); }
    public static void apply(PilotMapPacket packet) { sync(); state.apply(packet); }
    public static boolean hidden(UUID player) { sync(); return state.hidden(player); }
    public static boolean hidden(Entity entity) { return entity instanceof Player && hidden(entity.getUUID()); }
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) sync();
    }
    public static boolean mapOpen(net.minecraft.client.gui.screens.Screen screen) { for(Class<?> type=screen==null?null:screen.getClass();type!=null;type=type.getSuperclass())if(type.getName().equals("xaero.map.gui.GuiMap"))return true;return false; }
    private PilotMapClient() {}
}
