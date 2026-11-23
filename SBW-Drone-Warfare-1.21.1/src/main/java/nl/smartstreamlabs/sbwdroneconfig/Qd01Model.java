package nl.smartstreamlabs.sbwdroneconfig;
import software.bernie.geckolib.model.GeoModel;import software.bernie.geckolib.animation.AnimationState;import net.minecraft.resources.ResourceLocation;import net.minecraft.util.Mth;
public class Qd01Model extends GeoModel<Qd01Entity> {
 private static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,p);}
 private static final ResourceLocation MODEL=id("geo/quadruped_drone_qd01.geo.json"),STANDBY=id("textures/entity/quadruped_drone_qd01.png"),LINKED=id("textures/entity/quadruped_drone_qd01_linked.png"),ANIMATION=id("animations/quadruped_drone_qd01.animation.json");
 public ResourceLocation getModelResource(Qd01Entity e){return MODEL;}public ResourceLocation getTextureResource(Qd01Entity e){return e.remotelyControlled()?LINKED:STANDBY;}public ResourceLocation getAnimationResource(Qd01Entity e){return ANIMATION;}
 @Override public void setCustomAnimations(Qd01Entity e,long instance,AnimationState<Qd01Entity> state){super.setCustomAnimations(e,instance,state);float p=state.getPartialTick();
  var m=net.minecraft.client.Minecraft.getInstance();boolean detail=m.player!=null&&m.player.distanceToSqr(e)<48*48;e.gait().update(e,detail);
  for(int i=0;i<4;i++){final int k=i;String leg=Qd01Ground.LEGS[i];getBone("hip_roll_"+leg).ifPresent(b->b.setRotZ(e.gait().angle(k,0,p)));getBone("thigh_"+leg).ifPresent(b->b.setRotX(e.gait().angle(k,1,p)));getBone("shin_"+leg).ifPresent(b->b.setRotX(e.gait().angle(k,2,p)));getBone("foot_"+leg).ifPresent(b->b.setRotX(e.gait().angle(k,3,p)));getBone("foot_contact_"+leg).ifPresent(b->b.setTrackingMatrices(true));}
  getBone("turret_yaw").ifPresent(b->b.setRotY(Mth.rotLerp(p,e.getTurretYRotO(),e.getTurretYRot())*Mth.DEG_TO_RAD));
  getBone("turret_pitch").ifPresent(b->b.setRotX(-Mth.lerp(p,e.getTurretXRotO(),e.getTurretXRot())*Mth.DEG_TO_RAD));
  for(String anchor:new String[]{"muzzle_anchor","body_camera_anchor","aim_camera_anchor"})getBone(anchor).ifPresent(b->b.setTrackingMatrices(true));
 }
}
