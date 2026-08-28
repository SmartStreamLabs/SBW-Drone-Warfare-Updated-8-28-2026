package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public final class DroneSpotlightClient {
    private static final KeyMapping TOGGLE_SPOTLIGHT = new KeyMapping(
            "key.sbwdroneconfig.toggle_spotlight",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.sbwdroneconfig"
    );
    private static final Set<UUID> ACTIVE_SPOTLIGHT_DRONES = new HashSet<>();
    private static final int TOGGLE_COOLDOWN_TICKS = 6;
    private static int toggleCooldown;

    private DroneSpotlightClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(DroneSpotlightClient::onClientSetup);
        modBus.addListener(DroneSpotlightClient::onRegisterKeyMappings);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneSpotlightClient.class);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_SPOTLIGHT);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (toggleCooldown > 0) {
            toggleCooldown--;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            DroneSpotlightClientLightSystem.clearAll(minecraft.level);
            ACTIVE_SPOTLIGHT_DRONES.clear();
            return;
        }

        while (TOGGLE_SPOTLIGHT.consumeClick()) {
            if (!AddonConfig.enableSpotlightModule() || !isDroneFpvActive(minecraft.player) || toggleCooldown > 0) {
                continue;
            }

            AddonNetwork.toggleSpotlight();
            toggleCooldown = TOGGLE_COOLDOWN_TICKS;
        }

        DroneSpotlightClientLightSystem.tick(minecraft, ACTIVE_SPOTLIGHT_DRONES);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == null || !"hotbar".equals(event.getOverlay().id().getPath())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        UUID activeDroneId = getControlledDroneUuid(player);
        if (activeDroneId == null || !ACTIVE_SPOTLIGHT_DRONES.contains(activeDroneId)) {
            return;
        }

        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        renderScreenCone(event.getGuiGraphics(), width, height);

        Component text = Component.translatable("overlay.sbwdroneconfig.spotlight_on");
        int x = width - minecraft.font.width(text) - 10;
        event.getGuiGraphics().drawString(minecraft.font, text, x, 34, 0xFFF5D1, true);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || ACTIVE_SPOTLIGHT_DRONES.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel clientLevel = minecraft.level;
        if (clientLevel == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderSystem.disableDepthTest();

        Iterator<UUID> iterator = ACTIVE_SPOTLIGHT_DRONES.iterator();
        while (iterator.hasNext()) {
            UUID droneId = iterator.next();
            Entity drone = findEntity(clientLevel, droneId);
            if (drone == null || !drone.isAlive()) {
                iterator.remove();
                continue;
            }

            if (!AddonConfig.spotlightVisibleToOtherPlayers()) {
                LocalPlayer player = minecraft.player;
                if (player == null || !isLocalSpotlightDrone(player, droneId)) {
                    continue;
                }
            }

            renderSpotlightBeam(poseStack, lines, drone);
        }

        RenderSystem.enableDepthTest();
        poseStack.popPose();
        buffers.endBatch(RenderType.lines());
    }

    public static void handleSpotlightState(DroneSpotlightStateMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean localControlledDrone = player != null && message.droneId().equals(getControlledDroneUuid(player));

        if (message.active()) {
            ACTIVE_SPOTLIGHT_DRONES.add(message.droneId());
        } else {
            ACTIVE_SPOTLIGHT_DRONES.remove(message.droneId());
            DroneSpotlightClientLightSystem.clearForDrone(minecraft.level, message.droneId());
        }

        if (localControlledDrone && minecraft.level != null) {
            minecraft.level.playLocalSound(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    message.active() ? SoundEvents.REDSTONE_TORCH_BURNOUT : SoundEvents.LEVER_CLICK,
                    SoundSource.PLAYERS,
                    0.45F,
                    message.active() ? 1.2F : 0.85F,
                    false
            );
        }
    }

    private static void renderScreenCone(net.minecraft.client.gui.GuiGraphics guiGraphics, int width, int height) {
        int centerX = width / 2;
        int startY = height / 2;
        int beamHeight = Math.max(48, height / 3);

        for (int step = 0; step < 12; step++) {
            float progress = step / 11.0F;
            int segmentTop = startY + Math.round(progress * beamHeight);
            int segmentBottom = segmentTop + Math.max(6, beamHeight / 12);
            int halfWidth = Math.round(8 + progress * (width * 0.18F));
            int alpha = Mth.clamp((int) (48 - (progress * 28)), 10, 48);
            int color = (alpha << 24) | 0xFFF2B0;
            guiGraphics.fill(centerX - halfWidth, segmentTop, centerX + halfWidth, segmentBottom, color);
        }
    }

    private static void renderSpotlightBeam(PoseStack poseStack, VertexConsumer lines, Entity drone) {
        Vec3 direction = drone.getLookAngle().normalize();
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = Vec3.directionFromRotation(drone.getXRot(), drone.getYRot());
        }

        Vec3 origin = drone.position().add(0.0D, 0.15D, 0.0D).add(direction.scale(0.7D));
        Vec3 end = origin.add(direction.scale(AddonConfig.spotlightRange()));
        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = direction.cross(worldUp);
        if (right.lengthSqr() < 1.0E-4D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 beamUp = right.cross(direction).normalize();

        double endHalfWidth = 2.0D;
        double endHalfHeight = 1.3D;
        Vec3 topLeft = end.add(right.scale(-endHalfWidth)).add(beamUp.scale(endHalfHeight));
        Vec3 topRight = end.add(right.scale(endHalfWidth)).add(beamUp.scale(endHalfHeight));
        Vec3 bottomLeft = end.add(right.scale(-endHalfWidth)).add(beamUp.scale(-endHalfHeight));
        Vec3 bottomRight = end.add(right.scale(endHalfWidth)).add(beamUp.scale(-endHalfHeight));

        Matrix4f matrix = poseStack.last().pose();
        drawLine(lines, matrix, origin, topLeft, 255, 244, 180, 180);
        drawLine(lines, matrix, origin, topRight, 255, 244, 180, 180);
        drawLine(lines, matrix, origin, bottomLeft, 255, 224, 140, 150);
        drawLine(lines, matrix, origin, bottomRight, 255, 224, 140, 150);
        drawLine(lines, matrix, topLeft, topRight, 255, 244, 180, 90);
        drawLine(lines, matrix, topRight, bottomRight, 255, 224, 140, 70);
        drawLine(lines, matrix, bottomRight, bottomLeft, 255, 224, 140, 70);
        drawLine(lines, matrix, bottomLeft, topLeft, 255, 224, 140, 70);
    }

    private static void drawLine(VertexConsumer consumer, Matrix4f matrix, Vec3 from, Vec3 to, int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, (float) from.x, (float) from.y, (float) from.z).color(red, green, blue, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, (float) to.x, (float) to.y, (float) to.z).color(red, green, blue, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    static UUID getControlledDroneUuid(LocalPlayer player) {
        if (player == null) {
            return null;
        }

        ItemStack monitor = SbwCompat.getActiveLinkedMonitor(player);
        return monitor.isEmpty() ? null : SbwCompat.getLinkedDroneUuid(monitor);
    }

    private static boolean isDroneFpvActive(LocalPlayer player) {
        return getControlledDroneUuid(player) != null;
    }

    private static boolean isLocalSpotlightDrone(LocalPlayer player, UUID droneId) {
        if (player == null || droneId == null) {
            return false;
        }

        UUID active = getControlledDroneUuid(player);
        if (droneId.equals(active)) {
            return true;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (SbwCompat.isLinkedMonitor(mainHand) && droneId.equals(SbwCompat.getLinkedDroneUuid(mainHand))) {
            return true;
        }

        ItemStack offhand = player.getOffhandItem();
        return SbwCompat.isLinkedMonitor(offhand) && droneId.equals(SbwCompat.getLinkedDroneUuid(offhand));
    }

    private static Entity findEntity(ClientLevel level, UUID uuid) {
        for (Entity entity : level.entitiesForRendering()) {
            if (uuid.equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }
}
