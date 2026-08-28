package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class DroneJammerClient {
    private static final int RAISE_TICKS = 8;
    private static final int HUD_WIDTH = 146;
    private static final int HUD_HEIGHT = 50;
    private static final int HUD_MARGIN = 7;
    private static final int HUD_HOTBAR_CLEARANCE = 54;
    private static final int SELECTOR_TICKS = 30;
    private static final int HINT_TICKS = 44;
    private static final int BUTTON_FLASH_TICKS = 3;
    private static final float DISPLAY_TEXT_SCALE = 0.58F;
    private static final float DISPLAY_SMALL_TEXT_SCALE = 0.48F;
    private static final int DISPLAY_EDGE = 0xEE143B1E;
    private static final int DISPLAY = 0xEE031307;
    private static final int DISPLAY_DIM = 0xAA041909;
    private static final int COLOR_TEXT = 0xFFD9FFE0;
    private static final int COLOR_GREEN = 0xFF63FF7B;
    private static final int COLOR_DIM = 0xFF7BA984;
    private static final int COLOR_WARN = 0xFFFFD76A;
    private static final int COLOR_DANGER = 0xFFFF5B55;
    private static final ResourceLocation CUSTOM_BEEP_RESOURCE =
            new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "sounds/drone_radar_beep.ogg");

    private static final Map<UUID, Float> smoothedSignals = new HashMap<>();
    private static PortableFpvDetectorScanMessage latestScan = PortableFpvDetectorScanMessage.empty(false);
    private static DetectorSortMode sortMode = DetectorSortMode.SIGNAL;
    private static UUID clientSelectedTargetId;
    private static UUID lastSeenTargetId;
    private static String lastSeenTargetName = "";
    private static long lastScanClientTick;
    private static long nextBeepTick;
    private static boolean lastScanHadTargets;
    private static boolean raised;
    private static boolean lowering;
    private static boolean localVideoEnabled = true;
    private static int raiseTicks;
    private static int lowerTicks;
    private static int selectorTicks;
    private static int hintTicks;
    private static int buttonFlashTicks;
    private static InteractionHand raisedHand = InteractionHand.MAIN_HAND;
    private static Object openedLevel;

    private DroneJammerClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(DroneJammerClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneJammerClient.class);
    }

    public static void handleDetectorScan(PortableFpvDetectorScanMessage message) {
        PortableFpvDetectorScanMessage previous = latestScan;
        latestScan = message == null ? PortableFpvDetectorScanMessage.empty(false) : message;
        Minecraft minecraft = Minecraft.getInstance();
        lastScanClientTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();

        boolean hadTargets = previous.detectorActive() && !previous.targets().isEmpty();
        boolean hasTargets = latestScan.detectorActive() && !latestScan.targets().isEmpty();
        if (minecraft.player != null && DroneJammerItem.isActiveJammerHeld(minecraft.player)) {
            if (!hadTargets && hasTargets) {
                showActionBar("Wireless FPV signal detected");
            } else if (hadTargets && !hasTargets && latestScan.detectorActive()) {
                showActionBar("FPV signal lost");
            }
        }
        lastScanHadTargets = hasTargets;

        if (clientSelectedTargetId != null && targetById(clientSelectedTargetId) == null) {
            clientSelectedTargetId = latestScan.selectedTargetId();
        } else if (clientSelectedTargetId == null) {
            clientSelectedTargetId = latestScan.selectedTargetId();
        }

        PortableFpvDetectorTarget target = selectedTarget();
        if (target != null) {
            lastSeenTargetId = target.droneId();
            lastSeenTargetName = target.displayName();
        }
    }

    public static void openDetectorScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (raised && !lowering) {
            beginLowering();
            return;
        }

        InteractionHand hand = detectorHand(minecraft.player);
        if (hand == null) {
            return;
        }

        raised = true;
        lowering = false;
        localVideoEnabled = true;
        raiseTicks = 0;
        lowerTicks = 0;
        selectorTicks = SELECTOR_TICKS;
        hintTicks = HINT_TICKS;
        buttonFlashTicks = 0;
        raisedHand = hand;
        openedLevel = minecraft.level;
    }

    static List<PortableFpvDetectorTarget> targets(DetectorSortMode sortMode) {
        List<PortableFpvDetectorTarget> sorted = new ArrayList<>(latestScan.targets());
        Comparator<PortableFpvDetectorTarget> comparator = switch (sortMode) {
            case DISTANCE -> Comparator.comparingDouble(PortableFpvDetectorTarget::distance);
            case TYPE -> Comparator.comparing(PortableFpvDetectorTarget::displayName).thenComparingDouble(PortableFpvDetectorTarget::distance);
            case SIGNAL -> Comparator.comparingDouble(PortableFpvDetectorTarget::signalStrength).reversed().thenComparingDouble(PortableFpvDetectorTarget::distance);
        };
        sorted.sort(comparator);
        return sorted;
    }

    static PortableFpvDetectorTarget selectedTarget() {
        if (clientSelectedTargetId != null) {
            PortableFpvDetectorTarget target = targetById(clientSelectedTargetId);
            if (target != null) {
                return target;
            }
        }

        UUID selected = latestScan.selectedTargetId();
        if (selected != null) {
            PortableFpvDetectorTarget target = targetById(selected);
            if (target != null) {
                clientSelectedTargetId = selected;
                return target;
            }
        }
        return latestScan.targets().isEmpty() ? null : latestScan.targets().get(0);
    }

    static PortableFpvDetectorScanMessage latestScan() {
        return latestScan;
    }

    static void selectTarget(UUID targetId) {
        clientSelectedTargetId = targetId;
        AddonNetwork.selectPortableFpvDetectorTarget(new PortableFpvDetectorSelectMessage(targetId));
    }

    static boolean selectRelative(int direction, DetectorSortMode sortMode) {
        List<PortableFpvDetectorTarget> targets = targets(sortMode);
        if (targets.isEmpty()) {
            selectTarget(null);
            return false;
        }

        PortableFpvDetectorTarget current = selectedTarget();
        UUID selectedId = current == null ? latestScan.selectedTargetId() : current.droneId();
        int selectedIndex = 0;
        if (selectedId != null) {
            for (int index = 0; index < targets.size(); index++) {
                if (selectedId.equals(targets.get(index).droneId())) {
                    selectedIndex = index;
                    break;
                }
            }
        }

        int nextIndex = Math.floorMod(selectedIndex + direction, targets.size());
        UUID nextId = targets.get(nextIndex).droneId();
        if (nextId.equals(selectedId)) {
            return false;
        }
        selectTarget(nextId);
        selectorTicks = SELECTOR_TICKS;
        return true;
    }

    static DirectionInfo directionInfo(LocalPlayer player, PortableFpvDetectorTarget target) {
        if (player == null || target == null) {
            return DirectionInfo.EMPTY;
        }

        Vec3 offset = new Vec3(target.x() - player.getX(), target.y() - player.getY(), target.z() - player.getZ());
        double yawRadians = Math.toRadians(player.getYRot());
        double forwardX = -Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);
        double rightX = Math.cos(yawRadians);
        double rightZ = Math.sin(yawRadians);
        double relativeForward = (offset.x * forwardX) + (offset.z * forwardZ);
        double relativeRight = (offset.x * rightX) + (offset.z * rightZ);
        double relativeAngle = Math.toDegrees(Math.atan2(relativeRight, relativeForward));
        String relative = relativeLabelForAngle(relativeAngle);
        String cardinal = cardinalForOffset(offset.x, offset.z);
        String vertical = offset.y > 4.0D ? "UP" : offset.y < -4.0D ? "DN" : "LVL";
        return new DirectionInfo(relative, cardinal, vertical, relativeAngle, offset.y);
    }

    static int signalColor(PortableFpvDetectorTarget target) {
        if (target == null) {
            return COLOR_DIM;
        }
        if (target.signalStrength() >= 0.85D) {
            return COLOR_DANGER;
        }
        if (target.signalStrength() >= 0.65D) {
            return COLOR_WARN;
        }
        return COLOR_GREEN;
    }

    static void drawSignalBars(GuiGraphics guiGraphics, int x, int y, PortableFpvDetectorTarget target) {
        drawSignalBars(guiGraphics, x, y, target, 5, 5, 2, false);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == null || !"hotbar".equals(event.getOverlay().id().getPath())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || raised || !DroneJammerItem.isActiveJammerHeld(player)) {
            return;
        }

        int x = event.getWindow().getGuiScaledWidth() - HUD_WIDTH - HUD_MARGIN;
        int y = Math.max(HUD_MARGIN, event.getWindow().getGuiScaledHeight() - HUD_HEIGHT - HUD_HOTBAR_CLEARANCE);
        renderDetectorHud(event.getGuiGraphics(), minecraft, player, x, y);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !player.isAlive()) {
            closeRaisedImmediately();
            latestScan = PortableFpvDetectorScanMessage.empty(false);
            nextBeepTick = 0L;
            return;
        }

        if (raised && (openedLevel != minecraft.level || !isDetectorInHand(player, raisedHand))) {
            beginLowering();
        }

        if (!DroneJammerItem.isActiveJammerHeld(player)) {
            latestScan = PortableFpvDetectorScanMessage.empty(false);
            nextBeepTick = 0L;
            lastScanHadTargets = false;
        } else if (minecraft.level.getGameTime() - lastScanClientTick > Math.max(20L, AddonConfig.portableFpvDetectorScanIntervalTicks() * 4L)) {
            latestScan = PortableFpvDetectorScanMessage.empty(true);
        }

        if (lowering) {
            lowerTicks++;
            if (lowerTicks >= RAISE_TICKS) {
                closeRaisedImmediately();
            }
        } else if (raised) {
            raiseTicks++;
        }
        if (selectorTicks > 0) {
            selectorTicks--;
        }
        if (hintTicks > 0) {
            hintTicks--;
        }
        if (buttonFlashTicks > 0) {
            buttonFlashTicks--;
        }

        handleBeep(player);
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (!raised || lowering || DroneClientViewContext.isReadOnlyCameraInterceptActive()) {
            return;
        }

        int direction = event.getScrollDelta() > 0.0D ? -1 : 1;
        if (selectRelative(direction, sortMode)) {
            playPhysicalClick();
            flashButton();
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!raised || DroneClientViewContext.isReadOnlyCameraInterceptActive() || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        int key = event.getKey();
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            beginLowering();
            return;
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_UP) {
            if (selectRelative(-1, sortMode)) {
                playPhysicalClick();
                flashButton();
            }
            return;
        }
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_DOWN) {
            if (selectRelative(1, sortMode)) {
                playPhysicalClick();
                flashButton();
            }
            return;
        }
        if (key == GLFW.GLFW_KEY_S) {
            sortMode = switch (sortMode) {
                case SIGNAL -> DetectorSortMode.DISTANCE;
                case DISTANCE -> DetectorSortMode.TYPE;
                case TYPE -> DetectorSortMode.SIGNAL;
            };
            selectorTicks = SELECTOR_TICKS;
            playPhysicalClick();
            flashButton();
            return;
        }
        if (key == GLFW.GLFW_KEY_V) {
            localVideoEnabled = !localVideoEnabled;
            selectorTicks = SELECTOR_TICKS;
            playPhysicalClick();
            flashButton();
        }
    }

    @SubscribeEvent
    public static void onUseWhileRaised(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!raised || lowering || minecraft.player == null || !event.isUseItem() || !minecraft.player.isCrouching()) {
            return;
        }
        if (event.getHand() == raisedHand) {
            beginLowering();
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (!raised || DroneClientViewContext.isReadOnlyCameraInterceptActive() || event.getHand() != raisedHand || !DroneJammerItem.isDetector(event.getItemStack())) {
            return;
        }

        event.setCanceled(true);
        renderRaisedDetectorModel(event);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!raised || DroneClientViewContext.isReadOnlyCameraInterceptActive()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        renderRaisedDetectorDisplay(event.getGuiGraphics(), event.getPartialTick());
    }

    private static void renderRaisedDetectorModel(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        LocalPlayer player = minecraft.player;
        HumanoidArm arm = armForHand(player, event.getHand());
        boolean leftHanded = arm == HumanoidArm.LEFT;
        float side = leftHanded ? -1.0F : 1.0F;
        float progress = animationProgress(event.getPartialTick());

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(
                side * Mth.lerp(progress, 0.45F, 0.18F),
                Mth.lerp(progress, -0.48F, -0.18F),
                Mth.lerp(progress, -0.62F, -1.02F)
        );
        poseStack.mulPose(Axis.YP.rotationDegrees(side * Mth.lerp(progress, -28.0F, 7.0F)));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(progress, 18.0F, -9.0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * Mth.lerp(progress, -12.0F, 3.0F)));
        float scale = Mth.lerp(progress, 0.46F, 0.70F);
        poseStack.scale(scale, scale, scale);

        poseStack.pushPose();
        if (buttonFlashTicks > 0) {
            poseStack.translate(side * 0.015F, -0.015F, 0.0F);
        }
        minecraft.getItemRenderer().renderStatic(
                player,
                event.getItemStack(),
                ItemDisplayContext.FIXED,
                leftHanded,
                poseStack,
                event.getMultiBufferSource(),
                minecraft.level,
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY,
                player.getId()
        );
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderDetectorHud(GuiGraphics guiGraphics, Minecraft minecraft, LocalPlayer player, int x, int y) {
        PortableFpvDetectorTarget target = selectedTarget();
        List<PortableFpvDetectorTarget> targets = targets(sortMode);
        int color = target == null ? COLOR_DIM : signalColor(target);
        guiGraphics.fill(x, y, x + HUD_WIDTH, y + HUD_HEIGHT, 0xAA010503);
        guiGraphics.fill(x + 2, y + 2, x + HUD_WIDTH - 2, y + HUD_HEIGHT - 2, 0xBB031107);
        guiGraphics.fill(x, y, x + HUD_WIDTH, y + 1, color);
        guiGraphics.fill(x, y + HUD_HEIGHT - 1, x + HUD_WIDTH, y + HUD_HEIGHT, color);
        guiGraphics.fill(x, y, x + 1, y + HUD_HEIGHT, color);
        guiGraphics.fill(x + HUD_WIDTH - 1, y, x + HUD_WIDTH, y + HUD_HEIGHT, color);

        String count = targets.isEmpty() ? "--/--" : String.format(Locale.ROOT, "%02d/%02d", selectedIndex(targets, target) + 1, targets.size());
        guiGraphics.drawString(minecraft.font, "FPV RX", x + 7, y + 6, COLOR_GREEN, false);
        guiGraphics.drawString(minecraft.font, count, x + HUD_WIDTH - 36, y + 6, COLOR_TEXT, false);

        if (target == null) {
            guiGraphics.drawString(minecraft.font, "NO SIG", x + 7, y + 23, COLOR_DIM, false);
            return;
        }

        DirectionInfo direction = directionInfo(player, target);
        guiGraphics.drawString(minecraft.font, fitText(minecraft.font, shortSignalName(target.displayName()), 64, 1.0F), x + 7, y + 20, COLOR_TEXT, false);
        guiGraphics.drawString(minecraft.font, direction.cardinal() + " " + formatDistance(target.distance()), x + 7, y + 33, COLOR_DIM, false);
        drawSignalBars(guiGraphics, x + HUD_WIDTH - 44, y + 24, target, 5, 5, 2, true);
    }

    private static void renderRaisedDetectorDisplay(GuiGraphics guiGraphics, float partialTick) {
        ReceiverDisplay display = receiverDisplay(guiGraphics, partialTick);
        if (display.progress() <= 0.01F) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.fill(display.x() - 2, display.y() - 2, display.x() + display.w() + 2, display.y() + display.h() + 2, DISPLAY_EDGE);
        guiGraphics.fill(display.x(), display.y(), display.x() + display.w(), display.y() + display.h(), DISPLAY);
        for (int lineY = display.y() + 3; lineY < display.y() + display.h() - 2; lineY += 4) {
            guiGraphics.fill(display.x() + 2, lineY, display.x() + display.w() - 2, lineY + 1, 0x170AFF40);
        }

        int pad = Math.max(3, display.w() / 48);
        int topH = Math.max(10, display.h() / 10);
        renderDisplayTopLine(guiGraphics, font, display, pad, topH);

        int videoX = display.x() + pad;
        int videoY = display.y() + topH + pad;
        int videoAreaW = display.w() - (pad * 2);
        int videoAreaH = display.h() - topH - (pad * 2);
        int videoW = videoAreaW;
        int videoH = Math.round(videoW * 9.0F / 16.0F);
        if (videoH > videoAreaH) {
            videoH = videoAreaH;
            videoW = Math.round(videoH * 16.0F / 9.0F);
        }
        videoX += Math.max(0, (videoAreaW - videoW) / 2);
        videoY += Math.max(0, (videoAreaH - videoH) / 2);

        renderDisplayVideo(guiGraphics, font, videoX, videoY, videoW, videoH, partialTick);
        if (selectorTicks > 0) {
            renderSelectorFlash(guiGraphics, font, display, videoX, videoY, videoW, videoH);
        } else if (hintTicks > 0) {
            renderControlHints(guiGraphics, font, display);
        }

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderDisplayTopLine(GuiGraphics guiGraphics, Font font, ReceiverDisplay display, int pad, int topH) {
        PortableFpvDetectorTarget target = selectedTarget();
        List<PortableFpvDetectorTarget> targets = targets(sortMode);
        int selectedIndex = selectedIndex(targets, target);
        String count = targets.isEmpty() ? "--/--" : String.format(Locale.ROOT, "%02d/%02d", selectedIndex + 1, targets.size());
        int statusColor = target == null ? (lastSeenTargetId == null ? COLOR_DIM : COLOR_DANGER) : signalColor(target);

        guiGraphics.fill(display.x(), display.y(), display.x() + display.w(), display.y() + topH + 1, DISPLAY_DIM);
        drawScaled(guiGraphics, font, "FPV RX", display.x() + pad, display.y() + 3, COLOR_GREEN, DISPLAY_TEXT_SCALE);
        drawCenteredScaled(guiGraphics, font, count, display.x() + display.w() / 2, display.y() + 3, COLOR_TEXT, DISPLAY_TEXT_SCALE);
        guiGraphics.fill(display.x() + display.w() - pad - 7, display.y() + 4, display.x() + display.w() - pad - 2, display.y() + 9, statusColor);
    }

    private static void renderDisplayVideo(GuiGraphics guiGraphics, Font font, int x, int y, int w, int h, float partialTick) {
        PortableFpvDetectorTarget target = selectedTarget();
        int border = target == null ? COLOR_DIM : signalColor(target);
        if (target != null && target.signalStrength() >= 0.85D) {
            border = COLOR_DANGER;
        }
        guiGraphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
        guiGraphics.fill(x, y, x + w, y + h, 0xFF010501);

        boolean renderedFeed = localVideoEnabled
                && target != null
                && latestScan.videoEnabled()
                && PortableFpvDetectorVideoRenderer.render(guiGraphics, target, x, y, w, h, partialTick);
        if (renderedFeed) {
            renderCameraOverlay(guiGraphics, font, target, x, y, w, h);
            return;
        }

        renderVideoState(guiGraphics, font, target, x, y, w, h);
    }

    private static void renderCameraOverlay(GuiGraphics guiGraphics, Font font, PortableFpvDetectorTarget target, int x, int y, int w, int h) {
        DroneJammerClient.DirectionInfo direction = DroneJammerClient.directionInfo(Minecraft.getInstance().player, target);
        int stripH = Math.max(28, h / 3);
        int bottomY = y + h - stripH;
        int rightReserved = 46;
        guiGraphics.fill(x, y, x + w, y + 11, 0x88010402);
        guiGraphics.fill(x, bottomY, x + w, y + h, 0xAA010402);

        drawScaled(guiGraphics, font, "INT", x + 4, y + 3, COLOR_GREEN, DISPLAY_SMALL_TEXT_SCALE);
        drawRightScaled(guiGraphics, font, formatDistance(target.distance()), x + w - 4, y + 3, COLOR_TEXT, DISPLAY_SMALL_TEXT_SCALE);
        drawScaled(
                guiGraphics,
                font,
                fitText(font, shortSignalName(target.displayName()), Math.max(26, w - rightReserved - 10), DISPLAY_SMALL_TEXT_SCALE),
                x + 4,
                bottomY + 5,
                COLOR_TEXT,
                DISPLAY_SMALL_TEXT_SCALE
        );
        drawRightScaled(guiGraphics, font, direction.cardinal(), x + w - 4, bottomY + 5, COLOR_TEXT, DISPLAY_SMALL_TEXT_SCALE);
        drawScaled(guiGraphics, font, "SIG " + target.signalLabel(), x + 4, bottomY + 17, signalColor(target), DISPLAY_SMALL_TEXT_SCALE);
        drawSignalBars(guiGraphics, x + w - 37, y + h - 14, target, 5, 4, 2, true);
    }

    private static void renderVideoState(GuiGraphics guiGraphics, Font font, PortableFpvDetectorTarget target, int x, int y, int w, int h) {
        String label;
        int color = COLOR_DIM;
        if (target == null && lastSeenTargetId != null && latestScan.detectorActive()) {
            label = "SIGNAL LOST";
            color = COLOR_DANGER;
        } else if (target == null) {
            label = "NO SIG";
        } else if (!localVideoEnabled) {
            label = "VIDEO OFF";
        } else if (!target.cameraAvailable()) {
            label = "VIDEO UNAVAILABLE";
        } else {
            label = "VIDEO STANDBY";
            color = COLOR_WARN;
        }

        long tick = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
        for (int i = 0; i < 14; i++) {
            int yy = y + 3 + (int) Math.floorMod((i * 9L) + tick, Math.max(1, h - 6));
            guiGraphics.fill(x + 3, yy, x + w - 3, yy + 1, 0x142AFF56);
        }
        drawCenteredScaled(guiGraphics, font, label, x + w / 2, y + h / 2 - 4, color, DISPLAY_TEXT_SCALE);
    }

    private static void renderSelectorFlash(GuiGraphics guiGraphics, Font font, ReceiverDisplay display, int videoX, int videoY, int videoW, int videoH) {
        PortableFpvDetectorTarget target = selectedTarget();
        List<PortableFpvDetectorTarget> targets = targets(sortMode);
        int alpha = Mth.clamp(selectorTicks * 160 / SELECTOR_TICKS, 0, 160);
        int panelW = Math.min(display.w() - 14, Math.max(78, videoW * 2 / 3));
        int panelH = Math.min(videoH - 8, 34);
        int x = videoX + (videoW - panelW) / 2;
        int y = videoY + (videoH - panelH) / 2;
        guiGraphics.fill(x, y, x + panelW, y + panelH, (alpha << 24) | 0x00050B05);
        guiGraphics.fill(x, y, x + panelW, y + 1, COLOR_GREEN);
        guiGraphics.fill(x, y + panelH - 1, x + panelW, y + panelH, COLOR_GREEN);

        String index = targets.isEmpty() ? "SIGNAL --/--" : String.format(Locale.ROOT, "SIGNAL %02d/%02d", selectedIndex(targets, target) + 1, targets.size());
        drawCenteredScaled(guiGraphics, font, index, x + panelW / 2, y + 5, COLOR_GREEN, DISPLAY_SMALL_TEXT_SCALE);
        if (target == null) {
            drawCenteredScaled(guiGraphics, font, "NO SIG", x + panelW / 2, y + 17, COLOR_DIM, DISPLAY_SMALL_TEXT_SCALE);
            return;
        }
        drawCenteredScaled(guiGraphics, font, fitText(font, shortSignalName(target.displayName()), panelW - 10, DISPLAY_SMALL_TEXT_SCALE), x + panelW / 2, y + 17, COLOR_TEXT, DISPLAY_SMALL_TEXT_SCALE);
        drawScaled(guiGraphics, font, formatDistance(target.distance()), x + 7, y + 26, COLOR_TEXT, DISPLAY_SMALL_TEXT_SCALE);
        drawSignalBars(guiGraphics, x + panelW - 33, y + 20, target, 5, 4, 2, true);
    }

    private static void renderControlHints(GuiGraphics guiGraphics, Font font, ReceiverDisplay display) {
        int alpha = Mth.clamp(hintTicks * 170 / HINT_TICKS, 0, 170);
        int y = display.y() + display.h() - 11;
        String hint = "WHEEL/ARROWS  S SORT  V VIDEO  ESC CLOSE";
        int color = (alpha << 24) | (COLOR_DIM & 0x00FFFFFF);
        guiGraphics.fill(display.x() + 3, y - 2, display.x() + display.w() - 3, y + 9, (alpha / 2 << 24) | 0x00010402);
        drawCenteredScaled(guiGraphics, font, fitText(font, hint, display.w() - 10, DISPLAY_SMALL_TEXT_SCALE), display.x() + display.w() / 2, y, color, DISPLAY_SMALL_TEXT_SCALE);
    }

    private static ReceiverDisplay receiverDisplay(GuiGraphics guiGraphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        float progress = animationProgress(partialTick);
        int guiW = guiGraphics.guiWidth();
        int guiH = guiGraphics.guiHeight();
        HumanoidArm arm = minecraft.player == null ? HumanoidArm.RIGHT : armForHand(minecraft.player, raisedHand);
        float side = arm == HumanoidArm.LEFT ? -1.0F : 1.0F;
        int displayW = Mth.clamp(Math.round(guiW * 0.27F), 124, 260);
        int displayH = Math.round(displayW * 0.68F);
        float startCenterX = guiW * (side > 0.0F ? 0.84F : 0.16F);
        float raisedCenterX = guiW * (side > 0.0F ? 0.60F : 0.40F);
        float startCenterY = guiH * 0.82F;
        float raisedCenterY = guiH * 0.57F;
        int centerX = Math.round(Mth.lerp(progress, startCenterX, raisedCenterX));
        int centerY = Math.round(Mth.lerp(progress, startCenterY, raisedCenterY));
        return new ReceiverDisplay(centerX - displayW / 2, centerY - displayH / 2, displayW, displayH, progress);
    }

    private static void handleBeep(LocalPlayer player) {
        if (!AddonConfig.droneRadarBeepEnabled() || latestScan.targets().isEmpty() || !DroneJammerItem.isActiveJammerHeld(player)) {
            nextBeepTick = 0L;
            return;
        }

        PortableFpvDetectorTarget strongest = latestScan.targets().stream()
                .max(Comparator.comparingDouble(PortableFpvDetectorTarget::signalStrength))
                .orElse(null);
        if (strongest == null) {
            nextBeepTick = 0L;
            return;
        }

        long gameTime = player.level().getGameTime();
        if (gameTime < nextBeepTick) {
            return;
        }

        int interval = beepInterval(strongest.signalStrength());
        float pitch = (float) (0.75D + (strongest.signalStrength() * 0.9D));
        float volume = (float) Math.max(0.0D, AddonConfig.droneRadarBeepVolume() * (0.35D + (strongest.signalStrength() * 0.65D)));
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), selectBeepSound(), SoundSource.PLAYERS, volume, pitch, false);
        nextBeepTick = gameTime + interval;
    }

    private static SoundEvent selectBeepSound() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean hasBundledCustomBeep = minecraft.getResourceManager().getResource(CUSTOM_BEEP_RESOURCE).isPresent();
        return hasBundledCustomBeep && AddonSounds.DRONE_RADAR_BEEP.isPresent()
                ? AddonSounds.DRONE_RADAR_BEEP.get()
                : SoundEvents.NOTE_BLOCK_HAT.value();
    }

    private static int beepInterval(double signalStrength) {
        if (signalStrength >= 0.85D) {
            return 5;
        }
        if (signalStrength >= 0.65D) {
            return 10;
        }
        if (signalStrength >= 0.40D) {
            return 20;
        }
        if (signalStrength >= 0.20D) {
            return 30;
        }
        return 40;
    }

    private static void showActionBar(String text) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.gui.setOverlayMessage(Component.literal(text), false);
        }
    }

    private static void beginLowering() {
        if (!raised || lowering) {
            return;
        }
        lowering = true;
        lowerTicks = 0;
        hintTicks = 0;
        selectorTicks = 0;
    }

    private static void closeRaisedImmediately() {
        if (!raised && !lowering) {
            return;
        }
        raised = false;
        lowering = false;
        raiseTicks = 0;
        lowerTicks = 0;
        hintTicks = 0;
        selectorTicks = 0;
        buttonFlashTicks = 0;
        openedLevel = null;
        PortableFpvDetectorVideoRenderer.release();
    }

    private static float animationProgress(float partialTick) {
        float linear = lowering
                ? 1.0F - Mth.clamp((lowerTicks + partialTick) / RAISE_TICKS, 0.0F, 1.0F)
                : Mth.clamp((raiseTicks + partialTick) / RAISE_TICKS, 0.0F, 1.0F);
        return linear * linear * (3.0F - (2.0F * linear));
    }

    private static InteractionHand detectorHand(LocalPlayer player) {
        if (DroneJammerItem.isDetector(player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        if (DroneJammerItem.isDetector(player.getOffhandItem())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static boolean isDetectorInHand(LocalPlayer player, InteractionHand hand) {
        return hand != null && DroneJammerItem.isDetector(player.getItemInHand(hand));
    }

    private static HumanoidArm armForHand(LocalPlayer player, InteractionHand hand) {
        HumanoidArm mainArm = player.getMainArm();
        return hand == InteractionHand.MAIN_HAND ? mainArm : mainArm.getOpposite();
    }

    private static PortableFpvDetectorTarget targetById(UUID targetId) {
        if (targetId == null) {
            return null;
        }
        for (PortableFpvDetectorTarget target : latestScan.targets()) {
            if (targetId.equals(target.droneId())) {
                return target;
            }
        }
        return null;
    }

    private static int selectedIndex(List<PortableFpvDetectorTarget> targets, PortableFpvDetectorTarget target) {
        UUID selected = target == null ? clientSelectedTargetId : target.droneId();
        if (selected != null) {
            for (int i = 0; i < targets.size(); i++) {
                if (selected.equals(targets.get(i).droneId())) {
                    return i;
                }
            }
        }
        return 0;
    }

    private static float smoothedSignal(PortableFpvDetectorTarget target) {
        if (target == null) {
            return 0.0F;
        }
        float targetValue = (float) target.signalStrength();
        float previous = smoothedSignals.getOrDefault(target.droneId(), targetValue);
        float next = Mth.lerp(0.24F, previous, targetValue);
        smoothedSignals.put(target.droneId(), next);
        return next;
    }

    private static void drawSignalBars(GuiGraphics guiGraphics, int x, int y, PortableFpvDetectorTarget target, int bars, int barW, int gap, boolean useSmoothed) {
        float signal = useSmoothed ? smoothedSignal(target) : (float) (target == null ? 0.0D : target.signalStrength());
        int active = Mth.clamp((int) Math.ceil(signal * bars), 0, bars);
        int color = signalColor(target);
        for (int i = 0; i < bars; i++) {
            int barH = 3 + i * 2;
            int barX = x + i * (barW + gap);
            guiGraphics.fill(barX, y + 12 - barH, barX + barW, y + 12, i < active ? color : 0xFF1C3522);
        }
    }

    private static void flashButton() {
        buttonFlashTicks = BUTTON_FLASH_TICKS;
    }

    private static void playPhysicalClick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        minecraft.level.playLocalSound(
                minecraft.player.getX(),
                minecraft.player.getY(),
                minecraft.player.getZ(),
                SoundEvents.NOTE_BLOCK_CHIME.value(),
                SoundSource.PLAYERS,
                0.14F,
                1.45F,
                false
        );
    }

    private static String relativeLabelForAngle(double angle) {
        double normalized = Mth.wrapDegrees(angle);
        if (normalized >= -22.5D && normalized < 22.5D) {
            return "F";
        }
        if (normalized >= 22.5D && normalized < 67.5D) {
            return "FR";
        }
        if (normalized >= 67.5D && normalized < 112.5D) {
            return "R";
        }
        if (normalized >= 112.5D && normalized < 157.5D) {
            return "BR";
        }
        if (normalized >= -67.5D && normalized < -22.5D) {
            return "FL";
        }
        if (normalized >= -112.5D && normalized < -67.5D) {
            return "L";
        }
        if (normalized >= -157.5D && normalized < -112.5D) {
            return "BL";
        }
        return "B";
    }

    private static String cardinalForOffset(double offsetX, double offsetZ) {
        double degrees = Math.toDegrees(Math.atan2(offsetX, -offsetZ));
        int index = Math.floorMod((int) Math.round(degrees / 45.0D), 8);
        return switch (index) {
            case 0 -> "N";
            case 1 -> "NE";
            case 2 -> "E";
            case 3 -> "SE";
            case 4 -> "S";
            case 5 -> "SW";
            case 6 -> "W";
            default -> "NW";
        };
    }

    private static String shortSignalName(String name) {
        if (name == null || name.isBlank()) {
            return "FPV";
        }
        String upper = name.toUpperCase(Locale.ROOT);
        if (upper.contains("LUCAS")) {
            return "LUCAS";
        }
        if (upper.contains("FPV")) {
            return "FPV";
        }
        if (upper.contains("DRONE")) {
            return "DRONE";
        }
        return upper.length() <= 10 ? upper : upper.substring(0, 10);
    }

    private static String fitText(Font font, String value, int maxPixelWidth, float scale) {
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

    static String formatDistance(double distance) {
        return String.format(Locale.ROOT, "~%.0f m", distance);
    }

    private static void drawScaled(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    private static void drawCenteredScaled(GuiGraphics guiGraphics, Font font, String text, int centerX, int y, int color, float scale) {
        int textWidth = Math.round(font.width(text) * scale);
        drawScaled(guiGraphics, font, text, centerX - textWidth / 2, y, color, scale);
    }

    private static void drawRightScaled(GuiGraphics guiGraphics, Font font, String text, int rightX, int y, int color, float scale) {
        int textWidth = Math.round(font.width(text) * scale);
        drawScaled(guiGraphics, font, text, rightX - textWidth, y, color, scale);
    }

    enum DetectorSortMode {
        SIGNAL,
        DISTANCE,
        TYPE
    }

    record DirectionInfo(String relative, String cardinal, String vertical, double relativeAngle, double heightDelta) {
        static final DirectionInfo EMPTY = new DirectionInfo("-", "--", "LVL", 0.0D, 0.0D);
    }

    private record ReceiverDisplay(int x, int y, int w, int h, float progress) {
    }
}
