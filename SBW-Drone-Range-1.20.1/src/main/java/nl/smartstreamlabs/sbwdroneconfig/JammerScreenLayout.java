package nl.smartstreamlabs.sbwdroneconfig;

final class JammerScreenLayout {
    static final int PANEL_WIDTH = 280;
    static final int PANEL_HEIGHT = 196;
    static final int LONG_RANGE_PANEL_EXTRA_HEIGHT = 42;
    static final int PADDING = 10;
    static final int BUTTON_GAP = 8;
    static final int BUTTON_HEIGHT = 20;
    static final int TITLE_Y = 8;
    static final int RANGE_LABEL_Y = 23;
    static final int RANGE_SLIDER_Y = 37;
    static final int TOWER_STATUS_Y = 64;
    static final int TOWER_STATUS_LINE_GAP = 11;
    static final int TOWER_STATUS_SECOND_COLUMN_X = 126;
    static final int WHITELIST_INPUT_LABEL_Y = 61;
    static final int WHITELIST_INPUT_Y = 73;
    static final int WHITELIST_BUTTON_Y = 97;
    static final int WHITELIST_LIST_LABEL_Y = 123;
    static final int WHITELIST_LIST_Y = 135;
    static final int SAVE_BUTTON_Y = 168;

    static int panelHeight(boolean longRangeTower) {
        return PANEL_HEIGHT + (longRangeTower ? LONG_RANGE_PANEL_EXTRA_HEIGHT : 0);
    }

    static int panelTop(int screenHeight, boolean longRangeTower) {
        return Math.max(2, (screenHeight - panelHeight(longRangeTower)) / 2);
    }

    static int pairedButtonWidth() {
        return (PANEL_WIDTH - (PADDING * 2) - BUTTON_GAP) / 2;
    }

    private JammerScreenLayout() {
    }
}
