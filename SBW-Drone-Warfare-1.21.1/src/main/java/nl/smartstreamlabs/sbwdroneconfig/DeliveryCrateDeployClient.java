package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

public final class DeliveryCrateDeployClient {
    private static final KeyMapping DEPLOY_DELIVERY_CRATE = new KeyMapping(
            "key.sbwdroneconfig.deploy_delivery_crate",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "key.categories.sbwdroneconfig"
    );
    private static final KeyMapping PICKUP_DELIVERY_CRATE = new KeyMapping(
            "key.sbwdroneconfig.pickup_delivery_crate",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "key.categories.sbwdroneconfig"
    );
    private static final int CLIENT_COOLDOWN_TICKS = 8;
    private static int cooldownTicks;

    private DeliveryCrateDeployClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(DeliveryCrateDeployClient::onClientSetup);
        modBus.addListener(DeliveryCrateDeployClient::onRegisterKeyMappings);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DeliveryCrateDeployClient.class);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DEPLOY_DELIVERY_CRATE);
        event.register(PICKUP_DELIVERY_CRATE);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        handleCrateKey(player, DEPLOY_DELIVERY_CRATE, AddonNetwork::deployDeliveryCrate);
        handleCrateKey(player, PICKUP_DELIVERY_CRATE, AddonNetwork::pickupDeliveryCrate);
    }

    private static void handleCrateKey(LocalPlayer player, KeyMapping keyMapping, Runnable action) {
        while (keyMapping.consumeClick()) {
            if (cooldownTicks > 0 || SbwCompat.getLinkedDroneUuid(SbwCompat.getActiveLinkedMonitor(player)) == null) {
                continue;
            }

            action.run();
            cooldownTicks = CLIENT_COOLDOWN_TICKS;
        }
    }
}
