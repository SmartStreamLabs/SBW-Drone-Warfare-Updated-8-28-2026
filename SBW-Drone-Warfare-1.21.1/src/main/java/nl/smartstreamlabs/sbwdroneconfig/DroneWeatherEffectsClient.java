package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class DroneWeatherEffectsClient {
    private static final Random RANDOM = new Random();
    private static final List<Droplet> DROPLETS = new ArrayList<>();
    private static final int MAX_RAIN_DROPLETS = 18;
    private static final int MAX_STORM_DROPLETS = 36;
    private static final KeyMapping WIPE_DRONE_CAMERA = new KeyMapping(
            "key.sbwdroneconfig.wipe_drone_camera",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.sbwdroneconfig"
    );

    private DroneWeatherEffectsClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(DroneWeatherEffectsClient::onClientSetup);
        modBus.addListener(DroneWeatherEffectsClient::onRegisterKeyMappings);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneWeatherEffectsClient.class);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(WIPE_DRONE_CAMERA);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldRenderWeatherEffects()) {
            DROPLETS.clear();
            return;
        }

        if (WIPE_DRONE_CAMERA.consumeClick()) {
            wipeDroplets();
        }

        boolean thunder = minecraft.level != null && minecraft.level.isThundering();
        int maxDroplets = thunder ? MAX_STORM_DROPLETS : MAX_RAIN_DROPLETS;
        float spawnChance = thunder ? 0.58F : 0.24F;

        if (DROPLETS.size() < maxDroplets && RANDOM.nextFloat() < spawnChance) {
            DROPLETS.add(createDroplet(thunder));
            if (thunder && DROPLETS.size() < maxDroplets && RANDOM.nextFloat() < 0.35F) {
                DROPLETS.add(createDroplet(true));
            }
        }

        Iterator<Droplet> iterator = DROPLETS.iterator();
        while (iterator.hasNext()) {
            Droplet droplet = iterator.next();
            droplet.tick(thunder);
            if (droplet.isExpired()) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!shouldRenderWeatherEffects() || event.getOverlay() == null || !"hotbar".equals(event.getOverlay().id().getPath())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        boolean thunder = minecraft.level != null && minecraft.level.isThundering();
        int tint = thunder ? 0x4A182430 : 0x302A3440;

        guiGraphics.fill(0, 0, width, height, tint);

        if (DROPLETS.size() > (thunder ? 18 : 10)) {
            int blurAlpha = thunder ? 0x26 : 0x18;
            guiGraphics.fill(0, 0, width, height, (blurAlpha << 24) | 0x8EA6BA);
        }

        renderDroplets(guiGraphics, width, height, thunder);

        Component text = Component.translatable(
                thunder ? "overlay.sbwdroneconfig.storm_interference" : "overlay.sbwdroneconfig.rain_interference"
        );
        int x = width - minecraft.font.width(text) - 10;
        int y = 24;
        guiGraphics.drawString(minecraft.font, text, x, y, 0xB8D8FF, true);
    }

    private static void renderDroplets(GuiGraphics guiGraphics, int width, int height, boolean thunder) {
        float xScale = width / 320.0F;
        float yScale = height / 180.0F;

        for (Droplet droplet : DROPLETS) {
            int alpha = Mth.clamp((int) (droplet.alpha() * 255.0F), 0, 255);
            if (alpha <= 0) {
                continue;
            }

            int bodyColor = (alpha << 24) | 0xB7D7F2;
            int highlightColor = (Math.max(40, alpha - 45) << 24) | 0xEAF6FF;

            int x = Mth.floor(droplet.x * xScale);
            int y = Mth.floor(droplet.y * yScale);
            int dropletWidth = Math.max(2, Mth.floor(droplet.width * xScale));
            int dropletHeight = Math.max(5, Mth.floor(droplet.height * yScale));
            int tailHeight = thunder ? Math.max(2, dropletHeight / 3) : Math.max(1, dropletHeight / 4);

            guiGraphics.fill(x, y, x + dropletWidth, y + dropletHeight, bodyColor);
            guiGraphics.fill(x + 1, y, x + Math.max(2, dropletWidth / 2), y + Math.max(2, dropletHeight / 3), highlightColor);
            guiGraphics.fill(
                    x + Math.max(0, dropletWidth / 3),
                    y + dropletHeight,
                    x + Math.max(1, (dropletWidth * 2) / 3),
                    y + dropletHeight + tailHeight,
                    bodyColor
            );
        }
    }

    private static void wipeDroplets() {
        for (Droplet droplet : DROPLETS) {
            droplet.wipe();
        }
    }

    private static Droplet createDroplet(boolean thunder) {
        float width = thunder ? randomBetween(3.4F, 6.2F) : randomBetween(2.4F, 4.8F);
        float height = width * randomBetween(2.8F, 4.4F);
        float speed = thunder ? randomBetween(0.95F, 1.85F) : randomBetween(0.45F, 1.05F);
        int lifetime = thunder ? randomBetween(42, 72) : randomBetween(65, 110);
        return new Droplet(
                randomBetween(0.0F, 320.0F - width - 2.0F),
                randomBetween(-18.0F, -2.0F),
                width,
                height,
                speed,
                lifetime
        );
    }

    private static boolean shouldRenderWeatherEffects() {
        if (!AddonConfig.weatherEffectsEnabled() || !ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !minecraft.level.isRaining()) {
            return false;
        }

        return SbwCompat.isUsingLinkedMonitor(SbwCompat.getActiveLinkedMonitor(player));
    }

    private static float randomBetween(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }

    private static int randomBetween(int min, int max) {
        return min + RANDOM.nextInt(Math.max(1, max - min + 1));
    }

    private static final class Droplet {
        private float x;
        private float y;
        private final float width;
        private final float height;
        private final float speed;
        private final int maxLifetime;
        private int age;
        private boolean wiped;

        private Droplet(float x, float y, float width, float height, float speed, int maxLifetime) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
            this.maxLifetime = maxLifetime;
        }

        private void tick(boolean thunder) {
            this.age++;
            float drift = thunder ? randomBetween(-0.2F, 0.2F) : randomBetween(-0.08F, 0.08F);
            this.x += drift;
            this.y += this.speed * (thunder ? 1.18F : 1.0F);
            if (this.wiped) {
                this.y += thunder ? 3.6F : 2.4F;
            }
        }

        private float alpha() {
            float fadeIn = Math.min(1.0F, this.age / 8.0F);
            float fadeOutStart = this.maxLifetime * 0.55F;
            float fadeOut = this.age > fadeOutStart
                    ? Math.max(0.0F, 1.0F - ((this.age - fadeOutStart) / (this.maxLifetime - fadeOutStart)))
                    : 1.0F;
            float wipeFade = this.wiped ? Math.max(0.0F, 1.0F - (this.age / 12.0F)) : 1.0F;
            return Math.min(fadeIn, Math.min(fadeOut, wipeFade)) * 0.9F;
        }

        private boolean isExpired() {
            return this.age >= this.maxLifetime || this.y > 210.0F || this.alpha() <= 0.02F;
        }

        private void wipe() {
            this.wiped = true;
            this.age = Math.max(this.age, this.maxLifetime - 10);
        }
    }
}
