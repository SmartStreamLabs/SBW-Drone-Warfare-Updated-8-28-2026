package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.gui.GuiGraphics;

final class CubedFpvReferenceGuiLayout {
    static final float CANVAS_WIDTH = 1080.0F;
    static final float CANVAS_HEIGHT = 607.0F;
    static final float CENTER_X = CANVAS_WIDTH * 0.5F;
    static final float CENTER_Y = CANVAS_HEIGHT * 0.5F;

    private CubedFpvReferenceGuiLayout() {
    }

    static Canvas fitToScreen(GuiGraphics graphics) {
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        float sx = screenWidth / CANVAS_WIDTH;
        float sy = screenHeight / CANVAS_HEIGHT;
        return new Canvas(sx, sy, Math.min(sx, sy));
    }

    record Canvas(float sx, float sy, float textScale) {
        float x(float value) {
            return value * sx;
        }

        float y(float value) {
            return value * sy;
        }

        float size(float value) {
            return value * textScale;
        }

        int ix(float value) {
            return Math.round(x(value));
        }

        int iy(float value) {
            return Math.round(y(value));
        }

        int is(float value) {
            return Math.max(1, Math.round(size(value)));
        }
    }
}
