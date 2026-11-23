package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;import net.minecraft.client.renderer.*;
public final class Hg01Renderer extends software.bernie.geckolib.renderer.GeoEntityRenderer<Hg01Entity> implements FastMeshRenderer<Hg01Entity>{
 public Hg01Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context c){super(c,new Hg01Model());shadowRadius=.08f;}
 @Override protected void applyRotations(Hg01Entity e,PoseStack p,float age,float yaw,float partial){}
 public RenderType getRenderType(Hg01Entity e,net.minecraft.resources.ResourceLocation texture,MultiBufferSource b,float partial){return RenderType.entityCutoutNoCull(texture);}
 @Override public void renderCube(PoseStack p,software.bernie.geckolib.cache.object.GeoCube c,VertexConsumer v,int light,int overlay,float r,float g,float b,float a){FastMeshRenderer.super.renderCube(p,c,v,light,overlay,r,g,b,a);}
 @Override public void render(Hg01Entity e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){p.pushPose();p.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yaw));if(e.armed()){p.translate(0,.095,0);p.mulPose(com.mojang.math.Axis.XP.rotationDegrees((e.tickCount+partial)*17));p.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((e.tickCount+partial)*7));p.translate(0,-.095,0);}super.render(e,yaw,partial,p,b,light);p.popPose();}
}
