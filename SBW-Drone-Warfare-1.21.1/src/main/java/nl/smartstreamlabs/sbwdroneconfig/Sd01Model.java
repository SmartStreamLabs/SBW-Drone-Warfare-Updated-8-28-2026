package nl.smartstreamlabs.sbwdroneconfig;
import software.bernie.geckolib.model.GeoModel;import software.bernie.geckolib.animation.AnimationState;import net.minecraft.resources.ResourceLocation;
/** No controller plays the cosmetic reference loop: only the actual dive/stabilisation pose moves the fins. */
public final class Sd01Model extends GeoModel<Sd01Entity>{
 private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,s);}
 private static final ResourceLocation MODEL=id("geo/submarine_drone_sd01.geo.json"),TEXTURE=id("textures/entity/submarine_drone_sd01.png"),ANIMATION=id("animations/submarine_drone_sd01.animation.json");
 public ResourceLocation getModelResource(Sd01Entity e){return MODEL;}public ResourceLocation getTextureResource(Sd01Entity e){return TEXTURE;}public ResourceLocation getAnimationResource(Sd01Entity e){return ANIMATION;}
 @Override public void setCustomAnimations(Sd01Entity e,long instance,AnimationState<Sd01Entity> state){super.setCustomAnimations(e,instance,state);for(String n:new String[]{"Tailplanes_Left","Tailplanes_Right"})getBone(n).ifPresent(b->b.setRotX(-e.fin(state.getPartialTick())));}
}
