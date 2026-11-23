package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.*;import com.mojang.math.Axis;import net.minecraft.client.*;import net.minecraft.client.renderer.*;import net.minecraft.core.Direction;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.phys.Vec3;import org.joml.Vector3f;import software.bernie.geckolib.cache.object.*;import java.io.*;import java.util.*;

/** Original textured meshes, pre-baked once into fixed/door/lamp batches. No per-frame model construction. */

public final class Ps01Model{

 private static Ps01Model cached;private final GeoCube[][] cubes=new GeoCube[4][4];private final BakedGeoModel[][] models=new BakedGeoModel[4][4];private static final ResourceLocation[]TEXTURES=new ResourceLocation[4];static{for(int i=0;i<4;i++)TEXTURES[i]=ResourceLocation.tryParse("sbwdroneconfig:ps01/ps01_"+i+".png");}

 public static Ps01Model get(){if(cached==null)cached=new Ps01Model();return cached;}public static void clear(){if(cached!=null)for(var row:cached.models)for(var m:row)if(m!=null)Adv01Gpu.release(m);cached=null;}

 private Ps01Model(){try(var in=new DataInputStream(Minecraft.getInstance().getResourceManager().getResourceOrThrow(ResourceLocation.tryParse("sbwdroneconfig:ps01/ps01.bin")).open())){if(in.readInt()!=0x50533031)throw new IOException("PS-01 mesh header");int count=in.readInt();if(count<1||count>16)throw new IOException("PS-01 batch count");for(int n=0;n<count;n++){int group=in.readInt(),texture=in.readInt(),faces=in.readInt();if(group<0||group>3||texture<0||texture>3||faces<1||faces>30000)throw new IOException("PS-01 mesh bounds");var quads=new GeoQuad[faces];for(int f=0;f<faces;f++){float x=in.readFloat(),y=in.readFloat(),z=in.readFloat();var vertices=new GeoVertex[4];for(int k=0;k<4;k++)vertices[k]=new GeoVertex(in.readFloat(),in.readFloat(),in.readFloat()).withUVs(in.readFloat(),in.readFloat());quads[f]=new GeoQuad(vertices,new Vector3f(x,y,z),Direction.getNearest(x,y,z));}var cube=new GeoCube(quads,Vec3.ZERO,Vec3.ZERO,new Vec3(1,1,1),0,false);MeshRenderData.register(cube);cubes[group][texture]=cube;var bone=new GeoBone(null,"ps01_"+group+"_"+texture,false,0d,false,false);bone.getCubes().add(cube);models[group][texture]=new BakedGeoModel(List.of(bone),null);}if(in.read()!=-1)throw new IOException("PS-01 trailing mesh bytes");}catch(IOException e){throw new IllegalStateException("Cannot load complete PS-01",e);}}

 public void draw(PoseStack p,MultiBufferSource buffers,int light,int insideLight,int overlay,float progress,boolean doorGone,boolean lamps,float integrity,float doorIntegrity,boolean preview,boolean valid){float r=preview?(valid?.5f:1):1,g=preview?(valid?1:.25f):1,b=preview?(valid?.65f:.25f):1,a=preview?.35f:1;

  for(int group=0;group<4;group++){if(group==1&&doorGone)continue;p.pushPose();if(group==1){p.translate(Ps01Geometry.HINGE[0],Ps01Geometry.HINGE[1],Ps01Geometry.HINGE[2]);p.mulPose(Axis.YP.rotationDegrees(Ps01Geometry.angle(progress)));p.translate(-Ps01Geometry.HINGE[0],-Ps01Geometry.HINGE[1],-Ps01Geometry.HINGE[2]);}for(int texture=0;texture<4;texture++)if(cubes[group][texture]!=null){var type=preview?RenderType.entityTranslucent(TEXTURES[texture]):RenderType.entityCutoutNoCull(TEXTURES[texture]);draw(group,texture,p,buffers,type,group==2&&lamps?0xf000f0:group==3?insideLight:light,overlay,r,g,b,a);if(!preview&&((group==0||group==3)&&texture==1&&integrity<.95f||group==1&&doorIntegrity<.95f)){int stage=Math.max(0,Math.min(9,(int)((1-(group==1?doorIntegrity:integrity))*10)));var crack=ResourceLocation.tryParse("minecraft:textures/block/destroy_stage_"+stage+".png");draw(group,texture,p,buffers,RenderType.entityDecal(crack),light,overlay,1,1,1,.75f);}}p.popPose();}

 }

 private void draw(int group,int texture,PoseStack p,MultiBufferSource buffers,RenderType type,int light,int overlay,float r,float g,float b,float a){var cube=cubes[group][texture];var batch=buffers instanceof MultiBufferSource.BufferSource?Adv01Gpu.batch(models[group][texture]):null;if(batch!=null){batch.begin();batch.pose(cube,p);batch.draw(type,light,overlay,r,g,b,a);}else emit(p,buffers.getBuffer(type),MeshRenderData.get(cube),light,overlay,r,g,b,a);}

 private static void emit(PoseStack poses,VertexConsumer consumer,MeshRenderData data,int light,int overlay,float red,float green,float blue,float alpha){var m=poses.last().pose();var n=poses.last().normal();var v=data.values;for(int at=0;at<v.length;at+=23){float a=v[at],b=v[at+1],c=v[at+2];float nx=n.m00()*a+n.m10()*b+n.m20()*c,ny=n.m01()*a+n.m11()*b+n.m21()*c,nz=n.m02()*a+n.m12()*b+n.m22()*c;for(int i=at+3;i<at+23;i+=5){float x=v[i],y=v[i+1],z=v[i+2];float px=m.m00()*x+m.m10()*y+m.m20()*z+m.m30(),py=m.m01()*x+m.m11()*y+m.m21()*z+m.m31(),pz=m.m02()*x+m.m12()*y+m.m22()*z+m.m32();consumer.addVertex(px,py,pz,net.minecraft.util.FastColor.ARGB32.color((int)(alpha*255),(int)(red*255),(int)(green*255),(int)(blue*255)),v[i+3],v[i+4],overlay,light,nx,ny,nz);}}}

}

