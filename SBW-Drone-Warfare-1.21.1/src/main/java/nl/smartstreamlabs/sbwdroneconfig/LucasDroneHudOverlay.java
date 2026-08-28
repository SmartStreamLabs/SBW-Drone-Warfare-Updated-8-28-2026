package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class LucasDroneHudOverlay {
    private static final int HUD_WHITE = 0xEAF4F2;
    private static final int HUD_DIM = 0xAABAB6;
    private static final int HUD_GREEN = 0x8CFF9B;
    private static final int HUD_AMBER = 0xFFE08A;
    private static final int HUD_RED = 0xFF8A8A;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static int hudTick;

    private LucasDroneHudOverlay() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(LucasDroneHudOverlay::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(LucasDroneHudOverlay.class);
    }

    public static boolean shouldRenderForCurrentView() {
        return DroneClientViewContext.activeLucasDrone(Minecraft.getInstance()) != null;
    }

    public static boolean shouldReplaceDefaultSbwHud() {
        return shouldRenderForCurrentView();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            hudTick++;
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == null || !"hotbar".equals(event.getOverlay().id().getPath())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LucasDroneEntity lucasDrone = DroneClientViewContext.activeLucasDrone(minecraft);
        if (lucasDrone == null) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        double scale = Mth.clamp(AddonConfig.lucasHudScale(), 0.60D, 1.50D);
        int scaledWidth = Math.max(1, Mth.floor(width / scale));
        int scaledHeight = Math.max(1, Mth.floor(height / scale));

        renderCameraEffects(guiGraphics, width, height);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.scale((float) scale, (float) scale, 1.0F);
        renderTelemetryPanel(guiGraphics, minecraft, lucasDrone, 14, 14);
        renderHeadingCompass(guiGraphics, minecraft, lucasDrone, scaledWidth / 2, 16);
        renderStatusPanel(guiGraphics, minecraft, lucasDrone, scaledWidth - 158, 14);
        renderTargetingReticle(guiGraphics, scaledWidth / 2, scaledHeight / 2);
        renderLinkPanel(guiGraphics, minecraft, lucasDrone, 14, scaledHeight - 62);
        renderMissionPanel(guiGraphics, minecraft, lucasDrone, scaledWidth / 2, scaledHeight - 42);
        renderSystemPanel(guiGraphics, minecraft, lucasDrone, scaledWidth - 176, scaledHeight - 64);
        poseStack.popPose();
    }

    private static void renderTelemetryPanel(GuiGraphics guiGraphics, Minecraft minecraft, LucasDroneEntity drone, int x, int y) {
        int fuelPercent = Mth.clamp(drone.getLucasFuelPercent(), 0, 100);
        int fuelColor = fuelPercent <= 0 ? HUD_RED : fuelPercent <= AddonConfig.lucasLowFuelWarningPercent() ? HUD_AMBER : HUD_GREEN;
        drawPanelFrame(guiGraphics, x - 6, y - 6, 118, 48);
        draw(guiGraphics, minecraft, "FUEL: " + fuelPercent + "%", x, y, fuelColor);
        draw(guiGraphics, minecraft, "SPD: " + speedKmh(drone) + " km/h", x, y + 12, HUD_WHITE);
        draw(guiGraphics, minecraft, "ALT: " + Math.round(drone.getY()) + " m", x, y + 24, HUD_WHITE);
        if (fuelPercent <= 0) {
            draw(guiGraphics, minecraft, "FUEL EMPTY", x, y + 36, HUD_RED);
        } else if (fuelPercent <= AddonConfig.lucasLowFuelWarningPercent()) {
            draw(guiGraphics, minecraft, "LOW FUEL", x, y + 36, HUD_AMBER);
        }
    }

    private static void renderHeadingCompass(GuiGraphics guiGraphics, Minecraft minecraft, LucasDroneEntity drone, int centerX, int y) {
        int heading = headingDegrees(drone);
        int tapeWidth = 178;
        int left = centerX - tapeWidth / 2;
        drawPanelFrame(guiGraphics, left, y - 6, tapeWidth, 31);
        guiGraphics.fill(centerX - 1, y - 2, centerX + 1, y + 20, 0xB8EAF4F2);
        drawCentered(guiGraphics, minecraft, "HDG " + padHeading(heading), centerX, y + 14, HUD_WHITE);

        renderCompassCardinal(guiGraphics, minecraft, centerX, y, heading, "N", 0);
        renderCompassCardinal(guiGraphics, minecraft, centerX, y, heading, "E", 90);
        renderCompassCardinal(guiGraphics, minecraft, centerX, y, heading, "S", 180);
        renderCompassCardinal(guiGraphics, minecraft, centerX, y, heading, "W", 270);
    }

    private static void renderCompassCardinal(GuiGraphics guiGraphics, Minecraft minecraft, int centerX, int y, int heading, String label, int cardinalHeading) {
        float diff = Mth.wrapDegrees(cardinalHeading - heading);
        if (Math.abs(diff) > 72.0F) {
            return;
        }

        int x = centerX + Math.round(diff * 1.05F);
        int color = Math.abs(diff) < 8.0F ? HUD_GREEN : HUD_DIM;
        drawCentered(guiGraphics, minecraft, label, x, y, color);
        guiGraphics.fill(x, y + 10, x + 1, y + 15, 0xAAEAF4F2);
    }

    private static void renderStatusPanel(GuiGraphics guiGraphics, Minecraft minecraft, LucasDroneEntity drone, int x, int y) {
        drawPanelFrame(guiGraphics, x - 6, y - 6, 150, 48);
        draw(guiGraphics, minecraft, "MODE: MANUAL", x, y, HUD_WHITE);
        draw(guiGraphics, minecraft, cameraModeText(), x, y + 12, HUD_WHITE);
        draw(guiGraphics, minecraft, "ZOOM: x1", x, y + 24, HUD_DIM);
        draw(guiGraphics, minecraft, drone.isLucasStalling() ? "STALL WARN" : "STABLE", x, y + 36, drone.isLucasStalling() ? HUD_AMBER : HUD_GREEN);
    }

    private static void renderTargetingReticle(GuiGraphics guiGraphics, int centerX, int centerY) {
        int color = 0xDDEAF4F2;
        guiGraphics.fill(centerX - 18, centerY, centerX - 7, centerY + 1, color);
        guiGraphics.fill(centerX + 8, centerY, centerX + 19, centerY + 1, color);
        guiGraphics.fill(centerX, centerY - 18, centerX + 1, centerY - 7, color);
        guiGraphics.fill(centerX, centerY + 8, centerX + 1, centerY + 19, color);
        drawCorner(guiGraphics, centerX - 35, centerY - 23, 1);
        drawCorner(guiGraphics, centerX + 28, centerY - 23, -1);
        drawCornerBottom(guiGraphics, centerX - 35, centerY + 16, 1);
        drawCornerBottom(guiGraphics, centerX + 28, centerY + 16, -1);
    }

    private static void renderLinkPanel(GuiGraphics guiGraphics, Minecraft minecraft, LucasDroneEntity drone, int x, int y) {
        float jamProgress = DroneJamOverlayClient.getJamProgressFor(drone.getUUID());
        int linkQuality = Mth.clamp(Math.round((1.0F - jamProgress) * 100.0F), 0, 100);
        int latency = estimateLatencyMs(drone, jamProgress);
        drawPanelFrame(guiGraphics, x - 6, y - 6, 136, 44);
        draw(guiGraphics, minecraft, "LINK: " + linkQuality + "%", x, y, linkQuality <= 35 ? HUD_RED : HUD_WHITE);
        draw(guiGraphics, minecraft, "LATENCY: " + latency + " ms", x, y + 12, latency >= 120 ? HUD_AMBER : HUD_DIM);
        draw(guiGraphics, minecraft, "GPS: ACTIVE", x, y + 24, HUD_GREEN);
    }

    private static void renderMissionPanel(GuiGraphics guiGraphics, Minecraft minecraft, LucasDroneEntity drone, int centerX, int y) {
        int width = 196;
        int x = centerX - width / 2;
        int healthPercent = healthPercent(drone);
        int distance = distanceToTargetMeters(minecraft, drone);
        drawPanelFrame(guiGraphics, x, y - 8, width, 40);
        drawCentered(guiGraphics, minecraft, "HEALTH " + healthPercent + "%", centerX, y, healthPercent <= 30 ? HUD_RED : HUD_WHITE);
        drawCentered(guiGraphics, minecraft, "DISTANCE TO TARGET: " + distance + " m", centerX, y + 13, HUD_DIM);
        renderHealthBar(guiGraphics, x + 24, y + 25, width - 48, healthPercent);
    }

    private static void renderSystemPanel(GuiGraphics guiGraphics, Minecraft minecraft, LucasDroneEntity drone, int x, int y) {
        BlockPos pos = drone.blockPosition();
        drawPanelFrame(guiGraphics, x - 6, y - 6, 168, 48);
        draw(guiGraphics, minecraft, "TIME: " + LocalTime.now().format(TIME_FORMAT), x, y, HUD_WHITE);
        draw(guiGraphics, minecraft, "COORD: " + pos.getX() + " " + pos.getY() + " " + pos.getZ(), x, y + 12, HUD_DIM);
        draw(guiGraphics, minecraft, "REC", x, y + 24, HUD_WHITE);
        if ((hudTick / 10) % 2 == 0) {
            guiGraphics.fill(x + 24, y + 27, x + 29, y + 32, 0xFFE53935);
        }
    }

    private static void renderCameraEffects(GuiGraphics guiGraphics, int width, int height) {
        int edge = Math.max(24, Math.min(width, height) / 10);
        guiGraphics.fill(0, 0, width, edge, 0x18000000);
        guiGraphics.fill(0, height - edge, width, height, 0x18000000);
        guiGraphics.fill(0, 0, edge, height, 0x12000000);
        guiGraphics.fill(width - edge, 0, width, height, 0x12000000);

        int lineAlpha = 18;
        for (int y = Math.floorMod(hudTick, 6); y < height; y += 6) {
            guiGraphics.fill(0, y, width, y + 1, (lineAlpha << 24) | 0xDDEDED);
        }

        for (int i = 0; i < 18; i++) {
            int x = Math.floorMod(hudTick * 17 + i * 83, Math.max(1, width));
            int y = Math.floorMod(hudTick * 9 + i * 47, Math.max(1, height));
            guiGraphics.fill(x, y, Math.min(width, x + 1), Math.min(height, y + 1), 0x30EAF4F2);
        }
    }

    private static void renderHealthBar(GuiGraphics guiGraphics, int x, int y, int width, int healthPercent) {
        guiGraphics.fill(x, y, x + width, y + 3, 0x66000000);
        int fillWidth = Math.round(width * (Mth.clamp(healthPercent, 0, 100) / 100.0F));
        int color = healthPercent <= 30 ? 0xFFFF8A8A : 0xFFEAF4F2;
        guiGraphics.fill(x, y, x + fillWidth, y + 3, color);
    }

    private static void drawPanelFrame(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x26000000);
        guiGraphics.fill(x, y, x + 16, y + 1, 0xC8EAF4F2);
        guiGraphics.fill(x, y, x + 1, y + 10, 0xC8EAF4F2);
        guiGraphics.fill(x + width - 16, y, x + width, y + 1, 0xC8EAF4F2);
        guiGraphics.fill(x + width - 1, y, x + width, y + 10, 0xC8EAF4F2);
        guiGraphics.fill(x, y + height - 1, x + 16, y + height, 0xC8EAF4F2);
        guiGraphics.fill(x, y + height - 10, x + 1, y + height, 0xC8EAF4F2);
        guiGraphics.fill(x + width - 16, y + height - 1, x + width, y + height, 0xC8EAF4F2);
        guiGraphics.fill(x + width - 1, y + height - 10, x + width, y + height, 0xC8EAF4F2);
    }

    private static void drawCorner(GuiGraphics guiGraphics, int x, int y, int direction) {
        int endX = x + direction * 8;
        guiGraphics.fill(Math.min(x, endX), y, Math.max(x, endX) + 1, y + 1, 0xB8EAF4F2);
        guiGraphics.fill(x, y, x + 1, y + 8, 0xB8EAF4F2);
    }

    private static void drawCornerBottom(GuiGraphics guiGraphics, int x, int y, int direction) {
        int endX = x + direction * 8;
        guiGraphics.fill(Math.min(x, endX), y + 7, Math.max(x, endX) + 1, y + 8, 0xB8EAF4F2);
        guiGraphics.fill(x, y, x + 1, y + 8, 0xB8EAF4F2);
    }

    private static void draw(GuiGraphics guiGraphics, Minecraft minecraft, String text, int x, int y, int color) {
        guiGraphics.drawString(minecraft.font, Component.literal(text), x, y, color, true);
    }

    private static void drawCentered(GuiGraphics guiGraphics, Minecraft minecraft, String text, int centerX, int y, int color) {
        guiGraphics.drawString(minecraft.font, Component.literal(text), centerX - minecraft.font.width(text) / 2, y, color, true);
    }

    private static int speedKmh(LucasDroneEntity drone) {
        double speed = drone.getLucasAirspeed() > 0.0F ? drone.getLucasAirspeed() : drone.getDeltaMovement().length();
        return Math.max(0, Math.round((float) (speed * 72.0D)));
    }

    private static int headingDegrees(LucasDroneEntity drone) {
        int heading = Math.round(drone.getYRot() + 180.0F) % 360;
        return heading < 0 ? heading + 360 : heading;
    }

    private static String padHeading(int heading) {
        return String.format(Locale.ROOT, "%03d", Mth.clamp(heading, 0, 359));
    }

    private static String cameraModeText() {
        return "CAM: NORMAL";
    }

    private static int estimateLatencyMs(LucasDroneEntity drone, float jamProgress) {
        return Mth.clamp(Math.round((float) (28.0D + drone.getDeltaMovement().length() * 16.0D + jamProgress * 240.0D)), 18, 350);
    }

    private static int distanceToTargetMeters(Minecraft minecraft, LucasDroneEntity drone) {
        Entity camera = minecraft.getCameraEntity();
        if (camera == null) {
            return 0;
        }
        return Math.max(0, Math.round(camera.distanceTo(drone)));
    }

    private static int healthPercent(LucasDroneEntity drone) {
        Number value = readEntityDataNumber(drone, "com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity", "HEALTH");
        if (value == null) {
            value = readEntityDataNumber(drone, "com.atsuishio.superbwarfare.entity.vehicle.DroneEntity", "HEALTH");
        }
        if (value == null) {
            return drone.isAlive() ? 100 : 0;
        }

        float rawHealth = Math.max(0.0F, value.floatValue());
        if (rawHealth <= 5.0F) {
            return Mth.clamp(Math.round(rawHealth * 20.0F), 0, 100);
        }
        return Mth.clamp(Math.round(rawHealth), 0, 100);
    }

    @SuppressWarnings("unchecked")
    private static Number readEntityDataNumber(LucasDroneEntity drone, String className, String fieldName) {
        try {
            Field field = Class.forName(className).getField(fieldName);
            Object accessor = field.get(null);
            if (accessor instanceof EntityDataAccessor<?> entityDataAccessor) {
                Object value = drone.getEntityData().get((EntityDataAccessor<Object>) entityDataAccessor);
                if (value instanceof Number number) {
                    return number;
                }
            }
        } catch (ReflectiveOperationException | ClassCastException ignored) {
        }
        return null;
    }
}
