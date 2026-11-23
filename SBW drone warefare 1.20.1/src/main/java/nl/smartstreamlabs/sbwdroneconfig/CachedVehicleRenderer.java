package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;import net.minecraft.client.renderer.*;import software.bernie.geckolib.cache.object.*;
/** Shared resident rigid mesh path. Gecko still computes every bone/animation pose.
 * Different materials and bone-local light passes retain their original CPU route. */
public class CachedVehicleRenderer<T extends com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity> extends com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer<T> implements FastMeshRenderer<T>{
 private record Pass(Adv01Gpu.Batch batch,RenderType type,int light){}private Pass pass;
 public CachedVehicleRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context,software.bernie.geckolib.model.GeoModel<T> model){super(context,model);}
 @Override public void renderCube(PoseStack p,GeoCube cube,VertexConsumer consumer,int light,int overlay,float r,float g,float b,float a){if(pass!=null){pass.batch().pose(cube,p);return;}FastMeshRenderer.super.renderCube(p,cube,consumer,light,overlay,r,g,b,a);}
 @Override public void actuallyRender(PoseStack p,T entity,BakedGeoModel model,RenderType type,MultiBufferSource buffers,VertexConsumer consumer,boolean rerender,float partial,int light,int overlay,float r,float g,float b,float a){
  var previous=pass;var batch=!rerender&&type!=null&&(buffers instanceof MultiBufferSource.BufferSource&&consumer instanceof BufferBuilder||WhiteHotRender.gpuSource(buffers))?Adv01Gpu.batchGrouped(model):null;pass=batch==null?null:new Pass(batch,type,light);
  if(batch!=null)batch.begin();try{super.actuallyRender(p,entity,model,type,buffers,consumer,rerender,partial,light,overlay,r,g,b,a);if(batch!=null){WhiteHotRender.captureGpu(buffers,batch,type,a);batch.draw(type,light,overlay,r,g,b,a);}}finally{pass=previous;}
 }
 @Override public void renderRecursively(PoseStack p,T entity,GeoBone bone,RenderType type,MultiBufferSource buffers,VertexConsumer consumer,boolean rerender,float partial,int light,int overlay,float r,float g,float b,float a){var previous=pass;if(pass!=null&&(pass.type()!=type||pass.light()!=light))pass=null;try{super.renderRecursively(p,entity,bone,type,buffers,consumer,rerender,partial,light,overlay,r,g,b,a);}finally{pass=previous;}}
}
