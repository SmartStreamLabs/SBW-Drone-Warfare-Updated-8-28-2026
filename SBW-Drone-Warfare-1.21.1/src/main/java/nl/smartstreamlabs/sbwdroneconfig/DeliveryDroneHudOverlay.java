package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class DeliveryDroneHudOverlay {
    static final String HUD_ID = "DELIVERY-HUD-BALANCED-A3";
    private static final String[] CARDINAL_LABELS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final int WHITE = 0xFFEAF7F4;
    private static final int MUTED = 0xFFC1D1CD;
    private static final int GREEN = 0xFF66FF9C;
    private static final int CYAN = 0xFF85E8FF;
    private static final int AMBER = 0xFFFFC64A;
    private static final int RED = 0xFFFF5A5A;
    private static final int SHADOW = 0xB0000000;
    private static final int BACKDROP = 0x33000000;
    private static final int LINE_FAINT = 0x6685E8FF;
    private static final int LINE_SOFT = 0x9A85E8FF;
    private static final long TRANSIENT_NOTICE_MS = 2_600L;
    private static final Set<String> HIDDEN_OVERLAYS = Set.of(
            "crosshair",
            "hotbar",
            "selected_item_name",
            "item_name",
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

    private static UUID sessionDroneId;
    private static BlockPos lastDestination;
    private static boolean lastCargoMounted;
    private static String lastStatus = "";
    private static String transientNotice = "";
    private static long transientNoticeUntilMs;
    private static boolean hasSmoothedValues;
    private static float smoothedHeading;
    private static float smoothedPitch;
    private static float smoothedRoll;
    private static float smoothedSpeed;
    private static float smoothedVerticalSpeed;
    private static float smoothedSignal;

    private DeliveryDroneHudOverlay() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(DeliveryDroneHudOverlay::onClientSetup);
    }

    public static boolean shouldReplaceDefaultSbwHud() {
        return DroneClientViewContext.isLocalDeliveryViewOf(DroneClientViewContext.activeDeliveryDrone(Minecraft.getInstance()));
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(DeliveryDroneHudOverlay.class);
        SbwDroneRangeConfig.LOGGER.info("[SBW Drone Warfare] Registered Delivery Drone HUD {}", HUD_ID);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) { if(DroneOsdClient.ownsFullView())return;
        Minecraft minecraft = Minecraft.getInstance();
        DeliveryDroneEntity drone = DroneClientViewContext.activeDeliveryDrone(minecraft);
        if (!DroneClientViewContext.isLocalDeliveryViewOf(drone)) {
            clearSession();
            return;
        }

        Snapshot snapshot = Snapshot.collect(drone, event.getPartialTick().getGameTimeDeltaPartialTick(true));
        updateSessionState(drone.getUUID(), snapshot);
        updateSmoothedVisuals(snapshot);

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int margin = Mth.clamp(Math.round(Math.min(width, height) * 0.026F), 10, 24);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        renderFrameDetails(graphics, font, width, height, margin);
        renderTitle(graphics, font, snapshot, width);
        renderHeadingCompass(graphics, font, snapshot, width);
        renderArtificialHorizon(graphics, font, snapshot, width, height);
        renderFlightStrip(graphics, font, snapshot, margin, height);
        renderPowerLinkStrip(graphics, font, snapshot, width, margin);
        renderCargoBay(graphics, font, snapshot, width, height, margin);
        renderMissionStrip(graphics, font, snapshot, width, height, margin);
        renderDestinationMarker(graphics, font, snapshot, width, height, margin);
        renderWarningsAndNotice(graphics, font, snapshot, width);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiLayerEvent.Pre event) { if(DroneOsdClient.ownsFullView())return;
        if (!shouldReplaceDefaultSbwHud() || event.getName() == null) {
            return;
        }
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

    private static void renderFrameDetails(GuiGraphics graphics, Font font, int screenWidth, int screenHeight, int margin) {
        int bracket = Mth.clamp(Math.min(screenWidth, screenHeight) / 15, 42, 86);
        drawCorner(graphics, margin, margin, bracket, 1, 1, LINE_FAINT);
        drawCorner(graphics, screenWidth - margin, margin, bracket, -1, 1, LINE_FAINT);
        drawCorner(graphics, margin, screenHeight - margin, bracket, 1, -1, LINE_FAINT);
        drawCorner(graphics, screenWidth - margin, screenHeight - margin, bracket, -1, -1, LINE_FAINT);

        int guideHalf = Math.min(210, Math.max(110, screenWidth / 8));
        int centerX = screenWidth / 2;
        graphics.fill(centerX - guideHalf, 56, centerX - 28, 57, 0x3385E8FF);
        graphics.fill(centerX + 28, 56, centerX + guideHalf, 57, 0x3385E8FF);
        graphics.fill(centerX - guideHalf, screenHeight - 42, centerX - 45, screenHeight - 41, 0x2A85E8FF);
        graphics.fill(centerX + 45, screenHeight - 42, centerX + guideHalf, screenHeight - 41, 0x2A85E8FF);

        drawText(graphics, font, "CAM 01", margin + 10, margin + 8, MUTED);
        drawText(graphics, font, "LIVE", margin + 10, margin + 20, GREEN);
    }

    private static void renderTitle(GuiGraphics graphics, Font font, Snapshot snapshot, int screenWidth) {
        int centerX = screenWidth / 2;
        drawCentered(graphics, font, "DELIVERY DRONE", centerX, 8, WHITE);
        drawCentered(graphics, font, snapshot.status, centerX, 20, snapshot.statusColor());
        graphics.fill(centerX - 54, 34, centerX - 10, 35, LINE_FAINT);
        graphics.fill(centerX + 10, 34, centerX + 54, 35, LINE_FAINT);
    }

    private static void renderHeadingCompass(GuiGraphics graphics, Font font, Snapshot snapshot, int screenWidth) {
        int centerX = screenWidth / 2;
        int y = 41;
        int tapeHalf = Mth.clamp(screenWidth / 7, 118, 210);
        int left = centerX - tapeHalf;
        int right = centerX + tapeHalf;
        float pixelsPerDegree = tapeHalf / 62.0F;
        int startTick = (int) Math.floor((smoothedHeading - 75.0F) / 15.0F) * 15;

        drawBackdrop(graphics, left - 8, y - 8, (tapeHalf * 2) + 16, 30, BACKDROP);
        graphics.fill(left, y + 12, right, y + 13, 0x5585E8FF);
        for (int tick = startTick; tick <= startTick + 165; tick += 15) {
            float normalizedTick = normalizeDegrees(tick);
            float delta = Mth.wrapDegrees(normalizedTick - smoothedHeading);
            int x = Math.round(centerX + (delta * pixelsPerDegree));
            if (x < left - 8 || x > right + 8) {
                continue;
            }

            boolean cardinal = Math.floorMod(tick, 45) == 0;
            int tickHeight = cardinal ? 9 : 5;
            graphics.fill(x, y + 12, x + 1, y + 12 + tickHeight, cardinal ? LINE_SOFT : LINE_FAINT);
            String label = cardinal ? cardinalLabel(normalizedTick) : headingText(normalizedTick);
            drawCentered(graphics, font, label, x, y, cardinal ? WHITE : MUTED);
        }

        graphics.fill(centerX - 4, y + 24, centerX + 5, y + 25, WHITE);
        graphics.fill(centerX - 2, y + 22, centerX + 3, y + 24, WHITE);
        drawCentered(graphics, font, headingText(smoothedHeading), centerX, y + 31, CYAN);

        if (snapshot.hasDestination) {
            float destinationDelta = Mth.wrapDegrees(snapshot.destinationBearing - smoothedHeading);
            int markerX = Mth.clamp(Math.round(centerX + (destinationDelta * pixelsPerDegree)), left, right);
            drawSmallDiamond(graphics, markerX, y + 23, AMBER);
        }
    }

    private static void renderArtificialHorizon(GuiGraphics graphics, Font font, Snapshot snapshot, int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        float pitchOffset = Mth.clamp(smoothedPitch * 1.25F, -38.0F, 38.0F);
        float roll = Mth.clamp(smoothedRoll, -22.0F, 22.0F);
        int color = snapshot.hasDestination && snapshot.cargoMounted ? CYAN : WHITE;

        renderCenterGridMarks(graphics, centerX, centerY);
        drawRotatedLine(graphics, centerX - 34, centerY + pitchOffset, centerX - 9, centerY + pitchOffset, roll, color);
        drawRotatedLine(graphics, centerX + 9, centerY + pitchOffset, centerX + 34, centerY + pitchOffset, roll, color);
        graphics.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, color);

        for (int pitchMark = -10; pitchMark <= 10; pitchMark += 5) {
            if (pitchMark == 0) {
                continue;
            }
            float yOffset = pitchOffset - (pitchMark * 2.1F);
            if (Math.abs(yOffset) > 40.0F) {
                continue;
            }
            int halfWidth = pitchMark % 10 == 0 ? 14 : 8;
            int markColor = pitchMark > 0 ? 0x88EAF7F4 : 0x7785E8FF;
            drawRotatedLine(graphics, centerX - halfWidth, centerY + yOffset, centerX - 3, centerY + yOffset, roll, markColor);
            drawRotatedLine(graphics, centerX + 3, centerY + yOffset, centerX + halfWidth, centerY + yOffset, roll, markColor);
            if (Math.abs(pitchMark) == 10) {
                drawCentered(graphics, font, Integer.toString(pitchMark), centerX, Math.round(centerY + yOffset) + 4, MUTED);
            }
        }

        if (Math.abs(snapshot.pitchDegrees) >= 12.0F) {
            drawCentered(graphics, font, String.format(Locale.ROOT, "PITCH %+.0f", snapshot.pitchDegrees), centerX, centerY + 47, MUTED);
        }
        if (snapshot.hasDestination && snapshot.destinationDistance <= 8.0D && snapshot.cargoMounted) {
            graphics.fill(centerX - 22, centerY + 28, centerX - 7, centerY + 29, AMBER);
            graphics.fill(centerX + 7, centerY + 28, centerX + 22, centerY + 29, AMBER);
            graphics.fill(centerX - 22, centerY + 28, centerX - 21, centerY + 38, AMBER);
            graphics.fill(centerX + 21, centerY + 28, centerX + 22, centerY + 38, AMBER);
            drawCentered(graphics, font, "DELIVERY ZONE", centerX, centerY + 43, AMBER);
        }
    }

    private static void renderCenterGridMarks(GuiGraphics graphics, int centerX, int centerY) {
        graphics.fill(centerX - 56, centerY - 28, centerX - 50, centerY - 27, 0x2B85E8FF);
        graphics.fill(centerX + 50, centerY - 28, centerX + 56, centerY - 27, 0x2B85E8FF);
        graphics.fill(centerX - 56, centerY + 28, centerX - 50, centerY + 29, 0x2B85E8FF);
        graphics.fill(centerX + 50, centerY + 28, centerX + 56, centerY + 29, 0x2B85E8FF);
        graphics.fill(centerX - 72, centerY, centerX - 64, centerY + 1, 0x2485E8FF);
        graphics.fill(centerX + 64, centerY, centerX + 72, centerY + 1, 0x2485E8FF);
    }

    private static void renderFlightStrip(GuiGraphics graphics, Font font, Snapshot snapshot, int margin, int screenHeight) {
        int x = margin + 8;
        int y = screenHeight - margin - 88;
        int width = 94;
        int lineX = x - 8;

        drawBackdrop(graphics, x - 14, y - 9, width + 18, 86, BACKDROP);
        graphics.fill(lineX, y - 5, lineX + 1, y + 70, LINE_SOFT);
        for (int index = 0; index < 4; index++) {
            int tickY = y + (index * 20);
            graphics.fill(lineX, tickY, lineX + 6, tickY + 1, LINE_FAINT);
        }

        drawText(graphics, font, "SPD", x, y, CYAN);
        drawText(graphics, font, speed(smoothedSpeed), x + 34, y, WHITE);
        drawText(graphics, font, "ALT", x, y + 18, CYAN);
        drawText(graphics, font, Math.round(snapshot.altitudeMeters) + " m", x + 34, y + 18, WHITE);
        drawText(graphics, font, "V/S", x, y + 36, CYAN);
        drawText(graphics, font, verticalSpeed(smoothedVerticalSpeed), x + 34, y + 36, snapshot.verticalSpeedColor());
        drawText(graphics, font, "HDG", x, y + 54, CYAN);
        drawText(graphics, font, headingText(smoothedHeading), x + 34, y + 54, WHITE);
    }

    private static void renderPowerLinkStrip(GuiGraphics graphics, Font font, Snapshot snapshot, int screenWidth, int margin) {
        int width = 128;
        int x = screenWidth - margin - width;
        int y = margin + 26;

        drawBackdrop(graphics, x - 8, y - 8, width + 14, 66, BACKDROP);
        graphics.fill(x - 8, y - 4, x - 7, y + 52, LINE_SOFT);
        drawText(graphics, font, "LINK", x, y, CYAN);
        renderSignalBars(graphics, x + 43, y + 9, Math.round(smoothedSignal), snapshot.signalColor());
        drawText(graphics, font, Math.round(smoothedSignal) + "%", x + 76, y, snapshot.signalColor());

        if (snapshot.hasBattery) {
            drawText(graphics, font, "POWER", x, y + 18, CYAN);
            renderBattery(graphics, x + 48, y + 20, 38, 8, snapshot.batteryPercent, snapshot.batteryColor());
            drawText(graphics, font, snapshot.batteryText, x + 91, y + 18, snapshot.batteryColor());
        }

        if (snapshot.hardJammed) {
            drawText(graphics, font, "OFFLINE", x, y + 38, RED);
        } else if (snapshot.fiberActive) {
            drawText(graphics, font, "FIBER", x, y + 38, CYAN);
        } else if (snapshot.hasDestination) {
            drawText(graphics, font, "NAV LOCK", x, y + 38, GREEN);
        }
    }

    private static void renderCargoBay(GuiGraphics graphics, Font font, Snapshot snapshot, int screenWidth, int screenHeight, int margin) {
        int width = 112;
        int x = screenWidth - margin - width;
        int y = screenHeight - margin - 56;
        int color = snapshot.cargoColor();

        drawBackdrop(graphics, x - 8, y - 8, width + 14, 54, BACKDROP);
        drawText(graphics, font, "CARGO BAY", x, y, CYAN);
        if(snapshot.robotMounted){drawText(graphics,font,"GROUND ROBOT",x,y+14,CYAN);drawText(graphics,font,"Y: DEPLOY",x,y+29,WHITE);return;}
        if (!snapshot.cargoMounted) {
            drawText(graphics, font, "EMPTY", x, y + 14, MUTED);
            renderCargoSegments(graphics, x, y + 29, width - 18, 0, DeliveryDroneEntity.MOUNTED_CRATE_SLOT_CAPACITY, MUTED);
            return;
        }

        drawText(graphics, font, snapshot.cargoSlots + " / " + snapshot.cargoCapacity, x, y + 14, color);
        renderCargoSegments(graphics, x, y + 29, width - 18, snapshot.cargoSlots, snapshot.cargoCapacity, color);
    }

    private static void renderMissionStrip(GuiGraphics graphics, Font font, Snapshot snapshot, int screenWidth, int screenHeight, int margin) {
        int centerX = screenWidth / 2;
        int y = screenHeight - margin - 48;
        int maxWidth = Math.max(140, screenWidth - 300);

        if (!snapshot.hasDestination) {
            drawBackdrop(graphics, centerX - 74, y - 4, 148, 27, BACKDROP);
            drawCentered(graphics, font, "NO DESTINATION", centerX, y, AMBER);
            drawCentered(graphics, font, "SET DELIVERY TARGET", centerX, y + 12, MUTED);
            return;
        }

        String distanceEta = meters(snapshot.destinationDistance);
        if (!snapshot.etaText.isBlank()) {
            distanceEta += "  ETA " + snapshot.etaText;
        }
        String headingLine = snapshot.destinationDistance <= 12.0D ? "APPROACHING DELIVERY ZONE" : "DROP ZONE";
        drawBackdrop(graphics, centerX - 96, y - 7, 192, 45, BACKDROP);
        drawCentered(graphics, font, fit(font, headingLine, maxWidth), centerX, y - 1, AMBER);
        drawCentered(graphics, font, fit(font, distanceEta, maxWidth), centerX, y + 11, WHITE);
        if (snapshot.cargoMounted) {
            renderRouteStageLine(graphics, font, centerX, y + 29, snapshot);
        }
    }

    private static void renderRouteStageLine(GuiGraphics graphics, Font font, int centerX, int y, Snapshot snapshot) {
        int left = centerX - 82;
        int right = centerX + 82;
        int dotX = switch (snapshot.status) {
            case "DELIVERING" -> right - 12;
            case "APPROACHING" -> centerX + 46;
            case "EN ROUTE" -> centerX;
            default -> left + 12;
        };
        graphics.fill(left + 34, y + 4, right - 44, y + 5, 0x6685E8FF);
        graphics.fill(dotX - 2, y + 2, dotX + 3, y + 7, snapshot.statusColor());
        drawText(graphics, font, "PICKUP", left, y, MUTED);
        drawText(graphics, font, "DELIVERY", right - font.width("DELIVERY"), y, MUTED);
    }

    private static void renderDestinationMarker(GuiGraphics graphics, Font font, Snapshot snapshot, int screenWidth, int screenHeight, int margin) {
        if (!snapshot.hasDestination || !snapshot.cargoMounted) {
            return;
        }

        int safe = margin + 28;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        float delta = Mth.wrapDegrees(snapshot.destinationBearing - smoothedHeading);
        int x = centerX + Math.round(delta / 70.0F * 150.0F);
        boolean clamped = x < safe || x > screenWidth - safe;
        x = Mth.clamp(x, safe, screenWidth - safe);
        int y = Mth.clamp(centerY - 100 + Math.round(Mth.clamp(snapshot.pitchDegrees, -20.0F, 20.0F)), safe + 56, screenHeight - safe - 88);
        int color = snapshot.destinationDistance <= 16.0D ? AMBER : CYAN;

        drawSmallDiamond(graphics, x, y, color);
        if (clamped) {
            drawText(graphics, font, delta < 0.0F ? "<" : ">", x + (delta < 0.0F ? 9 : -13), y - 4, color);
        }
        String label = snapshot.destinationDistance <= 16.0D ? "APPROACHING" : "DROP";
        drawCentered(graphics, font, label, x, y + 9, color);
        drawCentered(graphics, font, meters(snapshot.destinationDistance), x, y + 21, MUTED);
    }

    private static void renderWarningsAndNotice(GuiGraphics graphics, Font font, Snapshot snapshot, int screenWidth) {
        int y = 70;
        if (snapshot.hardJammed) {
            drawCentered(graphics, font, "SIGNAL LOST", screenWidth / 2, y, RED);
            y += 12;
        } else if (snapshot.signalPercent <= 30) {
            drawCentered(graphics, font, "WEAK SIGNAL", screenWidth / 2, y, AMBER);
            y += 12;
        }

        if (snapshot.hasBattery && snapshot.batteryPercent <= 20.0D) {
            drawCentered(graphics, font, "LOW POWER - RETURN ADVISED", screenWidth / 2, y, snapshot.batteryPercent <= 10.0D ? RED : AMBER);
            y += 12;
        }

        long now = System.currentTimeMillis();
        if (!transientNotice.isBlank() && now <= transientNoticeUntilMs) {
            int alpha = fadeAlpha(now, transientNoticeUntilMs);
            drawCentered(graphics, font, transientNotice, screenWidth / 2, y, withAlpha(GREEN, alpha));
        }
    }

    private static void updateSessionState(UUID droneId, Snapshot snapshot) {
        long now = System.currentTimeMillis();
        if (!droneId.equals(sessionDroneId)) {
            sessionDroneId = droneId;
            lastDestination = snapshot.destination;
            lastCargoMounted = snapshot.cargoMounted;
            lastStatus = snapshot.status;
            transientNotice = "";
            transientNoticeUntilMs = 0L;
            hasSmoothedValues = false;
            return;
        }

        if (!sameBlockPos(lastDestination, snapshot.destination) && snapshot.hasDestination) {
            showNotice("DESTINATION SET", now);
        }
        if (!lastCargoMounted && snapshot.cargoMounted) {
            showNotice("CARGO LOADED", now);
        } else if (lastCargoMounted && !snapshot.cargoMounted) {
            showNotice("DELIVERY COMPLETE", now);
        } else if (!lastStatus.equals(snapshot.status) && "APPROACHING".equals(snapshot.status)) {
            showNotice("APPROACHING DELIVERY ZONE", now);
        }

        lastDestination = snapshot.destination;
        lastCargoMounted = snapshot.cargoMounted;
        lastStatus = snapshot.status;
    }

    private static void updateSmoothedVisuals(Snapshot snapshot) {
        if (!hasSmoothedValues) {
            smoothedHeading = snapshot.headingDegrees;
            smoothedPitch = snapshot.pitchDegrees;
            smoothedRoll = snapshot.rollDegrees;
            smoothedSpeed = (float) snapshot.speedMetersPerSecond;
            smoothedVerticalSpeed = (float) snapshot.verticalSpeed;
            smoothedSignal = snapshot.signalPercent;
            hasSmoothedValues = true;
            return;
        }

        smoothedHeading = smoothAngle(smoothedHeading, snapshot.headingDegrees, 0.24F);
        smoothedPitch = Mth.lerp(0.22F, smoothedPitch, snapshot.pitchDegrees);
        smoothedRoll = Mth.lerp(0.20F, smoothedRoll, snapshot.rollDegrees);
        smoothedSpeed = Mth.lerp(0.18F, smoothedSpeed, (float) snapshot.speedMetersPerSecond);
        smoothedVerticalSpeed = Mth.lerp(0.18F, smoothedVerticalSpeed, (float) snapshot.verticalSpeed);
        smoothedSignal = Mth.lerp(0.14F, smoothedSignal, snapshot.signalPercent);
    }

    private static float smoothAngle(float current, float target, float amount) {
        return normalizeDegrees(current + (Mth.wrapDegrees(target - current) * amount));
    }

    private static void showNotice(String text, long now) {
        transientNotice = text;
        transientNoticeUntilMs = now + TRANSIENT_NOTICE_MS;
    }

    private static void clearSession() {
        sessionDroneId = null;
        lastDestination = null;
        lastCargoMounted = false;
        lastStatus = "";
        transientNotice = "";
        transientNoticeUntilMs = 0L;
        hasSmoothedValues = false;
    }

    private static boolean sameBlockPos(BlockPos left, BlockPos right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.equals(right);
    }

    private static void renderSignalBars(GuiGraphics graphics, int x, int baselineY, int signalPercent, int color) {
        int activeBars = Mth.clamp((int) Math.ceil(signalPercent / 25.0D), 0, 4);
        for (int index = 0; index < 4; index++) {
            int barHeight = 4 + (index * 3);
            int barColor = index < activeBars ? color : 0x76576363;
            graphics.fill(x + (index * 6), baselineY - barHeight, x + (index * 6) + 4, baselineY, barColor);
        }
    }

    private static void renderBattery(GuiGraphics graphics, int x, int y, int width, int height, double percent, int color) {
        graphics.fill(x, y, x + width, y + 1, MUTED);
        graphics.fill(x, y + height - 1, x + width, y + height, MUTED);
        graphics.fill(x, y, x + 1, y + height, MUTED);
        graphics.fill(x + width - 1, y, x + width, y + height, MUTED);
        graphics.fill(x + width, y + 2, x + width + 2, y + height - 2, MUTED);
        int fillWidth = Mth.clamp(Math.round((width - 3) * ((float) percent / 100.0F)), 0, width - 3);
        if (fillWidth > 0) {
            graphics.fill(x + 2, y + 2, x + 2 + fillWidth, y + height - 2, color);
        }
    }

    private static void renderCargoSegments(GuiGraphics graphics, int x, int y, int width, int occupied, int capacity, int color) {
        int segments = 9;
        int gap = 2;
        int segmentWidth = Math.max(4, (width - ((segments - 1) * gap)) / segments);
        int active = capacity <= 0 ? 0 : Mth.clamp((int) Math.ceil((occupied / (double) capacity) * segments), 0, segments);
        for (int index = 0; index < segments; index++) {
            int sx = x + (index * (segmentWidth + gap));
            int segmentColor = index < active ? color : 0x66576363;
            graphics.fill(sx, y, sx + segmentWidth, y + 5, segmentColor);
        }
    }

    private static void drawCorner(GuiGraphics graphics, int x, int y, int length, int directionX, int directionY, int color) {
        int horizontalEnd = x + (length * directionX);
        int verticalEnd = y + (length * directionY);
        graphics.fill(Math.min(x, horizontalEnd), y - 1, Math.max(x, horizontalEnd), y + 1, color);
        graphics.fill(x - 1, Math.min(y, verticalEnd), x + 1, Math.max(y, verticalEnd), color);
    }

    private static void drawSmallDiamond(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x, y - 5, x + 1, y - 4, color);
        graphics.fill(x - 2, y - 3, x + 3, y - 2, color);
        graphics.fill(x - 4, y - 1, x + 5, y, color);
        graphics.fill(x - 2, y + 1, x + 3, y + 2, color);
        graphics.fill(x, y + 3, x + 1, y + 4, color);
        graphics.fill(x, y - 2, x + 1, y + 2, 0xAA000000);
    }

    private static void drawRotatedLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, float degrees, int color) {
        float centerX = (x1 + x2) * 0.5F;
        float centerY = (y1 + y2) * 0.5F;
        double radians = Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float rx1 = centerX + ((x1 - centerX) * cos) - ((y1 - centerY) * sin);
        float ry1 = centerY + ((x1 - centerX) * sin) + ((y1 - centerY) * cos);
        float rx2 = centerX + ((x2 - centerX) * cos) - ((y2 - centerY) * sin);
        float ry2 = centerY + ((x2 - centerX) * sin) + ((y2 - centerY) * cos);
        drawLine(graphics, rx1, ry1, rx2, ry2, color);
    }

    private static void drawLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, int color) {
        int steps = Math.max(1, Math.round(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1))));
        for (int index = 0; index <= steps; index++) {
            float t = index / (float) steps;
            int x = Math.round(Mth.lerp(t, x1, x2));
            int y = Math.round(Mth.lerp(t, y1, y2));
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static void drawText(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawString(font, text, x + 1, y + 1, SHADOW, false);
        graphics.drawString(font, text, x, y, color, false);
    }

    private static void drawCentered(GuiGraphics graphics, Font font, String text, int centerX, int y, int color) {
        drawText(graphics, font, text, centerX - (font.width(text) / 2), y, color);
    }

    private static void drawBackdrop(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    private static String fit(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int end = text.length();
        while (end > 0 && font.width(text.substring(0, end) + suffix) > maxWidth) {
            end--;
        }
        return text.substring(0, Math.max(0, end)) + suffix;
    }

    private static String speed(double value) {
        return String.format(Locale.ROOT, "%.1f m/s", Math.max(0.0D, value));
    }

    private static String verticalSpeed(double value) {
        return String.format(Locale.ROOT, "%+.1f", value);
    }

    private static String meters(double value) {
        return String.format(Locale.ROOT, "%.0f m", Math.max(0.0D, value));
    }

    private static String headingText(float value) {
        return String.format(Locale.ROOT, "%03d", Math.floorMod(Math.round(value), 360));
    }

    private static String cardinalLabel(float heading) {
        int index = Math.floorMod(Math.round(heading / 45.0F), CARDINAL_LABELS.length);
        return CARDINAL_LABELS[index];
    }

    private static String eta(double distanceMeters, double speedMetersPerSecond) {
        if (Double.isNaN(distanceMeters) || speedMetersPerSecond < 1.0D) {
            return "";
        }
        int seconds = Mth.clamp((int) Math.ceil(distanceMeters / speedMetersPerSecond), 0, 99 * 60 + 59);
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }

    private static int fadeAlpha(long now, long endTime) {
        long remaining = Math.max(0L, endTime - now);
        if (remaining >= 700L) {
            return 255;
        }
        return Mth.clamp(Math.round(255.0F * (remaining / 700.0F)), 0, 255);
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (Mth.clamp(alpha, 0, 255) << 24);
    }

    private static float normalizeDegrees(float degrees) {
        return (float) (((degrees % 360.0D) + 360.0D) % 360.0D);
    }

    private record Snapshot(
            boolean robotMounted,
            boolean cargoMounted,
            int cargoSlots,
            int cargoCapacity,
            BlockPos destination,
            boolean hasDestination,
            double destinationDistance,
            float destinationBearing,
            float headingDegrees,
            float pitchDegrees,
            float rollDegrees,
            double speedMetersPerSecond,
            double altitudeMeters,
            double verticalSpeed,
            boolean hasBattery,
            double batteryPercent,
            String batteryText,
            int signalPercent,
            boolean hardJammed,
            boolean fiberActive,
            String status,
            String etaText
    ) {
        static Snapshot collect(DeliveryDroneEntity drone, float partialTick) {
            Vec3 velocity = drone.getDeltaMovement();
            BlockPos target = drone.getSyncedDeliveryTarget();
            boolean hasTarget = target != null;
            double distance = hasTarget
                    ? Math.sqrt(drone.distanceToSqr(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D))
                    : Double.NaN;
            float heading = normalizeDegrees(Mth.rotLerp(partialTick, drone.yRotO, drone.getYRot()));
            float pitch = Mth.clamp(Mth.rotLerp(partialTick, drone.xRotO, drone.getXRot()), -45.0F, 45.0F);
            float yawRateBank = Mth.clamp(-Mth.wrapDegrees(drone.getYRot() - drone.yRotO) * 3.8F, -18.0F, 18.0F);
            float bearing = hasTarget ? bearingTo(drone.position(), target) : heading;
            double horizontalSpeed = Math.sqrt((velocity.x * velocity.x) + (velocity.z * velocity.z)) * 20.0D;
            double altitude = Math.max(0.0D, drone.getY());
            double battery = DroneEnergyClient.getBatteryPercentFor(drone.getUUID());
            boolean hasBattery = !Double.isNaN(battery);
            int signal = Mth.clamp(Math.round((1.0F - DroneJamOverlayClient.getJamProgressFor(drone.getUUID())) * 100.0F), 0, 100);
            boolean hardJammed = DroneJamOverlayClient.isHardJammed(drone.getUUID());
            boolean fiber = DroneFiberOpticClient.isFiberHudActiveFor(drone.getUUID());
            boolean cargoMounted = drone.hasMountedExtractionCrate();
            int occupiedSlots = drone.getMountedCargoOccupiedSlots();
            String status = status(cargoMounted, hasTarget, distance, hasBattery, battery, hardJammed);

            return new Snapshot(
                    DeliveryGroundCargo.loaded(drone),
                    cargoMounted,
                    occupiedSlots,
                    drone.getMountedCargoSlotCapacity(),
                    target,
                    hasTarget,
                    distance,
                    bearing,
                    heading,
                    pitch,
                    yawRateBank,
                    horizontalSpeed,
                    altitude,
                    velocity.y * 20.0D,
                    hasBattery,
                    battery,
                    hasBattery ? Math.round(battery) + "%" : "",
                    signal,
                    hardJammed,
                    fiber,
                    status,
                    eta(distance, horizontalSpeed)
            );
        }

        private static String status(boolean cargoMounted, boolean hasTarget, double distance, boolean hasBattery, double battery, boolean hardJammed) {
            if (hardJammed) {
                return "SIGNAL LOST";
            }
            if (hasBattery && battery <= 15.0D) {
                return "LOW BATTERY";
            }
            if (hasTarget && cargoMounted) {
                if (distance <= 4.0D) {
                    return "DELIVERING";
                }
                return distance <= 16.0D ? "APPROACHING" : "EN ROUTE";
            }
            return "IDLE";
        }

        private static float bearingTo(Vec3 origin, BlockPos target) {
            double dx = (target.getX() + 0.5D) - origin.x;
            double dz = (target.getZ() + 0.5D) - origin.z;
            return normalizeDegrees((float) Math.toDegrees(Math.atan2(-dx, dz)));
        }

        int cargoColor() {
            if (!cargoMounted) {
                return MUTED;
            }
            if (cargoSlots >= cargoCapacity) {
                return AMBER;
            }
            return GREEN;
        }

        int batteryColor() {
            if (!hasBattery) {
                return MUTED;
            }
            if (batteryPercent <= 15.0D) {
                return RED;
            }
            if (batteryPercent <= 30.0D) {
                return AMBER;
            }
            return GREEN;
        }

        int signalColor() {
            if (hardJammed || signalPercent <= 15) {
                return RED;
            }
            if (signalPercent <= 45) {
                return AMBER;
            }
            return GREEN;
        }

        int statusColor() {
            if ("SIGNAL LOST".equals(status)) {
                return RED;
            }
            if ("LOW BATTERY".equals(status)) {
                return AMBER;
            }
            if ("EN ROUTE".equals(status) || "APPROACHING".equals(status) || "DELIVERING".equals(status)) {
                return GREEN;
            }
            return CYAN;
        }

        int verticalSpeedColor() {
            return Math.abs(verticalSpeed) < 0.15D ? MUTED : verticalSpeed > 0.0D ? GREEN : AMBER;
        }
    }
}
