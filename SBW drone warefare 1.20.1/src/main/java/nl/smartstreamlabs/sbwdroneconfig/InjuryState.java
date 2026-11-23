package nl.smartstreamlabs.sbwdroneconfig;

/** Medical state is independent of the vanilla health pool. All durations are remaining online ticks. */
public final class InjuryState {
    public enum Stage { HEALTHY, LIGHT, SEVERE, STABILIZED, RECOVERING }
    public Stage stage = Stage.HEALTHY;
    public int bleeding, recovery, bleedClock, traumaWindow;
    public float trauma;
    public boolean rescueUsed;
    public boolean downed() { return stage == Stage.SEVERE || stage == Stage.STABILIZED; }
    public boolean active() { return stage != Stage.HEALTHY || bleeding > 0; }

    public void hit(float damage, float light, float severe, float repeated, int window, boolean saved) {
        hit(damage, damage, light, severe, repeated, window, saved);
    }
    public void hit(float damage, float impactTotal, float light, float severe, float repeated, int window, boolean saved) {
        if (!(damage > 0) || !Float.isFinite(damage)) return;
        if (traumaWindow <= 0) trauma = 0;
        trauma = Math.min(10000, trauma + damage);
        traumaWindow = window;
        boolean heavy = saved || downed() || impactTotal >= severe || trauma >= repeated;
        if (heavy) {
            stage = Stage.SEVERE;
            bleeding = 2;
            recovery = 0;
        } else if (damage >= light || trauma >= light * 2) {
            stage = Stage.LIGHT;
            bleeding = Math.max(bleeding, 1);
            recovery = 0;
        }
        if (saved) rescueUsed = true;
    }
    public boolean bandage(int recoveryTicks) {
        if (!active() || bleeding == 0 && downed()) return false;
        bleeding = bleedClock = 0;
        if (downed()) stage = Stage.STABILIZED;
        else { stage = Stage.RECOVERING; recovery = recoveryTicks; }
        return true;
    }
    public boolean medkit(int recoveryTicks) {
        if (!active()) return false;
        bleeding = bleedClock = 0;
        trauma = 0;
        traumaWindow = 0;
        stage = Stage.RECOVERING;
        recovery = recoveryTicks;
        return true;
    }
    public void tick() {
        if (traumaWindow > 0 && --traumaWindow == 0) trauma = 0;
        if (stage == Stage.RECOVERING && (recovery <= 0 || --recovery <= 0)) stage = Stage.HEALTHY;
    }
    public void normalize() {
        bleeding = Math.max(0, Math.min(2, bleeding));
        recovery = Math.max(0, Math.min(72000, recovery));
        traumaWindow = Math.max(0, Math.min(72000, traumaWindow));
        if (!Float.isFinite(trauma)) trauma = 0;
        trauma = Math.max(0, Math.min(10000, trauma));
        bleedClock = Math.max(0, Math.min(1200, bleedClock));
        if (stage == Stage.STABILIZED && bleeding != 0) stage = Stage.SEVERE;
        if (stage == Stage.SEVERE && bleeding == 0) stage = Stage.STABILIZED;
    }
}
