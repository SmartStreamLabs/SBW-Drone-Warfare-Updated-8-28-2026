package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticCableDamageSourceTest {
    @Test
    void segmentEntityExposesAttackableDamageableHitboxes() throws Exception {
        Path entitySource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticCableSegmentEntity.java"
        );
        String source = Files.readString(entitySource, StandardCharsets.UTF_8);

        assertTrue(source.contains("public boolean isAttackable()"),
                "Fiber cable segments should override isAttackable for melee targeting.");
        assertTrue(source.contains("public float getPickRadius()"),
                "Fiber cable segments should expose an explicit pick radius so SBW hitscan and melee traces can acquire the cable reliably.");
        assertTrue(source.contains("skipAttackInteraction"),
                "Fiber cable segments should short-circuit owner melee attacks before the cable ever takes damage.");
        assertTrue(source.contains("resolveResponsibleCableAttacker"),
                "Fiber cable damage checks should resolve the real responsible attacker so projectile protection can distinguish live FPV control from intentional cable-cutting.");
        assertTrue(
                Pattern.compile("public boolean canBeCollidedWith\\s*\\(\\s*\\)\\s*\\{\\s*return false;", Pattern.DOTALL).matcher(source).find(),
                "Fiber cable segments should stay non-solid so touching the cable does not physically collide the drone or its owner."
        );
        assertTrue(source.contains("protected AABB makeBoundingBox()"),
                "Fiber cable segments should build a real axis-aligned hitbox around the visible cable piece.");
        assertTrue(source.contains("this.segmentDirection != null"),
                "Fiber cable segment direction fallback must be null-safe during entity construction.");
        assertTrue(source.contains("fiberCableCanBeDamagedByProjectiles"),
                "Projectile damage config should still gate cable damage.");
        assertTrue(
                Pattern.compile(
                        "if \\(directEntity instanceof Projectile\\) \\{\\s*if \\(drone != null && FiberOpticLinkSystem\\.isProtectedProjectileCableBreaker\\(level, drone, responsibleAttacker\\)\\) \\{\\s*return false;\\s*\\}\\s*return responsibleAttacker != null && AddonConfig\\.fiberCableCanBeDamagedByProjectiles\\(\\);\\s*\\}",
                        Pattern.DOTALL
                ).matcher(source).find(),
                "Projectile cable damage should still reject ownerless shots, but it must allow real shots from a player who is no longer actively controlling the linked monitor."
        );
        assertTrue(source.contains("breakCableFromSegment"),
                "Destroying a cable segment should still sever the fiber link.");
        assertTrue(source.contains("recordDamageContext("),
                "Fiber cable segments should record the last accepted damage context so runtime sever logs can identify the real cause.");
        assertTrue(source.contains("describeLastDamageContext()"),
                "Fiber cable segments should expose the recorded damage context for sever diagnostics.");
        assertTrue(
                Pattern.compile(
                        "if \\(source\\.is\\(DamageTypeTags\\.IS_EXPLOSION\\)\\) \\{.*return AddonConfig\\.fiberCableCanBeDamagedByExplosions\\(\\);\\s*\\}.*if \\(directEntity instanceof Projectile\\) \\{\\s*if \\(drone != null && FiberOpticLinkSystem\\.isProtectedProjectileCableBreaker\\(level, drone, responsibleAttacker\\)\\) \\{\\s*return false;\\s*\\}\\s*return responsibleAttacker != null && AddonConfig\\.fiberCableCanBeDamagedByProjectiles\\(\\);\\s*\\}.*if \\(attacker instanceof Player\\) \\{\\s*return AddonConfig\\.fiberCableCanBeDamagedByPlayers\\(\\);\\s*\\}.*return false;",
                        Pattern.DOTALL
                ).matcher(source).find(),
                "Fiber cable segments should keep the same explosion and melee gates while handling projectile protection with a dedicated live-controller check."
        );
    }

    @Test
    void linkSystemIncludesProjectileFallbackBreakChecks() throws Exception {
        Path linkSystemSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticLinkSystem.java"
        );
        String source = Files.readString(linkSystemSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("fiberCableProjectileRaycastBreak()"),
                "The fiber link system should consult the projectile fallback break config.");
        assertTrue(source.contains("Projectile"),
                "The fiber link system should inspect projectile entities as a backup cable-cut path.");
        assertTrue(source.contains("expandTowards"),
                "Projectile fallback should consider projectile movement, not only the current position.");
        assertTrue(source.contains("isProtectedProjectileCableBreaker"),
                "Projectile fallback should use the projectile-specific protection path so ex-controllers can still intentionally sever the cable after leaving the monitor.");
        assertTrue(source.contains("segment.describeLastDamageContext()"),
                "The fiber sever path should log the recorded last-damage context so runtime logs reveal the exact cable breaker.");
    }
}
