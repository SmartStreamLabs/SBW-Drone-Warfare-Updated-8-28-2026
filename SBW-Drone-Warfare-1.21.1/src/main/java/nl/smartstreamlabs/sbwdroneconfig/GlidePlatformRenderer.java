package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
public final class GlidePlatformRenderer extends GeoBlockRenderer<GlidePlatformBlockEntity> implements FastMeshRenderer<GlidePlatformBlockEntity> {
 private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,path);}
 public GlidePlatformRenderer(){super(new GeoModel<>(){
  @Override public ResourceLocation getModelResource(GlidePlatformBlockEntity p){return id("geo/glide_bomb_platform_lp03.geo.json");}
  @Override public ResourceLocation getTextureResource(GlidePlatformBlockEntity p){return id("textures/block/glide_bomb_platform_lp03.png");}
  @Override public ResourceLocation getAnimationResource(GlidePlatformBlockEntity p){return id("animations/glide_bomb_platform_lp03.animation.json");}
  @Override public void setCustomAnimations(GlidePlatformBlockEntity p,long instance,software.bernie.geckolib.animation.AnimationState<GlidePlatformBlockEntity> state){
   super.setCustomAnimations(p,instance,state);float progress=p.openness(state.getPartialTick());for(String name:new String[]{"gate_left","gate_right","clamp_left","clamp_right"})getBone(name).ifPresent(b->{b.setRotX(GlidePlatformClampAnimation.rotation(name,0,progress));b.setRotY(GlidePlatformClampAnimation.rotation(name,1,progress));b.setRotZ(GlidePlatformClampAnimation.rotation(name,2,progress));b.setPosX(-GlidePlatformClampAnimation.position(name,0,progress));b.setPosY(GlidePlatformClampAnimation.position(name,1,progress));b.setPosZ(GlidePlatformClampAnimation.position(name,2,progress));});getBone("safety_cover").ifPresent(b->b.setRotZ(GlidePlatformClampAnimation.cover(1-p.lidClosedAmount(state.getPartialTick()))));
  }
 });}
 @Override protected void rotateBlock(Direction facing,PoseStack pose){pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));pose.translate(1,0,0);}
 @Override public boolean shouldRenderOffScreen(GlidePlatformBlockEntity be){return true;}
 @Override public int getViewDistance(){return 128;}
 @Override public void actuallyRender(PoseStack pose,GlidePlatformBlockEntity be,BakedGeoModel baked,RenderType type,MultiBufferSource buffers,VertexConsumer vertex,boolean rerender,float partial,int light,int overlay,int color){
  if(rerender){super.actuallyRender(pose,be,baked,type,buffers,vertex,true,partial,light,overlay,color);return;}
  pose.pushPose();super.actuallyRender(pose,be,baked,type,buffers,vertex,false,partial,light,overlay,color);pose.popPose();
  var stack=be.previewDrone();if(stack.isEmpty())return;
  var at=GlidePlatformLayout.mount(be.getBlockPos(),be.facing()).add(GlidePlatformLayout.forward(be.facing()).scale(be.previewTravel(partial)*GlidePlatformLayout.LAUNCH_SPEED));
  // Captured preRender origin removes the GeckoLib 4.7-only block-centre translation.
  pose.pushPose();pose.last().pose().set(blockRenderTranslations);pose.translate(at.x-be.getBlockPos().getX(),at.y-be.getBlockPos().getY(),at.z-be.getBlockPos().getZ());GlidePlatformPayloadRenderer.render(stack,be.getLevel(),be.facing().toYRot(),(float)GlidePlatformLayout.MOUNT_PITCH,0,pose,buffers,light);pose.popPose();
 }
}
