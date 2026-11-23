package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FpvImpactFragilitySourceTest {
    @Test
    void cubedFpvHasDedicatedImpactFragilitySystem() throws Exception {
        Path systemSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FpvImpactDamageSystem.java"
        );
        String source = Files.readString(systemSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("entity instanceof CubedFpvDroneEntity"),
                "Impact fragility must target only the Cubed FPV drone, not LUCAS or the base SBW drone.");
        assertTrue(source.contains("entity instanceof DeliveryDroneEntity"),
                "The Delivery Drone should not inherit the Cubed FPV crash-fragility path.");
        assertTrue(source.contains("horizontalCollision") || source.contains("verticalCollision"),
                "Impact fragility should only trigger after real block collision flags.");
        assertTrue(source.contains("getDeltaMovement().length()"),
                "The system should capture real pre-impact speed before SBW travel can clamp movement.");
        assertTrue(source.contains("drone.hurt("),
                "A hard FPV collision should apply actual entity damage so it can break normally.");
        assertTrue(source.contains("DESTROY_IMPACT_SPEED"),
                "Very hard FPV collisions should have a direct destroy threshold so the drone no longer feels tanky.");
    }

    @Test
    void droneTravelMixinInvokesImpactFragilityBeforeAndAfterTravel() throws Exception {
        Path mixinSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "mixin",
                "DroneEnergyMixin.java"
        );
        String source = Files.readString(mixinSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("FpvImpactDamageSystem.beforeDroneTravel(drone);"),
                "The mixin should capture FPV speed before the base travel collision logic runs.");
        assertTrue(source.contains("FpvImpactDamageSystem.afterDroneTravel(drone);"),
                "The mixin should convert post-travel collision flags into FPV impact damage.");
    }

    @Test
    void cubedFpvKeepsLowDedicatedHealth() throws Exception {
        Path droneSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "CubedFpvDroneEntity.java"
        );
        String source = Files.readString(droneSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("private static final float MAX_HEALTH = 1.0F"),
                "The Cubed FPV drone should stay fragile instead of inheriting SBW's high vehicle health.");
        assertTrue(source.contains("private static final float COMPONENT_MAX_HEALTH = 1.0F"),
                "The Cubed FPV drone should not inherit SBW's 50-health vehicle subcomponents.");
        assertTrue(source.contains("private static final float INCOMING_DAMAGE_MULTIPLIER = 8.0F"),
                "The Cubed FPV drone should amplify incoming damage so SBW vehicle mitigation cannot keep it tanky.");
        assertTrue(source.contains("public float getMaxHealth()"),
                "The Cubed FPV drone should expose its own low max health.");
        assertTrue(source.contains("public float getTurretMaxHealth()"),
                "The Cubed FPV drone should expose low turret/component health.");
        assertTrue(source.contains("public boolean hurt(DamageSource source, float amount)"),
                "The Cubed FPV drone should route incoming damage through its fragile FPV scaling.");
        assertTrue(source.contains("return MAX_HEALTH;"),
                "The max health override should use the dedicated FPV health constant.");
        assertTrue(source.contains("amount * getIncomingDamageMultiplier()"),
                "The fragile FPV damage multiplier must be applied before SBW's vehicle hurt logic.");
        assertTrue(source.contains("protected float getIncomingDamageMultiplier()"),
                "Delivery-style subclasses should be able to opt out without changing the FPV hurt path.");
        assertTrue(source.contains("clampDurabilityToFpvLimit();"),
                "Spawned or loaded Cubed FPV drones should be clamped back to the low FPV health limit.");
    }

    @Test
    void deliveryDroneUsesDedicatedDurableHealthLimits() throws Exception {
        Path droneSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "DeliveryDroneEntity.java"
        );
        String source = Files.readString(droneSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("private static final float DELIVERY_MAX_HEALTH = 40.0F"),
                "The Delivery Drone should have a much higher body health than the fragile Cubed FPV drone.");
        assertTrue(source.contains("private static final float DELIVERY_COMPONENT_MAX_HEALTH = 20.0F"),
                "The Delivery Drone should have higher component health for cargo use.");
        assertTrue(source.contains("private static final float DELIVERY_INCOMING_DAMAGE_MULTIPLIER = 1.0F"),
                "The Delivery Drone should not inherit the Cubed FPV incoming damage amplification.");
        assertTrue(source.contains("setHealth(getMaxHealth())"),
                "Fresh Delivery Drone entities should initialize the effective durable health, including the server profile.");
        assertTrue(source.contains("protected float getDroneMaxHealthLimit()"));
        assertTrue(source.contains("protected float getDroneComponentMaxHealthLimit()"));
        assertTrue(source.contains("protected float getIncomingDamageMultiplier()"));
    }
}
