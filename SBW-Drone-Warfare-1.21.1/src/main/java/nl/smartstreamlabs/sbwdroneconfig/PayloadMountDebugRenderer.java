package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

final class PayloadMountDebugRenderer {
    private static final float MARKER_SIZE = 0.18F;
    private static final int ALPHA = 255;

    private PayloadMountDebugRenderer() {
    }

    static void renderMarker(PoseStack poseStack, MultiBufferSource buffer) {
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        drawLine(lines, matrix, new Vec3(-MARKER_SIZE, 0.0D, 0.0D), new Vec3(MARKER_SIZE, 0.0D, 0.0D), 255, 48, 48);
        drawLine(lines, matrix, new Vec3(0.0D, -MARKER_SIZE, 0.0D), new Vec3(0.0D, MARKER_SIZE, 0.0D), 48, 255, 48);
        drawLine(lines, matrix, new Vec3(0.0D, 0.0D, -MARKER_SIZE), new Vec3(0.0D, 0.0D, MARKER_SIZE), 64, 128, 255);
    }

    private static void drawLine(VertexConsumer consumer, Matrix4f matrix, Vec3 from, Vec3 to, int red, int green, int blue) {
        consumer.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                .color(red, green, blue, ALPHA)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                .color(red, green, blue, ALPHA)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
