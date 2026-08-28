package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

final class CubedFpvReferenceGuiTextRenderer {
    static final int WHITE = 0xFFFFFFFF;
    static final int SOFT_WHITE = 0xFFE8E8E8;
    static final int MUTED_WHITE = 0xFFC8C8C8;
    static final int CAUTION = 0xFFFFC64A;
    static final int CRITICAL = 0xFFFF3A3A;
    static final int CONFIRMED = 0xFF9CFF9C;

    private static final int OUTLINE = 0xCC000000;

    private CubedFpvReferenceGuiTextRenderer() {
    }

    static void draw(GuiGraphics graphics, Font font, String text, float x, float y, float scale, int color, float tracking) {
        drawAligned(graphics, font, text, x, y, scale, color, tracking, 0.0F);
    }

    static void drawCentered(GuiGraphics graphics, Font font, String text, float centerX, float y, float scale, int color, float tracking) {
        drawAligned(graphics, font, text, centerX, y, scale, color, tracking, 0.5F);
    }

    static void drawRight(GuiGraphics graphics, Font font, String text, float right, float y, float scale, int color, float tracking) {
        drawAligned(graphics, font, text, right, y, scale, color, tracking, 1.0F);
    }

    static float width(Font font, String text, float scale, float tracking) {
        if (text == null || text.isEmpty()) {
            return 0.0F;
        }

        float raw = 0.0F;
        for (int index = 0; index < text.length(); index++) {
            raw += font.width(String.valueOf(text.charAt(index)));
            if (index < text.length() - 1) {
                raw += tracking;
            }
        }
        return raw * scale;
    }

    private static void drawAligned(GuiGraphics graphics, Font font, String text, float x, float y, float scale, int color, float tracking, float align) {
        if (text == null || text.isEmpty()) {
            return;
        }

        float alignedX = x - width(font, text, scale, tracking) * align;
        graphics.pose().pushPose();
        graphics.pose().translate(alignedX, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);

        float cursor = 0.0F;
        for (int index = 0; index < text.length(); index++) {
            String character = String.valueOf(text.charAt(index));
            drawOutlinedCharacter(graphics, font, character, Math.round(cursor), 0, color);
            cursor += font.width(character) + tracking;
        }

        graphics.pose().popPose();
    }

    private static void drawOutlinedCharacter(GuiGraphics graphics, Font font, String character, int x, int y, int color) {
        graphics.drawString(font, character, x - 1, y, OUTLINE, false);
        graphics.drawString(font, character, x + 1, y, OUTLINE, false);
        graphics.drawString(font, character, x, y - 1, OUTLINE, false);
        graphics.drawString(font, character, x, y + 1, OUTLINE, false);
        graphics.drawString(font, character, x, y, color, false);
    }
}
