package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class AbandonedFiberCableClient {
    private static final Map<UUID, AbandonedFiberCableData.CableRecord> CABLES = new LinkedHashMap<>();
    private static ClientLevel lastLevel;

    private AbandonedFiberCableClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(AbandonedFiberCableClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(AbandonedFiberCableClient.class);
    }

    public static void handleSync(AbandonedFiberCableSyncMessage message) {
        if (message == null) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            lastLevel = level;
        }
        if (message.replaceAll()) {
            CABLES.clear();
        }
        for (AbandonedFiberCableData.CableRecord cable : message.cables()) {
            if (cable != null && cable.points().size() >= 2) {
                CABLES.put(cable.cableId(), cable);
            }
        }
    }

    static Collection<AbandonedFiberCableData.CableRecord> cables() {
        return CABLES.values();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            CABLES.clear();
            lastLevel = null;
            return;
        }
        if (lastLevel != minecraft.level) {
            CABLES.clear();
            lastLevel = minecraft.level;
        }
    }
}
