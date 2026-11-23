package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.core.Direction;import net.minecraft.resources.ResourceLocation;import software.bernie.geckolib.model.GeoModel;import software.bernie.geckolib.renderer.GeoBlockRenderer;
public class HxPlatformRenderer extends GeoBlockRenderer<HxPlatformBlockEntity> implements FastMeshRenderer<HxPlatformBlockEntity>{
 private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,s);}
 public HxPlatformRenderer(){super(new GeoModel<>(){
  @Override public ResourceLocation getModelResource(HxPlatformBlockEntity e){return id("geo/hx2_launch_platform.geo.json");}
  @Override public ResourceLocation getTextureResource(HxPlatformBlockEntity e){return id("textures/entity/"+(e.phase()==HxPlatformBlockEntity.Phase.READY?"hx2_launch_platform_ready.png":"hx2_launch_platform.png"));}
  @Override public ResourceLocation getAnimationResource(HxPlatformBlockEntity e){return id("animations/hx2_launch_platform.animation.json");}
  @Override public void setCustomAnimations(HxPlatformBlockEntity e,long instance,software.bernie.geckolib.animation.AnimationState<HxPlatformBlockEntity> s){super.setCustomAnimations(e,instance,s);float partial=s.getPartialTick();if(Boolean.getBoolean("hx2.trace"))getBone("dock_anchor").ifPresent(b->b.setTrackingMatrices(true));getBone("carriage").ifPresent(b->b.setPosZ((float)(16*e.carriage(partial))));getBone("latch_left").ifPresent(b->b.setRotZ((float)Math.toRadians(-28*e.latches(partial))));getBone("latch_right").ifPresent(b->b.setRotZ((float)Math.toRadians(28*e.latches(partial))));}
 });}
 @Override protected void rotateBlock(Direction f,PoseStack p){p.mulPose(Axis.YP.rotationDegrees(180-f.toYRot()));}
 @Override public boolean shouldRenderOffScreen(HxPlatformBlockEntity e){return false;}
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(HxPlatformBlockEntity be){return be.getRenderBoundingBox();}
 @Override public int getViewDistance(){return 128;}
}
