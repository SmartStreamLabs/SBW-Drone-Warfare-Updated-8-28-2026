package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;import com.mojang.blaze3d.vertex.*;import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.*;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.phys.Vec3;import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.*;import org.joml.Vector3f;import java.util.*;import java.io.*;
/** All original meshes/materials/keyframes; one immutable shared model and resident rigid GPU batches. */
public final class Uf01Model {
 private static Uf01Model cached;
 private static ResourceLocation id(String s){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,s);}
 private static final ResourceLocation[] TEXTURES={id("textures/block/uf01_materials.png"),id("textures/block/uf01_flag_cloth.png"),id("textures/block/uf01_plaque.png")};
 private static final class Node{final String name;final double[] pivot;final List<Integer> children=new ArrayList<>();final GeoCube[] mesh=new GeoCube[3];int mask;Node(JsonObject o){name=o.get("name").getAsString();pivot=new double[3];for(int i=0;i<3;i++)pivot[i]=o.getAsJsonArray("pivot").get(i).getAsDouble();}}
 private final Node[] nodes;private final BakedGeoModel[] models=new BakedGeoModel[3];private final float[][][][] clips;private final float[][] angles;private double lastTime=Double.NaN,lastWeather;
 public static Uf01Model get(){if(cached==null)cached=new Uf01Model();return cached;}
 public static void clear(){if(cached!=null)for(var m:cached.models)Adv01Gpu.release(m);cached=null;}
 private Uf01Model(){
  var resources=Minecraft.getInstance().getResourceManager();
  try(var reader=resources.getResourceOrThrow(id("uf01/uf01.json")).openAsReader();var in=new DataInputStream(resources.getResourceOrThrow(id("uf01/uf01.bin")).open())){
   var meta=JsonParser.parseReader(reader).getAsJsonObject();var bs=meta.getAsJsonArray("bones");nodes=new Node[bs.size()];angles=new float[nodes.length][3];clips=new float[2][nodes.length][][];
   for(int i=0;i<nodes.length;i++){var b=bs.get(i).getAsJsonObject();nodes[i]=new Node(b);int parent=b.get("parent").getAsInt();if(parent>=0)nodes[parent].children.add(i);}
   for(int c=0;c<2;c++)for(var track:meta.getAsJsonArray("clips").get(c).getAsJsonObject().getAsJsonArray("tracks")){
    var t=track.getAsJsonObject();int bone=t.get("bone").getAsInt();var keys=t.getAsJsonArray("rotation");float[][] values=new float[33][3];for(int i=0;i<33;i++)for(int a=0;a<3;a++)values[i][a]=keys.get(i).getAsJsonArray().get(a).getAsFloat();clips[c][bone]=values;
   }
   if(in.readInt()!=0x55463031)throw new IOException("Invalid UF-01 mesh header");int count=in.readInt();if(count<1||count>100)throw new IOException("Invalid UF-01 part count");List<List<GeoBone>> groups=new ArrayList<>();for(int i=0;i<3;i++)groups.add(new ArrayList<>());
   for(int n=0;n<count;n++){int bone=in.readInt(),texture=in.readInt(),faces=in.readInt();if(bone<0||bone>=nodes.length||texture<0||texture>=3||faces<1||faces>20000)throw new IOException("Invalid UF-01 part");var quads=new GeoQuad[faces];
    for(int f=0;f<faces;f++){float x=in.readFloat(),y=in.readFloat(),z=in.readFloat();var vertices=new GeoVertex[4];for(int k=0;k<4;k++)vertices[k]=new GeoVertex(in.readFloat(),in.readFloat(),in.readFloat()).withUVs(in.readFloat(),in.readFloat());quads[f]=new GeoQuad(vertices,new Vector3f(x,y,z),Direction.getNearest(x,y,z));}
    var cube=new GeoCube(quads,Vec3.ZERO,Vec3.ZERO,new Vec3(1,1,1),0,false);MeshRenderData.register(cube);nodes[bone].mesh[texture]=cube;nodes[bone].mask|=1<<texture;var g=new GeoBone(null,nodes[bone].name,false,0d,false,false);g.getCubes().add(cube);groups.get(texture).add(g);
   }
   if(in.read()!=-1)throw new IOException("Trailing UF-01 mesh data");for(int i=nodes.length-1;i>=0;i--)for(int child:nodes[i].children)nodes[i].mask|=nodes[child].mask;
   for(int t=0;t<3;t++)models[t]=new BakedGeoModel(groups.get(t),null);
  }catch(IOException e){throw new IllegalStateException("Cannot load complete UF-01 model",e);}
 }
 /** Periodic Catmull-Rom through every supplied 1/8-second key, including its exact loop closure. */
 public static float sample(float[][] keys,double seconds,int axis){double at=(seconds%4+4)%4*8;int i=(int)Math.floor(at);float t=(float)(at-i);float a=keys[(i+31)%32][axis],b=keys[i%32][axis],c=keys[(i+1)%32][axis],d=keys[(i+2)%32][axis];return .5f*((2*b)+(-a+c)*t+(2*a-5*b+4*c-d)*t*t+(-a+3*b-3*c+d)*t*t*t);}
 private void animate(double seconds,float weather){if(seconds==lastTime&&weather==lastWeather)return;lastTime=seconds;lastWeather=weather;for(int i=0;i<nodes.length;i++)for(int axis=0;axis<3;axis++){float a=clips[0][i]==null?0:sample(clips[0][i],seconds,axis),b=clips[1][i]==null?0:sample(clips[1][i],seconds,axis);angles[i][axis]=a+(b-a)*weather;}}
 public void draw(PoseStack poses,MultiBufferSource buffers,int light,int overlay,double seconds,float weather,boolean preview,boolean valid){
  animate(seconds,weather);float r=preview?(valid?.6f:1):1,g=preview?(valid?1:.28f):1,b=preview?(valid?.85f:.28f):1,a=preview?.38f:1;
  for(int t=0;t<3;t++){var type=preview?RenderType.entityTranslucent(TEXTURES[t]):RenderType.entityCutoutNoCull(TEXTURES[t]);var batch=buffers instanceof MultiBufferSource.BufferSource?Adv01Gpu.batch(models[t]):null;var consumer=batch==null?buffers.getBuffer(type):null;if(batch!=null)batch.begin();walk(0,t,poses,consumer,batch,light,overlay,r,g,b,a);if(batch!=null)batch.draw(type,light,overlay,r,g,b,a);}
 }
 private void walk(int index,int texture,PoseStack p,VertexConsumer consumer,Adv01Gpu.Batch batch,int light,int overlay,float r,float g,float b,float a){
  Node n=nodes[index];if((n.mask&(1<<texture))==0)return;p.pushPose();float[] rot=angles[index];
  if(rot[0]!=0||rot[1]!=0||rot[2]!=0){p.translate(n.pivot[0],n.pivot[1],n.pivot[2]);p.mulPose(new org.joml.Quaternionf().rotationZYX((float)Math.toRadians(rot[2]),(float)Math.toRadians(rot[1]),(float)Math.toRadians(rot[0])));p.translate(-n.pivot[0],-n.pivot[1],-n.pivot[2]);}
  if(n.mesh[texture]!=null){if(batch!=null)batch.pose(n.mesh[texture],p);else emit(p,consumer,MeshRenderData.get(n.mesh[texture]),light,overlay,r,g,b,a);}
  for(int child:n.children)walk(child,texture,p,consumer,batch,light,overlay,r,g,b,a);p.popPose();
 }
 private static void emit(PoseStack poses,VertexConsumer consumer,MeshRenderData data,int light,int overlay,float red,float green,float blue,float alpha){
  var m=poses.last().pose();var n=poses.last().normal();var v=data.values;
  if(MeshBulkWriter.draw(consumer,data,m,n,light,overlay,red,green,blue,alpha))return;
  for(int at=0;at<v.length;at+=23){float a=v[at],b=v[at+1],c=v[at+2];float nx=n.m00()*a+n.m10()*b+n.m20()*c,ny=n.m01()*a+n.m11()*b+n.m21()*c,nz=n.m02()*a+n.m12()*b+n.m22()*c;for(int i=at+3;i<at+23;i+=5){float x=v[i],y=v[i+1],z=v[i+2];float px=m.m00()*x+m.m10()*y+m.m20()*z+m.m30(),py=m.m01()*x+m.m11()*y+m.m21()*z+m.m31(),pz=m.m02()*x+m.m12()*y+m.m22()*z+m.m32();consumer.vertex(px,py,pz,red,green,blue,alpha,v[i+3],v[i+4],overlay,light,nx,ny,nz);}}
 }
}
