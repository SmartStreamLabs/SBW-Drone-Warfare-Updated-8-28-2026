package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DroneJamOverlayClient {
    private static final Map<UUID, JamVisualState> SYNCED_STATES = new HashMap<>();
    private static int noiseTick;

    private DroneJamOverlayClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(DroneJamOverlayClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneJamOverlayClient.class);
    }

    public static void handleSync(DroneJamSyncMessage message) {
        if (message == null) {
            return;
        }

        if (message.jamProgress() <= 0.0F && !message.hardJammed()) {
            SYNCED_STATES.remove(message.droneId());
            return;
        }

        SYNCED_STATES.put(message.droneId(), new JamVisualState(message.jamProgress(), message.hardJammed()));
    }

    public static float getJamProgressFor(UUID droneId) {
        JamVisualState state = droneId == null ? null : SYNCED_STATES.get(droneId);
        return state == null ? 0.0F : state.jamProgress;
    }

    public static boolean isHardJammed(UUID droneId) {
        JamVisualState state = droneId == null ? null : SYNCED_STATES.get(droneId);
        return state != null && state.hardJammed;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            SYNCED_STATES.clear();
            noiseTick = 0;
            return;
        }

        noiseTick++;
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == null || !"hotbar".equals(event.getOverlay().id().getPath()) || !AddonConfig.enableDroneJammer()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            return;
        }
        if (DroneClientViewContext.isActiveCubedFpvView()) {
            return;
        }

        JamVisualState state = getActiveJamState(player);
        if (state == null || state.jamProgress < AddonConfig.overlayStartThreshold()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        float intensity = Mth.clamp(
                (state.jamProgress - (float) AddonConfig.overlayStartThreshold())
                        / (1.0F - (float) AddonConfig.overlayStartThreshold()),
                0.0F,
                1.0F
        );

        renderGrayWash(guiGraphics, width, height, intensity, state.hardJammed);
        renderStatic(guiGraphics, width, height, intensity);

        Component label = Component.translatable("overlay.sbwdroneconfig.jammer_interference");
        guiGraphics.drawString(
                minecraft.font,
                label,
                10,
                10,
                state.hardJammed ? 0xFF8C8C : 0xF2F2F2,
                true
        );
    }

    private static JamVisualState getActiveJamState(LocalPlayer player) {
        ItemStack activeMonitor = SbwCompat.getActiveLinkedMonitor(player);
        if (activeMonitor.isEmpty()) {
            return null;
        }

        UUID droneId = SbwCompat.getLinkedDroneUuid(activeMonitor);
        if (droneId == null) {
            return null;
        }

        return SYNCED_STATES.get(droneId);
    }

    private static void renderGrayWash(GuiGraphics guiGraphics, int width, int height, float intensity, boolean hardJammed) {
        int baseAlpha = Mth.clamp((int) (22 + (intensity * 110.0F)), 0, 180);
        int color = hardJammed ? 0xB0B0B0 : 0x8A8A8A;
        guiGraphics.fill(0, 0, width, height, (baseAlpha << 24) | color);
    }

    private static void renderStatic(GuiGraphics guiGraphics, int width, int height, float intensity) {
        int lineSpacing = Math.max(2, 7 - Mth.floor(intensity * 4.0F));
        int lineAlpha = Mth.clamp((int) (18 + (intensity * 56.0F)), 0, 120);
        for (int y = Math.floorMod(noiseTick, lineSpacing); y < height; y += lineSpacing) {
            guiGraphics.fill(0, y, width, Math.min(height, y + 1), (lineAlpha << 24) | 0xE5E5E5);
        }

        int specks = 18 + Mth.floor(intensity * 120.0F);
        for (int i = 0; i < specks; i++) {
            int x = Math.floorMod((noiseTick * 19) + (i * 37), Math.max(1, width));
            int y = Math.floorMod((noiseTick * 11) + (i * 23), Math.max(1, height));
            int size = 1 + Math.floorMod(noiseTick + i, 2);
            int alpha = Mth.clamp((int) (28 + (intensity * 120.0F) + Math.floorMod(i * 9, 24)), 0, 180);
            int brightness = 160 + Math.floorMod((noiseTick * 7) + (i * 13), 70);
            int shade = (brightness << 16) | (brightness << 8) | brightness;
            guiGraphics.fill(
                    x,
                    y,
                    Math.min(width, x + size),
                    Math.min(height, y + size),
                    (alpha << 24) | shade
            );
        }
    }

    private record JamVisualState(float jamProgress, boolean hardJammed) {
    }
}
