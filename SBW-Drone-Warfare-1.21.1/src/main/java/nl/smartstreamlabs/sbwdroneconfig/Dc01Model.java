package nl.smartstreamlabs.sbwdroneconfig;
import software.bernie.geckolib.model.GeoModel;import software.bernie.geckolib.animation.AnimationState;import net.minecraft.resources.ResourceLocation;import net.minecraft.util.Mth;
public class Dc01Model extends GeoModel<Dc01Entity> {
 private static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,p);}
 public ResourceLocation getModelResource(Dc01Entity e){return id("geo/drone_carrier_dc01.geo.json");}
 public ResourceLocation getTextureResource(Dc01Entity e){return id("textures/entity/drone_carrier_dc01"+(e.readyMask()!=0?"_ready":"")+".png");}
 public ResourceLocation getAnimationResource(Dc01Entity e){return id("animations/drone_carrier_dc01.animation.json");}
 @Override public void setCustomAnimations(Dc01Entity e,long instance,AnimationState<Dc01Entity> state){super.setCustomAnimations(e,instance,state);float p=state.getPartialTick();float steering=Mth.clamp(Mth.lerp(p,e.getRudderRotO(),e.getRudderRot()),-25*Mth.DEG_TO_RAD,25*Mth.DEG_TO_RAD);
  for(String side:new String[]{"l","r"}){float angle=-Mth.lerp(p,side.equals("l")?e.getLeftWheelRotO():e.getRightWheelRotO(),side.equals("l")?e.getLeftWheelRot():e.getRightWheelRot());for(int axle=0;axle<3;axle++){String a=new String[]{"f","m","r"}[axle];int index=(side.equals("l")?0:3)+axle;getBone("wheel_"+a+side).ifPresent(b->b.setRotX(angle));getBone((axle==1?"wheel_":"steer_")+a+side).ifPresent(b->b.setPosY(e.suspension(index,p)*16));if(axle!=1)getBone("steer_"+a+side).ifPresent(b->b.setRotY(a.equals("f")?steering:-steering));}}
  // These small supplied tabs are not an artificial clamp for every airframe.
  // Occupied bays fold them clear; real passenger mounts carry the load.
  for(String side:new String[]{"l","r"})for(String axle:new String[]{"f","r"})getBone("dock_clamp_"+axle+side).ifPresent(b->b.setRotZ(e.mask()==0?0:(side.equals("l")?-1:1)*35*Mth.DEG_TO_RAD));
 }
}
