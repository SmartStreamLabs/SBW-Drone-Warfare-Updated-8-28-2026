package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_DATA;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY_TAG;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.MAX_AMMO;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.AMMO;

public class LucasDroneRenderer extends GeoEntityRenderer<LucasDroneEntity> {
    private String entityNameCache = "";
    private Entity entityCache = null;
    private int attachedTick = Integer.MAX_VALUE;
    private int lastPayloadMountDebugTick = Integer.MIN_VALUE;

    public LucasDroneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LucasDroneModel());
        this.shadowRadius = 1.2F;
    }

    @Override
    public RenderType getRenderType(LucasDroneEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    protected void renderNameTag(LucasDroneEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, LucasDroneRenderMath.nameplateOffsetY(), 0.0D);
        super.renderNameTag(entity, displayName, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public void render(LucasDroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        if (DroneClientViewContext.isReadOnlyCameraIntercept(entity)) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        boolean firstPersonCamera = cameraType == CameraType.FIRST_PERSON;
        boolean suppressAttachmentsForCamera = firstPersonCamera || cameraType == CameraType.THIRD_PERSON_BACK;
        boolean controllingLinkedDrone = false;

        controllingLinkedDrone = SbwCompat.isUsingLinkedMonitorForDrone(player, entity);

        poseStack.pushPose();
        float droneVisualScale = LucasDroneRenderMath.droneVisualScale(controllingLinkedDrone, firstPersonCamera);
        poseStack.scale(droneVisualScale, droneVisualScale, droneVisualScale);
        poseStack.translate(0.0D, LucasDroneRenderMath.bodyTranslationY(), 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(LucasDroneRenderMath.renderYawDegrees(entity.getYaw(partialTicks))));
        poseStack.mulPose(Axis.XP.rotationDegrees(LucasDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks))));
        poseStack.mulPose(Axis.ZP.rotationDegrees(LucasDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks))));

        if (!LucasDroneRenderMath.shouldHideControlledDroneBody(controllingLinkedDrone, firstPersonCamera)) {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }

        poseStack.popPose();

        if (!controllingLinkedDrone || !suppressAttachmentsForCamera) {
            poseStack.pushPose();
            poseStack.translate(0.0D, LucasDroneRenderMath.attachmentBaseYOffset(), 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(LucasDroneRenderMath.renderAttachmentYawDegrees(entity.getYaw(partialTicks))));
            poseStack.mulPose(Axis.XP.rotationDegrees(LucasDroneRenderMath.renderAttachmentPitchDegrees(entity.getBodyPitch(partialTicks))));
            poseStack.mulPose(Axis.ZP.rotationDegrees(LucasDroneRenderMath.renderAttachmentRollDegrees(entity.getRoll(partialTicks))));
            renderAttachments(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    private void renderAttachments(LucasDroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var data = entity.getEntityData();
        var attached = data.get(DISPLAY_ENTITY);
        if (attached.isEmpty()) {
            return;
        }

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
        renderEntity.tickCount = displayData.get(11) >= 0 ? displayData.get(11).intValue() : entity.tickCount - attachedTick;

        var scale = new float[]{displayData.get(0), displayData.get(1), displayData.get(2)};
        var offset = new float[]{displayData.get(3), displayData.get(4), displayData.get(5)};
        var rotation = new float[]{displayData.get(6), displayData.get(7), displayData.get(8)};
        PayloadMountTransform mount = DronePayloadMounts.getPayloadMountTransform(
                DronePayloadMounts.DroneKind.LUCAS,
                attached,
                offset,
                rotation,
                scale
        );
        var xLength = displayData.get(9);
        var zLength = displayData.get(10);

        for (int index = 0; index < data.get(AMMO); index++) {
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
                    LucasDroneRenderMath.compensateAttachmentCoordinate(x + mount.offsetX()),
                    LucasDroneRenderMath.compensateAttachmentCoordinate(mount.offsetY()),
                    LucasDroneRenderMath.compensateAttachmentCoordinate(z + mount.offsetZ())
            );
            if (AddonConfig.debugPayloadMounts()) {
                PayloadMountDebugRenderer.renderMarker(poseStack, buffer);
                maybeLogPayloadMount(entity, attached, mount, partialTicks);
            }
            float attachmentScaleCompensation = LucasDroneRenderMath.attachmentScaleCompensation();
            poseStack.scale(attachmentScaleCompensation, attachmentScaleCompensation, attachmentScaleCompensation);
            poseStack.scale(mount.scaleX(), mount.scaleY(), mount.scaleZ());
            poseStack.mulPose(Axis.YP.rotationDegrees(mount.rotationYaw()));
            poseStack.mulPose(Axis.XP.rotationDegrees(mount.rotationPitch()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(mount.rotationRoll()));

            float payloadYaw = mount.useLocalRenderYaw() ? 0.0F : entityYaw;
            entityRenderDispatcher.render(renderEntity, 0, 0, 0, payloadYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    private void maybeLogPayloadMount(LucasDroneEntity entity, String payloadType, PayloadMountTransform mount, float partialTicks) {
        if (entity.tickCount == lastPayloadMountDebugTick || entity.tickCount % 20 != 0) {
            return;
        }

        lastPayloadMountDebugTick = entity.tickCount;
        SbwDroneRangeConfig.LOGGER.info(
                "LUCAS payload mount drone={} payload={} yaw={} pitch={} roll={} localOffset=[{}, {}, {}] payloadRotation=[pitch={}, roll={}, yaw={}] payloadScale=[{}, {}, {}]",
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
}
