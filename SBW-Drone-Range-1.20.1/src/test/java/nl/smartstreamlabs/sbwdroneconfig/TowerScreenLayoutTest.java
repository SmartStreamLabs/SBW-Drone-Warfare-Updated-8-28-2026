package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TowerScreenLayoutTest {
    private static final int HIGH_GUI_SCALE_VIEWPORT_HEIGHT = 240;

    @Test
    void radarJammerTowerLayoutFitsHighGuiScaleViewport() {
        int top = JammerScreenLayout.panelTop(HIGH_GUI_SCALE_VIEWPORT_HEIGHT, true);
        int panelBottom = top + JammerScreenLayout.panelHeight(true);
        int saveButtonBottom = top
                + JammerScreenLayout.SAVE_BUTTON_Y
                + JammerScreenLayout.LONG_RANGE_PANEL_EXTRA_HEIGHT
                + JammerScreenLayout.BUTTON_HEIGHT;

        assertTrue(panelBottom <= HIGH_GUI_SCALE_VIEWPORT_HEIGHT, "Radar tower panel should fit on high GUI scale screens.");
        assertTrue(saveButtonBottom <= HIGH_GUI_SCALE_VIEWPORT_HEIGHT, "Radar tower Save/Cancel buttons should stay visible.");
    }

    @Test
    void laserWhitelistLayoutFitsHighGuiScaleViewport() {
        int top = LaserTowerWhitelistScreen.panelTopForHeight(HIGH_GUI_SCALE_VIEWPORT_HEIGHT);
        int panelBottom = top + LaserTowerWhitelistScreen.panelHeight();

        assertTrue(panelBottom <= HIGH_GUI_SCALE_VIEWPORT_HEIGHT, "Laser tower whitelist panel should fit on high GUI scale screens.");
    }
}
