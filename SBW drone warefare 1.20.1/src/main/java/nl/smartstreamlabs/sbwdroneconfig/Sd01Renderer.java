package nl.smartstreamlabs.sbwdroneconfig;
public class Sd01Renderer extends CachedVehicleRenderer<Sd01Entity> {
 public Sd01Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context){super(context,new Sd01Model());shadowRadius=.6f;}
 /** SBW already applies the complete vehicle rotation. Gecko's generic 180-degree living-model basis is inappropriate here. */
 @Override protected void applyRotations(Sd01Entity e,com.mojang.blaze3d.vertex.PoseStack p,float age,float yaw,float partial){}
 @Override public net.minecraft.client.renderer.RenderType getRenderType(Sd01Entity e,net.minecraft.resources.ResourceLocation texture,net.minecraft.client.renderer.MultiBufferSource buffers,float partial){return net.minecraft.client.renderer.RenderType.entityCutoutNoCull(getTextureLocation(e));}
 @Override public void renderCube(com.mojang.blaze3d.vertex.PoseStack pose,software.bernie.geckolib.cache.object.GeoCube cube,com.mojang.blaze3d.vertex.VertexConsumer consumer,int light,int overlay,float red,float green,float blue,float alpha){super.renderCube(pose,cube,consumer,light,overlay,red,green,blue,alpha);}
}
