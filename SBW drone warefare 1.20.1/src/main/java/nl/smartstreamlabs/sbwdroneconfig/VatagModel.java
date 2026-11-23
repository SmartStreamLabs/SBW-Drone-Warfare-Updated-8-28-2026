package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.resources.ResourceLocation;import net.minecraft.util.Mth;import software.bernie.geckolib.model.GeoModel;import software.bernie.geckolib.core.animation.AnimationState;
public class VatagModel extends GeoModel<VatagEntity>{
 private static ResourceLocation id(String s){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,s);}
 public ResourceLocation getModelResource(VatagEntity e){return id("geo/vatag_ugv_v01.geo.json");}public ResourceLocation getTextureResource(VatagEntity e){return id("textures/entity/vatag_ugv_v01.png");}public ResourceLocation getAnimationResource(VatagEntity e){return id("animations/vatag_ugv_v01.animation.json");}
 @Override public void setCustomAnimations(VatagEntity e,long instance,AnimationState<VatagEntity> a){super.setCustomAnimations(e,instance,a);float p=a.getPartialTick();
  getBone("turret_yaw").ifPresent(b->b.setRotY(Mth.rotLerp(p,e.getTurretYRotO(),e.getTurretYRot())*Mth.DEG_TO_RAD));getBone("gun_pitch").ifPresent(b->b.setRotX(-Mth.lerp(p,e.getTurretXRotO(),e.getTurretXRot())*Mth.DEG_TO_RAD));getBone("gun_recoil").ifPresent(b->b.setPosZ(e.recoil(p)*16));
  for(String side:new String[]{"L","R"}){float roll=Mth.lerp(p,side.equals("L")?e.getLeftWheelRotO():e.getRightWheelRotO(),side.equals("L")?e.getLeftWheelRot():e.getRightWheelRot());for(int i=1;i<=4;i++){final int axle=i;final int wheel=(side.equals("L")?0:4)+i-1;getBone("suspension_"+side+i).ifPresent(b->b.setPosY(e.suspension(wheel,p)*16));getBone("wheel_spin_"+side+i).ifPresent(b->b.setRotX(roll));getBone("steer_"+side+i).ifPresent(b->b.setRotY((axle<=2?1:0)*Mth.lerp(p,e.getRudderRotO(),e.getRudderRot())));}}
 }
}
