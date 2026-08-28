package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_DATA;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY_TAG;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.AMMO;

public class CubedFpvDroneRenderer extends GeoEntityRenderer<CubedFpvDroneEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("minecraft", "textures/misc/white.png");
    static final Vec3 FIXED_PAYLOAD_ATTACHMENT_OFFSET = new Vec3(0.0D, 0.10D, 0.0D);
    static final Vec3 FIXED_PAYLOAD_CORD_END_OFFSET = new Vec3(0.0D, -0.10D, 0.0D);
    static final Vec3 FIXED_PAYLOAD_GRENADE_OFFSET = new Vec3(0.0D, -0.22D, 0.0D);
    static final float FIXED_PAYLOAD_GRENADE_SCALE = 0.50F;
    private static final int MODULE_HOUSING_COLOR = 0x141A1D;
    private static final int MODULE_HOUSING_DARK_COLOR = 0x070A0B;
    private static final int MODULE_END_COLOR = 0x252B2F;
    private static final int MODULE_FRAME_COLOR = 0x1B2024;
    private static final int MODULE_FRAME_DETAIL_COLOR = 0x343B40;
    private static final int MODULE_MOUNT_COLOR = 0x0B0E10;
    private static final int MODULE_OUTLET_COLOR = 0x050707;
    private static final int MODULE_CONNECTOR_COLOR = 0xA0502A;
    private static final int MODULE_GROOVE_COLOR = 0x3D454A;
    private static final int MODULE_BOLT_COLOR = 0x5B6268;
    private static final int MODULE_SOLID_ALPHA = 255;

    private String entityNameCache = "";
    private Entity entityCache = null;
    private int attachedTick = Integer.MAX_VALUE;
    private int lastPayloadDistanceLogTick = Integer.MIN_VALUE;

    public CubedFpvDroneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CubedFpvDroneModel());
        this.shadowRadius = 0.2F;
    }

    @Override
    public RenderType getRenderType(CubedFpvDroneEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void render(CubedFpvDroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        if (DroneClientViewContext.isLocalFpvViewOf(entity)) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        boolean firstPersonCamera = cameraType == CameraType.FIRST_PERSON;
        boolean suppressAttachmentsForCamera = firstPersonCamera || cameraType == CameraType.THIRD_PERSON_BACK;
        boolean controllingLinkedDrone = false;

        controllingLinkedDrone = SbwCompat.isUsingLinkedMonitorForDrone(player, entity);

        poseStack.pushPose();
        float droneVisualScale = CubedFpvDroneRenderMath.droneVisualScale();
        poseStack.scale(droneVisualScale, droneVisualScale, droneVisualScale);
        poseStack.translate(0.0D, CubedFpvDroneRenderMath.bodyTranslationY(), 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(CubedFpvDroneRenderMath.renderYawDegrees(entity.getYaw(partialTicks))));
        poseStack.mulPose(Axis.XP.rotationDegrees(CubedFpvDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks))));
        poseStack.mulPose(Axis.ZP.rotationDegrees(CubedFpvDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks))));

        boolean renderBody = !CubedFpvDroneRenderMath.shouldHideControlledDroneBody(controllingLinkedDrone, firstPersonCamera);
        if (renderBody) {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            if (hasFiberSpoolModule(entity)) {
                renderFiberSpoolModule(poseStack, buffer, packedLight);
            }
        }

        poseStack.popPose();

        if (!controllingLinkedDrone || !suppressAttachmentsForCamera) {
            poseStack.pushPose();
            applyMountedPayloadBaseTransform(entity, partialTicks, poseStack);
            renderAttachments(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    static boolean hasMountedPayload(CubedFpvDroneEntity entity) {
        var data = entity.getEntityData();
        return !data.get(DISPLAY_ENTITY).isEmpty()
                && data.get(AMMO) > 0
                && data.get(DISPLAY_DATA).size() >= 12;
    }

    static boolean hasFiberSpoolModule(CubedFpvDroneEntity entity) {
        return DroneFiberOpticClient.hasFiberSpoolModule(entity);
    }

    static Vec3 fiberCableExitWorldPosition(CubedFpvDroneEntity entity, float partialTicks) {
        return CubedFpvFiberSpoolGeometry.cableExitWorldPosition(entity, partialTicks);
    }

    static void applyMountedPayloadBaseTransform(CubedFpvDroneEntity entity, float partialTicks, PoseStack poseStack) {
        poseStack.translate(0.0D, CubedFpvDroneRenderMath.attachmentBaseYOffset(), 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(CubedFpvDroneRenderMath.renderYawDegrees(entity.getYaw(partialTicks))));
        poseStack.mulPose(Axis.XP.rotationDegrees(CubedFpvDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks))));
        poseStack.mulPose(Axis.ZP.rotationDegrees(CubedFpvDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks))));
    }

    private void renderAttachments(CubedFpvDroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!hasMountedPayload(entity)) {
            return;
        }

        var data = entity.getEntityData();
        var attached = data.get(DISPLAY_ENTITY);
        Entity renderEntity;
        if (entityNameCache.equals(attached) && entityCache != null) {
            renderEntity = entityCache;
        } else {
            renderEntity = EntityType.byString(attached)
                    .map(type -> type.create(entity.level()))
                    .orElse(null);
            if (renderEntity == null) {
                return;
            }

            var tag = data.get(DISPLAY_ENTITY_TAG);
            if (!tag.isEmpty()) {
                renderEntity.load(tag);
            }

            entityNameCache = attached;
            entityCache = renderEntity;
            attachedTick = entity.tickCount;
        }

        var displayData = data.get(DISPLAY_DATA);
        if (displayData.size() < 12) {
            return;
        }

        renderEntity.tickCount = displayData.get(11) >= 0 ? displayData.get(11).intValue() : entity.tickCount - attachedTick;
        var rotation = new float[]{displayData.get(6), displayData.get(7), displayData.get(8)};
        double distanceFromAttachmentToGrenade = FIXED_PAYLOAD_GRENADE_OFFSET.length();
        if (distanceFromAttachmentToGrenade > 0.75D) {
            if (entity.tickCount - lastPayloadDistanceLogTick >= 20) {
                lastPayloadDistanceLogTick = entity.tickCount;
                LOGGER.warn("[Cubed FPV] Invalid mounted payload transform: distanceFromAttachmentToGrenade={} blocks, attachment={}, grenade={}",
                        String.format("%.3f", distanceFromAttachmentToGrenade),
                        FIXED_PAYLOAD_ATTACHMENT_OFFSET,
                        FIXED_PAYLOAD_GRENADE_OFFSET);
            }
            return;
        }

        if (entity.tickCount - lastPayloadDistanceLogTick >= 40) {
            lastPayloadDistanceLogTick = entity.tickCount;
            LOGGER.info("[Cubed FPV] Mounted payload local transform: distanceFromAttachmentToGrenade={} blocks, cordLength={} blocks, grenadeLocalY={}, grenadeScale={}",
                    String.format("%.3f", distanceFromAttachmentToGrenade),
                    String.format("%.3f", Math.abs(FIXED_PAYLOAD_CORD_END_OFFSET.y)),
                    String.format("%.3f", FIXED_PAYLOAD_GRENADE_OFFSET.y),
                    String.format("%.2f", FIXED_PAYLOAD_GRENADE_SCALE));
        }

        poseStack.pushPose();
        poseStack.translate(FIXED_PAYLOAD_ATTACHMENT_OFFSET.x, FIXED_PAYLOAD_ATTACHMENT_OFFSET.y, FIXED_PAYLOAD_ATTACHMENT_OFFSET.z);
        renderCord(poseStack, buffer, FIXED_PAYLOAD_CORD_END_OFFSET);
        poseStack.translate(FIXED_PAYLOAD_GRENADE_OFFSET.x, FIXED_PAYLOAD_GRENADE_OFFSET.y, FIXED_PAYLOAD_GRENADE_OFFSET.z);

        float attachmentScaleCompensation = CubedFpvDroneRenderMath.attachmentScaleCompensation();
        poseStack.scale(attachmentScaleCompensation, attachmentScaleCompensation, attachmentScaleCompensation);
        poseStack.scale(FIXED_PAYLOAD_GRENADE_SCALE, FIXED_PAYLOAD_GRENADE_SCALE, FIXED_PAYLOAD_GRENADE_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) AddonConfig.fpvPayloadGrenadeRotationYaw() + rotation[2]));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) AddonConfig.fpvPayloadGrenadeRotationPitch() + rotation[0]));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) AddonConfig.fpvPayloadGrenadeRotationRoll() + rotation[1]));

        entityRenderDispatcher.render(renderEntity, 0, 0, 0, 0.0F, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static void renderFiberSpoolModule(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer solid = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));

        renderOctagonalModuleBodyZ(
                solid,
                matrix,
                normal,
                CubedFpvFiberSpoolGeometry.MAIN_HOUSING_MIN_Z,
                CubedFpvFiberSpoolGeometry.MAIN_HOUSING_MAX_Z,
                CubedFpvFiberSpoolGeometry.POD_CENTER_X,
                CubedFpvFiberSpoolGeometry.POD_CENTER_Y,
                CubedFpvFiberSpoolGeometry.MAIN_HOUSING_RADIUS_X,
                CubedFpvFiberSpoolGeometry.MAIN_HOUSING_RADIUS_Y,
                CubedFpvFiberSpoolGeometry.HOUSING_SEGMENTS,
                MODULE_HOUSING_COLOR,
                MODULE_SOLID_ALPHA,
                packedLight
        );
        renderOctagonalModuleBodyZ(
                solid,
                matrix,
                normal,
                CubedFpvFiberSpoolGeometry.LEFT_SPOOL_CAP_MIN_Z,
                CubedFpvFiberSpoolGeometry.LEFT_SPOOL_CAP_MAX_Z,
                CubedFpvFiberSpoolGeometry.POD_CENTER_X,
                CubedFpvFiberSpoolGeometry.POD_CENTER_Y,
                CubedFpvFiberSpoolGeometry.SPOOL_CAP_RADIUS_X,
                CubedFpvFiberSpoolGeometry.SPOOL_CAP_RADIUS_Y,
                CubedFpvFiberSpoolGeometry.HOUSING_SEGMENTS,
                MODULE_END_COLOR,
                MODULE_SOLID_ALPHA,
                packedLight
        );
        renderOctagonalModuleBodyZ(
                solid,
                matrix,
                normal,
                CubedFpvFiberSpoolGeometry.RIGHT_SPOOL_CAP_MIN_Z,
                CubedFpvFiberSpoolGeometry.RIGHT_SPOOL_CAP_MAX_Z,
                CubedFpvFiberSpoolGeometry.POD_CENTER_X,
                CubedFpvFiberSpoolGeometry.POD_CENTER_Y,
                CubedFpvFiberSpoolGeometry.SPOOL_CAP_RADIUS_X,
                CubedFpvFiberSpoolGeometry.SPOOL_CAP_RADIUS_Y,
                CubedFpvFiberSpoolGeometry.HOUSING_SEGMENTS,
                MODULE_END_COLOR,
                MODULE_SOLID_ALPHA,
                packedLight
        );
        renderOctagonalDiskZ(
                solid,
                matrix,
                normal,
                CubedFpvFiberSpoolGeometry.LEFT_SPOOL_CAP_MIN_Z - 0.001D,
                CubedFpvFiberSpoolGeometry.POD_CENTER_X,
                CubedFpvFiberSpoolGeometry.POD_CENTER_Y,
                CubedFpvFiberSpoolGeometry.SIDE_HUB_RADIUS_X,
                CubedFpvFiberSpoolGeometry.SIDE_HUB_RADIUS_Y,
                CubedFpvFiberSpoolGeometry.HOUSING_SEGMENTS,
                MODULE_BOLT_COLOR,
                MODULE_SOLID_ALPHA,
                packedLight,
                -1.0F
        );
        renderOctagonalDiskZ(
                solid,
                matrix,
                normal,
                CubedFpvFiberSpoolGeometry.RIGHT_SPOOL_CAP_MAX_Z + 0.001D,
                CubedFpvFiberSpoolGeometry.POD_CENTER_X,
                CubedFpvFiberSpoolGeometry.POD_CENTER_Y,
                CubedFpvFiberSpoolGeometry.SIDE_HUB_RADIUS_X,
                CubedFpvFiberSpoolGeometry.SIDE_HUB_RADIUS_Y,
                CubedFpvFiberSpoolGeometry.HOUSING_SEGMENTS,
                MODULE_BOLT_COLOR,
                MODULE_SOLID_ALPHA,
                packedLight,
                1.0F
        );

        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.UPPER_MOUNT_MIN, CubedFpvFiberSpoolGeometry.UPPER_MOUNT_MAX, MODULE_MOUNT_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.LEFT_CLAMP_MIN, CubedFpvFiberSpoolGeometry.LEFT_CLAMP_MAX, MODULE_MOUNT_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.RIGHT_CLAMP_MIN, CubedFpvFiberSpoolGeometry.RIGHT_CLAMP_MAX, MODULE_MOUNT_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.LEFT_FRAME_UPRIGHT_MIN, CubedFpvFiberSpoolGeometry.LEFT_FRAME_UPRIGHT_MAX, MODULE_FRAME_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.RIGHT_FRAME_UPRIGHT_MIN, CubedFpvFiberSpoolGeometry.RIGHT_FRAME_UPRIGHT_MAX, MODULE_FRAME_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.LEFT_FRAME_BOTTOM_RAIL_MIN, CubedFpvFiberSpoolGeometry.LEFT_FRAME_BOTTOM_RAIL_MAX, MODULE_FRAME_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.RIGHT_FRAME_BOTTOM_RAIL_MIN, CubedFpvFiberSpoolGeometry.RIGHT_FRAME_BOTTOM_RAIL_MAX, MODULE_FRAME_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.BOTTOM_FRAME_REAR_CROSSBAR_MIN, CubedFpvFiberSpoolGeometry.BOTTOM_FRAME_REAR_CROSSBAR_MAX, MODULE_FRAME_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.BOTTOM_FRAME_FRONT_CROSSBAR_MIN, CubedFpvFiberSpoolGeometry.BOTTOM_FRAME_FRONT_CROSSBAR_MAX, MODULE_FRAME_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.LEFT_FRAME_SHOULDER_MIN, CubedFpvFiberSpoolGeometry.LEFT_FRAME_SHOULDER_MAX, MODULE_FRAME_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.RIGHT_FRAME_SHOULDER_MIN, CubedFpvFiberSpoolGeometry.RIGHT_FRAME_SHOULDER_MAX, MODULE_FRAME_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.OUTLET_MIN, CubedFpvFiberSpoolGeometry.OUTLET_MAX, MODULE_OUTLET_COLOR, MODULE_SOLID_ALPHA, packedLight);
        renderBox(solid, matrix, normal, CubedFpvFiberSpoolGeometry.CONNECTOR_MIN, CubedFpvFiberSpoolGeometry.CONNECTOR_MAX, MODULE_CONNECTOR_COLOR, MODULE_SOLID_ALPHA, packedLight);

        renderEnclosedModuleDetails(poseStack, buffer);
    }

    private static void renderOctagonalModuleBodyZ(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normal,
            double minZ,
            double maxZ,
            double centerX,
            double centerY,
            double radiusX,
            double radiusY,
            int segments,
            int color,
            int alpha,
            int packedLight
    ) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        Vec3 leftCenter = new Vec3(centerX, centerY, minZ);
        Vec3 rightCenter = new Vec3(centerX, centerY, maxZ);

        for (int segment = 0; segment < segments; segment++) {
            double angle0 = (Math.PI * 2.0D * segment) / segments;
            double angle1 = (Math.PI * 2.0D * (segment + 1)) / segments;
            Vec3 left0 = cylinderPointZ(minZ, centerX, centerY, radiusX, radiusY, angle0);
            Vec3 right0 = cylinderPointZ(maxZ, centerX, centerY, radiusX, radiusY, angle0);
            Vec3 right1 = cylinderPointZ(maxZ, centerX, centerY, radiusX, radiusY, angle1);
            Vec3 left1 = cylinderPointZ(minZ, centerX, centerY, radiusX, radiusY, angle1);
            double midAngle = (angle0 + angle1) * 0.5D;
            float normalX = (float) Math.cos(midAngle);
            float normalY = (float) Math.sin(midAngle);

            putQuad(consumer, matrix, normal, left0, right0, right1, left1, red, green, blue, alpha, packedLight, normalX, normalY, 0.0F);
            putQuad(consumer, matrix, normal, leftCenter, left1, left0, leftCenter, red, green, blue, alpha, packedLight, 0.0F, 0.0F, -1.0F);
            putQuad(consumer, matrix, normal, rightCenter, right0, right1, rightCenter, red, green, blue, alpha, packedLight, 0.0F, 0.0F, 1.0F);
        }
    }

    private static void renderOctagonalDiskZ(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normal,
            double z,
            double centerX,
            double centerY,
            double radiusX,
            double radiusY,
            int segments,
            int color,
            int alpha,
            int packedLight,
            float normalZ
    ) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        Vec3 center = new Vec3(centerX, centerY, z);

        for (int segment = 0; segment < segments; segment++) {
            double angle0 = (Math.PI * 2.0D * segment) / segments;
            double angle1 = (Math.PI * 2.0D * (segment + 1)) / segments;
            Vec3 point0 = cylinderPointZ(z, centerX, centerY, radiusX, radiusY, angle0);
            Vec3 point1 = cylinderPointZ(z, centerX, centerY, radiusX, radiusY, angle1);
            putQuad(consumer, matrix, normal, center, point0, point1, center, red, green, blue, alpha, packedLight, 0.0F, 0.0F, normalZ);
        }
    }

    private static Vec3 cylinderPointZ(double z, double centerX, double centerY, double radiusX, double radiusY, double angle) {
        return new Vec3(
                centerX + Math.cos(angle) * radiusX,
                centerY + Math.sin(angle) * radiusY,
                z
        );
    }

    private static void renderBox(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Vec3 min, Vec3 max, int color, int alpha, int packedLight) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        Vec3 x0y0z0 = new Vec3(min.x, min.y, min.z);
        Vec3 x1y0z0 = new Vec3(max.x, min.y, min.z);
        Vec3 x1y1z0 = new Vec3(max.x, max.y, min.z);
        Vec3 x0y1z0 = new Vec3(min.x, max.y, min.z);
        Vec3 x0y0z1 = new Vec3(min.x, min.y, max.z);
        Vec3 x1y0z1 = new Vec3(max.x, min.y, max.z);
        Vec3 x1y1z1 = new Vec3(max.x, max.y, max.z);
        Vec3 x0y1z1 = new Vec3(min.x, max.y, max.z);

        putQuad(consumer, matrix, normal, x0y0z0, x1y0z0, x1y1z0, x0y1z0, red, green, blue, alpha, packedLight, 0.0F, 0.0F, -1.0F);
        putQuad(consumer, matrix, normal, x1y0z1, x0y0z1, x0y1z1, x1y1z1, red, green, blue, alpha, packedLight, 0.0F, 0.0F, 1.0F);
        putQuad(consumer, matrix, normal, x0y0z1, x0y0z0, x0y1z0, x0y1z1, red, green, blue, alpha, packedLight, -1.0F, 0.0F, 0.0F);
        putQuad(consumer, matrix, normal, x1y0z0, x1y0z1, x1y1z1, x1y1z0, red, green, blue, alpha, packedLight, 1.0F, 0.0F, 0.0F);
        putQuad(consumer, matrix, normal, x0y1z0, x1y1z0, x1y1z1, x0y1z1, red, green, blue, alpha, packedLight, 0.0F, 1.0F, 0.0F);
        putQuad(consumer, matrix, normal, x0y0z1, x1y0z1, x1y0z0, x0y0z0, red, green, blue, alpha, packedLight, 0.0F, -1.0F, 0.0F);
    }

    private static void putQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normal,
            Vec3 a,
            Vec3 b,
            Vec3 c,
            Vec3 d,
            int red,
            int green,
            int blue,
            int alpha,
            int packedLight,
            float normalX,
            float normalY,
            float normalZ
    ) {
        putSolidVertex(consumer, matrix, normal, a, red, green, blue, alpha, packedLight, normalX, normalY, normalZ);
        putSolidVertex(consumer, matrix, normal, b, red, green, blue, alpha, packedLight, normalX, normalY, normalZ);
        putSolidVertex(consumer, matrix, normal, c, red, green, blue, alpha, packedLight, normalX, normalY, normalZ);
        putSolidVertex(consumer, matrix, normal, d, red, green, blue, alpha, packedLight, normalX, normalY, normalZ);
    }

    private static void putSolidVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Vec3 point, int red, int green, int blue, int alpha, int packedLight, float normalX, float normalY, float normalZ) {
        consumer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha)
                .uv(0.5F, 0.5F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }

    private static void renderEnclosedModuleDetails(PoseStack poseStack, MultiBufferSource buffer) {
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        drawEndCapGrooveZ(lines, matrix, CubedFpvFiberSpoolGeometry.LEFT_SPOOL_CAP_MIN_Z - 0.002D, -1.0D);
        drawEndCapGrooveZ(lines, matrix, CubedFpvFiberSpoolGeometry.RIGHT_SPOOL_CAP_MAX_Z + 0.002D, 1.0D);
        drawCordLine(lines, matrix, CubedFpvFiberSpoolGeometry.CONNECTOR_CABLE_ORIGIN_LOCAL, CubedFpvFiberSpoolGeometry.CABLE_EXIT_LOCAL, 42, 36, 28, 255);
        drawCordLine(lines, matrix, new Vec3(-0.029D, -0.011D, -0.087D), new Vec3(-0.029D, -0.048D, -0.087D), 52, 59, 64, 210);
        drawCordLine(lines, matrix, new Vec3(-0.029D, -0.011D, 0.087D), new Vec3(-0.029D, -0.048D, 0.087D), 52, 59, 64, 210);
        drawSmallBolt(lines, matrix, -0.015D, 0.005D, -0.036D);
        drawSmallBolt(lines, matrix, -0.015D, 0.005D, 0.036D);
    }

    private static void drawEndCapGrooveZ(VertexConsumer lines, Matrix4f matrix, double z, double normalDirection) {
        drawEllipticRingZ(
                lines,
                matrix,
                z,
                CubedFpvFiberSpoolGeometry.POD_CENTER_X,
                CubedFpvFiberSpoolGeometry.POD_CENTER_Y,
                CubedFpvFiberSpoolGeometry.SIDE_GROOVE_RADIUS_X,
                CubedFpvFiberSpoolGeometry.SIDE_GROOVE_RADIUS_Y,
                CubedFpvFiberSpoolGeometry.HOUSING_SEGMENTS,
                MODULE_GROOVE_COLOR,
                190
        );
        drawEllipticRingZ(
                lines,
                matrix,
                z + (0.001D * normalDirection),
                CubedFpvFiberSpoolGeometry.POD_CENTER_X,
                CubedFpvFiberSpoolGeometry.POD_CENTER_Y,
                CubedFpvFiberSpoolGeometry.SIDE_HUB_RADIUS_X,
                CubedFpvFiberSpoolGeometry.SIDE_HUB_RADIUS_Y,
                CubedFpvFiberSpoolGeometry.HOUSING_SEGMENTS,
                MODULE_FRAME_DETAIL_COLOR,
                220
        );
    }

    private static void drawSmallBolt(VertexConsumer lines, Matrix4f matrix, double x, double y, double z) {
        double halfSize = 0.003D;
        drawCordLine(lines, matrix, new Vec3(x - halfSize, y, z), new Vec3(x + halfSize, y, z), (MODULE_BOLT_COLOR >> 16) & 0xFF, (MODULE_BOLT_COLOR >> 8) & 0xFF, MODULE_BOLT_COLOR & 0xFF, 230);
        drawCordLine(lines, matrix, new Vec3(x, y, z - halfSize), new Vec3(x, y, z + halfSize), (MODULE_BOLT_COLOR >> 16) & 0xFF, (MODULE_BOLT_COLOR >> 8) & 0xFF, MODULE_BOLT_COLOR & 0xFF, 230);
    }

    private static void drawEllipticRingZ(VertexConsumer consumer, Matrix4f matrix, double z, double centerX, double centerY, double radiusX, double radiusY, int segments, int color, int alpha) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        Vec3 previous = cylinderPointZ(z, centerX, centerY, radiusX, radiusY, 0.0D);
        for (int segment = 1; segment <= segments; segment++) {
            double angle = (Math.PI * 2.0D * segment) / segments;
            Vec3 next = cylinderPointZ(z, centerX, centerY, radiusX, radiusY, angle);
            drawCordLine(consumer, matrix, previous, next, red, green, blue, alpha);
            previous = next;
        }
    }

    static void renderCord(PoseStack poseStack, MultiBufferSource buffer, Vec3 end) {
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        drawCordLine(lines, matrix, Vec3.ZERO, end, 34, 31, 25, 255);
    }

    private static void drawCordLine(VertexConsumer consumer, Matrix4f matrix, Vec3 from, Vec3 to, int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                .color(red, green, blue, alpha)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                .color(red, green, blue, alpha)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
