package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.resources.ResourceLocation;
public final class Pm01Model extends software.bernie.geckolib.model.GeoModel<Pm01Entity>{
 private static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,p);}
 public ResourceLocation getModelResource(Pm01Entity e){return id("geo/patriot_missile_pm01.geo.json");}public ResourceLocation getTextureResource(Pm01Entity e){return id("textures/entity/patriot_missile_pm01.png");}public ResourceLocation getAnimationResource(Pm01Entity e){return id("animations/patriot_missile_pm01.animation.json");}
}
