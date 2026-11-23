package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class DroneConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 300;
    private static final int SIDEBAR_WIDTH = 132;
    private static final int CONTENT_MARGIN = 16;
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_SPACING = 24;
    private static final int SIDEBAR_CATEGORY_BUTTONS_PER_PAGE = 8;
    private static final int TITLE_Y_OFFSET = 16;
    private static final int SUBTITLE_Y_OFFSET = 30;
    private static final int CATEGORY_TITLE_Y_OFFSET = 56;
    private static final int CATEGORY_DESCRIPTION_Y_OFFSET = 68;
    private static final int CONTENT_START_Y_OFFSET = 92;

    private final Screen parent;
    private final DroneConfigCategory selectedCategory;
    private final DroneConfigDraft draft;
    private final int categoryPage;
    private final int sidebarPage;

    protected DroneConfigScreen(Screen parent, DroneConfigCategory selectedCategory, DroneConfigDraft draft) {
        this(parent, selectedCategory, draft, 0);
    }

    private DroneConfigScreen(Screen parent, DroneConfigCategory selectedCategory, DroneConfigDraft draft, int categoryPage) {
        this(parent, selectedCategory, draft, categoryPage, sidebarPageFor(selectedCategory));
    }

    private DroneConfigScreen(Screen parent, DroneConfigCategory selectedCategory, DroneConfigDraft draft, int categoryPage, int sidebarPage) {
        super(Component.translatable("screen.sbwdroneconfig.config.title"));
        this.parent = parent;
        this.selectedCategory = selectedCategory;
        this.draft = draft;
        this.categoryPage = clampCategoryPage(selectedCategory, categoryPage);
        this.sidebarPage = clampSidebarPage(sidebarPage);
    }

    @Override
    protected void init() {
        super.init();

        int left = panelLeft();
        int top = panelTop();
        int sidebarX = left + 12;
        int sidebarY = top + 52;

        DroneConfigCategory[] categories = DroneConfigCategory.values();
        int firstCategory = sidebarPage * SIDEBAR_CATEGORY_BUTTONS_PER_PAGE;
        int lastCategory = Math.min(categories.length, firstCategory + SIDEBAR_CATEGORY_BUTTONS_PER_PAGE);
        for (int i = firstCategory; i < lastCategory; i++) {
            // Keep the exact category bound to this widget; sidebar clicks must not resolve by index.
            final DroneConfigCategory clickedCategory = categories[i];
            int visibleIndex = i - firstCategory;
            Button categoryButton = addRenderableWidget(Button.builder(clickedCategory.title(), button -> openCategory(clickedCategory))
                    .pos(sidebarX, sidebarY + (visibleIndex * 24))
                    .size(SIDEBAR_WIDTH - 24, 20)
                    .build());
            categoryButton.active = clickedCategory != selectedCategory;
        }
        addSidebarPageButtons(sidebarX, sidebarY);

        int contentX = left + SIDEBAR_WIDTH + CONTENT_MARGIN;
        int contentY = contentAreaTop(top);
        int contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - (CONTENT_MARGIN * 2);
        addPageButtons(contentX, top, contentWidth);
        addCategoryWidgets(contentX, contentY, contentWidth);

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.config.reset_category"), button -> {
                    draft.resetCategory(selectedCategory);
                    if (minecraft != null) {
                        minecraft.setScreen(new DroneConfigScreen(parent, selectedCategory, draft, categoryPage));
                    }
                })
                .pos(contentX, top + PANEL_HEIGHT - 34)
                .size(120, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.config.close"), button -> onClose())
                .pos(contentX + 130, top + PANEL_HEIGHT - 34)
                .size(90, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.config.save"), button -> saveAndClose())
                .pos(contentX + contentWidth - 90, top + PANEL_HEIGHT - 34)
                .size(90, 20)
                .build());
    }

    private void addPageButtons(int contentX, int top, int contentWidth) {
        int pages = pageCount(selectedCategory);
        if (pages <= 1) {
            return;
        }

        int buttonY = top + CATEGORY_TITLE_Y_OFFSET - 2;
        int nextX = contentX + contentWidth - 20;
        Button previousButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> openCategoryPage(categoryPage - 1))
                .pos(nextX - 72, buttonY)
                .size(20, 18)
                .build());
        previousButton.active = categoryPage > 0;

        Button nextButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> openCategoryPage(categoryPage + 1))
                .pos(nextX, buttonY)
                .size(20, 18)
                .build());
        nextButton.active = categoryPage < pages - 1;
    }

    private void openCategoryPage(int page) {
        if (minecraft != null) {
            minecraft.setScreen(new DroneConfigScreen(parent, selectedCategory, draft, page));
        }
    }

    private void openCategory(DroneConfigCategory category) {
        if (minecraft != null) {
            minecraft.setScreen(new DroneConfigScreen(parent, category, draft, 0));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            DroneConfigCategory clickedCategory = categoryAtSidebarPosition(mouseX, mouseY);
            if (clickedCategory != null) {
                if (clickedCategory != selectedCategory) {
                    openCategory(clickedCategory);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private DroneConfigCategory categoryAtSidebarPosition(double mouseX, double mouseY) {
        int sidebarX = panelLeft() + 12;
        int sidebarY = panelTop() + 52;
        int sidebarButtonWidth = SIDEBAR_WIDTH - 24;
        if (mouseX < sidebarX || mouseX >= sidebarX + sidebarButtonWidth || mouseY < sidebarY) {
            return null;
        }

        int visibleIndex = (int) ((mouseY - sidebarY) / 24);
        int categoryIndex = sidebarPage * SIDEBAR_CATEGORY_BUTTONS_PER_PAGE + visibleIndex;
        DroneConfigCategory[] categories = DroneConfigCategory.values();
        if (visibleIndex < 0 || visibleIndex >= SIDEBAR_CATEGORY_BUTTONS_PER_PAGE
                || categoryIndex >= categories.length || mouseY >= sidebarY + (visibleIndex * 24) + CONTROL_HEIGHT) {
            return null;
        }
        return categories[categoryIndex];
    }

    private void addSidebarPageButtons(int sidebarX, int sidebarY) {
        int pages = sidebarPageCount();
        if (pages <= 1) {
            return;
        }

        int buttonY = sidebarY + (SIDEBAR_CATEGORY_BUTTONS_PER_PAGE * 24) + 4;
        Button previousButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> openSidebarPage(sidebarPage - 1))
                .pos(sidebarX, buttonY)
                .size(20, 18)
                .build());
        previousButton.active = sidebarPage > 0;

        Button nextButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> openSidebarPage(sidebarPage + 1))
                .pos(sidebarX + SIDEBAR_WIDTH - 44, buttonY)
                .size(20, 18)
                .build());
        nextButton.active = sidebarPage < pages - 1;
    }

    private void openSidebarPage(int page) {
        if (minecraft != null) {
            int clampedPage = clampSidebarPage(page);
            minecraft.setScreen(new DroneConfigScreen(parent, firstCategoryOnSidebarPage(clampedPage), draft, 0, clampedPage));
        }
    }

    private void addCategoryWidgets(int x, int y, int width) {
        switch (selectedCategory) {
            case DISTANCE_WAR -> addDoubleSlider(x,y,width,Component.translatable("screen.sbwdroneconfig.config.label.distance_war_volume"),0,1,()->draft.distanceWarVolume,value->draft.distanceWarVolume=value,DroneConfigScreen::multiplierText);
            case DRONE -> {
                addIntSlider(x, y, width, Component.translatable("screen.sbwdroneconfig.config.label.drone_max_range"),
                        1, 30000, () -> draft.droneMaxRange, value -> draft.droneMaxRange = value, value -> blocks(value));
                addToggle(x, y + CONTROL_SPACING, width, Component.translatable("screen.sbwdroneconfig.config.label.chunk_loading"),
                        () -> draft.enableDroneChunkLoading, value -> draft.enableDroneChunkLoading = value);
                addIntSlider(x, y + CONTROL_SPACING * 2, width, Component.translatable("screen.sbwdroneconfig.config.label.chunk_radius"),
                        0, 4, () -> draft.droneChunkLoadRadius, value -> draft.droneChunkLoadRadius = value, value -> chunks(value));
                addToggle(x, y + CONTROL_SPACING * 3, width, Component.translatable("screen.sbwdroneconfig.config.label.remote_tracking"),
                        () -> draft.enableDroneRemoteChunkTracking, value -> draft.enableDroneRemoteChunkTracking = value);
                addToggle(x, y + CONTROL_SPACING * 4, width, Component.translatable("screen.sbwdroneconfig.config.label.player_anchor"),
                        () -> draft.enableDronePlayerAnchor, value -> draft.enableDronePlayerAnchor = value);
                addToggle(x, y + CONTROL_SPACING * 5, width, Component.translatable("screen.sbwdroneconfig.config.label.crash_damage"),
                        () -> draft.enableDroneCrashExplosionDamage, value -> draft.enableDroneCrashExplosionDamage = value);
                addToggle(x, y + CONTROL_SPACING * 5, width, Component.translatable("screen.sbwdroneconfig.config.label.hide_player_on_minimap"),
                        () -> draft.hidePlayerOnMinimapDuringDroneControl, value -> draft.hidePlayerOnMinimapDuringDroneControl = value);
                addToggle(x, y + CONTROL_SPACING * 6, width, Component.translatable("screen.sbwdroneconfig.config.label.show_drone_marker"),
                        () -> draft.showDroneMarkerInstead, value -> draft.showDroneMarkerInstead = value);
            }
            case LUCAS_FLIGHT -> {
                if (categoryPage == 0) {
                    addLucasFlightPage(x, y, width);
                } else {
                    addLucasFuelAndAudioPage(x, y, width);
                }
            }
            case FUEL -> {
                addIntSlider(x, y, width, Component.translatable("screen.sbwdroneconfig.config.label.fuel_mixer_process_time"),
                        20, 12000, () -> draft.fuelMixerProcessTime, value -> draft.fuelMixerProcessTime = value, DroneConfigScreen::ticks);
                addIntSlider(x, y + CONTROL_SPACING, width, Component.translatable("screen.sbwdroneconfig.config.label.gasoline_jerrycan_fuel"),
                        100, 100000, () -> draft.gasolineJerrycanFuelAmount, value -> draft.gasolineJerrycanFuelAmount = value, DroneConfigScreen::units);
            }
            case BATTERY -> {
                addToggle(x, y, width, Component.translatable("screen.sbwdroneconfig.config.label.battery_enabled"),
                        () -> draft.enableBatterySystem, value -> draft.enableBatterySystem = value);
                addIntSlider(x, y + CONTROL_SPACING, width, Component.translatable("screen.sbwdroneconfig.config.label.max_energy"),
                        200, 5000, () -> draft.maxEnergy, value -> draft.maxEnergy = value, value -> units(value));
                addIntSlider(x, y + CONTROL_SPACING * 2, width, Component.translatable("screen.sbwdroneconfig.config.label.energy_use"),
                        0, 20, () -> draft.energyUsePerTick, value -> draft.energyUsePerTick = value, value -> perTick(value));
                addIntSlider(x, y + CONTROL_SPACING * 3, width, Component.translatable("screen.sbwdroneconfig.config.label.low_energy_threshold"),
                        0, 2000, () -> draft.lowEnergyThreshold, value -> draft.lowEnergyThreshold = value, value -> units(value));
                addIntSlider(x, y + CONTROL_SPACING * 4, width, Component.translatable("screen.sbwdroneconfig.config.label.battery_transfer"),
                        10, 500, () -> draft.batteryTransferRate, value -> draft.batteryTransferRate = value, value -> units(value));
                addToggle(x, y + CONTROL_SPACING * 5, width, Component.translatable("screen.sbwdroneconfig.config.label.return_home"),
                        () -> draft.returnHomeOnEmpty, value -> draft.returnHomeOnEmpty = value);
            }
            case JAMMER -> {
                if (categoryPage == 0) {
                    addToggle(x, y, width, Component.translatable("screen.sbwdroneconfig.config.label.jammer_enabled"),
                            () -> draft.enableDroneJammer, value -> draft.enableDroneJammer = value);
                    addIntSlider(x, y + CONTROL_SPACING, width, Component.translatable("screen.sbwdroneconfig.config.label.jammer_range"),
                            4, 128, () -> draft.jammerRange, value -> draft.jammerRange = value, value -> blocks(value));
                    addIntSlider(x, y + CONTROL_SPACING * 2, width, Component.translatable("screen.sbwdroneconfig.config.label.jammer_buildup"),
                            10, 400, () -> draft.jammerBuildUpTicks, value -> draft.jammerBuildUpTicks = value, value -> ticks(value));
                    addIntSlider(x, y + CONTROL_SPACING * 3, width, Component.translatable("screen.sbwdroneconfig.config.label.jammer_recovery"),
                            10, 400, () -> draft.jammerRecoveryTicks, value -> draft.jammerRecoveryTicks = value, value -> ticks(value));
                    addDoubleSlider(x, y + CONTROL_SPACING * 4, width, Component.translatable("screen.sbwdroneconfig.config.label.interference_threshold"),
                            0.10D, 0.95D, () -> draft.controlInterferenceThreshold, value -> draft.controlInterferenceThreshold = value, DroneConfigUiMath::toPercentLabel);
                    addDoubleSlider(x, y + CONTROL_SPACING * 5, width, Component.translatable("screen.sbwdroneconfig.config.label.hard_jam_threshold"),
                            0.50D, 1.00D, () -> draft.hardJamThreshold, value -> draft.hardJamThreshold = value, DroneConfigUiMath::toPercentLabel);
                    addToggle(x, y + CONTROL_SPACING * 6, width, Component.translatable("screen.sbwdroneconfig.config.label.affect_friendly"),
                            () -> draft.affectFriendlyDrones, value -> draft.affectFriendlyDrones = value);
                } else {
                    int columnGap = 12;
                    int columnWidth = (width - columnGap) / 2;
                    int rightColumnX = x + columnWidth + columnGap;
                    addIntSlider(x, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_detection_range"),
                            16, 1024, () -> draft.droneRadarDetectionRange, value -> draft.droneRadarDetectionRange = value, value -> blocks(value));
                    addToggle(x, y + CONTROL_SPACING, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_beep_enabled"),
                            () -> draft.droneRadarBeepEnabled, value -> draft.droneRadarBeepEnabled = value);
                    addDoubleSlider(x, y + CONTROL_SPACING * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_beep_volume"),
                            0.0D, 2.0D, () -> draft.droneRadarBeepVolume, value -> draft.droneRadarBeepVolume = value, DroneConfigUiMath::toPercentLabel);
                    addIntSlider(x, y + CONTROL_SPACING * 3, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_min_beep_interval"),
                            1, 100, () -> draft.droneRadarMinBeepIntervalTicks, value -> draft.droneRadarMinBeepIntervalTicks = value, value -> ticks(value));
                    addIntSlider(x, y + CONTROL_SPACING * 4, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_max_beep_interval"),
                            5, 200, () -> draft.droneRadarMaxBeepIntervalTicks, value -> draft.droneRadarMaxBeepIntervalTicks = value, value -> ticks(value));
                    addToggle(x, y + CONTROL_SPACING * 5, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_only_beep_when_held"),
                            () -> draft.droneRadarOnlyBeepWhenHeld, value -> draft.droneRadarOnlyBeepWhenHeld = value);
                    addToggle(rightColumnX, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_compact_mode"),
                            () -> draft.droneRadarCompactMode, value -> draft.droneRadarCompactMode = value);
                    addDoubleSlider(rightColumnX, y + CONTROL_SPACING, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_hud_scale"),
                            0.25D, 1.25D, () -> draft.droneRadarHudScale, value -> draft.droneRadarHudScale = value, DroneConfigUiMath::toPercentLabel);
                    addIntSlider(rightColumnX, y + CONTROL_SPACING * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_hud_x_offset"),
                            0, 256, () -> draft.droneRadarHudXOffset, value -> draft.droneRadarHudXOffset = value, value -> pixels(value));
                    addIntSlider(rightColumnX, y + CONTROL_SPACING * 3, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.radar_hud_y_offset"),
                            0, 256, () -> draft.droneRadarHudYOffset, value -> draft.droneRadarHudYOffset = value, value -> pixels(value));
                    addToggle(rightColumnX, y + CONTROL_SPACING * 4, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.debug_drone_radar"),
                            () -> draft.debugDroneRadar, value -> draft.debugDroneRadar = value);
                }
            }
            case WEATHER -> {
                addToggle(x, y, width, Component.translatable("screen.sbwdroneconfig.config.label.weather_effects"),
                        () -> draft.weatherEffectsEnabled, value -> draft.weatherEffectsEnabled = value);
                addToggle(x, y + CONTROL_SPACING, width, Component.translatable("screen.sbwdroneconfig.config.label.thermal_enabled"),
                        () -> draft.enableThermalVision, value -> draft.enableThermalVision = value);
                addIntSlider(x, y + CONTROL_SPACING * 2, width, Component.translatable("screen.sbwdroneconfig.config.label.thermal_range"),
                        8, 128, () -> draft.thermalVisionRange, value -> draft.thermalVisionRange = value, value -> blocks(value));
                addToggle(x, y + CONTROL_SPACING * 3, width, Component.translatable("screen.sbwdroneconfig.config.label.thermal_noise"),
                        () -> draft.thermalVisionNoiseEffect, value -> draft.thermalVisionNoiseEffect = value);
                addDoubleSlider(x, y + CONTROL_SPACING * 4, width, Component.translatable("screen.sbwdroneconfig.config.label.rain_visibility"),
                        0.10D, 1.00D, () -> draft.rainVisibilityMultiplier, value -> draft.rainVisibilityMultiplier = value, DroneConfigUiMath::toPercentLabel);
                addDoubleSlider(x, y + CONTROL_SPACING * 5, width, Component.translatable("screen.sbwdroneconfig.config.label.storm_instability"),
                        0.0D, 1.0D, () -> draft.stormInstabilityStrength, value -> draft.stormInstabilityStrength = value, DroneConfigScreen::multiplierText);
            }
            case SPOTLIGHT -> {
                addToggle(x, y, width, Component.translatable("screen.sbwdroneconfig.config.label.spotlight_enabled"),
                        () -> draft.enableSpotlightModule, value -> draft.enableSpotlightModule = value);
                addIntSlider(x, y + CONTROL_SPACING, width, Component.translatable("screen.sbwdroneconfig.config.label.spotlight_range"),
                        8, 100, () -> draft.spotlightRange, value -> draft.spotlightRange = value, value -> blocks(value));
                addIntSlider(x, y + CONTROL_SPACING * 2, width, Component.translatable("screen.sbwdroneconfig.config.label.spotlight_energy"),
                        0, 20, () -> draft.spotlightEnergyCostPerSecond, value -> draft.spotlightEnergyCostPerSecond = value, value -> perSecond(value));
                addToggle(x, y + CONTROL_SPACING * 3, width, Component.translatable("screen.sbwdroneconfig.config.label.spotlight_requires_module"),
                        () -> draft.spotlightRequiresModule, value -> draft.spotlightRequiresModule = value);
                addToggle(x, y + CONTROL_SPACING * 4, width, Component.translatable("screen.sbwdroneconfig.config.label.spotlight_auto_off"),
                        () -> draft.spotlightAutoOffWhenBatteryEmpty, value -> draft.spotlightAutoOffWhenBatteryEmpty = value);
                addToggle(x, y + CONTROL_SPACING * 5, width, Component.translatable("screen.sbwdroneconfig.config.label.spotlight_real_light"),
                        () -> draft.spotlightRealLightEnabled, value -> draft.spotlightRealLightEnabled = value);
                addIntSlider(x, y + CONTROL_SPACING * 6, width, Component.translatable("screen.sbwdroneconfig.config.label.spotlight_light_range"),
                        8, 100, () -> draft.spotlightLightRange, value -> draft.spotlightLightRange = value, value -> blocks(value));
            }
            case FPV_DRONE -> {
                if (categoryPage == 0) {
                    addFpvLinkAndAudioPage(x, y, width);
                } else {
                    addFpvCameraPage(x, y, width);
                }
            }
            case DELIVERY_DRONE, GLIDE_BOMB, HEAVY_BOMBER_DRONE, GERANIUM_2, BAYRAKTAR_TB2 -> addSharedFpvDronePage(x, y, width);
        }
    }

    private void addFpvLinkAndAudioPage(int x, int y, int width) {
        int columnGap = 12;
        int columnWidth = (width - columnGap) / 2;
        int rightColumnX = x + columnWidth + columnGap;

        addToggle(x, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.fiber_optic_enabled"),
                () -> draft.enableFiberOpticMode, value -> draft.enableFiberOpticMode = value);
        addToggle(x, y + CONTROL_SPACING, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.anti_drone_net_cuts_fiber"),
                () -> draft.antiDroneNetCutsFiber, value -> draft.antiDroneNetCutsFiber = value);
        addIntSlider(x, y + CONTROL_SPACING * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.fiber_optic_cable_length"),
                16, 4096, () -> draft.fiberOpticCableLength, value -> draft.fiberOpticCableLength = value, DroneConfigScreen::blocks);
        addIntSlider(x, y + CONTROL_SPACING * 3, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.fiber_optic_break_delay"),
                1, 2400, () -> draft.fiberOpticCableBreakDelayTicks, value -> draft.fiberOpticCableBreakDelayTicks = value, DroneConfigScreen::ticks);
        addDoubleSlider(x, y + CONTROL_SPACING * 4, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.fiber_optic_battery_drain"),
                1.0D, 10.0D, () -> draft.fiberOpticExtraBatteryDrainMultiplier, value -> draft.fiberOpticExtraBatteryDrainMultiplier = value, DroneConfigUiMath::toPercentLabel);
        addToggle(x, y + CONTROL_SPACING * 5, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.normal_jammers_affect_fiber"),
                () -> draft.normalJammersAffectFiberOptic, value -> draft.normalJammersAffectFiberOptic = value);
        addToggle(x, y + CONTROL_SPACING * 6, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.debug_fiber"),
                () -> draft.debugFiberOptic, value -> draft.debugFiberOptic = value);

        addDoubleSlider(rightColumnX, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.drone_speed"),
                DroneSpeedConfigLimits.MIN_DRONE_SPEED_MULTIPLIER, DroneSpeedConfigLimits.MAX_DRONE_SPEED_MULTIPLIER,
                () -> draft.droneSpeedMultiplier, value -> draft.droneSpeedMultiplier = value, DroneConfigUiMath::toPercentLabel);
        addDoubleSlider(rightColumnX, y + CONTROL_SPACING, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.engine_volume"),
                DroneAudioConfigLimits.MIN_ENGINE_VOLUME_MULTIPLIER, DroneAudioConfigLimits.MAX_ENGINE_VOLUME_MULTIPLIER,
                () -> draft.droneEngineVolumeMultiplier, value -> draft.droneEngineVolumeMultiplier = value, DroneConfigUiMath::toPercentLabel);
        addDoubleSlider(rightColumnX, y + CONTROL_SPACING * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.monitor_volume"),
                DroneAudioConfigLimits.MIN_MONITOR_HUM_VOLUME_MULTIPLIER, DroneAudioConfigLimits.MAX_MONITOR_HUM_VOLUME_MULTIPLIER,
                () -> draft.droneMonitorHumVolumeMultiplier, value -> draft.droneMonitorHumVolumeMultiplier = value, DroneConfigUiMath::toPercentLabel);
        addDoubleSlider(rightColumnX, y + CONTROL_SPACING * 3, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.airborne_volume"),
                DroneAudioConfigLimits.MIN_AIRBORNE_VOLUME_MULTIPLIER, DroneAudioConfigLimits.MAX_AIRBORNE_VOLUME_MULTIPLIER,
                () -> draft.droneAirborneVolumeMultiplier, value -> draft.droneAirborneVolumeMultiplier = value, DroneConfigUiMath::toPercentLabel);
        addToggle(rightColumnX, y + CONTROL_SPACING * 4, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.screenshot_sound"),
                () -> draft.enableDroneScreenshotSound, value -> draft.enableDroneScreenshotSound = value);
    }

    private void addFpvCameraPage(int x, int y, int width) {
        addDoubleSlider(x, y, width, Component.translatable("screen.sbwdroneconfig.config.label.fpv_camera_fov"),
                120.0D, 155.0D, () -> draft.fpvCameraFov, value -> draft.fpvCameraFov = value, DroneConfigScreen::degreesText);
        addDoubleSlider(x, y + CONTROL_SPACING, width, Component.translatable("screen.sbwdroneconfig.config.label.fpv_camera_tilt"),
                0.0D, 45.0D, () -> draft.fpvCameraTiltDegrees, value -> draft.fpvCameraTiltDegrees = value, DroneConfigScreen::degreesText);
        addToggle(x, y + CONTROL_SPACING * 2, width, Component.translatable("screen.sbwdroneconfig.config.label.fpv_fisheye_effect"),
                () -> draft.enableFpvFisheyeEffect, value -> draft.enableFpvFisheyeEffect = value);
        addToggle(x, y + CONTROL_SPACING * 3, width, Component.translatable("screen.sbwdroneconfig.config.label.fpv_camera_vibration"),
                () -> draft.enableFpvCameraVibration, value -> draft.enableFpvCameraVibration = value);
        addToggle(x, y + CONTROL_SPACING * 4, width, Component.translatable("screen.sbwdroneconfig.config.label.hide_player_on_minimap"),
                () -> draft.hidePlayerOnMinimapDuringDroneControl, value -> draft.hidePlayerOnMinimapDuringDroneControl = value);
        addToggle(x, y + CONTROL_SPACING * 5, width, Component.translatable("screen.sbwdroneconfig.config.label.show_drone_marker"),
                () -> draft.showDroneMarkerInstead, value -> draft.showDroneMarkerInstead = value);
    }

    private void addSharedFpvDronePage(int x, int y, int width) {
        int columnGap = 12;
        int columnWidth = (width - columnGap) / 2;
        int rightColumnX = x + columnWidth + columnGap;

        addDoubleSlider(x, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.shared_drone_speed"),
                DroneSpeedConfigLimits.MIN_DRONE_SPEED_MULTIPLIER, DroneSpeedConfigLimits.MAX_DRONE_SPEED_MULTIPLIER,
                () -> draft.droneSpeedMultiplier, value -> draft.droneSpeedMultiplier = value, DroneConfigUiMath::toPercentLabel);
        addDoubleSlider(x, y + CONTROL_SPACING, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.shared_drone_engine_volume"),
                DroneAudioConfigLimits.MIN_ENGINE_VOLUME_MULTIPLIER, DroneAudioConfigLimits.MAX_ENGINE_VOLUME_MULTIPLIER,
                () -> draft.droneEngineVolumeMultiplier, value -> draft.droneEngineVolumeMultiplier = value, DroneConfigUiMath::toPercentLabel);
        addDoubleSlider(x, y + CONTROL_SPACING * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.shared_drone_airborne_volume"),
                DroneAudioConfigLimits.MIN_AIRBORNE_VOLUME_MULTIPLIER, DroneAudioConfigLimits.MAX_AIRBORNE_VOLUME_MULTIPLIER,
                () -> draft.droneAirborneVolumeMultiplier, value -> draft.droneAirborneVolumeMultiplier = value, DroneConfigUiMath::toPercentLabel);

        addIntSlider(rightColumnX, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.drone_max_range"),
                1, 30000, () -> draft.droneMaxRange, value -> draft.droneMaxRange = value, DroneConfigScreen::blocks);
        addToggle(rightColumnX, y + CONTROL_SPACING, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.chunk_loading"),
                () -> draft.enableDroneChunkLoading, value -> draft.enableDroneChunkLoading = value);
        addToggle(rightColumnX, y + CONTROL_SPACING * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.crash_damage"),
                () -> draft.enableDroneCrashExplosionDamage, value -> draft.enableDroneCrashExplosionDamage = value);
    }

    private void addLucasFlightPage(int x, int y, int width) {
        int columnGap = 12;
        int columnWidth = (width - columnGap) / 2;
        int rightColumnX = x + columnWidth + columnGap;
        int lucasSpacing = 19;

        addIntSlider(x, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_takeoff_assist_ticks"),
                0, 200, () -> draft.lucasTakeoffAssistTicks, value -> draft.lucasTakeoffAssistTicks = value, DroneConfigScreen::ticks);
        addDoubleSlider(x, y + lucasSpacing, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_takeoff_forward_boost"),
                0.0D, 3.0D, () -> draft.lucasTakeoffForwardBoost, value -> draft.lucasTakeoffForwardBoost = value, DroneConfigScreen::flightSpeedText);
        addDoubleSlider(x, y + lucasSpacing * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_takeoff_up_boost"),
                0.0D, 1.0D, () -> draft.lucasTakeoffUpBoost, value -> draft.lucasTakeoffUpBoost = value, DroneConfigScreen::decimalText);
        addDoubleSlider(x, y + lucasSpacing * 3, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_min_speed"),
                0.05D, 4.0D, () -> draft.lucasMinSpeed, value -> draft.lucasMinSpeed = value, DroneConfigScreen::flightSpeedText);
        addDoubleSlider(x, y + lucasSpacing * 4, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_max_speed"),
                0.10D, 6.0D, () -> draft.lucasMaxSpeed, value -> draft.lucasMaxSpeed = value, DroneConfigScreen::flightSpeedText);
        addDoubleSlider(x, y + lucasSpacing * 5, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_acceleration"),
                0.001D, 0.20D, () -> draft.lucasAcceleration, value -> draft.lucasAcceleration = value, DroneConfigScreen::decimalText);
        addDoubleSlider(x, y + lucasSpacing * 6, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_turn_rate"),
                0.05D, 5.0D, () -> draft.lucasTurnRate, value -> draft.lucasTurnRate = value, DroneConfigScreen::decimalText);
        addDoubleSlider(x, y + lucasSpacing * 7, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_drag"),
                0.50D, 0.999D, () -> draft.lucasDrag, value -> draft.lucasDrag = value, DroneConfigUiMath::toPercentLabel);

        addDoubleSlider(rightColumnX, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_climb_speed"),
                0.0D, 1.0D, () -> draft.lucasClimbSpeed, value -> draft.lucasClimbSpeed = value, DroneConfigScreen::decimalText);
        addDoubleSlider(rightColumnX, y + lucasSpacing, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_descend_speed"),
                0.0D, 1.0D, () -> draft.lucasDescendSpeed, value -> draft.lucasDescendSpeed = value, DroneConfigScreen::decimalText);
        addDoubleSlider(rightColumnX, y + lucasSpacing * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_lift_strength"),
                0.0D, 0.20D, () -> draft.lucasLiftStrength, value -> draft.lucasLiftStrength = value, DroneConfigScreen::decimalText);
        addDoubleSlider(rightColumnX, y + lucasSpacing * 3, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_pitch_rate"),
                0.05D, 5.0D, () -> draft.lucasPitchRate, value -> draft.lucasPitchRate = value, DroneConfigScreen::decimalText);
        addDoubleSlider(rightColumnX, y + lucasSpacing * 4, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_roll_visual"),
                0.0D, 90.0D, () -> draft.lucasRollVisualAmount, value -> draft.lucasRollVisualAmount = value, DroneConfigScreen::degreesText);
        addDoubleSlider(rightColumnX, y + lucasSpacing * 5, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_min_lift_speed"),
                0.05D, 6.0D, () -> draft.lucasMinLiftSpeed, value -> draft.lucasMinLiftSpeed = value, DroneConfigScreen::flightSpeedText);
        addDoubleSlider(rightColumnX, y + lucasSpacing * 6, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_stall_gravity"),
                0.0D, 0.20D, () -> draft.lucasStallGravity, value -> draft.lucasStallGravity = value, DroneConfigScreen::decimalText);
        addToggle(rightColumnX, y + lucasSpacing * 7, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_stall_enabled"),
                () -> draft.lucasStallEnabled, value -> draft.lucasStallEnabled = value);
    }

    private void addLucasFuelAndAudioPage(int x, int y, int width) {
        int columnGap = 12;
        int columnWidth = (width - columnGap) / 2;
        int rightColumnX = x + columnWidth + columnGap;
        int lucasSpacing = 19;

        addIntSlider(x, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_max_fuel"),
                100, 10000, () -> draft.lucasMaxFuel, value -> draft.lucasMaxFuel = value, DroneConfigScreen::units);
        addDoubleSlider(x, y + lucasSpacing, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_fuel_idle"),
                0.0D, 2.0D, () -> draft.lucasFuelDrainIdle, value -> draft.lucasFuelDrainIdle = value, DroneConfigScreen::perTickDecimal);
        addDoubleSlider(x, y + lucasSpacing * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_fuel_flying"),
                0.0D, 2.0D, () -> draft.lucasFuelDrainFlying, value -> draft.lucasFuelDrainFlying = value, DroneConfigScreen::perTickDecimal);
        addDoubleSlider(x, y + lucasSpacing * 3, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_fuel_climb"),
                0.0D, 2.0D, () -> draft.lucasFuelDrainClimb, value -> draft.lucasFuelDrainClimb = value, DroneConfigScreen::perTickDecimal);
        addIntSlider(x, y + lucasSpacing * 4, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_low_fuel"),
                0, 100, () -> draft.lucasLowFuelWarningPercent, value -> draft.lucasLowFuelWarningPercent = value, value -> value + "%");
        addToggle(x, y + lucasSpacing * 5, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_can_fly_without_fuel"),
                () -> draft.lucasCanFlyWithoutFuel, value -> draft.lucasCanFlyWithoutFuel = value);

        addDoubleSlider(rightColumnX, y, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_engine_volume"),
                DroneAudioConfigLimits.MIN_ENGINE_VOLUME_MULTIPLIER, DroneAudioConfigLimits.MAX_ENGINE_VOLUME_MULTIPLIER,
                () -> draft.lucasEngineVolumeMultiplier, value -> draft.lucasEngineVolumeMultiplier = value, DroneConfigUiMath::toPercentLabel);
        addToggle(rightColumnX, y + lucasSpacing, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_can_hover"),
                () -> draft.lucasCanHover, value -> draft.lucasCanHover = value);
        addDoubleSlider(rightColumnX, y + lucasSpacing * 2, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.lucas_hud_scale"),
                0.60D, 1.50D, () -> draft.lucasHudScale, value -> draft.lucasHudScale = value, DroneConfigUiMath::toPercentLabel);
        addToggle(rightColumnX, y + lucasSpacing * 3, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.hide_player_on_minimap"),
                () -> draft.hidePlayerOnMinimapDuringDroneControl, value -> draft.hidePlayerOnMinimapDuringDroneControl = value);
        addToggle(rightColumnX, y + lucasSpacing * 4, columnWidth, Component.translatable("screen.sbwdroneconfig.config.label.show_drone_marker"),
                () -> draft.showDroneMarkerInstead, value -> draft.showDroneMarkerInstead = value);
    }

    private void saveAndClose() {
        draft.apply();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable("message.sbwdroneconfig.config_saved"), true);
        }
        onClose();
    }

    private Button addToggle(int x, int y, int width, Component label, BooleanSupplier getter, Consumer<Boolean> setter) {
        final Button[] buttonRef = new Button[1];
        buttonRef[0] = addRenderableWidget(Button.builder(toggleLabel(label, getter.getAsBoolean()), button -> {
                    boolean newValue = !getter.getAsBoolean();
                    setter.accept(newValue);
                    button.setMessage(toggleLabel(label, newValue));
                })
                .pos(x, y)
                .size(width, CONTROL_HEIGHT)
                .build());
        return buttonRef[0];
    }

    private IntSlider addIntSlider(int x, int y, int width, Component label, int min, int max, IntSupplier getter, IntConsumer setter, Function<Integer, String> valueFormatter) {
        return addRenderableWidget(new IntSlider(x, y, width, min, max, getter, setter, value -> valueLabel(label, valueFormatter.apply(value))));
    }

    private DoubleSlider addDoubleSlider(int x, int y, int width, Component label, double min, double max, DoubleSupplier getter, DoubleConsumer setter, Function<Double, String> valueFormatter) {
        return addRenderableWidget(new DoubleSlider(x, y, width, min, max, getter, setter, value -> valueLabel(label, valueFormatter.apply(value))));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        drawPanels(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        drawText(guiGraphics);
    }

    private void drawPanels(GuiGraphics guiGraphics) {
        int left = panelLeft();
        int top = panelTop();
        guiGraphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xE0121511);
        guiGraphics.fill(left + 1, top + 1, left + PANEL_WIDTH - 1, top + PANEL_HEIGHT - 1, 0xF01B201B);
        guiGraphics.fill(left + 10, top + 10, left + SIDEBAR_WIDTH, top + PANEL_HEIGHT - 10, 0xE0212A22);
        guiGraphics.fill(left + SIDEBAR_WIDTH + 6, top + 10, left + PANEL_WIDTH - 10, top + PANEL_HEIGHT - 10, 0xE0161A17);
        guiGraphics.fill(left + SIDEBAR_WIDTH + 6, top + 44, left + PANEL_WIDTH - 10, top + 46, 0xFF5F8F61);
        guiGraphics.fill(left + 12, top + 44, left + SIDEBAR_WIDTH - 2, top + 46, 0xFF3E5840);
    }

    private void drawText(GuiGraphics guiGraphics) {
        int left = panelLeft();
        int top = panelTop();
        int contentX = left + SIDEBAR_WIDTH + CONTENT_MARGIN;
        int contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - (CONTENT_MARGIN * 2);
        Font font = this.font;

        guiGraphics.drawString(font, title, left + 16, top + TITLE_Y_OFFSET, 0xF3F7F1, false);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.config.subtitle"), left + 16, subtitleTop(top), 0x95A997, false);
        guiGraphics.drawString(font, selectedCategory.title(), contentX, top + CATEGORY_TITLE_Y_OFFSET, 0xD6E3D5, false);
        drawPageIndicator(guiGraphics, font, contentX, top, contentWidth);

        int descriptionWidth = pageCount(selectedCategory) > 1 ? contentWidth - 104 : contentWidth - 6;
        List<FormattedCharSequence> descriptionLines = font.split(selectedCategory.description(), descriptionWidth);
        int descriptionY = categoryDescriptionTop(top);
        for (int i = 0; i < Math.min(2, descriptionLines.size()); i++) {
            guiGraphics.drawString(font, descriptionLines.get(i), contentX, descriptionY + (i * 10), 0x8FA291, false);
        }
    }

    private void drawPageIndicator(GuiGraphics guiGraphics, Font font, int contentX, int top, int contentWidth) {
        int pages = pageCount(selectedCategory);
        if (pages <= 1) {
            return;
        }

        Component pageText = Component.translatable("screen.sbwdroneconfig.config.page", categoryPage + 1, pages);
        int pageTextX = contentX + contentWidth - 66;
        guiGraphics.drawString(font, pageText, pageTextX, top + CATEGORY_TITLE_Y_OFFSET + 5, 0xB7C8B8, false);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int panelLeft() {
        return (width - PANEL_WIDTH) / 2;
    }

    private int panelTop() {
        return (height - PANEL_HEIGHT) / 2;
    }

    static int subtitleTop(int panelTop) {
        return panelTop + SUBTITLE_Y_OFFSET;
    }

    static int categoryDescriptionTop(int panelTop) {
        return panelTop + CATEGORY_DESCRIPTION_Y_OFFSET;
    }

    static int contentAreaTop(int panelTop) {
        return panelTop + CONTENT_START_Y_OFFSET;
    }

    static int pageCount(DroneConfigCategory category) {
        return switch (category) {
            case LUCAS_FLIGHT -> 2;
            case JAMMER -> 2;
            case FPV_DRONE -> 2;
            default -> 1;
        };
    }

    private static int clampCategoryPage(DroneConfigCategory category, int page) {
        return Math.max(0, Math.min(page, pageCount(category) - 1));
    }

    private static int sidebarPageCount() {
        int categories = DroneConfigCategory.values().length;
        return Math.max(1, (categories + SIDEBAR_CATEGORY_BUTTONS_PER_PAGE - 1) / SIDEBAR_CATEGORY_BUTTONS_PER_PAGE);
    }

    private static int sidebarPageFor(DroneConfigCategory category) {
        return category.ordinal() / SIDEBAR_CATEGORY_BUTTONS_PER_PAGE;
    }

    private static DroneConfigCategory firstCategoryOnSidebarPage(int page) {
        DroneConfigCategory[] categories = DroneConfigCategory.values();
        int index = Math.min(categories.length - 1, clampSidebarPage(page) * SIDEBAR_CATEGORY_BUTTONS_PER_PAGE);
        return categories[index];
    }

    private static int clampSidebarPage(int page) {
        return Math.max(0, Math.min(page, sidebarPageCount() - 1));
    }

    private static Component toggleLabel(Component label, boolean value) {
        return valueLabel(label, Component.translatable(value ? "options.on" : "options.off").getString());
    }

    private static Component valueLabel(Component label, String value) {
        return Component.translatable("screen.sbwdroneconfig.config.value", label, value);
    }

    private static String blocks(int value) {
        return value + " blocks";
    }

    private static String flightSpeedText(double value) {
        return String.format(Locale.ROOT, "%.2f b/t", value);
    }

    private static String decimalText(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static String degreesText(double value) {
        return String.format(Locale.ROOT, "%.0f deg", value);
    }

    private static String chunks(int value) {
        return value + " chunks";
    }

    private static String ticks(int value) {
        return value + " ticks";
    }

    private static String pixels(int value) {
        return value + " px";
    }

    private static String units(int value) {
        return value + " units";
    }

    private static String perTick(int value) {
        return value + "/tick";
    }

    private static String perTickDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f/tick", value);
    }

    private static String perSecond(int value) {
        return value + "/sec";
    }

    private static String multiplierText(double value) {
        return String.format(Locale.ROOT, "%.2fx", value);
    }

    private static final class IntSlider extends AbstractSliderButton {
        private final int min;
        private final int max;
        private final IntSupplier getter;
        private final IntConsumer setter;
        private final Function<Integer, Component> labelFactory;

        private IntSlider(int x, int y, int width, int min, int max, IntSupplier getter, IntConsumer setter, Function<Integer, Component> labelFactory) {
            super(x, y, width, CONTROL_HEIGHT, Component.empty(), DroneConfigUiMath.normalizedFromInt(getter.getAsInt(), min, max));
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
            this.labelFactory = labelFactory;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(labelFactory.apply(getter.getAsInt()));
        }

        @Override
        protected void applyValue() {
            setter.accept(DroneConfigUiMath.intFromNormalized(value, min, max));
            updateMessage();
        }
    }

    private static final class DoubleSlider extends AbstractSliderButton {
        private final double min;
        private final double max;
        private final DoubleSupplier getter;
        private final DoubleConsumer setter;
        private final Function<Double, Component> labelFactory;

        private DoubleSlider(int x, int y, int width, double min, double max, DoubleSupplier getter, DoubleConsumer setter, Function<Double, Component> labelFactory) {
            super(x, y, width, CONTROL_HEIGHT, Component.empty(), DroneConfigUiMath.normalizedFromDouble(getter.getAsDouble(), min, max));
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
            this.labelFactory = labelFactory;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(labelFactory.apply(getter.getAsDouble()));
        }

        @Override
        protected void applyValue() {
            setter.accept(DroneConfigUiMath.doubleFromNormalized(value, min, max));
            updateMessage();
        }
    }
}
