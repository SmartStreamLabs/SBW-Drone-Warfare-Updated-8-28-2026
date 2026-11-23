package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryDroneHudOverlaySourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");

    @Test
    void deliveryDroneUsesDedicatedLogisticsHudInsteadOfCubedFpvOsd() throws Exception {
        String hud = Files.readString(MAIN.resolve("DeliveryDroneHudOverlay.java"));
        String context = Files.readString(MAIN.resolve("DroneClientViewContext.java"));
        String cubedOverlay = Files.readString(MAIN.resolve("CubedFpvReferenceGuiOverlay.java"));
        String cubedTelemetry = Files.readString(MAIN.resolve("CubedFpvReferenceGuiTelemetry.java"));
        String gogglesOverlay = Files.readString(MAIN.resolve("FpvGogglesOverlayClient.java"));
        String mixin = Files.readString(MAIN.resolve("mixin/DroneHudOverlayMixin.java"));
        String bootstrap = Files.readString(MAIN.resolve("SbwDroneRangeConfig.java"));

        assertTrue(hud.contains("class DeliveryDroneHudOverlay"));
        assertTrue(hud.contains("HUD_ID = \"DELIVERY-HUD-BALANCED-A3\""));
        assertTrue(hud.contains("DeliveryDroneEntity drone"));
        assertTrue(hud.contains("DroneClientViewContext.activeDeliveryDrone"));
        assertTrue(hud.contains("DroneClientViewContext.isLocalDeliveryViewOf"));
        assertTrue(hud.contains("RenderGuiEvent.Post"));
        assertTrue(hud.contains("RenderGuiOverlayEvent.Pre"));
        assertTrue(hud.contains("RenderHandEvent"));
        assertTrue(hud.contains("\"DELIVERY DRONE\""));
        assertTrue(hud.contains("\"CARGO BAY\""));
        assertTrue(hud.contains("\"EMPTY\""));
        assertTrue(hud.contains("\"LINK\""));
        assertTrue(hud.contains("\"POWER\""));
        assertTrue(hud.contains("\"SPD\""));
        assertTrue(hud.contains("\"ALT\""));
        assertTrue(hud.contains("\"V/S\""));
        assertTrue(hud.contains("\"HDG\""));
        assertTrue(hud.contains("\"DROP ZONE\""));
        assertTrue(hud.contains("\"NO DESTINATION\""));
        assertTrue(hud.contains("\"SET DELIVERY TARGET\""));
        assertTrue(hud.contains("\"LOW POWER - RETURN ADVISED\""));
        assertTrue(hud.contains("CARDINAL_LABELS"));
        assertTrue(hud.contains("renderTitle"));
        assertTrue(hud.contains("renderHeadingCompass"));
        assertTrue(hud.contains("renderArtificialHorizon"));
        assertTrue(hud.contains("renderFlightStrip"));
        assertTrue(hud.contains("renderPowerLinkStrip"));
        assertTrue(hud.contains("renderCargoBay"));
        assertTrue(hud.contains("renderMissionStrip"));
        assertTrue(hud.contains("renderDestinationMarker"));
        assertTrue(hud.contains("renderFrameDetails"));
        assertTrue(hud.contains("renderRouteStageLine"));
        assertTrue(hud.contains("renderCargoSegments"));
        assertTrue(hud.contains("renderBattery"));
        assertTrue(hud.contains("drawRotatedLine"));
        assertTrue(hud.contains("smoothAngle"));
        assertTrue(hud.contains("getMountedCargoOccupiedSlots()"));
        assertTrue(hud.contains("getSyncedDeliveryTarget()"));
        assertTrue(hud.contains("DroneEnergyClient.getBatteryPercentFor"));
        assertTrue(hud.contains("DroneJamOverlayClient.getJamProgressFor"));
        assertTrue(hud.contains("DroneFiberOpticClient.isFiberHudActiveFor"));
        assertTrue(context.contains("entity.getType() == AddonEntities.DELIVERY_DRONE.get()"));
        assertTrue(gogglesOverlay.contains("DroneClientViewContext.isActiveDeliveryView()"));

        assertFalse(hud.contains("\"FPV LINK\""));
        assertFalse(hud.contains("\"CUBED FPV\""));
        assertFalse(hud.contains("\"C U B E D   F P V\""));
        assertFalse(hud.contains("\"ARM\""));
        assertFalse(hud.contains("\"ACRO\""));
        assertFalse(hud.contains("\"WEAPON\""));
        assertFalse(hud.contains("\"AMMO\""));
        assertFalse(hud.contains("\"TARGET LOCK\""));
        assertFalse(hud.contains("\"LINK AND POWER\""));
        assertFalse(hud.contains("\"FLIGHT DATA\""));
        assertFalse(hud.contains("\"DESTINATION\""));
        assertFalse(hud.contains("\"ROUTE\""));
        assertFalse(hud.contains("\"CRATE: \""));
        assertFalse(hud.contains("\"STATUS: \""));
        assertFalse(hud.contains("\"NEXT: SET TARGET\""));
        assertFalse(hud.contains("\"CONTROL: MANUAL\""));
        assertFalse(hud.contains("\"BEARING: --\""));
        assertFalse(hud.contains("drawPanel"));
        assertFalse(hud.contains("renderSubtleGrid"));
        assertFalse(hud.contains("LucasDroneEntity"));
        assertFalse(hud.contains("CubedFpvReferenceGuiTelemetry"));

        assertTrue(cubedOverlay.contains("!(drone instanceof DeliveryDroneEntity)"));
        assertTrue(cubedTelemetry.contains("drone instanceof DeliveryDroneEntity"));
        assertTrue(mixin.contains("DeliveryDroneHudOverlay.shouldReplaceDefaultSbwHud()"));
        assertTrue(bootstrap.contains("DeliveryDroneHudOverlay.init(modBus);"));
    }

    @Test
    void deliveryDroneSyncsOnlyRealHudDataNeededByTheClient() throws Exception {
        String entity = Files.readString(MAIN.resolve("DeliveryDroneEntity.java"));

        assertTrue(entity.contains("MOUNTED_CRATE_OCCUPIED_SLOTS"));
        assertTrue(entity.contains("HAS_SYNCED_DELIVERY_TARGET"));
        assertTrue(entity.contains("getMountedCargoOccupiedSlots()"));
        assertTrue(entity.contains("getMountedCargoSlotCapacity()"));
        assertTrue(entity.contains("getSyncedDeliveryTarget()"));
        assertTrue(entity.contains("syncDeliveryTarget(target)"));
        assertTrue(entity.contains("countOccupiedCrateSlots(mountedCrateData)"));
        assertTrue(entity.contains("MOUNTED_CRATE_SLOT_CAPACITY = 27"));
    }
}
