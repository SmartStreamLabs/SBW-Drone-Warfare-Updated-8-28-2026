package nl.smartstreamlabs.sbwdroneconfig;

/**
 * Restored tower defaults. Keeping them together prevents this block from
 * inheriting the regular jammer's short-range limits.
 */
public final class RadarJammerTowerSettings {
    public static final int MAX_BATTERY_CAPACITY = 24_000;
    public static final int SOLAR_CHARGE_RATE = 25;
    public static final int CHARGING_STATION_CHARGE_RATE = 100;
    public static final int JAMMER_ENERGY_DRAIN_RATE = 4;
    public static final int MINIMUM_ENERGY_TO_ACTIVATE = 100;

    private RadarJammerTowerSettings() {
    }
}
