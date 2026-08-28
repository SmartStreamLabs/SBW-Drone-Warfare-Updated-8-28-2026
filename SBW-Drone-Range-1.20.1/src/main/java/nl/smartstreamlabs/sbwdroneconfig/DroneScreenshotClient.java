package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DroneScreenshotClient {
    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
    private static final long SCREENSHOT_COOLDOWN_MS = 1000L;
    private static final KeyMapping TAKE_DRONE_SCREENSHOT = new KeyMapping(
            "key.sbwdroneconfig.take_drone_screenshot",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.sbwdroneconfig"
    );

    private static long lastScreenshotAt;
    private static int flashTicks;

    private DroneScreenshotClient() {
    }

    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener(DroneScreenshotClient::onClientSetup);
        modBus.addListener(DroneScreenshotClient::onRegisterKeyMappings);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(DroneScreenshotClient.class);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TAKE_DRONE_SCREENSHOT);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft minecraft = Minecraft.getInstance();
        if (flashTicks > 0) {
            flashTicks--;
        }

        while (TAKE_DRONE_SCREENSHOT.consumeClick()) {
            if (!isDroneFpvActive(minecraft.player)) {
                continue;
            }

            long now = System.currentTimeMillis();
            if (now - lastScreenshotAt < SCREENSHOT_COOLDOWN_MS) {
                continue;
            }

            takeDroneScreenshot(minecraft);
            lastScreenshotAt = now;
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        if (flashTicks <= 0) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int alpha = Math.min(0x55, flashTicks * 0x18);
        guiGraphics.fill(0, 0, width, height, (alpha << 24) | 0xFFFFFF);
    }

    private static void takeDroneScreenshot(Minecraft minecraft) {
        if (minecraft == null || minecraft.getMainRenderTarget() == null || minecraft.player == null) {
            return;
        }

        File screenshotDir = new File(minecraft.gameDirectory, "screenshots/drone");
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }

        String fileName = "drone_" + FILE_NAME_FORMAT.format(LocalDateTime.now()) + ".png";
        Screenshot.grab(screenshotDir, fileName, minecraft.getMainRenderTarget(), component -> {
        });

        minecraft.player.displayClientMessage(Component.translatable("message.sbwdroneconfig.drone_screenshot_saved"), true);
        if (AddonConfig.enableDroneScreenshotSound() && minecraft.level != null) {
            minecraft.level.playLocalSound(
                    minecraft.player.getX(),
                    minecraft.player.getY(),
                    minecraft.player.getZ(),
                    SoundEvents.BOOK_PAGE_TURN,
                    SoundSource.PLAYERS,
                    0.55F,
                    0.85F,
                    false
            );
        }
        flashTicks = 3;
    }

    private static boolean isDroneFpvActive(LocalPlayer player) {
        if (player == null) {
            return false;
        }

        return isUsingLinkedMonitor(player.getMainHandItem()) || isUsingLinkedMonitor(player.getOffhandItem());
    }

    private static boolean isUsingLinkedMonitor(ItemStack stack) {
        return SbwCompat.isUsingLinkedMonitor(stack);
    }
}
