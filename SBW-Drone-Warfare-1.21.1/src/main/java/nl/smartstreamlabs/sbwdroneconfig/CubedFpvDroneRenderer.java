package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
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
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_DATA;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY_TAG;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.MAX_AMMO;

public class CubedFpvDroneRenderer extends CachedEntityRenderer<CubedFpvDroneEntity> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");
    private static final int MODULE_HOUSING_COLOR = 0x141A1D;
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
    private int lastPayloadMountDebugTick = Integer.MIN_VALUE;

    public CubedFpvDroneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CubedFpvDroneModel());
        this.shadowRadius = 0.2F;
    }

    @Override
    public RenderType getRenderType(CubedFpvDroneEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void render(CubedFpvDroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {poseStack.pushPose();try{Dc01DockRender.apply(entity,partialTicks,poseStack);renderDrone(entity,entityYaw,partialTicks,poseStack,buffer,packedLight);}finally{poseStack.popPose();}}
    
    @Override public boolean shouldRender(CubedFpvDroneEntity drone,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z){var c=Dc01Docking.carrier(drone);return c==null?super.shouldRender(drone,frustum,x,y,z):drone.isAlive()&&frustum.isVisible(c.getBoundingBoxForCulling());}
    private void renderDrone(CubedFpvDroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
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
        if(renderBody)Hg01PayloadRenderer.fpv(entity,partialTicks,poseStack,buffer,packedLight);

        if (!controllingLinkedDrone || !suppressAttachmentsForCamera) {
            poseStack.pushPose();
            poseStack.translate(0.0D, CubedFpvDroneRenderMath.attachmentBaseYOffset(), 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(CubedFpvDroneRenderMath.renderAttachmentYawDegrees(entity.getYaw(partialTicks))));
            poseStack.mulPose(Axis.XP.rotationDegrees(CubedFpvDroneRenderMath.renderAttachmentPitchDegrees(entity.getBodyPitch(partialTicks))));
            poseStack.mulPose(Axis.ZP.rotationDegrees(CubedFpvDroneRenderMath.renderAttachmentRollDegrees(entity.getRoll(partialTicks))));
            renderAttachments(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    static boolean hasMountedPayload(CubedFpvDroneEntity entity) {
        var data = entity.getEntityData();
        return !data.get(DISPLAY_ENTITY).isEmpty()
                && SbwCompat.getDroneAmmo(entity) > 0
                && data.get(DISPLAY_DATA).size() >= 12;
    }

    static boolean hasFiberSpoolModule(CubedFpvDroneEntity entity) {
        return DroneFiberOpticClient.hasFiberSpoolModule(entity);
    }

    static Vec3 fiberCableExitWorldPosition(CubedFpvDroneEntity entity, float partialTicks) {
        return CubedFpvFiberSpoolGeometry.cableExitWorldPosition(entity, partialTicks);
    }

    private void renderAttachments(CubedFpvDroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if(Hg01Payload.is(entity.hgVisual()))return;
        var data = entity.getEntityData();
        var attached = data.get(DISPLAY_ENTITY);
        if (attached.isEmpty()) {
            return;
        }

        var snapshot=data.get(DISPLAY_DATA);if(snapshot.size()<12||snapshot.stream().anyMatch(v->v==null||!Float.isFinite(v)))return;
        boolean nativeRgo=attached.equals("superbwarfare:rgo_grenade");
        Entity renderEntity;
        if(nativeRgo){renderEntity=null;}else if (entityNameCache.equals(attached) && entityCache != null && entityCache.level()==entity.level()) {
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
                renderEntity.load(tag.copy());
            }

            entityNameCache = attached;
            entityCache = renderEntity;
            attachedTick = entity.tickCount;
        }

        var displayData = data.get(DISPLAY_DATA);
        if(renderEntity!=null)renderEntity.tickCount = displayData.get(11) >= 0 ? displayData.get(11).intValue() : entity.tickCount - attachedTick;

        var scale = new float[]{displayData.get(0), displayData.get(1), displayData.get(2)};
        var offset = new float[]{displayData.get(3), displayData.get(4), displayData.get(5)};
        var rotation = new float[]{displayData.get(6), displayData.get(7), displayData.get(8)};
        PayloadMountTransform mount = DronePayloadMounts.getPayloadMountTransform(
                DronePayloadMounts.DroneKind.FPV,
                attached,
                offset,
                rotation,
                scale
        );
        var xLength = displayData.get(9);
        var zLength = displayData.get(10);
        int ammoCount = SbwCompat.getDroneAmmo(entity);

        for (int index = 0; index < ammoCount; index++) {
            float x;
            float z;
            if (data.get(MAX_AMMO) == 1) {
                x = 0;
                z = 0;
            } else {
                x = xLength / 2 * (index % 2 == 0 ? 1 : -1);
                var rows = data.get(MAX_AMMO) / 2;
                var row = index / 2;
                if (rows < 2) {
                    z = 0;
                } else {
                    var rowLength = zLength / rows;
                    z = -zLength / 2 + rowLength * row;
                }
            }

            poseStack.pushPose();
            poseStack.translate(
                    CubedFpvDroneRenderMath.compensateAttachmentCoordinate(x + mount.offsetX()),
                    CubedFpvDroneRenderMath.compensateAttachmentCoordinate(mount.offsetY()),
                    CubedFpvDroneRenderMath.compensateAttachmentCoordinate(z + mount.offsetZ())
            );
            if (AddonConfig.debugPayloadMounts()) {
                PayloadMountDebugRenderer.renderMarker(poseStack, buffer);
                maybeLogPayloadMount(entity, attached, mount, partialTicks);
            }
            float attachmentScaleCompensation = CubedFpvDroneRenderMath.attachmentScaleCompensation();
            poseStack.scale(attachmentScaleCompensation, attachmentScaleCompensation, attachmentScaleCompensation);
            poseStack.scale(mount.scaleX(), mount.scaleY(), mount.scaleZ());
            poseStack.mulPose(Axis.YP.rotationDegrees(mount.rotationYaw()));
            poseStack.mulPose(Axis.XP.rotationDegrees(mount.rotationPitch()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(mount.rotationRoll()));

            float payloadYaw = mount.useLocalRenderYaw() ? 0.0F : entityYaw;
            if(nativeRgo){var grenade=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(attached)));Minecraft.getInstance().getItemRenderer().renderStatic(grenade,net.minecraft.world.item.ItemDisplayContext.NONE,packedLight,OverlayTexture.NO_OVERLAY,poseStack,buffer,entity.level(),entity.getId());}else entityRenderDispatcher.render(renderEntity, 0, 0, 0, payloadYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    private void maybeLogPayloadMount(CubedFpvDroneEntity entity, String payloadType, PayloadMountTransform mount, float partialTicks) {
        if (entity.tickCount == lastPayloadMountDebugTick || entity.tickCount % 20 != 0) {
            return;
        }

        lastPayloadMountDebugTick = entity.tickCount;
        SbwDroneRangeConfig.LOGGER.info(
                "FPV payload mount drone={} payload={} yaw={} pitch={} roll={} localOffset=[{}, {}, {}] payloadRotation=[pitch={}, roll={}, yaw={}] payloadScale=[{}, {}, {}]",
                entity.getUUID(),
                payloadType,
                entity.getYaw(partialTicks),
                entity.getBodyPitch(partialTicks),
                entity.getRoll(partialTicks),
                mount.offsetX(),
                mount.offsetY(),
                mount.offsetZ(),
                mount.rotationPitch(),
                mount.rotationRoll(),
                mount.rotationYaw(),
                mount.scaleX(),
                mount.scaleY(),
                mount.scaleZ()
        );
    }

    private static void renderFiberSpoolModule(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        FiberSpoolHousingModel.render(poseStack, buffer, packedLight);
    }

    private static void renderOctagonalModuleBodyZ(
            VertexConsumer consumer,
            Matrix4f matrix,
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

            putQuad(consumer, matrix, left0, right0, right1, left1, red, green, blue, alpha, packedLight, normalX, normalY, 0.0F);
            putQuad(consumer, matrix, leftCenter, left1, left0, leftCenter, red, green, blue, alpha, packedLight, 0.0F, 0.0F, -1.0F);
            putQuad(consumer, matrix, rightCenter, right0, right1, rightCenter, red, green, blue, alpha, packedLight, 0.0F, 0.0F, 1.0F);
        }
    }

    private static void renderOctagonalDiskZ(
            VertexConsumer consumer,
            Matrix4f matrix,
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
            putQuad(consumer, matrix, center, point0, point1, center, red, green, blue, alpha, packedLight, 0.0F, 0.0F, normalZ);
        }
    }

    private static Vec3 cylinderPointZ(double z, double centerX, double centerY, double radiusX, double radiusY, double angle) {
        return new Vec3(
                centerX + Math.cos(angle) * radiusX,
                centerY + Math.sin(angle) * radiusY,
                z
        );
    }

    private static void renderBox(VertexConsumer consumer, Matrix4f matrix, Vec3 min, Vec3 max, int color, int alpha, int packedLight) {
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

        putQuad(consumer, matrix, x0y0z0, x1y0z0, x1y1z0, x0y1z0, red, green, blue, alpha, packedLight, 0.0F, 0.0F, -1.0F);
        putQuad(consumer, matrix, x1y0z1, x0y0z1, x0y1z1, x1y1z1, red, green, blue, alpha, packedLight, 0.0F, 0.0F, 1.0F);
        putQuad(consumer, matrix, x0y0z1, x0y0z0, x0y1z0, x0y1z1, red, green, blue, alpha, packedLight, -1.0F, 0.0F, 0.0F);
        putQuad(consumer, matrix, x1y0z0, x1y0z1, x1y1z1, x1y1z0, red, green, blue, alpha, packedLight, 1.0F, 0.0F, 0.0F);
        putQuad(consumer, matrix, x0y1z0, x1y1z0, x1y1z1, x0y1z1, red, green, blue, alpha, packedLight, 0.0F, 1.0F, 0.0F);
        putQuad(consumer, matrix, x0y0z1, x1y0z1, x1y0z0, x0y0z0, red, green, blue, alpha, packedLight, 0.0F, -1.0F, 0.0F);
    }

    private static void putQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
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
        putSolidVertex(consumer, matrix, a, red, green, blue, alpha, packedLight, normalX, normalY, normalZ);
        putSolidVertex(consumer, matrix, b, red, green, blue, alpha, packedLight, normalX, normalY, normalZ);
        putSolidVertex(consumer, matrix, c, red, green, blue, alpha, packedLight, normalX, normalY, normalZ);
        putSolidVertex(consumer, matrix, d, red, green, blue, alpha, packedLight, normalX, normalY, normalZ);
    }

    private static void putSolidVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 point, int red, int green, int blue, int alpha, int packedLight, float normalX, float normalY, float normalZ) {
        consumer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .setColor(red, green, blue, alpha)
                .setUv(0.5F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(normalX, normalY, normalZ);
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
        consumer.addVertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                .setColor(red, green, blue, alpha)
                .setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                .setColor(red, green, blue, alpha)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
