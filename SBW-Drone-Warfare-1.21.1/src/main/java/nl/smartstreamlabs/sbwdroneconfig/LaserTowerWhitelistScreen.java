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
        int panelWidth = 280;
        int left = (this.width - panelWidth) / 2;
        int top = this.height / 2 - 90;

        input = new EditBox(this.font, left + 20, top + 60, panelWidth - 40, 20,
                Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.input"));
        input.setMaxLength(36);
        addRenderableWidget(input);

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.add"), button -> submit(false))
                .bounds(left + 20, top + 86, 110, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.remove"), button -> submit(true))
                .bounds(left + 150, top + 86, 110, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left + 90, top + 150, 100, 20)
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
        renderBackground(graphics);
        int panelWidth = 280;
        int left = (this.width - panelWidth) / 2;
        int top = this.height / 2 - 90;

        graphics.fill(left, top, left + panelWidth, top + 180, 0xE0101010);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 12, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.hint"), left + 20, top + 34, 0xA0C8FF, false);

        int listY = top + 116;
        if (entries.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.empty"), left + 20, listY, 0xAAAAAA, false);
        } else {
            int visibleEntries = Math.min(entries.size(), 4);
            for (int i = 0; i < visibleEntries; i++) {
                graphics.drawString(this.font, "- " + entries.get(i), left + 20, listY + i * 10, 0xD8FFE0, false);
            }
            if (entries.size() > visibleEntries) {
                graphics.drawString(this.font, Component.translatable("screen.sbwdroneconfig.laser_tower_whitelist.more", entries.size() - visibleEntries),
                        left + 20, listY + visibleEntries * 10, 0xAAAAAA, false);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
