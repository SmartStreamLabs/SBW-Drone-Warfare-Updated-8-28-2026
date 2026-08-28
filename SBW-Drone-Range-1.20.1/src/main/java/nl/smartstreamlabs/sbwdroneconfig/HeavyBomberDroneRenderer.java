package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class HeavyBomberDroneRenderer extends EntityRenderer<HeavyBomberDroneEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "textures/item/heavy_bomber_drone.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");
    private static final float BODY_TRANSLATION_X = 0.0F;
    private static final float BODY_TRANSLATION_Y = 0.24F;
    private static final float BODY_VISUAL_SCALE = 1.0F;
    private static final float MODEL_YAW_CORRECTION_DEGREES = 180.0F;
    private static final float ITEM_FIXED_SCALE = 0.72F;
    private static final float PROPELLER_Y = 1.36F;
    private static final float PROPELLER_INNER_RADIUS = 0.06F;
    private static final float PROPELLER_OUTER_RADIUS = 0.62F;
    private static final float PROPELLER_ROOT_HALF_WIDTH = 0.055F;
    private static final float PROPELLER_TIP_HALF_WIDTH = 0.032F;
    private static final float PROPELLER_SPIN_DEGREES_PER_TICK = 64.0F;
    private static final double[][] PROPELLER_CENTERS = {
            {0.949D, PROPELLER_Y, 1.604D},
            {1.919D, PROPELLER_Y, 0.0D},
            {0.949D, PROPELLER_Y, -1.604D},
            {-0.949D, PROPELLER_Y, -1.604D},
            {-1.919D, PROPELLER_Y, 0.0D},
            {-0.949D, PROPELLER_Y, 1.604D}
    };
    private static final float[] PROPELLER_DIRECTIONS = {1.0F, -1.0F, 1.0F, -1.0F, 1.0F, -1.0F};

    public HeavyBomberDroneRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.2F;
    }

    @Override
    public void render(HeavyBomberDroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        if (DroneClientViewContext.isLocalFpvViewOf(entity)) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        boolean firstPersonCamera = cameraType == CameraType.FIRST_PERSON;
        boolean controllingLinkedDrone = SbwCompat.isUsingLinkedMonitorForDrone(player, entity);
        boolean renderBody = !CubedFpvDroneRenderMath.shouldHideControlledDroneBody(controllingLinkedDrone, firstPersonCamera);

        if (renderBody) {
            poseStack.pushPose();
            applyBodyTransform(entity, partialTicks, poseStack);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    new ItemStack(AddonItems.HEAVY_BOMBER_DRONE.get()),
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );
            renderPropellers(entity, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HeavyBomberDroneEntity entity) {
        return TEXTURE;
    }

    private static void applyBodyTransform(HeavyBomberDroneEntity entity, float partialTicks, PoseStack poseStack) {
        poseStack.scale(BODY_VISUAL_SCALE, BODY_VISUAL_SCALE, BODY_VISUAL_SCALE);
        poseStack.translate(0.0D, BODY_TRANSLATION_Y, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderYawDegrees(entity, partialTicks)));
        poseStack.mulPose(Axis.XP.rotationDegrees(CubedFpvDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks))));
        poseStack.mulPose(Axis.ZP.rotationDegrees(CubedFpvDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks))));
        poseStack.translate(BODY_TRANSLATION_X, 0.0D, 0.0D);
    }

    private static float renderYawDegrees(HeavyBomberDroneEntity entity, float partialTicks) {
        return CubedFpvDroneRenderMath.renderYawDegrees(entity.getYaw(partialTicks)) + MODEL_YAW_CORRECTION_DEGREES;
    }

    private static void renderPropellers(HeavyBomberDroneEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(ITEM_FIXED_SCALE, ITEM_FIXED_SCALE, ITEM_FIXED_SCALE);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));
        float baseAngle = (entity.tickCount + partialTicks) * PROPELLER_SPIN_DEGREES_PER_TICK;

        for (int index = 0; index < PROPELLER_CENTERS.length; index++) {
            double[] center = PROPELLER_CENTERS[index];
            float angle = baseAngle * PROPELLER_DIRECTIONS[index];
            renderPropellerPair(consumer, matrix, center, angle, packedLight);
        }
        poseStack.popPose();
    }

    private static void renderPropellerPair(VertexConsumer consumer, Matrix4f matrix, double[] center, float angleDegrees, int packedLight) {
        renderPropellerBlade(consumer, matrix, center, angleDegrees, packedLight);
        renderPropellerBlade(consumer, matrix, center, angleDegrees + 180.0F, packedLight);
    }

    private static void renderPropellerBlade(VertexConsumer consumer, Matrix4f matrix, double[] center, float angleDegrees, int packedLight) {
        double angle = Math.toRadians(angleDegrees);
        double dirX = Math.cos(angle);
        double dirZ = Math.sin(angle);
        double sideX = -dirZ;
        double sideZ = dirX;
        double x = center[0];
        double y = center[1] + 0.012D;
        double z = center[2];

        Vec3 rootLeft = new Vec3(
                x + dirX * PROPELLER_INNER_RADIUS + sideX * PROPELLER_ROOT_HALF_WIDTH,
                y,
                z + dirZ * PROPELLER_INNER_RADIUS + sideZ * PROPELLER_ROOT_HALF_WIDTH
        );
        Vec3 rootRight = new Vec3(
                x + dirX * PROPELLER_INNER_RADIUS - sideX * PROPELLER_ROOT_HALF_WIDTH,
                y,
                z + dirZ * PROPELLER_INNER_RADIUS - sideZ * PROPELLER_ROOT_HALF_WIDTH
        );
        Vec3 tipRight = new Vec3(
                x + dirX * PROPELLER_OUTER_RADIUS - sideX * PROPELLER_TIP_HALF_WIDTH,
                y,
                z + dirZ * PROPELLER_OUTER_RADIUS - sideZ * PROPELLER_TIP_HALF_WIDTH
        );
        Vec3 tipLeft = new Vec3(
                x + dirX * PROPELLER_OUTER_RADIUS + sideX * PROPELLER_TIP_HALF_WIDTH,
                y,
                z + dirZ * PROPELLER_OUTER_RADIUS + sideZ * PROPELLER_TIP_HALF_WIDTH
        );

        putPropellerVertex(consumer, matrix, rootLeft, 245, packedLight);
        putPropellerVertex(consumer, matrix, rootRight, 245, packedLight);
        putPropellerVertex(consumer, matrix, tipRight, 245, packedLight);
        putPropellerVertex(consumer, matrix, tipLeft, 245, packedLight);
    }

    private static void putPropellerVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 point, int alpha, int packedLight) {
        consumer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .setColor(10, 12, 12, alpha)
                .setUv(0.5F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
