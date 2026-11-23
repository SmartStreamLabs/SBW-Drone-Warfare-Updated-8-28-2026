package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WhiteHotMaterialsTest {
    @Test void regularEntityTexturesAreCapturedAcrossBothSerializations(){
        assertTrue(WhiteHotMaterials.capture("entity_cutout_no_cull:CompositeState[texture[minecraft:pig.png]]"));
        assertTrue(WhiteHotMaterials.capture("RenderType[entity_translucent:CompositeState[texture[sbwdroneconfig:vehicle.png]]]"));
        assertTrue(WhiteHotMaterials.capture("lightning"));
    }
    @Test void groundShadowsAndInvisibleWaterMasksNeverBecomeHeat(){
        assertFalse(WhiteHotMaterials.capture("RenderType[entity_shadow:CompositeState[texture[minecraft:shadow.png]]]"));
        assertFalse(WhiteHotMaterials.capture("entity_shadow"));
        assertFalse(WhiteHotMaterials.capture("RenderType[water_mask:CompositeState[]]"));
    }
    @Test void nameplatesAndDebugBoxesNeverBecomeBodyGeometry(){
        assertFalse(WhiteHotMaterials.capture("RenderType[text:CompositeState[texture[font]]]"));
        assertFalse(WhiteHotMaterials.capture("RenderType[lines:CompositeState[]]"));
        assertFalse(WhiteHotMaterials.capture("RenderType[entity_glint:CompositeState[]]"));
        assertFalse(WhiteHotMaterials.capture("RenderType[outline:CompositeState[]]"));
    }
}
