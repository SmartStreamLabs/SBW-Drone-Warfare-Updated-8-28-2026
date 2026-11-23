package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Locale;
import java.util.UUID;

public final class LucasFlightDebugClient {
    private LucasFlightDebugClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(LucasFlightDebugClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(LucasFlightDebugClient.class);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == null || !"hotbar".equals(event.getOverlay().id().getPath())) {
            return;
        }
        if (LucasDroneHudOverlay.shouldRenderForCurrentView()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !(player.level() instanceof ClientLevel level)) {
            return;
        }

        ItemStack activeMonitor = SbwCompat.getActiveLinkedMonitor(player);
        if (activeMonitor.isEmpty()) {
            return;
        }

        UUID linkedDroneId = SbwCompat.getLinkedDroneUuid(activeMonitor);
        if (linkedDroneId == null) {
            return;
        }

        LucasDroneEntity lucasDrone = null;
        Entity linkedEntity = DroneFiberOpticClient.findActiveDroneEntity(level);
        if (linkedEntity instanceof LucasDroneEntity activeLucas && linkedDroneId.equals(activeLucas.getUUID())) {
            lucasDrone = activeLucas;
        } else {
            for (Entity entity : level.entitiesForRendering()) {
                if (linkedDroneId.equals(entity.getUUID()) && entity instanceof LucasDroneEntity fallbackLucas) {
                    lucasDrone = fallbackLucas;
                    break;
                }
            }
        }

        if (lucasDrone == null) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int x = 10;
        int y = 60;
        boolean fiber = DroneFiberOpticClient.isFiberHudActiveFor(lucasDrone.getUUID());
        double speed = lucasDrone.getLucasAirspeed() > 0.0F ? lucasDrone.getLucasAirspeed() : lucasDrone.getDeltaMovement().length();
        int throttlePercent = Math.round(lucasDrone.getLucasThrottle() * 100.0F);
        String stallValue = Component.translatable(
                lucasDrone.isLucasStalling()
                        ? "overlay.sbwdroneconfig.lucas_stall_true"
                        : "overlay.sbwdroneconfig.lucas_stall_false"
        ).getString();

        guiGraphics.drawString(
                minecraft.font,
                Component.translatable(fiber ? "overlay.sbwdroneconfig.link_fiber" : "overlay.sbwdroneconfig.link_wireless"),
                x,
                y,
                fiber ? 0x8ED6FF : 0xB8C7D6,
                true
        );
        guiGraphics.drawString(
                minecraft.font,
                Component.translatable("overlay.sbwdroneconfig.lucas_speed", String.format(Locale.ROOT, "%.2f", speed)),
                x,
                y + 12,
                0xE8F0FF,
                true
        );
        guiGraphics.drawString(
                minecraft.font,
                Component.translatable("overlay.sbwdroneconfig.lucas_throttle", throttlePercent),
                x,
                y + 24,
                0xA8FF9E,
                true
        );
        guiGraphics.drawString(
                minecraft.font,
                Component.translatable("overlay.sbwdroneconfig.lucas_pitch", String.format(Locale.ROOT, "%.1f", lucasDrone.getXRot())),
                x,
                y + 36,
                0xFFE28A,
                true
        );
        guiGraphics.drawString(
                minecraft.font,
                Component.translatable("overlay.sbwdroneconfig.lucas_stall", stallValue),
                x,
                y + 48,
                lucasDrone.isLucasStalling() ? 0xFF6B6B : 0x8CFF5A,
                true
        );
    }
}
