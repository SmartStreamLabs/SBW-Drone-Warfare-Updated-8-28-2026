package nl.smartstreamlabs.sbwdroneconfig;
import software.bernie.geckolib.model.GeoModel;import software.bernie.geckolib.core.animation.AnimationState;import net.minecraft.resources.ResourceLocation;import net.minecraft.util.Mth;
public class Wd01Model extends GeoModel<Wd01Entity> {
 private static ResourceLocation id(String p){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,p);}
 private static final ResourceLocation MODEL=id("geo/water_drone_wd01.geo.json"),TEXTURE=id("textures/entity/water_drone_wd01.png"),LINKED=id("textures/entity/water_drone_wd01_linked.png"),ANIMATION=id("animations/water_drone_wd01.animation.json");
 public ResourceLocation getModelResource(Wd01Entity e){return MODEL;}
 public ResourceLocation getTextureResource(Wd01Entity e){return e.remotelyControlled()?LINKED:TEXTURE;}
 public ResourceLocation getAnimationResource(Wd01Entity e){return ANIMATION;}
 @Override public void setCustomAnimations(Wd01Entity e,long instance,AnimationState<Wd01Entity> state){super.setCustomAnimations(e,instance,state);float p=state.getPartialTick();
  getBone("propeller_port").ifPresent(b->b.setRotZ(e.propellerSpin(p)));getBone("propeller_starboard").ifPresent(b->b.setRotZ(-e.propellerSpin(p)));
  float rudder=Mth.clamp(Mth.lerp(p,e.getRudderRotO(),e.getRudderRot()),-.45f,.45f);for(String side:new String[]{"port","starboard"})getBone("rudder_"+side).ifPresent(b->b.setRotY(rudder));
  getBone("camera_yaw").ifPresent(b->b.setRotY(Mth.rotLerp(p,e.getTurretYRotO(),e.getTurretYRot())*Mth.DEG_TO_RAD));
  getBone("camera_pitch").ifPresent(b->b.setRotX(-Mth.lerp(p,e.getTurretXRotO(),e.getTurretXRot())*Mth.DEG_TO_RAD));
 }
}
