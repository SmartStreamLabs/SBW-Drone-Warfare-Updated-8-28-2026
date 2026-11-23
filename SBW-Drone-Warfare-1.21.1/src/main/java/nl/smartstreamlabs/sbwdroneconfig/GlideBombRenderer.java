package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GlideBombRenderer extends EntityRenderer<GlideBombEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "textures/item/glide_bomb.png");
    private static final float BODY_TRANSLATION_Y = 0.72F;
    private static final float BODY_VISUAL_SCALE = 1.0F;
    private static final float MODEL_YAW_CORRECTION_DEGREES = 180.0F;

    public GlideBombRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
    }

    @Override
    public void render(GlideBombEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        if (DroneClientViewContext.isLocalFpvViewOf(entity)) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        boolean firstPersonCamera = cameraType == CameraType.FIRST_PERSON;
        boolean controllingLinkedDrone = SbwCompat.isUsingLinkedMonitorForDrone(player, entity);
        boolean renderBody = !CubedFpvDroneRenderMath.shouldHideControlledDroneBody(controllingLinkedDrone, firstPersonCamera);

        if(entity.isLp03Fit() && entity.lp03RenderBlend(partialTicks)<1){
            if(renderBody){float blend=entity.lp03RenderBlend(partialTicks);var stack=new ItemStack(AddonItems.GLIDE_BOMB.get());float pitch=net.minecraft.util.Mth.lerp(partialTicks,entity.xRotO,entity.getXRot())-(float)GlidePlatformLayout.RAIL_PITCH;poseStack.pushPose();poseStack.translate(0,-entity.lp03VisualRise(partialTicks),0);if(blend<=0)GlidePlatformPayloadRenderer.render(stack,entity.level(),entity.getYaw(partialTicks),pitch,entity.getRoll(partialTicks),poseStack,buffer,packedLight);else GlidePlatformPayloadRenderer.renderBridge(stack,entity.level(),entity.getYaw(partialTicks),pitch,entity.getRoll(partialTicks),renderYawDegrees(entity,partialTicks),CubedFpvDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks)),CubedFpvDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks)),BODY_VISUAL_SCALE,BODY_TRANSLATION_Y,blend,poseStack,buffer,packedLight);poseStack.popPose();}
            super.render(entity,entityYaw,partialTicks,poseStack,buffer,packedLight);return;
        }

        if (renderBody) {
            poseStack.pushPose();
            poseStack.scale(BODY_VISUAL_SCALE, BODY_VISUAL_SCALE, BODY_VISUAL_SCALE);
            poseStack.translate(0.0D, BODY_TRANSLATION_Y, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(renderYawDegrees(entity, partialTicks)));
            poseStack.mulPose(Axis.XP.rotationDegrees(CubedFpvDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks))));
            poseStack.mulPose(Axis.ZP.rotationDegrees(CubedFpvDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks))));
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    new ItemStack(AddonItems.GLIDE_BOMB.get()),
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

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GlideBombEntity entity) {
        return TEXTURE;
    }

    private static float renderYawDegrees(GlideBombEntity entity, float partialTicks) {
        return CubedFpvDroneRenderMath.renderYawDegrees(entity.getYaw(partialTicks)) + MODEL_YAW_CORRECTION_DEGREES;
    }
}
