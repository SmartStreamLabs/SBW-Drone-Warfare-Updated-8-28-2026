package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FuelMixerScreen extends AbstractContainerScreen<FuelMixerMenu> {
    private static final int PROGRESS_X = 74;
    private static final int PROGRESS_Y = 52;
    private static final int PROGRESS_WIDTH = 42;
    private static final int PROGRESS_HEIGHT = 8;

    public FuelMixerScreen(FuelMixerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 180;
        this.inventoryLabelY = 86;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, 0xE0101010);
        guiGraphics.fill(left + 4, top + 4, left + imageWidth - 4, top + imageHeight - 4, 0xFF2A2E24);

        drawSlot(guiGraphics, left + 44, top + 26);
        drawSlot(guiGraphics, left + 44, top + 48);
        drawSlot(guiGraphics, left + 44, top + 70);
        drawSlot(guiGraphics, left + 130, top + 48);

        guiGraphics.fill(left + PROGRESS_X, top + PROGRESS_Y, left + PROGRESS_X + PROGRESS_WIDTH, top + PROGRESS_Y + PROGRESS_HEIGHT, 0xFF111111);
        guiGraphics.fill(left + PROGRESS_X + 1, top + PROGRESS_Y + 1, left + PROGRESS_X + PROGRESS_WIDTH - 1, top + PROGRESS_Y + PROGRESS_HEIGHT - 1, 0xFF4E3A18);
        int progress = menu.getScaledProgress(PROGRESS_WIDTH - 2);
        guiGraphics.fill(left + PROGRESS_X + 1, top + PROGRESS_Y + 1, left + PROGRESS_X + 1 + progress, top + PROGRESS_Y + PROGRESS_HEIGHT - 1, 0xFFFFB02E);

        guiGraphics.renderItem(new ItemStack(AddonItems.GASOLINE_CANISTER.get()), left + 81, top + 25);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.fuel_mixer.fuel"), left + 77, top + 13, 0xFFE6D99A, false);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.fuel_mixer.output"), left + 118, top + 32, 0xFFBFC8AF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderFuelMixerHints(guiGraphics, mouseX, mouseY);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF050505);
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFFB8B8B8);
        guiGraphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF303030);
    }

    private void renderFuelMixerHints(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(44, 26, 16, 16, mouseX, mouseY)) {
            renderHint(guiGraphics, mouseX, mouseY, "screen.sbwdroneconfig.fuel_mixer.tooltip.coal");
        } else if (isHovering(44, 48, 16, 16, mouseX, mouseY)) {
            renderHint(guiGraphics, mouseX, mouseY, "screen.sbwdroneconfig.fuel_mixer.tooltip.blaze_powder");
        } else if (isHovering(44, 70, 16, 16, mouseX, mouseY)) {
            renderHint(guiGraphics, mouseX, mouseY, "screen.sbwdroneconfig.fuel_mixer.tooltip.redstone");
        } else if (isHovering(130, 48, 16, 16, mouseX, mouseY)) {
            renderHint(guiGraphics, mouseX, mouseY, "screen.sbwdroneconfig.fuel_mixer.tooltip.output");
        } else if (isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            renderHint(guiGraphics, mouseX, mouseY, "screen.sbwdroneconfig.fuel_mixer.tooltip.progress");
        }
    }

    private void renderHint(GuiGraphics guiGraphics, int mouseX, int mouseY, String translationKey) {
        guiGraphics.renderComponentTooltip(font, List.of(Component.translatable(translationKey)), mouseX, mouseY);
    }
}
