package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.resources.ResourceLocation;import software.bernie.geckolib.model.GeoModel;import software.bernie.geckolib.core.animation.AnimationState;
public final class Gad01Model extends GeoModel<Gad01Entity>{
 private static ResourceLocation id(String p){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,p);}
 public ResourceLocation getModelResource(Gad01Entity e){return id("geo/ground_assault_drone_gad01.geo.json");}public ResourceLocation getTextureResource(Gad01Entity e){return id("textures/entity/ground_assault_drone_gad01.png");}public ResourceLocation getAnimationResource(Gad01Entity e){return id("animations/ground_assault_drone_gad01.animation.json");}
 @Override public void setCustomAnimations(Gad01Entity e,long id,AnimationState<Gad01Entity> state){super.setCustomAnimations(e,id,state);for(String side:new String[]{"left","right"})for(String axle:new String[]{"front","rear"})getBone("wheel_"+side+"_"+axle).ifPresent(b->b.setRotX((float)(-e.wheelTravel(side.equals("left"),state.getPartialTick())/.2456264)));}
}
