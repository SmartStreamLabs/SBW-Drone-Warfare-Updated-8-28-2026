package nl.smartstreamlabs.sbwdroneconfig;
public class Qd01Renderer extends CachedVehicleRenderer<Qd01Entity> {
 public Qd01Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context c){super(c,new Qd01Model());shadowRadius=.6f;}
 @Override public void vehicleAxis(Qd01Entity e,com.mojang.blaze3d.vertex.PoseStack p,float yaw,float partial){var frame=e.bodyFrame(partial);var delta=frame.transformPosition(new org.joml.Vector3d()).sub(e.getPosition(partial).x,e.getPosition(partial).y,e.getPosition(partial).z);p.translate(delta.x,delta.y,delta.z);p.mulPose(frame.getUnnormalizedRotation(new org.joml.Quaterniond()).get(new org.joml.Quaternionf()));}
 @Override protected void applyRotations(Qd01Entity e,com.mojang.blaze3d.vertex.PoseStack p,float age,float yaw,float partial,float nativeScale){}
 @Override public net.minecraft.client.renderer.RenderType getRenderType(Qd01Entity e,net.minecraft.resources.ResourceLocation texture,net.minecraft.client.renderer.MultiBufferSource buffers,float partial){return net.minecraft.client.renderer.RenderType.entityCutoutNoCull(getTextureLocation(e));}
 @Override public void renderCube(com.mojang.blaze3d.vertex.PoseStack pose,software.bernie.geckolib.cache.object.GeoCube cube,com.mojang.blaze3d.vertex.VertexConsumer consumer,int light,int overlay,int color){super.renderCube(pose,cube,consumer,light,overlay,color);}
}
