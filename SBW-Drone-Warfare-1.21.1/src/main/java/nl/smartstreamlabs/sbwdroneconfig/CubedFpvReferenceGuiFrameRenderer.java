package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

final class CubedFpvReferenceGuiFrameRenderer {
    private static final int ARM_OUTLINE = 0xFF151719;
    private static final int ARM_MAIN = 0xFF2C2F33;
    private static final int ARM_SECONDARY = 0xFF35393D;
    private static final int ARM_BEVEL = 0xFF555B60;
    private static final int ARM_EDGE = 0xFF686E72;
    private static final int ARM_SHADOW = 0xFF202326;
    private static final int ARM_SEAM = 0xFF1D2023;
    private static final int NOSE_OUTLINE = 0xD111160C;
    private static final int NOSE_MAIN = 0xE05B6A36;
    private static final int NOSE_LEFT = 0xE0788848;
    private static final int NOSE_RIGHT = 0xE045522B;
    private static final int NOSE_RIDGE = 0xD02F3A22;

    private CubedFpvReferenceGuiFrameRenderer() {
    }

    static void render(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        renderLeftArm(graphics, layout);
        renderRightArm(graphics, layout);
        renderNose(graphics, layout);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderLeftArm(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout) {
        renderArm(graphics, layout, false);
    }

    private static void renderRightArm(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout) {
        renderArm(graphics, layout, true);
    }

    private static void renderArm(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout, boolean right) {
        drawPolygon(graphics, layout, ARM_OUTLINE, side(LEFT_ARM_BASE, right));
        drawPolygon(graphics, layout, ARM_SHADOW, side(LEFT_ARM_DARK_SIDE, right));
        drawPolygon(graphics, layout, ARM_MAIN, side(LEFT_ARM_MAIN_FACE, right));
        drawPolygon(graphics, layout, ARM_SECONDARY, side(LEFT_ARM_SECONDARY_FACE, right));
        drawPolygon(graphics, layout, ARM_BEVEL, side(LEFT_ARM_LOWER_BEVEL, right));
        drawPolygon(graphics, layout, ARM_EDGE, side(LEFT_ARM_LOWER_EDGE, right));
        drawPolygon(graphics, layout, ARM_SEAM, side(LEFT_ARM_PANEL_SEAM, right));
    }

    private static void renderNose(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout) {
        drawPolygon(graphics, layout, NOSE_OUTLINE, offset(NOSE_OUTER, 0.0F, 5.0F));
        drawPolygon(graphics, layout, NOSE_MAIN, NOSE_OUTER);
        drawPolygon(graphics, layout, NOSE_LEFT, NOSE_LEFT_TOP_FACE);
        drawPolygon(graphics, layout, NOSE_RIGHT, mirror(NOSE_LEFT_TOP_FACE));
        drawPolygon(graphics, layout, NOSE_RIDGE, NOSE_CENTER_RIDGE);
    }

    private static void drawPolygon(GuiGraphics graphics, CubedFpvReferenceGuiLayout.Canvas layout, int color, float[][] points) {
        if (points.length < 3) {
            return;
        }

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int index = 1; index < points.length - 1; index++) {
            vertex(buffer, matrix, layout, points[0][0], points[0][1], color);
            vertex(buffer, matrix, layout, points[index][0], points[index][1], color);
            vertex(buffer, matrix, layout, points[index + 1][0], points[index + 1][1], color);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, CubedFpvReferenceGuiLayout.Canvas layout, float x, float y, int color) {
        int alpha = color >>> 24 & 0xFF;
        int red = color >>> 16 & 0xFF;
        int green = color >>> 8 & 0xFF;
        int blue = color & 0xFF;
        buffer.addVertex(matrix, layout.x(x), layout.y(y), 0.0F).setColor(red, green, blue, alpha);
    }

    private static float[][] mirror(float[][] points) {
        float[][] mirrored = new float[points.length][2];
        for (int index = 0; index < points.length; index++) {
            mirrored[index][0] = CubedFpvReferenceGuiLayout.CANVAS_WIDTH - points[index][0];
            mirrored[index][1] = points[index][1];
        }
        return mirrored;
    }

    private static float[][] offset(float[][] points, float dx, float dy) {
        float[][] shifted = new float[points.length][2];
        for (int index = 0; index < points.length; index++) {
            shifted[index][0] = points[index][0] + dx;
            shifted[index][1] = points[index][1] + dy;
        }
        return shifted;
    }

    private static float[][] side(float[][] points, boolean right) {
        return right ? mirror(points) : points;
    }

    private static final float[][] LEFT_ARM_BASE = {
            {0.0F, 188.0F},
            {92.0F, 208.0F},
            {405.0F, 314.0F},
            {385.0F, 350.0F},
            {118.0F, 307.0F},
            {0.0F, 286.0F}
    };

    private static final float[][] LEFT_ARM_DARK_SIDE = {
            {0.0F, 188.0F},
            {92.0F, 208.0F},
            {405.0F, 314.0F},
            {399.0F, 321.0F},
            {89.0F, 219.0F},
            {0.0F, 201.0F}
    };

    private static final float[][] LEFT_ARM_MAIN_FACE = {
            {0.0F, 201.0F},
            {86.0F, 219.0F},
            {400.0F, 320.0F},
            {389.0F, 341.0F},
            {112.0F, 294.0F},
            {0.0F, 275.0F}
    };

    private static final float[][] LEFT_ARM_SECONDARY_FACE = {
            {0.0F, 205.0F},
            {82.0F, 222.0F},
            {396.0F, 322.0F},
            {391.0F, 331.0F},
            {108.0F, 282.0F},
            {0.0F, 263.0F}
    };

    private static final float[][] LEFT_ARM_LOWER_BEVEL = {
            {0.0F, 275.0F},
            {112.0F, 294.0F},
            {389.0F, 341.0F},
            {385.0F, 350.0F},
            {118.0F, 307.0F},
            {0.0F, 286.0F}
    };

    private static final float[][] LEFT_ARM_LOWER_EDGE = {
            {0.0F, 282.0F},
            {117.0F, 303.0F},
            {386.0F, 345.0F},
            {385.0F, 350.0F},
            {118.0F, 307.0F},
            {0.0F, 286.0F}
    };

    private static final float[][] LEFT_ARM_PANEL_SEAM = {
            {92.0F, 234.0F},
            {394.0F, 327.0F},
            {394.0F, 328.0F},
            {92.0F, 235.0F}
    };

    private static final float[][] NOSE_OUTER = {
            {390.0F, 607.0F},
            {452.0F, 505.0F},
            {516.0F, 458.0F},
            {540.0F, 445.0F},
            {566.0F, 458.0F},
            {634.0F, 505.0F},
            {704.0F, 607.0F}
    };

    private static final float[][] NOSE_LEFT_TOP_FACE = {
            {405.0F, 607.0F},
            {463.0F, 510.0F},
            {525.0F, 463.0F},
            {540.0F, 456.0F},
            {540.0F, 607.0F}
    };

    private static final float[][] NOSE_CENTER_RIDGE = {
            {531.0F, 456.0F},
            {549.0F, 456.0F},
            {560.0F, 607.0F},
            {520.0F, 607.0F}
    };
}
