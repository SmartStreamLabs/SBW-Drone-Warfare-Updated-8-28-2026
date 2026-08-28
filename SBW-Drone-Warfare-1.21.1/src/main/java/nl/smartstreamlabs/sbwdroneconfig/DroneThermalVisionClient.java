package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class DroneThermalVisionClient {
    private static final float PLAYER_RED = 1.00F;
    private static final float PLAYER_GREEN = 0.35F;
    private static final float PLAYER_BLUE = 0.10F;
    private static final float HOSTILE_RED = 1.00F;
    private static final float HOSTILE_GREEN = 0.68F;
    private static final float HOSTILE_BLUE = 0.15F;
    private static final float ANIMAL_RED = 0.98F;
    private static final float ANIMAL_GREEN = 0.90F;
    private static final float ANIMAL_BLUE = 0.48F;
    private static final int THERMAL_TOGGLE_COOLDOWN_TICKS = 8;
    private static final int THERMAL_NOISE_LINES = 20;

    private static KeyMapping toggleThermalKey;
    private static boolean thermalVisionEnabled;
    private static int toggleCooldownTicks;
    private static int thermalNoiseTick;
    private static final Set<UUID> HIDDEN_DRONE_OPERATORS = new HashSet<>();

    private DroneThermalVisionClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(DroneThermalVisionClient::onClientSetup);
        modBus.addListener(DroneThermalVisionClient::onRegisterKeyMappings);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        FMLJavaModLoadingContext.get().getModEventBus();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneThermalVisionClient.class);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        toggleThermalKey = new KeyMapping(
                "key.sbwdroneconfig.toggle_thermal",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.categories.sbwdroneconfig"
        );
        event.register(toggleThermalKey);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || toggleThermalKey == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            thermalVisionEnabled = false;
            toggleCooldownTicks = 0;
            HIDDEN_DRONE_OPERATORS.clear();
            return;
        }

        if (toggleCooldownTicks > 0) {
            toggleCooldownTicks--;
        }
        thermalNoiseTick++;

        boolean activeDroneView = isActiveDroneView(minecraft.player);
        if (!activeDroneView) {
            thermalVisionEnabled = false;
        }

        while (toggleThermalKey.consumeClick()) {
            if (!AddonConfig.thermalVisionKeybindEnabled()) {
                continue;
            }

            if (toggleCooldownTicks > 0) {
                continue;
            }

            if (!activeDroneView || !AddonConfig.enableThermalVision()) {
                minecraft.player.displayClientMessage(
                        Component.translatable("message.sbwdroneconfig.thermal_unavailable"),
                        true
                );
                thermalVisionEnabled = false;
                continue;
            }

            thermalVisionEnabled = !thermalVisionEnabled;
            toggleCooldownTicks = THERMAL_TOGGLE_COOLDOWN_TICKS;
            if (minecraft.level != null) {
                minecraft.level.playLocalSound(
                        minecraft.player.getX(),
                        minecraft.player.getY(),
                        minecraft.player.getZ(),
                        thermalVisionEnabled ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                        SoundSource.PLAYERS,
                        0.45F,
                        thermalVisionEnabled ? 1.45F : 1.05F,
                        false
                );
            }
            minecraft.player.displayClientMessage(
                    Component.translatable(
                            thermalVisionEnabled
                                    ? "message.sbwdroneconfig.thermal_enabled"
                                    : "message.sbwdroneconfig.thermal_disabled"
                    ),
                    true
            );
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || !shouldRenderThermal()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        double range = AddonConfig.thermalVisionRange();
        AABB searchBox = new AABB(cameraPos, cameraPos).inflate(range);
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderSystem.disableDepthTest();

        for (LivingEntity living : minecraft.level.getEntitiesOfClass(LivingEntity.class, searchBox, DroneThermalVisionClient::shouldHighlight)) {
            AABB box = living.getBoundingBox().inflate(0.08D);
            if (living instanceof Player) {
                LevelRenderer.renderLineBox(poseStack, lines, box, PLAYER_RED, PLAYER_GREEN, PLAYER_BLUE, 1.0F);
            } else if (isHostile(living)) {
                LevelRenderer.renderLineBox(poseStack, lines, box, HOSTILE_RED, HOSTILE_GREEN, HOSTILE_BLUE, 1.0F);
            } else {
                LevelRenderer.renderLineBox(poseStack, lines, box, ANIMAL_RED, ANIMAL_GREEN, ANIMAL_BLUE, 1.0F);
            }
        }

        RenderSystem.enableDepthTest();
        poseStack.popPose();
        buffers.endBatch(RenderType.lines());
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!shouldRenderThermal() || event.getOverlay() == null || !"hotbar".equals(event.getOverlay().id().getPath())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        // This is the upgraded version of the old drone terminal overlay path:
        // instead of creating a separate camera system, we tint and decorate the
        // existing FPV HUD/terminal render stage when thermal mode is active.
        guiGraphics.fill(0, 0, width, height, 0x6A060D08);
        guiGraphics.fill(0, 0, width, height, 0x221C3629);

        if (AddonConfig.thermalVisionNoiseEffect()) {
            renderThermalNoise(guiGraphics, width, height);
        }

        Component text = Component.translatable("overlay.sbwdroneconfig.thermal_on");
        int x = width - minecraft.font.width(text) - 10;
        int y = 10;
        guiGraphics.drawString(minecraft.font, text, x, y, 0xFFD87A, true);
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity() == null) {
            return;
        }

        Player renderedPlayer = event.getEntity();

        // Hide any player that the server flagged as an active drone operator. This avoids
        // relying on remote hand-item NBT state, which can be inconsistent on other clients.
        if (HIDDEN_DRONE_OPERATORS.contains(renderedPlayer.getUUID()) || isAnyDroneOperator(renderedPlayer)) {
            event.setCanceled(true);
        }
    }

    public static void handleDroneOperatorVisibility(DroneOperatorVisibilityMessage message) {
        if (message.hidden()) {
            HIDDEN_DRONE_OPERATORS.add(message.playerId());
        } else {
            HIDDEN_DRONE_OPERATORS.remove(message.playerId());
        }
    }

    public static boolean shouldHideDefaultTargetBoxes() {
        return shouldRenderThermal();
    }

    private static boolean shouldHighlight(LivingEntity living) {
        if (living == null || !living.isAlive() || living.isSpectator()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && living.getUUID().equals(minecraft.player.getUUID())) {
            return false;
        }
        if (living instanceof Player) {
            return AddonConfig.thermalHighlightPlayers();
        }
        if (isHostile(living)) {
            return AddonConfig.thermalHighlightHostileMobs();
        }
        return AddonConfig.thermalHighlightAnimals();
    }

    private static boolean shouldRenderThermal() {
        if (!thermalVisionEnabled || !AddonConfig.enableThermalVision() || !ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && isActiveDroneView(minecraft.player);
    }

    private static void renderThermalNoise(GuiGraphics guiGraphics, int width, int height) {
        int lineSpacing = Math.max(4, height / THERMAL_NOISE_LINES);
        for (int y = (thermalNoiseTick % lineSpacing); y < height; y += lineSpacing) {
            guiGraphics.fill(0, y, width, y + 1, 0x181E3128);
        }

        int specks = Math.max(14, width / 30);
        for (int i = 0; i < specks; i++) {
            int x = Math.floorMod((thermalNoiseTick * 13) + (i * 37), Math.max(1, width));
            int y = Math.floorMod((thermalNoiseTick * 7) + (i * 19), Math.max(1, height));
            int alpha = 28 + Math.floorMod(thermalNoiseTick + (i * 9), 20);
            guiGraphics.fill(x, y, Math.min(width, x + 2), Math.min(height, y + 2), (alpha << 24) | 0xB4FFD0);
        }
    }

    private static boolean isActiveDroneView(LocalPlayer player) {
        if (player == null) {
            return false;
        }

        return SbwCompat.isUsingLinkedMonitor(SbwCompat.getActiveLinkedMonitor(player));
    }

    private static boolean isAnyDroneOperator(Player player) {
        if (player == null) {
            return false;
        }

        return SbwCompat.isUsingLinkedMonitor(SbwCompat.getActiveLinkedMonitor(player));
    }

    private static boolean isHostile(LivingEntity living) {
        return living instanceof Enemy
                || living.getType().getCategory().isFriendly() == false
                && living.getType() != EntityType.PLAYER
                && !(living instanceof Animal);
    }
}
