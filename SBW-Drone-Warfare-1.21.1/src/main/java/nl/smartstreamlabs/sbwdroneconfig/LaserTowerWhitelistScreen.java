package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class LaserTowerWhitelistScreen extends Screen {
    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 166;
    private static final int PADDING = 12;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 20;

    private final int entityId;
    private final List<String> entries;
    private EditBox input;

    private LaserTowerWhitelistScreen(int entityId, List<String> entries) {
        super(Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.title"));
        this.entityId = entityId;
        this.entries = new ArrayList<>(entries);
    }

    public static void open(int entityId, List<String> entries) {
        Minecraft.getInstance().setScreen(new LaserTowerWhitelistScreen(entityId, entries));
    }

    @Override
    protected void init() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = panelTop();
        int buttonWidth = (PANEL_WIDTH - (PADDING * 2) - BUTTON_GAP) / 2;

        input = new EditBox(this.font, left + PADDING, top + 54, PANEL_WIDTH - (PADDING * 2), BUTTON_HEIGHT,
                Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.input"));
        input.setMaxLength(36);
        addRenderableWidget(input);

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.add"), button -> submit(false))
                .bounds(left + PADDING, top + 78, buttonWidth, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.remove"), button -> submit(true))
                .bounds(left + PADDING + buttonWidth + BUTTON_GAP, top + 78, buttonWidth, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left + (PANEL_WIDTH - 96) / 2, top + 138, 96, BUTTON_HEIGHT)
                .build());
    }

    private void submit(boolean remove) {
        String value = input == null ? "" : input.getValue().trim();
        if (!value.isEmpty()) {
            AddonNetwork.updateLaserTowerWhitelist(new UpdateLaserTowerWhitelistMessage(entityId, value, remove));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (input != null && input.isFocused() && keyCode == 257) {
            submit(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = panelTop();

        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xE0101010);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 12, 0xFFFFFF);
        int hintY = top + 32;
        for (var line : this.font.split(Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.hint"), PANEL_WIDTH - (PADDING * 2))) {
            graphics.drawString(this.font, line, left + PADDING, hintY, 0xA0C8FF, false);
            hintY += 9;
        }

        int listY = top + 104;
        if (entries.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.empty"), left + PADDING, listY, 0xAAAAAA, false);
        } else {
            int visibleEntries = Math.min(entries.size(), 3);
            for (int i = 0; i < visibleEntries; i++) {
                graphics.drawString(this.font, "- " + entries.get(i), left + PADDING, listY + i * 10, 0xD8FFE0, false);
            }
            if (entries.size() > visibleEntries) {
                graphics.drawString(this.font, Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.more", entries.size() - visibleEntries),
                        left + PADDING, listY + visibleEntries * 10, 0xAAAAAA, false);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    static int panelTopForHeight(int screenHeight) {
        return Math.max(2, (screenHeight - PANEL_HEIGHT) / 2);
    }

    static int panelHeight() {
        return PANEL_HEIGHT;
    }

    private int panelTop() {
        return panelTopForHeight(this.height);
    }
}
