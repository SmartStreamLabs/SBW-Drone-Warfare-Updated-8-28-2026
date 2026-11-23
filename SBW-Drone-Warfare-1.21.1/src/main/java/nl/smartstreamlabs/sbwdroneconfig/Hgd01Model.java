package nl.smartstreamlabs.sbwdroneconfig;
import software.bernie.geckolib.model.GeoModel;import software.bernie.geckolib.animation.AnimationState;import net.minecraft.resources.ResourceLocation;import net.minecraft.util.Mth;
public final class Hgd01Model extends GeoModel<Hgd01Entity> {
 private final double[] shoePose=new double[3];
 private static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,p);}
 public ResourceLocation getModelResource(Hgd01Entity e){return id("geo/heavy_ground_drone_hgd01.geo.json");}public ResourceLocation getTextureResource(Hgd01Entity e){return id("textures/entity/heavy_ground_drone_hgd01.png");}public ResourceLocation getAnimationResource(Hgd01Entity e){return id("animations/heavy_ground_drone_hgd01.animation.json");}
 @Override public void setCustomAnimations(Hgd01Entity e,long instance,AnimationState<Hgd01Entity> state){super.setCustomAnimations(e,instance,state);float p=state.getPartialTick(),yaw=Mth.rotLerp(p,e.getTurretYRotO(),e.getTurretYRot())*Mth.DEG_TO_RAD,pitch=-Mth.lerp(p,e.getTurretXRotO(),e.getTurretXRot())*Mth.DEG_TO_RAD;
  getBone("turret_yaw").ifPresent(b->b.setRotY(yaw));for(String n:new String[]{"gun_pitch","laser_left_pitch","laser_right_pitch","sensor_pitch"})getBone(n).ifPresent(b->b.setRotX(pitch));getBone("sensor_yaw").ifPresent(b->b.setRotY(0));getBone("rear_ramp").ifPresent(b->b.setRotX(e.ramp(p)*100*Mth.DEG_TO_RAD));
  for(String n:new String[]{"roof_hatch_left","roof_hatch_right"})getBone(n).ifPresent(b->b.setRotZ((n.endsWith("left")?1:-1)*e.ramp(p)*100*Mth.DEG_TO_RAD));
  boolean detail=net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(e.position())<64*64;for(int side=0;side<2;side++)for(int i=0;i<92;i++){Hgd01TrackPath.pose(i,detail?e.trackTravel(side==0,p):0,shoePose);var b=getBone(Hgd01TrackPath.bone(side,i));if(b.isPresent()){b.get().setPosY((float)shoePose[0]);b.get().setPosZ((float)shoePose[1]);b.get().setRotX((float)shoePose[2]);}}
  for(String side:new String[]{"left","right"}){float rot=(float)(-e.trackTravel(side.equals("left"),p)/.55);for(int i=1;i<=6;i++)getBone("roadwheel_"+side+"_"+i).ifPresent(b->b.setRotX(rot));for(String n:new String[]{"front_idler_","rear_idler_"})getBone(n+side).ifPresent(b->b.setRotX(rot*.7f));}
 }
}
