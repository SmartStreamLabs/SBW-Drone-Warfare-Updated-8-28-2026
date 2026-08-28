package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.UUID;

public final class LucasFuelClient {
    private static FuelState state;

    private LucasFuelClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(LucasFuelClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(LucasFuelClient.class);
    }

    public static void handleSync(LucasFuelSyncMessage message) {
        state = new FuelState(
                message.droneId(),
                message.currentFuel(),
                message.maxFuel(),
                message.emptyFuel(),
                message.lowFuel()
        );
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!shouldRenderHud() || event.getOverlay() == null || !"hotbar".equals(event.getOverlay().id().getPath())) {
            return;
        }
        if (LucasDroneHudOverlay.shouldRenderForCurrentView()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int percent = state.maxFuel <= 0 ? 0 : Math.round((state.currentFuel * 100.0F) / state.maxFuel);
        int x = 10;
        int y = 132;
        int color = state.emptyFuel ? 0xFF3B30 : state.lowFuel ? 0xFFB000 : 0x8CFF5A;

        guiGraphics.drawString(
                minecraft.font,
                Component.translatable("overlay.sbwdroneconfig.lucas_fuel", percent),
                x,
                y,
                color,
                true
        );

        if (state.emptyFuel) {
            guiGraphics.drawString(
                    minecraft.font,
                    Component.translatable("overlay.sbwdroneconfig.lucas_fuel_empty"),
                    x,
                    y + 12,
                    0xFF3B30,
                    true
            );
        } else if (state.lowFuel) {
            guiGraphics.drawString(
                    minecraft.font,
                    Component.translatable("overlay.sbwdroneconfig.lucas_fuel_low"),
                    x,
                    y + 12,
                    0xFFB000,
                    true
            );
        }
    }

    private static boolean shouldRenderHud() {
        if (state == null) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }
        ItemStack activeMonitor = SbwCompat.getActiveLinkedMonitor(player);
        if (activeMonitor.isEmpty()) {
            return false;
        }
        UUID linkedDrone = SbwCompat.getLinkedDroneUuid(activeMonitor);
        return linkedDrone != null && linkedDrone.equals(state.droneId);
    }

    private record FuelState(UUID droneId, int currentFuel, int maxFuel, boolean emptyFuel, boolean lowFuel) {
    }
}
