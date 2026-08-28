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

    protected JammerScreen(net.minecraft.core.BlockPos blockPos, int currentRange, int minRange, int maxRange, boolean longRangeTower, int energy, int maxEnergy, boolean charging, boolean operational, List<String> whitelistedGamertags) {
        super(Component.translatable(longRangeTower ? "screen.sbwdroneconfig.radar_jammer_tower.title" : "screen.sbwdroneconfig.jammer.title"));
        this.blockPos = blockPos;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.longRangeTower = longRangeTower;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.charging = charging;
        this.operational = operational;
        this.whitelistedGamertags = new ArrayList<>(whitelistedGamertags);
        this.rangeSlider = new RangeSlider(0, 0, 0, 0, currentRange, minRange, maxRange);
    }

    public static void open(OpenJammerConfigMessage message) {
        Minecraft.getInstance().setScreen(new JammerScreen(
                message.blockPos(),
                message.range(),
                message.minRange(),
                message.maxRange(),
                message.longRangeTower(),
                message.energy(),
                message.maxEnergy(),
                message.charging(),
                message.operational(),
                message.whitelistedGamertags()
        ));
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = JammerScreenLayout.PANEL_WIDTH;
        int left = (width - panelWidth) / 2;
        int top = JammerScreenLayout.panelTop(height, longRangeTower);
        int towerOffset = towerContentOffset();
        int pairedButtonWidth = JammerScreenLayout.pairedButtonWidth();
        int secondButtonX = left + JammerScreenLayout.PADDING + pairedButtonWidth + JammerScreenLayout.BUTTON_GAP;

        rangeSlider = addRenderableWidget(new RangeSlider(
                left + JammerScreenLayout.PADDING,
                top + JammerScreenLayout.RANGE_SLIDER_Y,
                panelWidth - (JammerScreenLayout.PADDING * 2),
                JammerScreenLayout.BUTTON_HEIGHT,
                rangeSlider.getRange(),
                minRange,
                maxRange
        ));

        input = new EditBox(
                font,
                left + JammerScreenLayout.PADDING,
                top + JammerScreenLayout.WHITELIST_INPUT_Y + towerOffset,
                panelWidth - (JammerScreenLayout.PADDING * 2),
                JammerScreenLayout.BUTTON_HEIGHT,
                Component.translatable("screen.sbwdroneconfig.jammer.whitelist_input")
        );
        input.setMaxLength(32);
        addRenderableWidget(input);

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.jammer.whitelist_add"), button -> submitWhitelist(false))
                .pos(left + JammerScreenLayout.PADDING, top + JammerScreenLayout.WHITELIST_BUTTON_Y + towerOffset)
                .size(pairedButtonWidth, JammerScreenLayout.BUTTON_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.jammer.whitelist_remove"), button -> submitWhitelist(true))
                .pos(secondButtonX, top + JammerScreenLayout.WHITELIST_BUTTON_Y + towerOffset)
                .size(pairedButtonWidth, JammerScreenLayout.BUTTON_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.jammer.save"), button -> saveAndClose())
                .pos(left + JammerScreenLayout.PADDING, top + JammerScreenLayout.SAVE_BUTTON_Y + towerOffset)
                .size(pairedButtonWidth, JammerScreenLayout.BUTTON_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .pos(secondButtonX, top + JammerScreenLayout.SAVE_BUTTON_Y + towerOffset)
                .size(pairedButtonWidth, JammerScreenLayout.BUTTON_HEIGHT)
                .build());
    }

    private void saveAndClose() {
        AddonNetwork.updateJammerRange(new UpdateJammerRangeMessage(blockPos, rangeSlider.getRange()));
        onClose();
    }

    private void submitWhitelist(boolean remove) {
        String value = input == null ? "" : input.getValue().trim();
        if (value.isEmpty()) {
            return;
        }
        AddonNetwork.updateJammerWhitelist(new UpdateJammerWhitelistMessage(blockPos, value, remove));
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int panelWidth = JammerScreenLayout.PANEL_WIDTH;
        int towerOffset = towerContentOffset();
        int panelHeight = JammerScreenLayout.panelHeight(longRangeTower);
        int left = (width - panelWidth) / 2;
        int top = JammerScreenLayout.panelTop(height, longRangeTower);
        guiGraphics.fill(left, top, left + panelWidth, top + panelHeight, 0xD0101010);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderForegroundText(guiGraphics, left, top, towerOffset);
    }

    private void renderForegroundText(GuiGraphics guiGraphics, int left, int top, int towerOffset) {
        guiGraphics.drawCenteredString(font, title, width / 2, top + JammerScreenLayout.TITLE_Y, 0xFFFFFF);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.jammer.range"), left + JammerScreenLayout.PADDING, top + JammerScreenLayout.RANGE_LABEL_Y, 0xC0C0C0, false);
        if (longRangeTower) {
            guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.radar_jammer_tower.hint", maxRange), left + JammerScreenLayout.PADDING + 58, top + JammerScreenLayout.RANGE_LABEL_Y, 0x80FF80, false);
            renderTowerPowerStatus(guiGraphics, left, top);
        }
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.jammer.whitelist_input"), left + JammerScreenLayout.PADDING, top + JammerScreenLayout.WHITELIST_INPUT_LABEL_Y + towerOffset, 0xC0C0C0, false);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.jammer.whitelist"), left + JammerScreenLayout.PADDING, top + JammerScreenLayout.WHITELIST_LIST_LABEL_Y + towerOffset, 0xC0C0C0, false);

        int y = top + JammerScreenLayout.WHITELIST_LIST_Y + towerOffset;
        int visibleEntries = longRangeTower ? 2 : 4;
        for (int i = 0; i < Math.min(visibleEntries, whitelistedGamertags.size()); i++) {
            guiGraphics.drawString(font, "- " + whitelistedGamertags.get(i), left + 14, y + i * 11, 0xFFFFFF, false);
        }
        if (whitelistedGamertags.size() > visibleEntries) {
            guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.jammer.whitelist_more", whitelistedGamertags.size() - visibleEntries), left + 14, y + visibleEntries * 11, 0xA0A0A0, false);
        }
    }

    private int towerContentOffset() {
        return longRangeTower ? JammerScreenLayout.LONG_RANGE_PANEL_EXTRA_HEIGHT : 0;
    }

    private void renderTowerPowerStatus(GuiGraphics guiGraphics, int left, int top) {
        int x = left + JammerScreenLayout.PADDING;
        int y = top + JammerScreenLayout.TOWER_STATUS_Y;
        int lineGap = JammerScreenLayout.TOWER_STATUS_LINE_GAP;
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.radar_jammer_tower.energy", energy, maxEnergy), x, y, 0xFFE6F2D0, false);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.radar_jammer_tower.charging", Component.translatable(charging ? "options.on" : "options.off")), x, y + lineGap, charging ? 0xFF80FF80 : 0xFFFFA060, false);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.radar_jammer_tower.status", Component.translatable(operational ? "screen.sbwdroneconfig.radar_jammer_tower.status.active" : "screen.sbwdroneconfig.radar_jammer_tower.status.inactive")), x + JammerScreenLayout.TOWER_STATUS_SECOND_COLUMN_X, y + lineGap, operational ? 0xFF80FF80 : 0xFFFF6060, false);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.radar_jammer_tower.charge_rates",
                AddonConfig.radarTowerSolarChargeRate(), AddonConfig.radarTowerChargingStationChargeRate()), x, y + lineGap * 2, 0xFFB0D8FF, false);
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
            this.range = clampRange((int) Math.round(minRange + value * (maxRange - minRange)), minRange, maxRange);
            updateMessage();
        }

        public int getRange() {
            return range;
        }

        private static double toValue(int range, int minRange, int maxRange) {
            int clamped = clampRange(range, minRange, maxRange);
            return (double) (clamped - minRange) / (double) (maxRange - minRange);
        }

        private static int clampRange(int range, int minRange, int maxRange) {
            return Math.max(minRange, Math.min(maxRange, range));
        }
    }
}
