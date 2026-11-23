package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.util.RenderUtils;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
/** Equivalent affine vertex submission for our imported meshes. Ordinary cubes retain Gecko's path.
 * Bone poses, lighting, UVs, colors, buffer/render types and animation remain owned by GeckoLib.
 */
public interface FastMeshRenderer<T extends GeoAnimatable> extends GeoRenderer<T> {
 @Override default void renderCube(PoseStack pose,GeoCube cube,VertexConsumer consumer,int light,int overlay,float red,float green,float blue,float alpha){
  MeshRenderData data=MeshRenderData.get(cube);
  if(data==null){GeoRenderer.super.renderCube(pose,cube,consumer,light,overlay,red,green,blue,alpha);return;}
  RenderUtils.translateToPivotPoint(pose,cube);RenderUtils.rotateMatrixAroundCube(pose,cube);RenderUtils.translateAwayFromPivotPoint(pose,cube);
  var m=pose.last().pose();var n=pose.last().normal();var v=data.values;
  if(MeshBulkWriter.draw(consumer,data,m,n,light,overlay,red,green,blue,alpha))return;
  for(int at=0;at<v.length;at+=23){
   float a=v[at],b=v[at+1],c=v[at+2];
   float nx=n.m00()*a+n.m10()*b+n.m20()*c,ny=n.m01()*a+n.m11()*b+n.m21()*c,nz=n.m02()*a+n.m12()*b+n.m22()*c;
   if(data.flipX&&nx<0)nx=-nx;if(data.flipY&&ny<0)ny=-ny;if(data.flipZ&&nz<0)nz=-nz;
   for(int i=at+3;i<at+23;i+=5){float x=v[i],y=v[i+1],z=v[i+2];
    float px=m.m00()*x+m.m10()*y+m.m20()*z+m.m30(),py=m.m01()*x+m.m11()*y+m.m21()*z+m.m31(),pz=m.m02()*x+m.m12()*y+m.m22()*z+m.m32();
    consumer.vertex(px,py,pz,red,green,blue,alpha,v[i+3],v[i+4],overlay,light,nx,ny,nz);
   }
  }
 }
}
