package nl.smartstreamlabs.sbwdroneconfig;

/** Explicit world-space visual dimensions. Never changes physical explosion power. */
public record ExplosionVfxProfile(float scale, float smokeDensity, int smokeLife, boolean heavy, int emissionTicks) {
    public float fireDiameter() { return scale * 2; }
    public float plumeHeight() { return 8 + scale * 3.2F; }
    public static ExplosionVfxProfile of(float radius, int profile) {
        float r = Float.isFinite(radius) ? Math.max(0, Math.min(64, radius)) : 0;
        // Radius is the unmodified physical power, NOT a visual diameter.
        float diameter = r <= 2 ? 3 + r*1.5F : r <= 6 ? 6 + (r-2)*4.5F
                : r <= 17 ? 24 + (r-6)*24/11F : 48 + Math.min(16,(r-17)*16/41F);
        boolean grenade=profile==4;
        boolean heavy = r >= 6||grenade;
        return new ExplosionVfxProfile(diameter/2, grenade?2.5F:Math.min(2.5F,0.65F+r*0.04F),
                heavy ? 420 : r >= 2 ? 300 : 180, heavy, heavy ? 100 : 60);
    }
}
