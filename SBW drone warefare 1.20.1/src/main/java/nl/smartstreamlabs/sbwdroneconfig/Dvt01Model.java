package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;import com.mojang.blaze3d.vertex.*;import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.*;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.phys.Vec3;import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.*;import org.joml.Vector3f;import java.util.*;import java.io.*;
/** All original meshes/materials/keyframes; one immutable shared model and resident rigid GPU batches. */
public final class Dvt01Model {
 private static Dvt01Model cached;
 private static ResourceLocation id(String s){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,s);}
 private static final ResourceLocation[] TEXTURES={id("textures/block/dvt01_materials.png"),id("textures/block/dvt01_screen_offline_flipped.png"),id("textures/block/dvt01_identification.png"),id("textures/block/dvt01_screen_offline_flipped.png")};
 private static final class Node{final String name;final double[] pivot;final float[] rest=new float[3];final List<Integer> children=new ArrayList<>();final GeoCube[] mesh=new GeoCube[4];int mask;Node(JsonObject o){name=o.get("name").getAsString();pivot=new double[3];for(int i=0;i<3;i++){pivot[i]=o.getAsJsonArray("pivot").get(i).getAsDouble();rest[i]=o.getAsJsonArray("rotation").get(i).getAsFloat();}}}
 private final Node[] nodes;private final BakedGeoModel[] models=new BakedGeoModel[4];private final float[][] angles;
 public static Dvt01Model get(){if(cached==null)cached=new Dvt01Model();return cached;}
 public static void clear(){if(cached!=null)for(var m:cached.models)Adv01Gpu.release(m);cached=null;}
 private Dvt01Model(){
  var resources=Minecraft.getInstance().getResourceManager();
  try(var reader=resources.getResourceOrThrow(id("dvt01/dvt01.json")).openAsReader();var in=new DataInputStream(resources.getResourceOrThrow(id("dvt01/dvt01.bin")).open())){
   var meta=JsonParser.parseReader(reader).getAsJsonObject();var bs=meta.getAsJsonArray("bones");nodes=new Node[bs.size()];angles=new float[nodes.length][3];
   for(int i=0;i<nodes.length;i++){var b=bs.get(i).getAsJsonObject();nodes[i]=new Node(b);int parent=b.get("parent").getAsInt();if(parent>=0)nodes[parent].children.add(i);}
   if(in.readInt()!=0x44565431)throw new IOException("Invalid DVT-01 mesh header");int count=in.readInt();if(count<1||count>200)throw new IOException("Invalid DVT-01 part count");List<List<GeoBone>> groups=new ArrayList<>();for(int i=0;i<4;i++)groups.add(new ArrayList<>());
   for(int n=0;n<count;n++){int bone=in.readInt(),texture=in.readInt(),faces=in.readInt();if(bone<0||bone>=nodes.length||texture<0||texture>=4||faces<1||faces>20000)throw new IOException("Invalid DVT-01 part");var quads=new GeoQuad[faces];
    for(int f=0;f<faces;f++){float x=in.readFloat(),y=in.readFloat(),z=in.readFloat();var vertices=new GeoVertex[4];for(int k=0;k<4;k++)vertices[k]=new GeoVertex(in.readFloat(),in.readFloat(),in.readFloat()).withUVs(in.readFloat(),in.readFloat());quads[f]=new GeoQuad(vertices,new Vector3f(x,y,z),Direction.getNearest(x,y,z));}
    var cube=new GeoCube(quads,Vec3.ZERO,Vec3.ZERO,new Vec3(1,1,1),0,false);MeshRenderData.register(cube);nodes[bone].mesh[texture]=cube;nodes[bone].mask|=1<<texture;var g=new GeoBone(null,nodes[bone].name,false,0d,false,false);g.getCubes().add(cube);groups.get(texture).add(g);
   }
   if(in.read()!=-1)throw new IOException("Trailing DVT-01 mesh data");for(int i=nodes.length-1;i>=0;i--)for(int child:nodes[i].children)nodes[i].mask|=nodes[child].mask;
   for(int t=0;t<4;t++)models[t]=new BakedGeoModel(groups.get(t),null);
  }catch(IOException e){throw new IllegalStateException("Cannot load complete DVT-01 model",e);}
 }

 public void draw(PoseStack poses,MultiBufferSource buffers,int light,int overlay,float closed,float folded,ResourceLocation surface){
  for(int i=0;i<nodes.length;i++){System.arraycopy(nodes[i].rest,0,angles[i],0,3);if(nodes[i].name.equals("Lid"))angles[i][0]-=100*closed;if(nodes[i].name.equals("Antenna"))angles[i][0]-=90*folded;}
  for(int t=0;t<4;t++){var texture=t==3&&surface!=null?surface:TEXTURES[t];var type=t==3?RenderType.entityTranslucentEmissive(texture):RenderType.entityCutoutNoCull(texture);var batch=t!=3&&buffers instanceof MultiBufferSource.BufferSource?Adv01Gpu.batch(models[t]):null;var consumer=batch==null?buffers.getBuffer(type):null;if(batch!=null)batch.begin();walk(0,t,poses,consumer,batch,t==3?15728880:light,overlay,1,1,1,1);if(batch!=null)batch.draw(type,t==3?15728880:light,overlay,1,1,1,1);}
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
