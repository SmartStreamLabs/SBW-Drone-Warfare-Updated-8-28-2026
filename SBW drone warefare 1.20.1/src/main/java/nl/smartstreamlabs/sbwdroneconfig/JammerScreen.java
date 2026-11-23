package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class JammerScreen extends Screen {
    private final net.minecraft.core.BlockPos blockPos;
    private final int minRange;
    private final int maxRange;
    private final boolean longRangeTower;
    private final int energy;
    private final int maxEnergy;
    private final boolean charging;
    private final boolean operational;
    private final List<String> whitelistedGamertags;
    private RangeSlider rangeSlider;
    private EditBox input;

    protected JammerScreen(OpenJammerConfigMessage message) {
        super(Component.translatable(message.longRangeTower() ? "screen.sbwdroneconfig.radar_jammer_tower.title" : "screen.sbwdroneconfig.jammer.title"));
        this.blockPos = message.blockPos();
        this.minRange = message.minRange();
        this.maxRange = message.maxRange();
        this.longRangeTower = message.longRangeTower();
        this.energy = message.energy();
        this.maxEnergy = message.maxEnergy();
        this.charging = message.charging();
        this.operational = message.operational();
        this.whitelistedGamertags = new ArrayList<>(message.whitelistedGamertags());
        this.rangeSlider = new RangeSlider(0, 0, 0, 0, message.range(), minRange, maxRange);
    }

    public static void open(OpenJammerConfigMessage message) {
        Minecraft.getInstance().setScreen(new JammerScreen(message));
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = JammerScreenLayout.PANEL_WIDTH;
        int left = (width - panelWidth) / 2;
        int top = JammerScreenLayout.PANEL_TOP;
        int towerOffset = towerContentOffset();

        rangeSlider = addRenderableWidget(new RangeSlider(left + JammerScreenLayout.PADDING, top + JammerScreenLayout.RANGE_SLIDER_Y,
                panelWidth - (JammerScreenLayout.PADDING * 2), JammerScreenLayout.BUTTON_HEIGHT,
                rangeSlider.getRange(), minRange, maxRange));
        input = new EditBox(font, left + JammerScreenLayout.PADDING, top + JammerScreenLayout.WHITELIST_INPUT_Y + towerOffset,
                panelWidth - (JammerScreenLayout.PADDING * 2), JammerScreenLayout.BUTTON_HEIGHT,
                Component.translatable("screen.sbwdroneconfig.jammer.whitelist_input"));
        input.setMaxLength(32);
        addRenderableWidget(input);

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.jammer.whitelist_add"), button -> submitWhitelist(false))
                .pos(left + JammerScreenLayout.PADDING, top + JammerScreenLayout.WHITELIST_BUTTON_Y + towerOffset).size(116, JammerScreenLayout.BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.jammer.whitelist_remove"), button -> submitWhitelist(true))
                .pos(left + 134, top + JammerScreenLayout.WHITELIST_BUTTON_Y + towerOffset).size(116, JammerScreenLayout.BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.jammer.save"), button -> saveAndClose())
                .pos(left + JammerScreenLayout.PADDING, top + JammerScreenLayout.SAVE_BUTTON_Y + towerOffset).size(116, JammerScreenLayout.BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .pos(left + 134, top + JammerScreenLayout.SAVE_BUTTON_Y + towerOffset).size(116, JammerScreenLayout.BUTTON_HEIGHT).build());
    }

    private void saveAndClose() {
        AddonNetwork.updateJammerRange(new UpdateJammerRangeMessage(blockPos, rangeSlider.getRange()));
        onClose();
    }

    private void submitWhitelist(boolean remove) {
        String value = input == null ? "" : input.getValue().trim();
        if (!value.isEmpty()) {
            AddonNetwork.updateJammerWhitelist(new UpdateJammerWhitelistMessage(blockPos, value, remove));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (input != null && input.isFocused() && keyCode == 257) {
            submitWhitelist(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelWidth = JammerScreenLayout.PANEL_WIDTH;
        int towerOffset = towerContentOffset();
        int left = (width - panelWidth) / 2;
        int top = JammerScreenLayout.PANEL_TOP;
        graphics.fill(left, top, left + panelWidth, top + JammerScreenLayout.PANEL_HEIGHT + towerOffset, 0xD0101010);
        graphics.drawCenteredString(font, title, width / 2, top + JammerScreenLayout.TITLE_Y, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("screen.sbwdroneconfig.jammer.range"), left + JammerScreenLayout.PADDING, top + JammerScreenLayout.RANGE_LABEL_Y, 0xC0C0C0, false);
        if (longRangeTower) {
            graphics.drawString(font, Component.translatable("screen.sbwdroneconfig.radar_jammer_tower.hint", maxRange), left + 84, top + JammerScreenLayout.RANGE_LABEL_Y, 0x80FF80, false);
            renderTowerPowerStatus(graphics, left, top);
        }
        graphics.drawString(font, Component.translatable("screen.sbwdroneconfig.jammer.whitelist_input"), left + JammerScreenLayout.PADDING, top + JammerScreenLayout.WHITELIST_INPUT_LABEL_Y + towerOffset, 0xC0C0C0, false);
        graphics.drawString(font, Component.translatable("screen.sbwdroneconfig.jammer.whitelist"), left + JammerScreenLayout.PADDING, top + JammerScreenLayout.WHITELIST_LIST_LABEL_Y + towerOffset, 0xC0C0C0, false);
        int y = top + JammerScreenLayout.WHITELIST_LIST_Y + towerOffset;
        for (int i = 0; i < Math.min(4, whitelistedGamertags.size()); i++) {
            graphics.drawString(font, "- " + whitelistedGamertags.get(i), left + 14, y + i * 11, 0xFFFFFF, false);
        }
        if (whitelistedGamertags.size() > 4) {
            graphics.drawString(font, Component.translatable("screen.sbwdroneconfig.jammer.whitelist_more", whitelistedGamertags.size() - 4), left + 14, y + 44, 0xA0A0A0, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private int towerContentOffset() {
        return longRangeTower ? 38 : 0;
    }

    private void renderTowerPowerStatus(GuiGraphics graphics, int left, int top) {
        int x = left + JammerScreenLayout.PADDING;
        int y = top + JammerScreenLayout.RANGE_SLIDER_Y + JammerScreenLayout.BUTTON_HEIGHT + 7;
        graphics.drawString(font, Component.translatable("screen.sbwdroneconfig.radar_jammer_tower.energy", energy, maxEnergy), x, y, 0xFFE6F2D0, false);
        graphics.drawString(font, Component.translatable("screen.sbwdroneconfig.radar_jammer_tower.charging", Component.translatable(charging ? "options.on" : "options.off")), x, y + 11, charging ? 0xFF80FF80 : 0xFFFFA060, false);
        graphics.drawString(font, Component.translatable("screen.sbwdroneconfig.radar_jammer_tower.status", Component.translatable(operational ? "screen.sbwdroneconfig.radar_jammer_tower.status.active" : "screen.sbwdroneconfig.radar_jammer_tower.status.inactive")), x + 122, y + 11, operational ? 0xFF80FF80 : 0xFFFF6060, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class RangeSlider extends AbstractSliderButton {
        private int range;
        private final int minRange;
        private final int maxRange;

        private RangeSlider(int x, int y, int width, int height, int currentRange, int minRange, int maxRange) {
            super(x, y, width, height, Component.empty(), toValue(currentRange, minRange, maxRange));
            this.minRange = minRange;
            this.maxRange = maxRange;
            this.range = clampRange(currentRange, minRange, maxRange);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("screen.sbwdroneconfig.jammer.range_value", range));
        }

        @Override
        protected void applyValue() {
            range = clampRange((int) Math.round(minRange + value * (maxRange - minRange)), minRange, maxRange);
            updateMessage();
        }

        private int getRange() {
            return range;
        }

        private static double toValue(int range, int minRange, int maxRange) {
            return (double) (clampRange(range, minRange, maxRange) - minRange) / (double) (maxRange - minRange);
        }

        private static int clampRange(int range, int minRange, int maxRange) {
            return Math.max(minRange, Math.min(maxRange, range));
        }
    }
}
