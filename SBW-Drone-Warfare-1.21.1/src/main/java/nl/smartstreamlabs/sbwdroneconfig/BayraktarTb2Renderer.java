package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BayraktarTb2Renderer extends EntityRenderer<BayraktarTb2Entity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "textures/item/ukrainian_bayraktar_tb2.png");
    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("minecraft", "textures/misc/white.png");
    private static final float BODY_TRANSLATION_Y = 0.22F;
    private static final float BODY_VISUAL_SCALE = 1.45F;
    private static final float MODEL_YAW_CORRECTION_DEGREES = 180.0F;
    private static final float ITEM_FIXED_SCALE = 1.0F;
    private static final double[] PUSHER_PROPELLER_CENTER = {0.0D, 1.142D, -1.156D};
    private static final float PUSHER_PROPELLER_INNER_RADIUS = 0.13F;
    private static final float PUSHER_PROPELLER_OUTER_RADIUS = 0.88F;
    private static final float PUSHER_PROPELLER_ROOT_HALF_WIDTH = 0.035F;
    private static final float PUSHER_PROPELLER_TIP_HALF_WIDTH = 0.085F;
    private static final float PUSHER_PROPELLER_SPIN_DEGREES_PER_TICK = 105.0F;

    public BayraktarTb2Renderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 6.0F;
    }

    @Override
    public void render(BayraktarTb2Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        if (DroneClientViewContext.isReadOnlyCameraIntercept(entity)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        boolean controllingLinkedDrone = SbwCompat.isUsingLinkedMonitorForDrone(player, entity);
        boolean activeCameraDrone = DroneClientViewContext.activeDroneEntity(minecraft) == entity;
        boolean renderBody = !controllingLinkedDrone && !activeCameraDrone;

        if (renderBody) {
            poseStack.pushPose();
            poseStack.scale(BODY_VISUAL_SCALE, BODY_VISUAL_SCALE, BODY_VISUAL_SCALE);
            poseStack.translate(0.0D, BODY_TRANSLATION_Y, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(renderYawDegrees(entity, partialTicks)));
            poseStack.mulPose(Axis.XP.rotationDegrees(LucasDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks))));
            poseStack.mulPose(Axis.ZP.rotationDegrees(LucasDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks))));
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    new ItemStack(AddonItems.UKRAINIAN_BAYRAKTAR_TB2.get()),
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );
            renderPusherPropeller(entity, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BayraktarTb2Entity entity) {
        return TEXTURE;
    }

    private static float renderYawDegrees(BayraktarTb2Entity entity, float partialTicks) {
        return LucasDroneRenderMath.renderYawDegrees(entity.getYaw(partialTicks)) + MODEL_YAW_CORRECTION_DEGREES;
    }

    private static void renderPusherPropeller(BayraktarTb2Entity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(ITEM_FIXED_SCALE, ITEM_FIXED_SCALE, ITEM_FIXED_SCALE);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE_TEXTURE));
        float baseAngle = (entity.tickCount + partialTicks) * PUSHER_PROPELLER_SPIN_DEGREES_PER_TICK;

        renderPusherPropellerBlade(consumer, matrix, normal, PUSHER_PROPELLER_CENTER, baseAngle, packedLight);
        renderPusherPropellerBlade(consumer, matrix, normal, PUSHER_PROPELLER_CENTER, baseAngle + 120.0F, packedLight);
        renderPusherPropellerBlade(consumer, matrix, normal, PUSHER_PROPELLER_CENTER, baseAngle + 240.0F, packedLight);
        poseStack.popPose();
    }

    private static void renderPusherPropellerBlade(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, double[] center, float angleDegrees, int packedLight) {
        double angle = Math.toRadians(angleDegrees);
        double dirX = Math.cos(angle);
        double dirY = Math.sin(angle);
        double sideX = -dirY;
        double sideY = dirX;
        double x = center[0];
        double y = center[1];
        double z = center[2];

        putPusherPropellerVertex(
                consumer, matrix, normal,
                x + dirX * PUSHER_PROPELLER_INNER_RADIUS + sideX * PUSHER_PROPELLER_ROOT_HALF_WIDTH,
                y + dirY * PUSHER_PROPELLER_INNER_RADIUS + sideY * PUSHER_PROPELLER_ROOT_HALF_WIDTH,
                z,
                packedLight
        );
        putPusherPropellerVertex(
                consumer, matrix, normal,
                x + dirX * PUSHER_PROPELLER_INNER_RADIUS - sideX * PUSHER_PROPELLER_ROOT_HALF_WIDTH,
                y + dirY * PUSHER_PROPELLER_INNER_RADIUS - sideY * PUSHER_PROPELLER_ROOT_HALF_WIDTH,
                z,
                packedLight
        );
        putPusherPropellerVertex(
                consumer, matrix, normal,
                x + dirX * PUSHER_PROPELLER_OUTER_RADIUS - sideX * PUSHER_PROPELLER_TIP_HALF_WIDTH,
                y + dirY * PUSHER_PROPELLER_OUTER_RADIUS - sideY * PUSHER_PROPELLER_TIP_HALF_WIDTH,
                z,
                packedLight
        );
        putPusherPropellerVertex(
                consumer, matrix, normal,
                x + dirX * PUSHER_PROPELLER_OUTER_RADIUS + sideX * PUSHER_PROPELLER_TIP_HALF_WIDTH,
                y + dirY * PUSHER_PROPELLER_OUTER_RADIUS + sideY * PUSHER_PROPELLER_TIP_HALF_WIDTH,
                z,
                packedLight
        );
    }

    private static void putPusherPropellerVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, double x, double y, double z, int packedLight) {
        consumer.vertex(matrix, (float) x, (float) y, (float) z)
                .color(8, 9, 11, 255)
                .uv(0.5F, 0.5F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
