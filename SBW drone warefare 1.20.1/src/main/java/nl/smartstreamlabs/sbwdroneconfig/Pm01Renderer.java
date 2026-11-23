package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;import net.minecraft.client.renderer.*;import net.minecraft.util.Mth;
public final class Pm01Renderer extends software.bernie.geckolib.renderer.GeoEntityRenderer<Pm01Entity> implements FastMeshRenderer<Pm01Entity>{
 private Adv01Gpu.Batch batch;
 public Pm01Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context c){super(c,new Pm01Model());shadowRadius=.15f;}
 @Override protected void applyRotations(Pm01Entity e,PoseStack p,float age,float yaw,float partial){}
 public RenderType getRenderType(Pm01Entity e,net.minecraft.resources.ResourceLocation t,MultiBufferSource b,float p){return RenderType.entityCutoutNoCull(t);}
 @Override public void renderCube(PoseStack p,software.bernie.geckolib.cache.object.GeoCube c,VertexConsumer v,int light,int overlay,float r,float g,float b,float a){if(batch!=null){batch.pose(c,p);return;}FastMeshRenderer.super.renderCube(p,c,v,light,overlay,r,g,b,a);}
 @Override public void actuallyRender(PoseStack p,Pm01Entity e,software.bernie.geckolib.cache.object.BakedGeoModel model,RenderType type,MultiBufferSource buffers,VertexConsumer consumer,boolean rerender,float partial,int light,int overlay,float r,float g,float b,float a){var previous=batch;var current=!rerender&&type!=null&&(buffers instanceof MultiBufferSource.BufferSource&&consumer instanceof BufferBuilder||WhiteHotRender.gpuSource(buffers))?Adv01Gpu.batch(model):null;batch=current;if(current!=null)current.begin();try{super.actuallyRender(p,e,model,type,buffers,consumer,rerender,partial,light,overlay,r,g,b,a);if(current!=null){WhiteHotRender.captureGpu(buffers,current,type,a);current.draw(type,light,overlay,r,g,b,a);}}finally{batch=previous;}}
 @Override public void render(Pm01Entity e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){p.pushPose();p.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-Mth.rotLerp(partial,e.yRotO,e.getYRot())));p.mulPose(com.mojang.math.Axis.XP.rotationDegrees(Mth.lerp(partial,e.xRotO,e.getXRot())));super.render(e,yaw,partial,p,buffers,light);p.popPose();}
}
