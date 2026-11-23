package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Locale;
import java.util.Set;

public final class CubedFpvReferenceGuiOverlay {
    public static final String GUI_BUILD_ID = "GUI-RUNTIME-B2";
    private static final float NORMAL_TEXT = 1.16F;
    private static final float SMALL_TEXT = 1.02F;
    private static final float SPACED_TEXT = 1.28F;
    private static final float CALLSIGN_TEXT = 1.46F;
    private static final float COMPASS_CENTER_X = 550.0F;
    private static final float COMPASS_Y = 90.0F;
    private static final float COMPASS_LEFT_X = 438.0F;
    private static final float COMPASS_RIGHT_X = 672.0F;
    private static final float CALLSIGN_CENTER_X = 550.0F;
    private static final float CALLSIGN_BASELINE_Y = 146.0F;
    private static final float STATUS_X = 185.0F;
    private static final float STATUS_Y = 125.0F;
    private static final float LINK_X = 740.0F;
    private static final float LINK_Y = 90.0F;
    private static final float LINK_SPACING = 17.0F;
    private static final float SPEED_X = 190.0F;
    private static final float SPEED_Y = 280.0F;
    private static final float TELEMETRY_X = 190.0F;
    private static final float ALTITUDE_Y = 350.0F;
    private static final float ANGLE_Y = 390.0F;
    private static final float BATTERY_Y = 430.0F;
    private static final float SECONDARY_BATTERY_Y = 468.0F;
    private static final float RETICLE_X = 560.0F;
    private static final float RETICLE_Y = 314.0F;
    private static final float MODE_X = 550.0F;
    private static final float MODE_Y = 437.0F;
    private static final float SECONDARY_MODE_X = 470.0F;
    private static final float SECONDARY_MODE_Y = 476.0F;
    private static final int WHITE = CubedFpvReferenceGuiTextRenderer.WHITE;
    private static final int SOFT_WHITE = CubedFpvReferenceGuiTextRenderer.SOFT_WHITE;
    private static boolean firstFrameLogged;
    private static final Set<String> HIDDEN_OVERLAYS = Set.of(
            "crosshair",
            "hotbar",
            "selected_item_name",
            "item_name",
            "action_bar",
            "actionbar",
            "overlay_message",
            "experience_bar",
            "experience_level",
            "player_health",
            "armor_level",
            "food_level",
            "air_level",
            "mount_health",
            "jump_meter",
            "vehicle_health"
    );

