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

public class Geranium2Renderer extends EntityRenderer<Geranium2Entity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "textures/item/geranium_2.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");
    private static final float BODY_TRANSLATION_Y = 0.76F;
    private static final float BODY_TRANSLATION_Z = -0.25F;
    private static final float BODY_VISUAL_SCALE = 1.15F;
    private static final float MODEL_YAW_CORRECTION_DEGREES = 0.0F;
    private static final float ITEM_FIXED_SCALE = 1.1F;
    private static final double[] TAIL_PROPELLER_CENTER = {0.0D, 0.035D, 2.015D};
    private static final float TAIL_PROPELLER_INNER_RADIUS = 0.045F;
    private static final float TAIL_PROPELLER_OUTER_RADIUS = 0.34F;
    private static final float TAIL_PROPELLER_ROOT_HALF_WIDTH = 0.028F;
    private static final float TAIL_PROPELLER_TIP_HALF_WIDTH = 0.018F;
    private static final float TAIL_PROPELLER_SPIN_DEGREES_PER_TICK = 82.0F;

    public Geranium2Renderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.9F;
    }

    @Override
    public void render(Geranium2Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
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
            poseStack.scale(BODY_VISUAL_SCALE, BODY_VISUAL_SCALE, BODY_VISUAL_SCALE);
            poseStack.translate(0.0D, BODY_TRANSLATION_Y, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(renderYawDegrees(entity, partialTicks)));
            poseStack.mulPose(Axis.XP.rotationDegrees(CubedFpvDroneRenderMath.renderBodyPitchDegrees(entity.getBodyPitch(partialTicks))));
            poseStack.mulPose(Axis.ZP.rotationDegrees(CubedFpvDroneRenderMath.renderRollDegrees(entity.getRoll(partialTicks))));
            poseStack.translate(0.0D, 0.0D, BODY_TRANSLATION_Z);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    new ItemStack(AddonItems.GERANIUM_2.get()),
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );
            renderTailPropeller(entity, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Geranium2Entity entity) {
        return TEXTURE;
    }

    private static float renderYawDegrees(Geranium2Entity entity, float partialTicks) {
        return CubedFpvDroneRenderMath.renderYawDegrees(entity.getYaw(partialTicks)) + MODEL_YAW_CORRECTION_DEGREES;
    }

    private static void renderTailPropeller(Geranium2Entity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(ITEM_FIXED_SCALE, ITEM_FIXED_SCALE, ITEM_FIXED_SCALE);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));
        float baseAngle = (entity.tickCount + partialTicks) * TAIL_PROPELLER_SPIN_DEGREES_PER_TICK;

        renderTailPropellerBlade(consumer, matrix, TAIL_PROPELLER_CENTER, baseAngle, packedLight);
        renderTailPropellerBlade(consumer, matrix, TAIL_PROPELLER_CENTER, baseAngle + 180.0F, packedLight);
        poseStack.popPose();
    }

    private static void renderTailPropellerBlade(VertexConsumer consumer, Matrix4f matrix, double[] center, float angleDegrees, int packedLight) {
        double angle = Math.toRadians(angleDegrees);
        double dirX = Math.cos(angle);
        double dirY = Math.sin(angle);
        double sideX = -dirY;
        double sideY = dirX;
        double x = center[0];
        double y = center[1];
        double z = center[2];

        Vec3 rootLeft = new Vec3(
                x + dirX * TAIL_PROPELLER_INNER_RADIUS + sideX * TAIL_PROPELLER_ROOT_HALF_WIDTH,
                y + dirY * TAIL_PROPELLER_INNER_RADIUS + sideY * TAIL_PROPELLER_ROOT_HALF_WIDTH,
                z
        );
        Vec3 rootRight = new Vec3(
                x + dirX * TAIL_PROPELLER_INNER_RADIUS - sideX * TAIL_PROPELLER_ROOT_HALF_WIDTH,
                y + dirY * TAIL_PROPELLER_INNER_RADIUS - sideY * TAIL_PROPELLER_ROOT_HALF_WIDTH,
                z
        );
        Vec3 tipRight = new Vec3(
                x + dirX * TAIL_PROPELLER_OUTER_RADIUS - sideX * TAIL_PROPELLER_TIP_HALF_WIDTH,
                y + dirY * TAIL_PROPELLER_OUTER_RADIUS - sideY * TAIL_PROPELLER_TIP_HALF_WIDTH,
                z
        );
        Vec3 tipLeft = new Vec3(
                x + dirX * TAIL_PROPELLER_OUTER_RADIUS + sideX * TAIL_PROPELLER_TIP_HALF_WIDTH,
                y + dirY * TAIL_PROPELLER_OUTER_RADIUS + sideY * TAIL_PROPELLER_TIP_HALF_WIDTH,
                z
        );

        putTailPropellerVertex(consumer, matrix, rootLeft, packedLight);
        putTailPropellerVertex(consumer, matrix, rootRight, packedLight);
        putTailPropellerVertex(consumer, matrix, tipRight, packedLight);
        putTailPropellerVertex(consumer, matrix, tipLeft, packedLight);
    }

    private static void putTailPropellerVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 point, int packedLight) {
        consumer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .setColor(10, 12, 12, 230)
                .setUv(0.5F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}
