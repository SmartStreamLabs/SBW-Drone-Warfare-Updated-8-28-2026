package nl.smartstreamlabs.sbwdroneconfig;
public class Dc01Renderer extends CachedVehicleRenderer<Dc01Entity> {
 public Dc01Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context){super(context,new Dc01Model());shadowRadius=1.6f;}

 @Override public void vehicleAxis(Dc01Entity e,com.mojang.blaze3d.vertex.PoseStack p,float yaw,float partial){var frame=e.getVehicleTransform(partial);var at=frame.transformPosition(new org.joml.Vector3d()).sub(e.getPosition(partial).x,e.getPosition(partial).y,e.getPosition(partial).z);p.translate(at.x,at.y,at.z);p.mulPose(frame.getUnnormalizedRotation(new org.joml.Quaterniond()).get(new org.joml.Quaternionf()));}
 @Override protected void applyRotations(Dc01Entity e,com.mojang.blaze3d.vertex.PoseStack p,float age,float yaw,float partial){}
 @Override public void renderCube(com.mojang.blaze3d.vertex.PoseStack pose,software.bernie.geckolib.cache.object.GeoCube cube,com.mojang.blaze3d.vertex.VertexConsumer consumer,int light,int overlay,float red,float green,float blue,float alpha){super.renderCube(pose,cube,consumer,light,overlay,red,green,blue,alpha);}
}
