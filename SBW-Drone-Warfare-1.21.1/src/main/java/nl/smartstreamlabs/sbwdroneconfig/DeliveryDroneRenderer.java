package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DeliveryDroneRenderer extends GeoEntityRenderer<DeliveryDroneEntity> {
    private static final Vec3 CRATE_CORD_END_OFFSET = new Vec3(0.0D, -0.42D, 0.0D);
    private static final float BODY_VISUAL_SCALE = 1.0F;
    private static final float BODY_TRANSLATION_Y = 0.15F;
    private static final float CRATE_SCALE = 0.62F;
    private static final float MODEL_YAW_CORRECTION_DEGREES = 180.0F;

    public DeliveryDroneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DeliveryDroneModel());
        this.shadowRadius = 0.55F;
    }

    @Override
    public RenderType getRenderType(DeliveryDroneEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void render(DeliveryDroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        if (DroneClientViewContext.isLocalFpvViewOf(entity)) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        boolean firstPersonCamera = cameraType == CameraType.FIRST_PERSON;
        boolean suppressCargoForCamera = firstPersonCamera || cameraType == CameraType.THIRD_PERSON_BACK;
        boolean controllingLinkedDrone = SbwCompat.isUsingLinkedMonitorForDrone(player, entity);

        poseStack.pushPose();
        poseStack.scale(BODY_VISUAL_SCALE, BODY_VISUAL_SCALE, BODY_VISUAL_SCALE);
        poseStack.translate(0.0D, BODY_TRANSLATION_Y, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderYawDegrees(entity, partialTicks)));
        poseStack.mulPose(Axis.XP.rotationDegrees(CubedFpvDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks))));
        poseStack.mulPose(Axis.ZP.rotationDegrees(CubedFpvDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks))));
        boolean renderBody = !CubedFpvDroneRenderMath.shouldHideControlledDroneBody(controllingLinkedDrone, firstPersonCamera);
        if (renderBody) {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
        poseStack.popPose();

        if (entity.hasMountedExtractionCrate() && (!controllingLinkedDrone || !suppressCargoForCamera)) {
            poseStack.pushPose();
            applyCargoBaseTransform(entity, partialTicks, poseStack);
            renderMountedCrate(entity, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    private static void applyCargoBaseTransform(DeliveryDroneEntity entity, float partialTicks, PoseStack poseStack) {
        poseStack.translate(0.0D, BODY_TRANSLATION_Y, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderYawDegrees(entity, partialTicks)));
        poseStack.mulPose(Axis.XP.rotationDegrees(CubedFpvDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks))));
        poseStack.mulPose(Axis.ZP.rotationDegrees(CubedFpvDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks))));
    }

    private static float renderYawDegrees(DeliveryDroneEntity entity, float partialTicks) {
        return CubedFpvDroneRenderMath.renderYawDegrees(entity.getYaw(partialTicks)) + MODEL_YAW_CORRECTION_DEGREES;
    }

    private void renderMountedCrate(DeliveryDroneEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.12D, 0.0D);
        CubedFpvDroneRenderer.renderCord(poseStack, buffer, CRATE_CORD_END_OFFSET);
        poseStack.translate(CRATE_CORD_END_OFFSET.x, CRATE_CORD_END_OFFSET.y - 0.12D, CRATE_CORD_END_OFFSET.z);
        poseStack.scale(CRATE_SCALE, CRATE_SCALE, CRATE_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(
                new ItemStack(AddonItems.EXTRACTION_CRATE.get()),
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();
    }
}
