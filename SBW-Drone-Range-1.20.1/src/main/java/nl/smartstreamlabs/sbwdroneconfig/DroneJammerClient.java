package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class DroneJammerClient {
    private static final int HUD_WIDTH = 190;
    private static final int HUD_HEIGHT = 98;
    private static final int HUD_MARGIN = 8;
    private static final int HUD_HOTBAR_CLEARANCE = 52;
    private static final int COLOR_FRAME = 0xDD050806;
    private static final int COLOR_SCREEN = 0xE4041708;
    private static final int COLOR_TEXT = 0xFFD8FFE0;
    private static final int COLOR_GREEN = 0xFF5CFF7B;
    private static final int COLOR_DIM = 0xFF74967A;
    private static final int COLOR_WARN = 0xFFFFE06A;
    private static final int COLOR_DANGER = 0xFFFF6666;
    private static final ResourceLocation CUSTOM_BEEP_RESOURCE =
            ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "sounds/drone_radar_beep.ogg");

    private static PortableFpvDetectorScanMessage latestScan = PortableFpvDetectorScanMessage.empty(false);
    private static long lastScanClientTick;
    private static long nextBeepTick;

    private DroneJammerClient() {
    }

    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener(DroneJammerClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(DroneJammerClient.class);
    }

    public static void handleDetectorScan(PortableFpvDetectorScanMessage message) {
        latestScan = message == null ? PortableFpvDetectorScanMessage.empty(false) : message;
        Minecraft minecraft = Minecraft.getInstance();
        lastScanClientTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }

    public static void openDetectorScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.setScreen(new PortableFpvDetectorScreen());
        }
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
        UUID selected = latestScan.selectedTargetId();
        if (selected != null) {
            for (PortableFpvDetectorTarget target : latestScan.targets()) {
                if (selected.equals(target.droneId())) {
                    return target;
                }
            }
        }
        return latestScan.targets().isEmpty() ? null : latestScan.targets().get(0);
    }

    static PortableFpvDetectorScanMessage latestScan() {
        return latestScan;
    }

    static void selectTarget(UUID targetId) {
        AddonNetwork.selectPortableFpvDetectorTarget(new PortableFpvDetectorSelectMessage(targetId));
    }

    static void selectRelative(int direction, DetectorSortMode sortMode) {
        List<PortableFpvDetectorTarget> targets = targets(sortMode);
        if (targets.isEmpty()) {
            selectTarget(null);
            return;
        }

        UUID selectedId = latestScan.selectedTargetId();
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
        selectTarget(targets.get(nextIndex).droneId());
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
        String arrow = arrowForRelativeAngle(relativeAngle);
        String cardinal = cardinalForOffset(offset.x, offset.z);
        String vertical = offset.y > 4.0D ? "\u25B2" : offset.y < -4.0D ? "\u25BC" : "-";
        return new DirectionInfo(arrow, cardinal, vertical, relativeAngle, offset.y);
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
        int activeBars = target == null ? 0 : Mth.clamp((int) Math.ceil(target.signalStrength() * 5.0D), 0, 5);
        for (int index = 0; index < 5; index++) {
            int barHeight = 3 + (index * 2);
            int color = index < activeBars ? signalColor(target) : 0xFF24432A;
            guiGraphics.fill(x + (index * 7), y + (12 - barHeight), x + (index * 7) + 5, y + 12, color);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !DroneJammerItem.isActiveJammerHeld(player)) {
            latestScan = PortableFpvDetectorScanMessage.empty(false);
            nextBeepTick = 0L;
            return;
        }

        if (minecraft.level.getGameTime() - lastScanClientTick > Math.max(20L, AddonConfig.portableFpvDetectorScanIntervalTicks() * 4L)) {
            latestScan = PortableFpvDetectorScanMessage.empty(true);
        }
        handleBeep(player);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !AddonConfig.portableFpvDetectorHudEnabled() || !DroneJammerItem.isActiveJammerHeld(player)) {
            return;
        }
        if (minecraft.screen instanceof PortableFpvDetectorScreen) {
            return;
        }

        int x = minecraft.getWindow().getGuiScaledWidth() - HUD_WIDTH - HUD_MARGIN;
        int y = minecraft.getWindow().getGuiScaledHeight() - HUD_HEIGHT - HUD_HOTBAR_CLEARANCE;
        renderCompactHud(event.getGuiGraphics(), minecraft, player, x, Math.max(HUD_MARGIN, y));
    }

    private static void renderCompactHud(GuiGraphics guiGraphics, Minecraft minecraft, LocalPlayer player, int x, int y) {
        PortableFpvDetectorTarget target = selectedTarget();
        DirectionInfo direction = directionInfo(player, target);

        guiGraphics.fill(x, y, x + HUD_WIDTH, y + HUD_HEIGHT, COLOR_FRAME);
        guiGraphics.fill(x + 4, y + 4, x + HUD_WIDTH - 4, y + HUD_HEIGHT - 4, COLOR_SCREEN);
        for (int scanlineY = y + 8; scanlineY < y + HUD_HEIGHT - 4; scanlineY += 4) {
            guiGraphics.fill(x + 5, scanlineY, x + HUD_WIDTH - 5, scanlineY + 1, 0x220B4514);
        }

        guiGraphics.drawString(minecraft.font, Component.translatable("overlay.sbwdroneconfig.fpv_detector.title"), x + 9, y + 9, COLOR_GREEN, false);
        guiGraphics.drawString(
                minecraft.font,
                Component.translatable(target == null ? "overlay.sbwdroneconfig.fpv_detector.scanning" : "overlay.sbwdroneconfig.fpv_detector.detected"),
                x + 9,
                y + 21,
                target == null ? COLOR_DIM : signalColor(target),
                false
        );
        guiGraphics.drawString(minecraft.font, Component.translatable("overlay.sbwdroneconfig.fpv_detector.count", latestScan.targets().size()), x + 9, y + 33, COLOR_TEXT, false);

        if (target == null) {
            guiGraphics.drawString(minecraft.font, Component.translatable("overlay.sbwdroneconfig.fpv_detector.no_signal"), x + 9, y + 51, COLOR_DIM, false);
            return;
        }

        guiGraphics.drawString(minecraft.font, Component.literal(shortTargetName(target.displayName())), x + 9, y + 45, COLOR_TEXT, false);
        guiGraphics.drawString(minecraft.font, Component.literal(direction.arrow() + " " + direction.cardinal() + "  " + direction.vertical()), x + 9, y + 57, COLOR_GREEN, false);
        guiGraphics.drawString(minecraft.font, Component.translatable("overlay.sbwdroneconfig.fpv_detector.distance", formatDistance(target.distance())), x + 9, y + 69, COLOR_TEXT, false);
        drawSignalBars(guiGraphics, x + 116, y + 60, target);
        guiGraphics.drawString(minecraft.font, Component.literal(target.signalLabel()), x + 116, y + 75, signalColor(target), false);
        guiGraphics.drawString(
                minecraft.font,
                Component.translatable(target.cameraAvailable() ? "overlay.sbwdroneconfig.fpv_detector.video_available" : "overlay.sbwdroneconfig.fpv_detector.video_unavailable"),
                x + 9,
                y + 84,
                target.cameraAvailable() ? COLOR_GREEN : COLOR_DIM,
                false
        );
    }

    private static void handleBeep(LocalPlayer player) {
        if (!AddonConfig.droneRadarBeepEnabled() || latestScan.targets().isEmpty()) {
            nextBeepTick = 0L;
            return;
        }

        PortableFpvDetectorTarget strongest = latestScan.targets().get(0);
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
        return hasBundledCustomBeep ? AddonSounds.DRONE_RADAR_BEEP.get() : SoundEvents.NOTE_BLOCK_HAT.value();
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

    private static String arrowForRelativeAngle(double angle) {
        double normalized = Mth.wrapDegrees(angle);
        if (normalized >= -22.5D && normalized < 22.5D) {
            return "\u2191";
        }
        if (normalized >= 22.5D && normalized < 67.5D) {
            return "\u2197";
        }
        if (normalized >= 67.5D && normalized < 112.5D) {
            return "\u2192";
        }
        if (normalized >= 112.5D && normalized < 157.5D) {
            return "\u2198";
        }
        if (normalized >= -67.5D && normalized < -22.5D) {
            return "\u2196";
        }
        if (normalized >= -112.5D && normalized < -67.5D) {
            return "\u2190";
        }
        if (normalized >= -157.5D && normalized < -112.5D) {
            return "\u2199";
        }
        return "\u2193";
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

    private static String shortTargetName(String name) {
        if (name == null || name.isBlank()) {
            return "UNKNOWN DRONE";
        }
        String upper = name.toUpperCase(Locale.ROOT);
        return upper.length() <= 22 ? upper : upper.substring(0, 22);
    }

    static String formatDistance(double distance) {
        return String.format(Locale.ROOT, "~%.0f m", distance);
    }

    enum DetectorSortMode {
        SIGNAL,
        DISTANCE,
        TYPE
    }

    record DirectionInfo(String arrow, String cardinal, String vertical, double relativeAngle, double heightDelta) {
        static final DirectionInfo EMPTY = new DirectionInfo("-", "--", "-", 0.0D, 0.0D);
    }
}
