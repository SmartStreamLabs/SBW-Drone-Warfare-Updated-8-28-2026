package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

class SbwDronePolyMeshFactoryTest {
    @Test
    void registersNamespaceScopedPolyMeshFactory() throws Exception {
        Class<?> factoryClass;
        try {
            factoryClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.SbwDronePolyMeshFactory");
        } catch (ClassNotFoundException exception) {
            fail("SbwDronePolyMeshFactory should exist for the restored cubed drone mesh renderer.");
            return;
        }

        Method registerMethod = factoryClass.getMethod("registerForModNamespace");
        Field instanceField = factoryClass.getField("INSTANCE");
        Object factoryInstance = instanceField.get(null);
        Class<?> bakedModelFactoryClass = Class.forName("software.bernie.geckolib.loading.object.BakedModelFactory");
        Method getForNamespaceMethod = bakedModelFactoryClass.getMethod("getForNamespace", String.class);

        registerMethod.invoke(null);

        assertSame(factoryInstance, getForNamespaceMethod.invoke(null, SbwDroneRangeConfig.MOD_ID),
                "The mod namespace should use the custom poly-mesh factory so the cubed drone body can render its GLB mesh.");
    }
}