    private CubedFpvReferenceGuiOverlay() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(CubedFpvReferenceGuiOverlay::onClientSetup);
    }

    public static boolean shouldReplaceDefaultSbwHud() {
        CubedFpvDroneEntity drone = DroneClientViewContext.activeCubedFpvDrone(Minecraft.getInstance());
        return !(drone instanceof DeliveryDroneEntity) && DroneClientViewContext.isLocalFpvViewOf(drone);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(CubedFpvReferenceGuiOverlay.class);
        SbwDroneRangeConfig.LOGGER.info(
                "[SBW Drone Warfare] Registered Cubed FPV GUI: {}\nClass: {}",
                GUI_BUILD_ID,
                CubedFpvReferenceGuiOverlay.class.getName()
        );
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) { if(DroneOsdClient.ownsFullView())return;
        CubedFpvReferenceGuiTelemetry.onClientTick();
        suppressActionBarMessage();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) { if(DroneOsdClient.ownsFullView())return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        CubedFpvReferenceGuiTelemetry.Snapshot telemetry = CubedFpvReferenceGuiTelemetry.collect(
                minecraft,
                event.getPartialTick().getGameTimeDeltaPartialTick(true)
        );
        if (telemetry == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        CubedFpvReferenceGuiLayout.Canvas layout = CubedFpvReferenceGuiLayout.fitToScreen(graphics);

        logFirstFrame(minecraft, graphics);
        suppressActionBarMessage();
        CubedFpvReferenceGuiFrameRenderer.render(graphics, layout);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        renderCompass(graphics, font, layout, telemetry);
        renderCallsign(graphics, font, layout, telemetry);
        renderStatus(graphics, font, layout, telemetry);
        renderLink(graphics, font, layout, telemetry);
        renderFlightTelemetry(graphics, font, layout, telemetry);
        renderMode(graphics, font, layout, telemetry);
        renderReticle(graphics, layout);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiLayerEvent.Pre event) { if(DroneOsdClient.ownsFullView())return;
        if (!shouldReplaceDefaultSbwHud() || event.getName() == null) {
            return;
        }

        suppressActionBarMessage();
        if (HIDDEN_OVERLAYS.contains(event.getName().getPath())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (shouldReplaceDefaultSbwHud()) {
            event.setCanceled(true);
        }
    }

    private static void renderCompass(GuiGraphics graphics, Font font, CubedFpvReferenceGuiLayout.Canvas layout, CubedFpvReferenceGuiTelemetry.Snapshot telemetry) {
        float heading = telemetry.heading();
        int baseHeading = ((int) Math.floor(heading / 10.0F)) * 10;
        float fraction = heading - baseHeading;
        float pixelsPerDegree = (COMPASS_RIGHT_X - COMPASS_LEFT_X) / 100.0F;

        for (int slot = -5; slot <= 5; slot++) {
            int markHeading = baseHeading + (slot * 10);
            float x = COMPASS_CENTER_X + (((slot * 10.0F) - fraction) * pixelsPerDegree);
            int normalized = Math.floorMod(markHeading, 360);
            int markerWidth = Math.abs(x - COMPASS_CENTER_X) < 9.0F ? 3 : 2;
            int markerHeight = normalized % 30 == 0 ? 5 : 3;
            compassMarker(graphics, layout, x, COMPASS_Y, markerWidth, markerHeight, normalized % 30 == 0);

            String label = headingLabel(normalized);
            if (!label.isEmpty()) {
                CubedFpvReferenceGuiTextRenderer.drawCentered(
                        graphics,
                        font,
                        label,
                        layout.x(x),
                        layout.y(COMPASS_Y + 15.0F),
                        SMALL_TEXT * layout.textScale(),
                        WHITE,
                        1.0F
                );
            }
        }

        CubedFpvReferenceGuiTextRenderer.drawCentered(
                graphics,
                font,
                telemetry.headingText(),
                layout.x(COMPASS_CENTER_X),
                layout.y(110.0F),
                SMALL_TEXT * layout.textScale(),
                WHITE,
                0.9F
        );
    }

    private static void renderCallsign(GuiGraphics graphics, Font font, CubedFpvReferenceGuiLayout.Canvas layout, CubedFpvReferenceGuiTelemetry.Snapshot telemetry) {
        CubedFpvReferenceGuiTextRenderer.drawCentered(
                graphics,
                font,
                telemetry.callsign(),
                layout.x(CALLSIGN_CENTER_X),
                layout.y(CALLSIGN_BASELINE_Y - 12.0F),
                CALLSIGN_TEXT * layout.textScale(),
                WHITE,
                1.9F
        );
    }

    private static void renderStatus(GuiGraphics graphics, Font font, CubedFpvReferenceGuiLayout.Canvas layout, CubedFpvReferenceGuiTelemetry.Snapshot telemetry) {
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.armText(), layout.x(STATUS_X), layout.y(STATUS_Y), NORMAL_TEXT * layout.textScale(), WHITE, 0.8F);
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.timerText(), layout.x(STATUS_X), layout.y(STATUS_Y + 18.0F), NORMAL_TEXT * layout.textScale(), WHITE, 0.8F);
    }

    private static void renderLink(GuiGraphics graphics, Font font, CubedFpvReferenceGuiLayout.Canvas layout, CubedFpvReferenceGuiTelemetry.Snapshot telemetry) {
        int linkColor = telemetry.linkColor();
        renderTinySignalGlyph(graphics, layout, LINK_X - 22.0F, LINK_Y, linkColor);
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.linkQualityText(), layout.x(LINK_X), layout.y(LINK_Y), NORMAL_TEXT * layout.textScale(), linkColor, 0.7F);
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.rssiText(), layout.x(LINK_X), layout.y(LINK_Y + LINK_SPACING), NORMAL_TEXT * layout.textScale(), WHITE, 0.7F);
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.latencyText(), layout.x(LINK_X), layout.y(LINK_Y + (LINK_SPACING * 2.0F)), NORMAL_TEXT * layout.textScale(), WHITE, 0.7F);
    }

    private static void renderFlightTelemetry(GuiGraphics graphics, Font font, CubedFpvReferenceGuiLayout.Canvas layout, CubedFpvReferenceGuiTelemetry.Snapshot telemetry) {
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.speedText(), layout.x(SPEED_X), layout.y(SPEED_Y), NORMAL_TEXT * layout.textScale(), WHITE, 0.8F);
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.altitudeText(), layout.x(TELEMETRY_X), layout.y(ALTITUDE_Y), NORMAL_TEXT * layout.textScale(), WHITE, 0.8F);
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.angleText(), layout.x(TELEMETRY_X), layout.y(ANGLE_Y), NORMAL_TEXT * layout.textScale(), WHITE, 0.8F);
        renderVerticalBattery(graphics, layout, TELEMETRY_X - 18.0F, BATTERY_Y - 2.0F, telemetry.batteryColor());
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.batteryText(), layout.x(TELEMETRY_X), layout.y(BATTERY_Y), NORMAL_TEXT * layout.textScale(), telemetry.batteryColor(), 0.8F);
        renderVerticalBattery(graphics, layout, TELEMETRY_X - 18.0F, SECONDARY_BATTERY_Y - 2.0F, telemetry.batteryColor());
        CubedFpvReferenceGuiTextRenderer.draw(graphics, font, telemetry.secondaryBatteryText(), layout.x(TELEMETRY_X), layout.y(SECONDARY_BATTERY_Y), NORMAL_TEXT * layout.textScale(), telemetry.batteryColor(), 0.8F);
    }

    private static void renderMode(GuiGraphics graphics, Font font, CubedFpvReferenceGuiLayout.Canvas layout, CubedFpvReferenceGuiTelemetry.Snapshot telemetry) {
        CubedFpvReferenceGuiTextRenderer.drawCentered(graphics, font, telemetry.flightModeText(), layout.x(MODE_X), layout.y(MODE_Y), SPACED_TEXT * layout.textScale(), WHITE, 2.0F);
        CubedFpvReferenceGuiTextRenderer.drawCentered(graphics, font, telemetry.linkModeText(), layout.x(SECONDARY_MODE_X), layout.y(SECONDARY_MODE_Y), NORMAL_TEXT * layout.textScale(), WHITE, 1.3F);
    }

    private static void renderReticle(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout) {
        dot(graphics, layout, RETICLE_X, RETICLE_Y, 1.5F, 1.5F, WHITE);
        dot(graphics, layout, RETICLE_X, RETICLE_Y - 22.0F, 1.5F, 1.5F, SOFT_WHITE);
        dot(graphics, layout, RETICLE_X + 16.0F, RETICLE_Y - 16.0F, 1.5F, 1.5F, SOFT_WHITE);
        dot(graphics, layout, RETICLE_X + 22.0F, RETICLE_Y, 1.5F, 1.5F, SOFT_WHITE);
        dot(graphics, layout, RETICLE_X + 16.0F, RETICLE_Y + 16.0F, 1.5F, 1.5F, SOFT_WHITE);
        dot(graphics, layout, RETICLE_X, RETICLE_Y + 22.0F, 1.5F, 1.5F, SOFT_WHITE);
        dot(graphics, layout, RETICLE_X - 16.0F, RETICLE_Y + 16.0F, 1.5F, 1.5F, SOFT_WHITE);
        dot(graphics, layout, RETICLE_X - 22.0F, RETICLE_Y, 1.5F, 1.5F, SOFT_WHITE);
        dot(graphics, layout, RETICLE_X - 16.0F, RETICLE_Y - 16.0F, 1.5F, 1.5F, SOFT_WHITE);
    }

    private static void logFirstFrame(Minecraft minecraft, GuiGraphics graphics) {
        if (firstFrameLogged) {
            return;
        }
        firstFrameLogged = true;
        SbwDroneRangeConfig.LOGGER.info(
                "[SBW Drone Warfare] First Cubed FPV GUI frame: {}\nRenderer: {}\nScreen: {}x{}\nGUI scale: {}",
                GUI_BUILD_ID,
                CubedFpvReferenceGuiOverlay.class.getName(),
                graphics.guiWidth(),
                graphics.guiHeight(),
                minecraft.getWindow().getGuiScale()
        );
    }

    private static void renderTinySignalGlyph(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout, float x, float y, int color) {
        float bottom = y + 12.0F;
        for (int index = 0; index < 4; index++) {
            float height = 3.0F + (index * 2.0F);
            outlinedRect(graphics, layout, x + (index * 4.0F), bottom - height, 2.0F, height, color);
        }
    }

    private static void renderVerticalBattery(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout, float x, float y, int color) {
        rect(graphics, layout, x + 2.0F, y - 2.0F, 4.0F, 1.0F, color);
        outlinedRect(graphics, layout, x, y, 8.0F, 11.0F, 0x00000000);
        rect(graphics, layout, x + 2.0F, y + 2.0F, 4.0F, 7.0F, color);
    }

    private static void compassMarker(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout, float centerX, float centerY, float width, float height, boolean major) {
        if (!major) {
            dot(graphics, layout, centerX, centerY, width, height, SOFT_WHITE);
            return;
        }

        int w = layout.is(width);
        int h = layout.is(height);
        int x = layout.ix(centerX);
        int y = layout.iy(centerY);
        graphics.fill(x - (w / 2) - 1, y - (h / 2) - 1, x + (w / 2) + 2, y + (h / 2) + 2, 0x99000000);
        graphics.fill(x - (w / 2), y - (h / 2), x + (w / 2) + 1, y - (h / 2) + 1, WHITE);
        graphics.fill(x - (w / 2), y + (h / 2), x + (w / 2) + 1, y + (h / 2) + 1, WHITE);
        graphics.fill(x - (w / 2), y - (h / 2), x - (w / 2) + 1, y + (h / 2) + 1, WHITE);
        graphics.fill(x + (w / 2), y - (h / 2), x + (w / 2) + 1, y + (h / 2) + 1, WHITE);
    }

    private static void dot(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout, float centerX, float centerY, float width, float height, int color) {
        int w = layout.is(width);
        int h = layout.is(height);
        int x = layout.ix(centerX);
        int y = layout.iy(centerY);
        graphics.fill(x - (w / 2) - 1, y - (h / 2) - 1, x + (w / 2) + 2, y + (h / 2) + 2, 0x99000000);
        graphics.fill(x - (w / 2), y - (h / 2), x + (w / 2) + 1, y + (h / 2) + 1, color);
    }

    private static void rect(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout, float x, float y, float width, float height, int color) {
        int left = layout.ix(x);
        int top = layout.iy(y);
        graphics.fill(left, top, left + layout.is(width), top + layout.is(height), color);
    }

    private static void outlinedRect(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout, float x, float y, float width, float height, int color) {
        int left = layout.ix(x);
        int top = layout.iy(y);
        int right = left + layout.is(width);
        int bottom = top + layout.is(height);
        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, 0x99000000);
        if ((color >>> 24) != 0) {
            graphics.fill(left, top, right, bottom, color);
        }
    }

    private static void suppressActionBarMessage() {
        if (shouldReplaceDefaultSbwHud()) {
            Minecraft.getInstance().gui.setOverlayMessage(Component.empty(), false);
        }
    }

    private static String three(double value) {
        int rounded = Math.max(0, Math.min(999, (int) Math.round(value)));
        return String.format(Locale.ROOT, "%03d", rounded);
    }

    private static String headingLabel(int heading) {
        return switch (Math.floorMod(heading, 360)) {
            case 0 -> "N";
            case 90 -> "E";
            case 180 -> "S";
            case 270 -> "W";
            default -> "";
        };
    }
}
