package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class VehicleAntiDroneNetClient {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            SbwDroneRangeConfig.MOD_ID,
            "textures/block/anti_drone_net.png"
    );
    private static final int RED = 255;
    private static final int GREEN = 255;
    private static final int BLUE = 255;
    private static final int ACTIVE_STATE_TTL_TICKS = 40;
    private static final Map<Integer, Long> ACTIVE_VEHICLE_NETS = new HashMap<>();

    private VehicleAntiDroneNetClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(VehicleAntiDroneNetClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(VehicleAntiDroneNetClient.class);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS
                || !AddonConfig.vehicleAntiDroneNetEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel clientLevel = minecraft.level;
        if (clientLevel == null) {
            ACTIVE_VEHICLE_NETS.clear();
            return;
        }

        long gameTime = clientLevel.getGameTime();
        pruneVehicleNetState(clientLevel, gameTime);

        RenderType renderType = RenderType.entityTranslucent(TEXTURE);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer quads = buffers.getBuffer(renderType);
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPosition = event.getCamera().getPosition();
        int alpha = Mth.clamp((int) Math.round(AddonConfig.vehicleAntiDroneNetOpacity() * 255.0D), 128, 255);

        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        for (Entity entity : clientLevel.entitiesForRendering()) {
            if (!shouldRenderVehicleNet(clientLevel, entity, gameTime)) {
                continue;
            }

            VehicleAntiDroneNetGeometry.NetBox box = VehicleAntiDroneNetGeometry.createBox(
                    entity,
                    AddonConfig.vehicleAntiDroneNetHorizontalPadding(),
                    AddonConfig.vehicleAntiDroneNetTopPadding()
            );
            VehicleAntiDroneNetGeometry.Basis basis = VehicleAntiDroneNetGeometry.basis(entity, event.getPartialTick());
            int packedLight = minecraft.getEntityRenderDispatcher().getPackedLightCoords(entity, event.getPartialTick());
            renderNetBox(poseStack, quads, basis, box, alpha, packedLight);
        }

        RenderSystem.enableCull();
        poseStack.popPose();
        buffers.endBatch(renderType);
    }

    public static void handleVehicleNetState(VehicleAntiDroneNetStateMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel clientLevel = minecraft.level;
        if (clientLevel == null) {
            ACTIVE_VEHICLE_NETS.clear();
            return;
        }

        if (message.active()) {
            ACTIVE_VEHICLE_NETS.put(message.entityId(), clientLevel.getGameTime() + ACTIVE_STATE_TTL_TICKS);
        } else {
            ACTIVE_VEHICLE_NETS.remove(message.entityId());
        }
    }

    private static boolean shouldRenderVehicleNet(ClientLevel clientLevel, Entity entity, long gameTime) {
        if (!SbwCompat.isSbwVehicle(entity)) {
            return false;
        }

        Long expiresAt = ACTIVE_VEHICLE_NETS.get(entity.getId());
        if (expiresAt != null && expiresAt >= gameTime) {
            return true;
        }

        return SbwCompat.hasVehicleAntiDroneNetUpgrade(entity);
    }

    private static void pruneVehicleNetState(ClientLevel clientLevel, long gameTime) {
        Iterator<Map.Entry<Integer, Long>> iterator = ACTIVE_VEHICLE_NETS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            if (entry.getValue() < gameTime || clientLevel.getEntity(entry.getKey()) == null) {
                iterator.remove();
            }
        }
    }

    private static void renderNetBox(
            PoseStack poseStack,
            VertexConsumer quads,
            VehicleAntiDroneNetGeometry.Basis basis,
            VehicleAntiDroneNetGeometry.NetBox box,
            int alpha,
            int packedLight
    ) {
        double x0 = box.minX();
        double x1 = box.maxX();
        double y0 = box.minY();
        double y1 = box.maxY();
        double z0 = box.minZ();
        double z1 = box.maxZ();

        renderTiledQuad(poseStack, quads, basis,
                x0, y1, z0,
                x1, y1, z0,
                x1, y1, z1,
                x0, y1, z1,
                box.width(),
                box.length(),
                alpha,
                packedLight);
        renderTiledQuad(poseStack, quads, basis,
                x0, y0, z0,
                x0, y1, z0,
                x1, y1, z0,
                x1, y0, z0,
                box.height(),
                box.width(),
                alpha,
                packedLight);
        renderTiledQuad(poseStack, quads, basis,
                x1, y0, z1,
                x1, y1, z1,
                x0, y1, z1,
                x0, y0, z1,
                box.height(),
                box.width(),
                alpha,
                packedLight);
        renderTiledQuad(poseStack, quads, basis,
                x0, y0, z1,
                x0, y1, z1,
                x0, y1, z0,
                x0, y0, z0,
                box.height(),
                box.length(),
                alpha,
                packedLight);
        renderTiledQuad(poseStack, quads, basis,
                x1, y0, z0,
                x1, y1, z0,
                x1, y1, z1,
                x1, y0, z1,
                box.height(),
                box.length(),
                alpha,
                packedLight);
    }

    private static void renderTiledQuad(
            PoseStack poseStack,
            VertexConsumer quads,
            VehicleAntiDroneNetGeometry.Basis basis,
            double ax,
            double ay,
            double az,
            double bx,
            double by,
            double bz,
            double cx,
            double cy,
            double cz,
            double dx,
            double dy,
            double dz,
            double uLength,
            double vLength,
            int alpha,
            int packedLight
    ) {
        Vec3 a = new Vec3(ax, ay, az);
        Vec3 b = new Vec3(bx, by, bz);
        Vec3 c = new Vec3(cx, cy, cz);
        Vec3 d = new Vec3(dx, dy, dz);
        int uSegments = Math.max(1, (int) Math.ceil(uLength / VehicleAntiDroneNetGeometry.TILE_SIZE));
        int vSegments = Math.max(1, (int) Math.ceil(vLength / VehicleAntiDroneNetGeometry.TILE_SIZE));

        for (int u = 0; u < uSegments; u++) {
            double u0 = (double) u / (double) uSegments;
            double u1 = (double) (u + 1) / (double) uSegments;
            for (int v = 0; v < vSegments; v++) {
                double v0 = (double) v / (double) vSegments;
                double v1 = (double) (v + 1) / (double) vSegments;
                Vec3 p00 = bilerp(a, b, c, d, u0, v0);
                Vec3 p10 = bilerp(a, b, c, d, u1, v0);
                Vec3 p11 = bilerp(a, b, c, d, u1, v1);
                Vec3 p01 = bilerp(a, b, c, d, u0, v1);
                float maxU = (float) Math.min(1.0D, uLength / (double) uSegments / VehicleAntiDroneNetGeometry.TILE_SIZE);
                float maxV = (float) Math.min(1.0D, vLength / (double) vSegments / VehicleAntiDroneNetGeometry.TILE_SIZE);
                renderQuad(poseStack, quads, basis, p00, p10, p11, p01, maxU, maxV, alpha, packedLight);
            }
        }
    }

    private static void renderQuad(
            PoseStack poseStack,
            VertexConsumer quads,
            VehicleAntiDroneNetGeometry.Basis basis,
            Vec3 localA,
            Vec3 localB,
            Vec3 localC,
            Vec3 localD,
            float uMax,
            float vMax,
            int alpha,
            int packedLight
    ) {
        Vec3 a = basis.toWorld(localA.x, localA.y, localA.z);
        Vec3 b = basis.toWorld(localB.x, localB.y, localB.z);
        Vec3 c = basis.toWorld(localC.x, localC.y, localC.z);
        Vec3 d = basis.toWorld(localD.x, localD.y, localD.z);
        Vec3 faceNormal = b.subtract(a).cross(d.subtract(a));
        if (faceNormal.lengthSqr() < 1.0E-6D) {
            faceNormal = basis.up();
        } else {
            faceNormal = faceNormal.normalize();
        }

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        putVertex(quads, matrix, normal, a, 0.0F, 0.0F, alpha, packedLight, faceNormal);
        putVertex(quads, matrix, normal, b, uMax, 0.0F, alpha, packedLight, faceNormal);
        putVertex(quads, matrix, normal, c, uMax, vMax, alpha, packedLight, faceNormal);
        putVertex(quads, matrix, normal, d, 0.0F, vMax, alpha, packedLight, faceNormal);

        Vec3 reverseNormal = faceNormal.scale(-1.0D);
        putVertex(quads, matrix, normal, d, 0.0F, vMax, alpha, packedLight, reverseNormal);
        putVertex(quads, matrix, normal, c, uMax, vMax, alpha, packedLight, reverseNormal);
        putVertex(quads, matrix, normal, b, uMax, 0.0F, alpha, packedLight, reverseNormal);
        putVertex(quads, matrix, normal, a, 0.0F, 0.0F, alpha, packedLight, reverseNormal);
    }

    private static Vec3 bilerp(Vec3 a, Vec3 b, Vec3 c, Vec3 d, double u, double v) {
        Vec3 top = lerp(a, b, u);
        Vec3 bottom = lerp(d, c, u);
        return lerp(top, bottom, v);
    }

    private static Vec3 lerp(Vec3 a, Vec3 b, double progress) {
        return new Vec3(
                Mth.lerp(progress, a.x, b.x),
                Mth.lerp(progress, a.y, b.y),
                Mth.lerp(progress, a.z, b.z)
        );
    }

    private static void putVertex(
            VertexConsumer quads,
            Matrix4f matrix,
            Matrix3f normal,
            Vec3 point,
            float u,
            float v,
            int alpha,
            int packedLight,
            Vec3 faceNormal
    ) {
        quads.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(RED, GREEN, BLUE, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, (float) faceNormal.x, (float) faceNormal.y, (float) faceNormal.z)
                .endVertex();
    }
}
