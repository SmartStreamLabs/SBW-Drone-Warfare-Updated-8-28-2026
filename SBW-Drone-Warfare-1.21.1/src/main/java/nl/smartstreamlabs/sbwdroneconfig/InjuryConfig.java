package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

public final class InjuryConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED, PROJECTILES, EXPLOSIONS, IMPACTS, BEFORE_DEATH, SELF_SEVERE;
    public static final ModConfigSpec.DoubleValue LIGHT, SEVERE, REPEATED, BLEED_LIGHT, BLEED_HEAVY, CRAWL, HEAL;
    public static final ModConfigSpec.IntValue WINDOW, BLEED_INTERVAL, BANDAGE, MEDKIT, SELF_MEDKIT, RECOVERY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXTRA, EXCLUDED;
    static {
        var b = ConfigSchema.builder();
        ENABLED = b.comment("Server authority. Disabling releases movement restrictions and clears medical state on login/online players.").define("enabled", true);
        b.push("damageSources");
        PROJECTILES = b.define("projectiles", true);
        EXPLOSIONS = b.define("explosions", true);
        IMPACTS = b.define("droneImpacts", true);
        EXTRA = b.comment("Additional exact damage-type registry IDs. Datapack tags sbwdroneconfig:injury_sources and injury_excluded are supported.").defineListAllowEmpty("additionalTypes", List.of(), v -> v instanceof String);
        EXCLUDED = b.defineListAllowEmpty("excludedTypes", List.of(), v -> v instanceof String);
        b.pop().push("severity");
        LIGHT = b.comment("Actual health damage after armor, resistance and absorption, in health points (2 = one heart).").defineInRange("lightHit", 2d, .1, 1000);
        SEVERE = b.defineInRange("severeHit", 10d, .1, 1000);
        REPEATED = b.defineInRange("accumulatedSevere", 14d, .1, 1000);
        WINDOW = b.defineInRange("accumulationWindowTicks", 200, 1, 72000);
        BEFORE_DEATH = b.comment("Once per life, an eligible fatal combat hit can leave 2 health points. Never when already downed; never void/kill/bypass-invulnerability. A held totem retains its normal priority.").define("downBeforeDeath", true);
        b.pop().push("bleeding");
        BLEED_LIGHT = b.defineInRange("lightDamage", .25, 0, 20);
        BLEED_HEAVY = b.defineInRange("heavyDamage", .75, 0, 20);
        BLEED_INTERVAL = b.comment("One periodic damage mechanism only; existing bleeding pulses do not interrupt their own treatment.").defineInRange("intervalTicks", 60, 10, 1200);
        b.pop().push("movement");
        CRAWL = b.comment("Maximum horizontal movement per server tick in blocks, including a bounded packet jitter allowance. Normal predicted crawl is slower than this limit.").defineInRange("crawlBlocksPerTick", .06, .01, .12);
        RECOVERY = b.defineInRange("recoveryTicks", 100, 0, 72000);
        b.pop().push("treatment");
        BANDAGE = b.defineInRange("bandageTicks", 80, 10, 7200);
        MEDKIT = b.defineInRange("assistedMedkitTicks", 160, 10, 7200);
        SELF_MEDKIT = b.defineInRange("selfMedkitTicks", 280, 10, 7200);
        SELF_SEVERE = b.comment("Keep enabled for independent singleplayer recovery.").define("selfTreatSevere", true);
        HEAL = b.defineInRange("medkitHealthRestored", 6d, 0, 1000);
        b.pop();
        SPEC = b.build();
    }
    private InjuryConfig() {}
}
