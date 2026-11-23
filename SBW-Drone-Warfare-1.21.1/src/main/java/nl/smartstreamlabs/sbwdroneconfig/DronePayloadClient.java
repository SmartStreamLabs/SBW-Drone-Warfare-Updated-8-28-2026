package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class DronePayloadClient {
    private static final KeyMapping DROP_MORTAR_PAYLOAD = new KeyMapping(
            "key.sbwdroneconfig.drop_mortar_payload",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.sbwdroneconfig"
    );
    private static final int CLIENT_COOLDOWN_TICKS = 8;
    private static int cooldownTicks;

    private DronePayloadClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(DronePayloadClient::onClientSetup);
        modBus.addListener(DronePayloadClient::onRegisterKeyMappings);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(DronePayloadClient.class);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DROP_MORTAR_PAYLOAD);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        while (DROP_MORTAR_PAYLOAD.consumeClick()) {
            if (cooldownTicks > 0 || DroneSpotlightClient.getControlledDroneUuid(player) == null) {
                continue;
            }

            AddonNetwork.dropMortarPayload();
            cooldownTicks = CLIENT_COOLDOWN_TICKS;
        }
    }
}
