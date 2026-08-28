package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class LucasDroneDefinitionTest {
    @Test
    void exposesStableIdsAndAssetLocations() throws Exception {
        Class<?> definitionClass;
        try {
            definitionClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.LucasDroneDefinition");
        } catch (ClassNotFoundException exception) {
            fail("LucasDroneDefinition should exist once the LUCAS drone variant is implemented.");
            return;
        }

        Field idField = definitionClass.getField("ID");
        Field displayNameField = definitionClass.getField("DISPLAY_NAME");
        Method modelMethod = definitionClass.getMethod("model");
        Method textureMethod = definitionClass.getMethod("texture");
        Method animationMethod = definitionClass.getMethod("animation");

        assertEquals("lucas_drone", idField.get(null));
        assertEquals("LUCAS Drone", displayNameField.get(null));
        assertEquals(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "geo/lucas_drone.geo.json"), modelMethod.invoke(null));
        assertEquals(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "textures/entity/lucas_drone.png"), textureMethod.invoke(null));
        assertEquals(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "animations/fpv_drone.animation.json"), animationMethod.invoke(null));
    }
}
