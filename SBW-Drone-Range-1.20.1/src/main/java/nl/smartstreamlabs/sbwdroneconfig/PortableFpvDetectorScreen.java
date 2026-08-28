package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class PortableFpvDetectorScreen extends Screen {
    private static final int ANIMATION_TICKS = 8;
    private static final float BODY_ASPECT = 1.55F;
    private static final float TEXT_SCALE = 0.70F;
    private static final float SMALL_TEXT_SCALE = 0.62F;
    private static final int BODY = 0xF0050606;
    private static final int BODY_EDGE = 0xFF161A18;
    private static final int BODY_HIGHLIGHT = 0xFF252C28;
    private static final int DISPLAY_EDGE = 0xFF1C7A34;
    private static final int DISPLAY = 0xEF021207;
    private static final int DISPLAY_DIM = 0xAA031508;
    private static final int GREEN = 0xFF62FF7C;
    private static final int GREEN_DIM = 0xFF74A87D;
    private static final int TEXT = 0xFFD8FFE0;
    private static final int AMBER = 0xFFFFD66A;
    private static final int RED = 0xFFFF5A54;
    private static final int CHARCOAL = 0xFF101413;

    private final Map<UUID, Float> smoothedSignals = new HashMap<>();
    private final List<DeviceControl> controls = new ArrayList<>();
    private DroneJammerClient.DetectorSortMode sortMode = DroneJammerClient.DetectorSortMode.SIGNAL;
    private int openTicks;
    private int closeTicks;
    private boolean closing;
    private int bodyX;
    private int bodyY;
    private int bodyW;
    private int bodyH;
    private int displayX;
    private int displayY;
    private int displayW;
    private int displayH;
    private int targetListX;
    private int targetListY;
    private int targetListW;
    private int targetListH;
    private int targetRowStartY;
    private int targetRowHeight;
    private int visibleTargets;
    private int scrollOffset;
    private UUID lastSeenTargetId;
    private String lastSeenTargetName = "";
    private Component hoverTooltip;
    private Object openedLevel;

    public PortableFpvDetectorScreen() {
        super(Component.translatable("screen.sbwdroneconfig.fpv_detector.title"));
    }

    @Override
    protected void init() {
        clearWidgets();
        openedLevel = minecraft == null ? null : minecraft.level;
    }

    @Override
    public void tick() {
        if (minecraft == null || minecraft.player == null || minecraft.level == null || !minecraft.player.isAlive()) {
            closeImmediately();
            return;
        }
        if (openedLevel != null && minecraft.level != openedLevel) {
            beginClosing();
        }
        if (!isHoldingDetector(minecraft)) {
            beginClosing();
        }

        PortableFpvDetectorTarget target = DroneJammerClient.selectedTarget();
        if (target != null) {
            lastSeenTargetId = target.droneId();
            lastSeenTargetName = target.displayName();
        }

        if (closing) {
            closeTicks++;
            if (closeTicks >= ANIMATION_TICKS) {
                closeImmediately();
            }
        } else {
            openTicks++;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        hoverTooltip = null;
        Layout layout = layout(partialTick);
        int backdropAlpha = (int) (0x55 * layout.progress());
        guiGraphics.fill(0, 0, width, height, (backdropAlpha << 24));
        renderPhysicalReceiver(guiGraphics, layout, mouseX, mouseY, partialTick);
        if (hoverTooltip != null) {
            guiGraphics.renderTooltip(font, hoverTooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            beginClosing();
            return true;
        }
        if (keyCode == 263 || keyCode == 265) {
            DroneJammerClient.selectRelative(-1, sortMode);
            playButtonSound();
            return true;
        }
        if (keyCode == 262 || keyCode == 264) {
            DroneJammerClient.selectRelative(1, sortMode);
            playButtonSound();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || closing) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        for (DeviceControl control : controls) {
            if (control.contains(mouseX, mouseY)) {
                activateControl(control.id());
                playButtonSound();
                return true;
            }
        }

        if (inside(mouseX, mouseY, targetListX, targetRowStartY, targetListW, targetListH - (targetRowStartY - targetListY))) {
            int row = (int) ((mouseY - targetRowStartY) / Math.max(1, targetRowHeight));
            List<PortableFpvDetectorTarget> targets = DroneJammerClient.targets(sortMode);
            int index = scrollOffset + row;
            if (index >= 0 && index < targets.size()) {
                DroneJammerClient.selectTarget(targets.get(index).droneId());
                lastSeenTargetId = targets.get(index).droneId();
                lastSeenTargetName = targets.get(index).displayName();
                playButtonSound();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (inside(mouseX, mouseY, targetListX, targetListY, targetListW, targetListH)) {
            scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(scrollY), 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        beginClosing();
    }

    @Override
    public void removed() {
        PortableFpvDetectorVideoRenderer.release();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Layout layout(float partialTick) {
        float progress = animationProgress(partialTick);
        int maxW = Math.max(220, width - 24);
        int maxH = Math.max(150, height - 24);
        bodyW = Math.min(maxW, Math.max(240, Math.round(width * 0.64F)));
        bodyH = Math.round(bodyW / BODY_ASPECT);
        int targetMaxH = Math.round(height * 0.68F);
        if (bodyH > targetMaxH) {
            bodyH = Math.min(maxH, targetMaxH);
            bodyW = Math.round(bodyH * BODY_ASPECT);
        }
        bodyW = Math.min(bodyW, maxW);
        bodyH = Math.min(bodyH, maxH);
        bodyX = (width - bodyW) / 2;
        int restingY = (height - bodyH) / 2 + Math.round(height * 0.04F);
        bodyY = restingY + Math.round((1.0F - progress) * bodyH * 0.46F);

        displayX = bodyX + Math.round(bodyW * 0.075F);
        displayY = bodyY + Math.round(bodyH * 0.18F);
        displayW = Math.round(bodyW * 0.78F);
        displayH = Math.round(bodyH * 0.62F);
        return new Layout(progress);
    }

    private float animationProgress(float partialTick) {
        if (closing) {
            return 1.0F - Mth.clamp((closeTicks + partialTick) / ANIMATION_TICKS, 0.0F, 1.0F);
        }
        return Mth.clamp((openTicks + partialTick) / ANIMATION_TICKS, 0.0F, 1.0F);
    }

    private void renderPhysicalReceiver(GuiGraphics guiGraphics, Layout layout, int mouseX, int mouseY, float partialTick) {
        renderAntennas(guiGraphics);
        drawReceiverBody(guiGraphics);
        drawDisplay(guiGraphics, mouseX, mouseY, partialTick);
        drawPhysicalControls(guiGraphics, mouseX, mouseY);
    }

    private void renderAntennas(GuiGraphics guiGraphics) {
        int baseY = bodyY + Math.round(bodyH * 0.14F);
        int leftBaseX = bodyX + Math.round(bodyW * 0.22F);
        int rightBaseX = bodyX + Math.round(bodyW * 0.76F);
        guiGraphics.fill(leftBaseX - 3, baseY - 5, leftBaseX + 3, baseY + 7, BODY_EDGE);
        guiGraphics.fill(rightBaseX - 3, baseY - 4, rightBaseX + 3, baseY + 7, BODY_EDGE);
        drawLine(guiGraphics, leftBaseX, baseY - 5, leftBaseX - Math.round(bodyW * 0.08F), bodyY - Math.round(bodyH * 0.08F), 0xFF202723);
        drawLine(guiGraphics, rightBaseX, baseY - 5, rightBaseX + Math.round(bodyW * 0.06F), bodyY - Math.round(bodyH * 0.10F), 0xFF202723);
        drawLine(guiGraphics, leftBaseX + 1, baseY - 5, leftBaseX - Math.round(bodyW * 0.08F) + 1, bodyY - Math.round(bodyH * 0.08F), 0xFF080A09);
        drawLine(guiGraphics, rightBaseX + 1, baseY - 5, rightBaseX + Math.round(bodyW * 0.06F) + 1, bodyY - Math.round(bodyH * 0.10F), 0xFF080A09);
    }

    private void drawReceiverBody(GuiGraphics guiGraphics) {
        guiGraphics.fill(bodyX, bodyY, bodyX + bodyW, bodyY + bodyH, BODY_EDGE);
        guiGraphics.fill(bodyX + 4, bodyY + 4, bodyX + bodyW - 4, bodyY + bodyH - 4, BODY);
        guiGraphics.fill(bodyX + 8, bodyY + 8, bodyX + bodyW - 8, bodyY + bodyH - 8, 0xFF090C0B);

        for (int y = bodyY + 12; y < bodyY + bodyH - 12; y += 9) {
            guiGraphics.fill(bodyX + 12, y, bodyX + bodyW - 12, y + 1, 0x12000000);
        }
        for (int x = bodyX + 16; x < bodyX + bodyW - 16; x += 38) {
            guiGraphics.fill(x, bodyY + 10, x + 8, bodyY + bodyH - 10, 0x0500FF66);
        }

        int corner = Math.max(18, Math.round(bodyW * 0.045F));
        drawCornerBlock(guiGraphics, bodyX + 6, bodyY + 6, corner, corner);
        drawCornerBlock(guiGraphics, bodyX + bodyW - corner - 6, bodyY + 6, corner, corner);
        drawCornerBlock(guiGraphics, bodyX + 6, bodyY + bodyH - corner - 6, corner, corner);
        drawCornerBlock(guiGraphics, bodyX + bodyW - corner - 6, bodyY + bodyH - corner - 6, corner, corner);

        int sideX = displayX + displayW + Math.max(8, bodyW / 70);
        int buttonW = Math.max(12, bodyW / 34);
        int buttonH = Math.max(12, bodyH / 14);
        for (int i = 0; i < 4; i++) {
            int y = displayY + 8 + (i * (buttonH + 6));
            guiGraphics.fill(sideX, y, sideX + buttonW, y + buttonH, 0xFF151A17);
            guiGraphics.fill(sideX + 2, y + 2, sideX + buttonW - 2, y + buttonH - 2, 0xFF252C27);
        }

        int grilleX = sideX;
        int grilleY = displayY + displayH - Math.max(34, bodyH / 7);
        int grilleW = Math.max(28, bodyW / 13);
        int grilleH = Math.max(22, bodyH / 12);
        guiGraphics.fill(grilleX, grilleY, grilleX + grilleW, grilleY + grilleH, 0xFF0B0F0D);
        for (int i = 4; i < grilleW - 3; i += 6) {
            guiGraphics.fill(grilleX + i, grilleY + 4, grilleX + i + 2, grilleY + grilleH - 4, 0xFF2E3832);
        }

        int knobR = Math.max(10, bodyH / 18);
        int knobX = bodyX + bodyW - Math.round(bodyW * 0.11F);
        int knobY = bodyY + bodyH - Math.round(bodyH * 0.18F);
        guiGraphics.fill(knobX - knobR, knobY - knobR, knobX + knobR, knobY + knobR, 0xFF050707);
        guiGraphics.fill(knobX - knobR + 3, knobY - knobR + 3, knobX + knobR - 3, knobY + knobR - 3, 0xFF222925);
        drawLine(guiGraphics, knobX, knobY, knobX + knobR - 4, knobY - knobR + 5, 0xFF556258);

        guiGraphics.fill(bodyX + bodyW - 46, bodyY + 19, bodyX + bodyW - 39, bodyY + 26, GREEN);
        guiGraphics.fill(bodyX + bodyW - 34, bodyY + 19, bodyX + bodyW - 27, bodyY + 26, selectedTarget() == null ? RED : 0xFF3A140E);
    }

    private void drawCornerBlock(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        guiGraphics.fill(x, y, x + w, y + h, 0xFF151917);
        guiGraphics.fill(x + 3, y + 3, x + w - 3, y + h - 3, BODY_HIGHLIGHT);
    }

    private void drawDisplay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(displayX - 4, displayY - 4, displayX + displayW + 4, displayY + displayH + 4, 0xFF041207);
        guiGraphics.fill(displayX - 2, displayY - 2, displayX + displayW + 2, displayY + displayH + 2, DISPLAY_EDGE);
        guiGraphics.fill(displayX, displayY, displayX + displayW, displayY + displayH, DISPLAY);
        guiGraphics.fill(displayX + 1, displayY + 1, displayX + displayW - 1, displayY + displayH - 1, 0xEE021006);

        for (int y = displayY + 4; y < displayY + displayH - 3; y += 4) {
            guiGraphics.fill(displayX + 2, y, displayX + displayW - 2, y + 1, 0x180B4C18);
        }
        guiGraphics.fill(displayX + 8, displayY + 5, displayX + displayW - 8, displayY + 16, 0x1FFFFFFF);

        int pad = Math.max(5, Math.round(displayW * 0.018F));
        int topBarH = Math.max(17, Math.round(displayH * 0.105F));
        renderTopStatusBar(guiGraphics, pad, topBarH);

        int contentY = displayY + topBarH + pad + 2;
        int contentH = displayH - topBarH - (pad * 2) - 2;
        targetListX = displayX + pad;
        targetListY = contentY;
        targetListW = Math.round(displayW * 0.285F);
        targetListH = contentH;
        int gap = Math.max(5, Math.round(displayW * 0.018F));
        int rightX = targetListX + targetListW + gap;
        int rightW = displayX + displayW - pad - rightX;
        int infoH = Math.max(26, Math.round(displayH * 0.18F));
        int videoAreaH = Math.max(40, contentH - infoH - 5);
        int videoW = rightW;
        int videoH = Math.round(videoW * 9.0F / 16.0F);
        if (videoH > videoAreaH) {
            videoH = videoAreaH;
            videoW = Math.round(videoH * 16.0F / 9.0F);
        }
        int videoX = rightX + Math.max(0, (rightW - videoW) / 2);
        int videoY = contentY;
        int infoY = videoY + videoH + 5;

        renderTargetList(guiGraphics, mouseX, mouseY);
        renderVideoPanel(guiGraphics, videoX, videoY, videoW, videoH);
        renderInfoStrip(guiGraphics, rightX, infoY, rightW, Math.max(18, displayY + displayH - pad - infoY));
    }

    private void renderTopStatusBar(GuiGraphics guiGraphics, int pad, int topBarH) {
        guiGraphics.fill(displayX + 1, displayY + 1, displayX + displayW - 1, displayY + topBarH, DISPLAY_DIM);
        PortableFpvDetectorTarget target = selectedTarget();
        List<PortableFpvDetectorTarget> targets = DroneJammerClient.targets(sortMode);
        int selectedIndex = selectedIndex(targets);
        String count = targets.isEmpty() ? "SIGNAL --/--" : String.format(Locale.ROOT, "SIGNAL %02d/%02d", selectedIndex + 1, targets.size());
        String status = statusText(target);
        int statusColor = statusColor(target);

        drawScaled(guiGraphics, Component.literal("FPV RECEIVER"), displayX + pad + 2, displayY + 5, GREEN, TEXT_SCALE, false);
        drawCenteredScaled(guiGraphics, Component.literal(count), displayX + (displayW / 2), displayY + 5, TEXT, TEXT_SCALE, false);
        int statusWidth = Math.round(font.width(status) * TEXT_SCALE);
        int statusX = displayX + displayW - pad - statusWidth - 13;
        guiGraphics.fill(statusX - 11, displayY + 7, statusX - 5, displayY + 13, statusColor);
        drawScaled(guiGraphics, Component.literal(status), statusX, displayY + 5, statusColor, TEXT_SCALE, false);
    }

    private void renderTargetList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.fill(targetListX, targetListY, targetListX + targetListW, targetListY + targetListH, 0x99020B05);
        drawRect(guiGraphics, targetListX, targetListY, targetListW, targetListH, 0x553AFF62);
        drawScaled(guiGraphics, Component.literal("TARGETS"), targetListX + 6, targetListY + 5, GREEN, SMALL_TEXT_SCALE, false);

        targetRowHeight = Math.max(15, Math.round(displayH * 0.075F));
        targetRowStartY = targetListY + 19;
        visibleTargets = Math.max(1, (targetListY + targetListH - targetRowStartY - 4) / targetRowHeight);
        List<PortableFpvDetectorTarget> targets = DroneJammerClient.targets(sortMode);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll());

        if (targets.isEmpty()) {
            drawCenteredScaled(guiGraphics, Component.literal("NO WIRELESS SIGNALS"), targetListX + targetListW / 2, targetListY + targetListH / 2 - 4, GREEN_DIM, SMALL_TEXT_SCALE, false);
            return;
        }

        UUID selected = DroneJammerClient.latestScan().selectedTargetId();
        for (int visible = 0; visible < visibleTargets; visible++) {
            int index = scrollOffset + visible;
            if (index >= targets.size()) {
                break;
            }
            PortableFpvDetectorTarget target = targets.get(index);
            int rowY = targetRowStartY + (visible * targetRowHeight);
            boolean selectedRow = selected != null && selected.equals(target.droneId());
            boolean hovered = inside(mouseX, mouseY, targetListX + 3, rowY, targetListW - 6, targetRowHeight - 1);
            if (selectedRow || hovered) {
                guiGraphics.fill(targetListX + 3, rowY, targetListX + targetListW - 3, rowY + targetRowHeight - 1, selectedRow ? 0x552AFF56 : 0x332AFF56);
            }
            if (selectedRow) {
                drawRect(guiGraphics, targetListX + 3, rowY, targetListW - 6, targetRowHeight - 1, GREEN);
            }

            int markerColor = markerColor(target);
            guiGraphics.fill(targetListX + 7, rowY + 4, targetListX + 14, rowY + 11, markerColor);
            drawScaled(guiGraphics, Component.literal(markerLabel(target)), targetListX + 8, rowY + 3, 0xFF021006, 0.55F, false);
            String name = fitText(target.displayName(), targetListW - 72, SMALL_TEXT_SCALE);
            drawScaled(guiGraphics, Component.literal((selectedRow ? "> " : "  ") + name), targetListX + 18, rowY + 4, selectedRow ? GREEN : TEXT, SMALL_TEXT_SCALE, false);
            drawMiniSignalBars(guiGraphics, targetListX + targetListW - 38, rowY + 4, target, 4, false);
            if (hovered && !name.equals(target.displayName())) {
                hoverTooltip = Component.literal(target.displayName());
            }
        }

        if (maxScroll() > 0) {
            int scrollTrackX = targetListX + targetListW - 4;
            int trackH = targetListH - 25;
            int thumbH = Math.max(8, trackH * visibleTargets / targets.size());
            int thumbY = targetRowStartY + Math.round((trackH - thumbH) * (scrollOffset / (float) maxScroll()));
            guiGraphics.fill(scrollTrackX, targetRowStartY, scrollTrackX + 1, targetListY + targetListH - 5, 0x553AFF62);
            guiGraphics.fill(scrollTrackX - 1, thumbY, scrollTrackX + 2, thumbY + thumbH, GREEN);
        }
    }

    private void renderVideoPanel(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        PortableFpvDetectorTarget target = selectedTarget();
        int border = target == null ? GREEN_DIM : signalColor(target, smoothedSignal(target));
        guiGraphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, border);
        guiGraphics.fill(x, y, x + w, y + h, 0xFF010401);

        boolean renderedFeed = target != null && PortableFpvDetectorVideoRenderer.render(guiGraphics, target, x, y, w, h);
        if (renderedFeed) {
            renderVideoOverlay(guiGraphics, target, x, y, w, h);
        } else {
            renderVideoState(guiGraphics, target, x, y, w, h);
        }
    }

    private void renderVideoOverlay(GuiGraphics guiGraphics, PortableFpvDetectorTarget target, int x, int y, int w, int h) {
        DroneJammerClient.DirectionInfo direction = DroneJammerClient.directionInfo(minecraft.player, target);
        int topH = 17;
        int bottomH = Math.max(22, h / 5);
        int bottomY = y + h - bottomH;
        int rightReserved = 56;
        guiGraphics.fill(x, y, x + w, y + topH, 0x99010602);
        guiGraphics.fill(x, bottomY, x + w, y + h, 0x99010602);
        drawScaled(guiGraphics, Component.literal("INTERCEPT"), x + 5, y + 5, GREEN, SMALL_TEXT_SCALE, false);
        drawRightScaled(guiGraphics, Component.literal(DroneJammerClient.formatDistance(target.distance())), x + w - 5, y + 5, TEXT, SMALL_TEXT_SCALE, false);
        drawScaled(
                guiGraphics,
                Component.literal(fitText(target.displayName().toUpperCase(Locale.ROOT), Math.max(24, w - rightReserved - 10), SMALL_TEXT_SCALE)),
                x + 5,
                bottomY + 5,
                TEXT,
                SMALL_TEXT_SCALE,
                false
        );
        drawRightScaled(guiGraphics, Component.literal(direction.arrow() + " " + direction.cardinal()), x + w - 5, bottomY + 5, GREEN, SMALL_TEXT_SCALE, false);
        drawMiniSignalBars(guiGraphics, x + w - 42, y + h - 13, target, 5, true);
        if (target.signalStrength() >= 0.85D) {
            drawScaled(guiGraphics, Component.literal("DANGER"), x + 5, bottomY + 14, RED, 0.56F, false);
        }
    }

    private void renderVideoState(GuiGraphics guiGraphics, PortableFpvDetectorTarget target, int x, int y, int w, int h) {
        String label;
        int color = GREEN_DIM;
        if (target == null && lastSeenTargetId != null && DroneJammerClient.latestScan().detectorActive()) {
            label = "SIGNAL LOST";
            color = RED;
        } else if (target == null) {
            label = DroneJammerClient.latestScan().targets().isEmpty() ? "NO WIRELESS SIGNALS" : "SELECT A SIGNAL";
        } else if (!target.cameraAvailable()) {
            label = "VIDEO UNAVAILABLE";
        } else if (!DroneJammerClient.latestScan().videoEnabled()) {
            label = "VIDEO STANDBY";
        } else {
            label = "VIDEO STANDBY";
            color = AMBER;
        }

        long tick = minecraft == null || minecraft.level == null ? 0L : minecraft.level.getGameTime();
        for (int i = 0; i < 24; i++) {
            int yy = y + 3 + (int) Math.floorMod((i * 11L) + tick, Math.max(1, h - 6));
            guiGraphics.fill(x + 3, yy, x + w - 3, yy + 1, 0x142AFF56);
        }
        drawCenteredScaled(guiGraphics, Component.literal(label), x + w / 2, y + h / 2 - 4, color, TEXT_SCALE, false);
    }

    private void renderInfoStrip(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        PortableFpvDetectorTarget target = selectedTarget();
        guiGraphics.fill(x, y, x + w, y + h, 0x88020B05);
        drawRect(guiGraphics, x, y, w, h, 0x443AFF62);
        if (target == null) {
            String label = lastSeenTargetId == null ? "SELECT A SIGNAL" : "LAST: " + fitText(lastSeenTargetName, w - 40, TEXT_SCALE);
            drawCenteredScaled(guiGraphics, Component.literal(label), x + w / 2, y + h / 2 - 4, GREEN_DIM, TEXT_SCALE, false);
            return;
        }

        DroneJammerClient.DirectionInfo direction = DroneJammerClient.directionInfo(minecraft.player, target);
        int signal = Math.round(smoothedSignal(target) * 100.0F);
        String primary = fitText(target.displayName().toUpperCase(Locale.ROOT), Math.round(w * 0.30F), TEXT_SCALE)
                + " | " + direction.arrow() + " " + direction.cardinal()
                + " | " + DroneJammerClient.formatDistance(target.distance())
                + " | " + direction.vertical() + " " + Math.round(direction.heightDelta()) + " m";
        drawScaled(guiGraphics, Component.literal(primary), x + 6, y + 5, TEXT, TEXT_SCALE, false);

        String meter = AddonConfig.portableFpvDetectorShowSignalPercent()
                ? "SIGNAL " + signal + "% " + target.signalLabel()
                : "SIGNAL " + target.signalLabel();
        String owner = ownerLabel(target);
        String secondary = meter + " | LINK: " + target.linkMode().name()
                + (owner.isBlank() ? "" : " | " + owner)
                + " | VIDEO: " + (target.cameraAvailable() ? "ACTIVE" : "UNAVAILABLE");
        drawScaled(guiGraphics, Component.literal(fitText(secondary, w - 12, SMALL_TEXT_SCALE)), x + 6, y + Math.max(15, h - 12), signalColor(target, smoothedSignal(target)), SMALL_TEXT_SCALE, false);
    }

    private void drawPhysicalControls(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        controls.clear();
        int y = displayY + displayH + Math.max(8, bodyH / 30);
        int h = Math.max(14, bodyH / 17);
        int x = displayX + Math.max(4, displayW / 22);
        int gap = Math.max(5, bodyW / 95);
        controls.add(new DeviceControl("prev", "<", "Previous target", x, y, Math.max(22, bodyW / 18), h));
        x += controls.get(controls.size() - 1).w() + gap;
        controls.add(new DeviceControl("next", ">", "Next target", x, y, Math.max(22, bodyW / 18), h));
        x += controls.get(controls.size() - 1).w() + gap;
        controls.add(new DeviceControl("sort", "SORT: " + sortLabel(), "Change sorting", x, y, Math.max(62, bodyW / 8), h));
        x += controls.get(controls.size() - 1).w() + gap;
        controls.add(new DeviceControl("close", "EXIT", "Close feed", x, y, Math.max(38, bodyW / 13), h));

        for (DeviceControl control : controls) {
            boolean hovered = control.contains(mouseX, mouseY);
            int fill = hovered ? 0xFF1B2820 : CHARCOAL;
            int outline = hovered ? GREEN : 0xFF285134;
            guiGraphics.fill(control.x(), control.y(), control.x() + control.w(), control.y() + control.h(), 0xFF050807);
            guiGraphics.fill(control.x() + 1, control.y() + 1, control.x() + control.w() - 1, control.y() + control.h() - 1, fill);
            drawRect(guiGraphics, control.x(), control.y(), control.w(), control.h(), outline);
            drawCenteredScaled(guiGraphics, Component.literal(control.label()), control.x() + control.w() / 2, control.y() + 5, hovered ? GREEN : TEXT, SMALL_TEXT_SCALE, false);
            if (hovered) {
                hoverTooltip = Component.literal(control.tooltip());
            }
        }
    }

    private void activateControl(String id) {
        switch (id) {
            case "prev" -> DroneJammerClient.selectRelative(-1, sortMode);
            case "next" -> DroneJammerClient.selectRelative(1, sortMode);
            case "sort" -> sortMode = switch (sortMode) {
                case SIGNAL -> DroneJammerClient.DetectorSortMode.DISTANCE;
                case DISTANCE -> DroneJammerClient.DetectorSortMode.TYPE;
                case TYPE -> DroneJammerClient.DetectorSortMode.SIGNAL;
            };
            case "close" -> beginClosing();
            default -> {
            }
        }
    }

    private PortableFpvDetectorTarget selectedTarget() {
        PortableFpvDetectorTarget target = DroneJammerClient.selectedTarget();
        if (target != null) {
            lastSeenTargetId = target.droneId();
            lastSeenTargetName = target.displayName();
        }
        return target;
    }

    private String statusText(PortableFpvDetectorTarget target) {
        if (!DroneJammerClient.latestScan().detectorActive()) {
            return "NO SIGNAL";
        }
        if (target == null && lastSeenTargetId != null) {
            return "SIGNAL LOST";
        }
        if (target == null) {
            return DroneJammerClient.latestScan().targets().isEmpty() ? "SCANNING" : "SIGNAL";
        }
        return target.cameraAvailable() ? "INTERCEPT" : "SIGNAL";
    }

    private int statusColor(PortableFpvDetectorTarget target) {
        if (target == null) {
            return lastSeenTargetId == null ? GREEN_DIM : RED;
        }
        if (target.signalStrength() >= 0.85D) {
            return RED;
        }
        if (target.signalStrength() >= 0.65D) {
            return AMBER;
        }
        return GREEN;
    }

    private int selectedIndex(List<PortableFpvDetectorTarget> targets) {
        UUID selected = DroneJammerClient.latestScan().selectedTargetId();
        if (selected != null) {
            for (int i = 0; i < targets.size(); i++) {
                if (selected.equals(targets.get(i).droneId())) {
                    return i;
                }
            }
        }
        return 0;
    }

    private float smoothedSignal(PortableFpvDetectorTarget target) {
        if (target == null) {
            return 0.0F;
        }
        float targetValue = (float) target.signalStrength();
        float previous = smoothedSignals.getOrDefault(target.droneId(), targetValue);
        float next = Mth.lerp(0.22F, previous, targetValue);
        smoothedSignals.put(target.droneId(), next);
        return next;
    }

    private void drawMiniSignalBars(GuiGraphics guiGraphics, int x, int y, PortableFpvDetectorTarget target, int bars, boolean useSmoothed) {
        float signal = useSmoothed ? smoothedSignal(target) : (float) target.signalStrength();
        int active = Mth.clamp((int) Math.ceil(signal * bars), 0, bars);
        int color = signalColor(target, signal);
        for (int i = 0; i < bars; i++) {
            int barH = 3 + i * 2;
            int barX = x + i * 5;
            guiGraphics.fill(barX, y + 10 - barH, barX + 3, y + 10, i < active ? color : 0xFF1C3522);
        }
    }

    private int signalColor(PortableFpvDetectorTarget target, float signal) {
        if (target == null) {
            return GREEN_DIM;
        }
        if (signal >= 0.85F) {
            return RED;
        }
        if (signal >= 0.65F) {
            return AMBER;
        }
        return GREEN;
    }

    private String sortLabel() {
        return switch (sortMode) {
            case SIGNAL -> "SIGNAL";
            case DISTANCE -> "RANGE";
            case TYPE -> "TYPE";
        };
    }

    private int markerColor(PortableFpvDetectorTarget target) {
        String value = (target.displayName() + " " + target.entityTypeId()).toLowerCase(Locale.ROOT);
        if (value.contains("lucas")) {
            return AMBER;
        }
        if (value.contains("superb") || value.contains("sbw")) {
            return 0xFF77C7FF;
        }
        return GREEN;
    }

    private String markerLabel(PortableFpvDetectorTarget target) {
        String value = (target.displayName() + " " + target.entityTypeId()).toLowerCase(Locale.ROOT);
        if (value.contains("lucas")) {
            return "L";
        }
        if (value.contains("superb") || value.contains("sbw")) {
            return "S";
        }
        return "F";
    }

    private String ownerLabel(PortableFpvDetectorTarget target) {
        if (target.ownerName().isBlank()) {
            return "";
        }
        if (minecraft != null && minecraft.player != null
                && target.ownerName().equalsIgnoreCase(minecraft.player.getGameProfile().getName())) {
            return "OWNER: SELF";
        }
        return "OWNER: " + fitText(target.ownerName(), 70, SMALL_TEXT_SCALE);
    }

    private int maxScroll() {
        return Math.max(0, DroneJammerClient.targets(sortMode).size() - visibleTargets);
    }

    private String fitText(String value, int maxPixelWidth, float scale) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        int unscaledWidth = Math.max(1, Math.round(maxPixelWidth / Math.max(0.1F, scale)));
        if (font.width(value) <= unscaledWidth) {
            return value;
        }
        String suffix = "...";
        String trimmed = value;
        while (trimmed.length() > 1 && font.width(trimmed + suffix) > unscaledWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + suffix;
    }

    private void drawScaled(GuiGraphics guiGraphics, Component text, int x, int y, int color, float scale, boolean shadow) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, text, 0, 0, color, shadow);
        guiGraphics.pose().popPose();
    }

    private void drawCenteredScaled(GuiGraphics guiGraphics, Component text, int centerX, int y, int color, float scale, boolean shadow) {
        int textWidth = Math.round(font.width(text) * scale);
        drawScaled(guiGraphics, text, centerX - textWidth / 2, y, color, scale, shadow);
    }

    private void drawRightScaled(GuiGraphics guiGraphics, Component text, int rightX, int y, int color, float scale, boolean shadow) {
        int textWidth = Math.round(font.width(text) * scale);
        drawScaled(guiGraphics, text, rightX - textWidth, y, color, scale, shadow);
    }

    private void drawRect(GuiGraphics guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.fill(x, y, x + w, y + 1, color);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
        guiGraphics.fill(x, y, x + 1, y + h, color);
        guiGraphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void drawLine(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int steps = Math.max(dx, dy);
        if (steps == 0) {
            guiGraphics.fill(x0, y0, x0 + 1, y0 + 1, color);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            int x = Math.round(x0 + ((x1 - x0) * t));
            int y = Math.round(y0 + ((y1 - y0) * t));
            guiGraphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private boolean isHoldingDetector(Minecraft minecraft) {
        return minecraft.player != null
                && (DroneJammerItem.isDetector(minecraft.player.getMainHandItem())
                || DroneJammerItem.isDetector(minecraft.player.getOffhandItem()));
    }

    private void beginClosing() {
        if (!closing) {
            closing = true;
            closeTicks = 0;
        }
    }

    private void closeImmediately() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    private void playButtonSound() {
        if (minecraft != null && minecraft.player != null && minecraft.level != null) {
            minecraft.level.playLocalSound(
                    minecraft.player.getX(),
                    minecraft.player.getY(),
                    minecraft.player.getZ(),
                    SoundEvents.NOTE_BLOCK_CHIME.value(),
                    SoundSource.PLAYERS,
                    0.18F,
                    1.35F,
                    false
            );
        }
    }

    private static boolean inside(double x, double y, int rx, int ry, int rw, int rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private record Layout(float progress) {
    }

    private record DeviceControl(String id, String label, String tooltip, int x, int y, int w, int h) {
        boolean contains(double mouseX, double mouseY) {
            return inside(mouseX, mouseY, x, y, w, h);
        }
    }
}
