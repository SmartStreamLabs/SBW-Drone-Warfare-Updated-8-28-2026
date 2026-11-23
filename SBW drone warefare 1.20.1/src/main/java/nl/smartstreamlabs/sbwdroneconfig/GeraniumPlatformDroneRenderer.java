package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.client.*;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.entity.EntityRendererProvider;import net.minecraft.resources.ResourceLocation;import software.bernie.geckolib.renderer.GeoEntityRenderer;import software.bernie.geckolib.model.GeoModel;
/** Direct geometry path: no item display context, GUI scale, fixed transform or legacy body offset. */
public final class GeraniumPlatformDroneRenderer extends CachedEntityRenderer<Geranium2Entity> {
 private static ResourceLocation id(String p){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,p);}
 public GeraniumPlatformDroneRenderer(EntityRendererProvider.Context context){super(context,new GeoModel<>(){
  @Override public ResourceLocation getModelResource(Geranium2Entity e){return id("geo/lp02_geranium_reference.geo.json");}
  @Override public ResourceLocation getTextureResource(Geranium2Entity e){return id("textures/item/geranium_2.png");}
  @Override public ResourceLocation getAnimationResource(Geranium2Entity e){return id("animations/lp02_geranium_reference.animation.json");}
  @Override public void setCustomAnimations(Geranium2Entity e,long instance,software.bernie.geckolib.core.animation.AnimationState<Geranium2Entity> state){super.setCustomAnimations(e,instance,state);getBone("lp02_rotor").ifPresent(b->b.setRotZ((float)Math.toRadians(((e.tickCount+state.getPartialTick())*82)%360)));}
 });shadowRadius=.9F;}
 @Override protected void applyRotations(Geranium2Entity e,PoseStack p,float age,float yaw,float partial){}
 @Override public void render(Geranium2Entity e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){
  if(DroneClientViewContext.isLocalFpvViewOf(e) || (SbwCompat.isUsingLinkedMonitorForDrone(Minecraft.getInstance().player,e) && Minecraft.getInstance().options.getCameraType()==CameraType.FIRST_PERSON))return;
  p.pushPose();p.mulPose(Axis.YP.rotationDegrees(-e.getYaw(partial)));p.mulPose(Axis.XP.rotationDegrees(net.minecraft.util.Mth.lerp(partial,e.xRotO,e.getXRot())));p.mulPose(Axis.ZP.rotationDegrees(-e.getRoll(partial)));p.mulPose(Axis.YP.rotationDegrees(180));p.scale(-1,1,1);p.translate(0,-.01,0);super.render(e,yaw,partial,p,buffers,light);p.popPose();
 }
}
