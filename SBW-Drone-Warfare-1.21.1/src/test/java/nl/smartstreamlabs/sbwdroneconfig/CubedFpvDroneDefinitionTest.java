package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class CubedFpvDroneDefinitionTest {
    @Test
    void exposesStableIdsAndAssetLocations() throws Exception {
        Class<?> definitionClass;
        try {
            definitionClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.CubedFpvDroneDefinition");
        } catch (ClassNotFoundException exception) {
            fail("CubedFpvDroneDefinition should exist once the new drone variant is implemented.");
            return;
        }

        Field idField = definitionClass.getField("ID");
        Field displayNameField = definitionClass.getField("DISPLAY_NAME");
        Method modelMethod = definitionClass.getMethod("model");
        Method textureMethod = definitionClass.getMethod("texture");
        Method animationMethod = definitionClass.getMethod("animation");

        assertEquals("cubed_fpv_drone", idField.get(null));
        assertEquals("FPV Drone", displayNameField.get(null));
        assertEquals(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "geo/cubed_fpv_drone.geo.json"), modelMethod.invoke(null));
        assertEquals(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "textures/entity/cubed_fpv_drone.png"), textureMethod.invoke(null));
        assertEquals(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "animations/fpv_drone.animation.json"), animationMethod.invoke(null));
    }
}
