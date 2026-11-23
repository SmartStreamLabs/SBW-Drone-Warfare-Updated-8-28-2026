package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;import com.mojang.blaze3d.vertex.*;import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.*;import net.minecraft.core.Direction;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.phys.Vec3;import org.joml.Matrix4f;import org.joml.Matrix3f;import org.joml.Vector3f;import software.bernie.geckolib.cache.object.*;import java.io.*;import java.util.*;

/** Original REV02 meshes and numeric keyframes, loaded once on the render thread.
 * Every texture retains its original UV dimensions; the glass has a separate pass. */
public final class DwfModel{
 private static DwfModel cached,gad;
 public static DwfModel get(){if(cached==null)cached=new DwfModel("dwf01");return cached;}
 public static DwfModel gad(){if(gad==null)gad=new DwfModel("dwf01_gad");return gad;}
 public static void clear(){if(cached!=null)cached.release();if(gad!=null)gad.release();cached=gad=null;}
 private void release(){for(var band:lamps)for(var part:band)DwfStaticGpu.release(part.cube());if(frozenCubes!=null)for(var cube:frozenCubes)DwfStaticGpu.release(cube);for(var m:models)Adv01Gpu.release(m);if(frozenModels!=null)for(var m:frozenModels)Adv01Gpu.release(m);}
 private record Bone(String name,int parent,Vector3f pivot,Vector3f rest){}
 private record Part(int bone,int texture,GeoCube cube){}
 private record Track(int bone,String channel,float[][] points){}
 private final List<Part>[]lamps=new List[]{new ArrayList<>(),new ArrayList<>(),new ArrayList<>()};private Matrix4f[]restPose;
 private final List<Bone>bones=new ArrayList<>();private final List<Part>parts=new ArrayList<>();
 private final List<List<Track>> animations=new ArrayList<>();private BakedGeoModel[]models,frozenModels;private GeoCube[]frozenCubes;
 private ResourceLocation[]textures;private final int workpiece;
 private final String folder;
 private ResourceLocation resource(String path){return ResourceLocation.tryParse("sbwdroneconfig:"+folder+"/"+path);}
 private DwfModel(String folder){this.folder=folder;
  var resources=Minecraft.getInstance().getResourceManager();
  try(var reader=new InputStreamReader(resources.getResourceOrThrow(resource("dwf01.json")).open(),java.nio.charset.StandardCharsets.UTF_8)){
   var json=JsonParser.parseReader(reader).getAsJsonObject();int anchor=-1;
   for(var value:json.getAsJsonArray("bones")){var b=value.getAsJsonObject();String name=b.get("name").getAsString();if(name.equals("Workpiece_Anchor"))anchor=bones.size();bones.add(new Bone(name,b.get("parent").getAsInt(),vec(b.getAsJsonArray("pivot")),vec(b.getAsJsonArray("rotation"))));}workpiece=anchor;
   for(var value:json.getAsJsonArray("animations")){
    var tracks=new ArrayList<Track>();for(var v:value.getAsJsonObject().getAsJsonArray("tracks")){var t=v.getAsJsonObject();int bone=t.get("bone").getAsInt();for(var c:t.getAsJsonObject("channels").entrySet()){var rows=c.getValue().getAsJsonArray();float[][]data=new float[rows.size()][5];for(int i=0;i<data.length;i++)for(int k=0;k<rows.get(i).getAsJsonArray().size();k++)data[i][k]=rows.get(i).getAsJsonArray().get(k).getAsFloat();tracks.add(new Track(bone,c.getKey(),data));}}animations.add(List.copyOf(tracks));
   }
   var descriptions=json.getAsJsonArray("parts");int textureCount=0;for(var d:descriptions)textureCount=Math.max(textureCount,d.getAsJsonObject().get("texture").getAsInt()+1);models=new BakedGeoModel[textureCount];textures=new ResourceLocation[textureCount];List<GeoBone>[]groups=new List[textureCount];for(int i=0;i<textureCount;i++)groups[i]=new ArrayList<>();
   try(var in=new DataInputStream(new BufferedInputStream(resources.getResourceOrThrow(resource("dwf01.bin")).open(),65536))){
    if(in.readInt()!=0x44454631||in.readInt()!=descriptions.size())throw new IOException("DWF mesh header");
    for(int n=0;n<descriptions.size();n++){
     var desc=descriptions.get(n).getAsJsonObject();int bone=desc.get("bone").getAsInt(),texture=desc.get("texture").getAsInt(),count=in.readInt();if(count<0||count>100000)throw new IOException("DWF mesh count");var quads=new GeoQuad[count];
     for(int f=0;f<count;f++){var normal=new Vector3f(in.readFloat(),in.readFloat(),in.readFloat());var vertices=new GeoVertex[4];for(int i=0;i<4;i++)vertices[i]=new GeoVertex(in.readFloat(),in.readFloat(),in.readFloat()).withUVs(in.readFloat(),in.readFloat());quads[f]=new GeoQuad(vertices,normal,Direction.getNearest(normal.x,normal.y,normal.z));}
     var cube=new GeoCube(quads,Vec3.ZERO,Vec3.ZERO,new Vec3(1,1,1),0,false);MeshRenderData.register(cube);parts.add(new Part(bone,texture,cube));if(bones.get(bone).name().equals("Lighting")){for(int band=0;band<3;band++){float lo=4.95f+band*.13f,hi=5.06f+band*.13f;var selected=new ArrayList<GeoQuad>();for(var q:quads){boolean inside=true;for(var v:q.vertices())if(v.position().y()<lo-.001f||v.position().y()>hi+.001f||v.position().x()<3.4f)inside=false;if(inside)selected.add(q);}if(!selected.isEmpty()){var lens=new GeoCube(selected.toArray(GeoQuad[]::new),Vec3.ZERO,Vec3.ZERO,new Vec3(1,1,1),0,false);MeshRenderData.register(lens);lamps[band].add(new Part(bone,texture,lens));}}}var node=new GeoBone(null,bones.get(bone).name(),false,0d,false,false);node.getCubes().add(cube);groups[texture].add(node);
    }if(in.read()!=-1)throw new IOException("DWF trailing mesh bytes");
   }
   for(int i=0;i<models.length;i++){models[i]=new BakedGeoModel(groups[i],null);textures[i]=resource("texture_"+i+".png");}bakeFrozen();
  }catch(IOException failure){throw new IllegalStateException("Cannot load DWF-01 REV02",failure);}
 }
 /** Flatten the original rest pose ONCE, retaining every original visible face,
  * UV and material. Far and idle machines share one rigid part per material. */
 private void bakeFrozen(){
  var matrices=restPose();frozenModels=new BakedGeoModel[models.length];frozenCubes=new GeoCube[models.length];
  for(int texture=0;texture<models.length;texture++){var quads=new ArrayList<GeoQuad>();
   for(var part:parts)if(part.texture()==texture&&!bones.get(part.bone()).name().startsWith("Stored_Output_")){var matrix=matrices[part.bone()];if(Math.abs(matrix.determinant())<1e-10f)continue;var normals=new Matrix3f(matrix);for(var q:part.cube().quads()){if(q==null)continue;var normal=normals.transform(new Vector3f(q.normal())).normalize();var vertices=new GeoVertex[4];for(int n=0;n<4;n++){var v=q.vertices()[n];var point=matrix.transformPosition(new Vector3f(v.position()));vertices[n]=new GeoVertex(point.x,point.y,point.z).withUVs(v.texU(),v.texV());}quads.add(new GeoQuad(vertices,normal,Direction.getNearest(normal.x,normal.y,normal.z)));}}
   var cube=new GeoCube(quads.toArray(GeoQuad[]::new),Vec3.ZERO,Vec3.ZERO,new Vec3(1,1,1),0,false);frozenCubes[texture]=cube;MeshRenderData.register(cube);var bone=new GeoBone(null,"DWF_static_"+texture,false,0d,false,false);bone.getCubes().add(cube);frozenModels[texture]=new BakedGeoModel(List.of(bone),null);
  }
 }
 private void drawFrozen(PoseStack poses,MultiBufferSource buffers,int light,int overlay,boolean glass){
  for(int texture=0;texture<models.length;texture++){if((texture==3)!=glass)continue;var cube=frozenCubes[texture];if(cube.quads().length==0)continue;var type=glass?DwfRenderTypes.glass(textures[texture]):RenderType.entityCutoutNoCull(textures[texture]);if(!glass&&buffers instanceof MultiBufferSource.BufferSource&&DwfStaticGpu.draw(cube,type,poses,light,overlay))continue;var batch=!glass&&buffers instanceof MultiBufferSource.BufferSource?Adv01Gpu.batch(frozenModels[texture]):null;if(batch!=null){batch.begin();batch.pose(cube,poses);batch.draw(type,light,overlay,1,1,1,1);}else emit(poses,buffers.getBuffer(type),MeshRenderData.get(cube),light,overlay);}
 }
 private static Vector3f vec(JsonArray values){return new Vector3f(values.get(0).getAsFloat(),values.get(1).getAsFloat(),values.get(2).getAsFloat());}
 public static final class PoseBuffer{
  private final Vector3f[]rotations,translations,scales;private final Matrix4f[]result;
  private PoseBuffer(int size){rotations=new Vector3f[size];translations=new Vector3f[size];scales=new Vector3f[size];result=new Matrix4f[size];for(int i=0;i<size;i++){rotations[i]=new Vector3f();translations[i]=new Vector3f();scales[i]=new Vector3f(1);result[i]=new Matrix4f();}}
 }
 public PoseBuffer poseBuffer(){return new PoseBuffer(bones.size());}
 public Matrix4f[]restPose(){if(restPose==null)restPose=poses(poseBuffer(),0,0,0);return restPose;}
 public void lamp(PoseStack poses,MultiBufferSource buffers,Matrix4f[]matrices,DwfVisualState.Lamp lamp,int overlay){if(lamp.band()<0||lamp.brightness()<.5f)return;for(var part:lamps[lamp.band()]){poses.pushPose();poses.last().pose().mul(matrices[part.bone()]);poses.last().normal().mul(new Matrix3f(matrices[part.bone()]));var type=RenderType.eyes(textures[part.texture()]);if(!(buffers instanceof MultiBufferSource.BufferSource)||!DwfStaticGpu.emissive(part.cube(),type,poses,overlay))emit(poses,buffers.getBuffer(RenderType.eyes(textures[part.texture()])),MeshRenderData.get(part.cube()),LightTexture.FULL_BRIGHT,overlay);poses.popPose();}}
 public Matrix4f[] poses(PoseBuffer buffer,float seconds,float inputLid,float outputLid){
  var rotations=buffer.rotations;var translations=buffer.translations;for(int i=0;i<bones.size();i++){rotations[i].set(bones.get(i).rest());translations[i].zero();buffer.scales[i].set(1,1,1);}
  apply(animations.get(0),Math.max(0,Math.min(12,seconds)),rotations,translations,buffer.scales);apply(animations.get(1),Math.max(0,Math.min(.7f,inputLid*.7f)),rotations,translations,buffer.scales);apply(animations.get(2),Math.max(0,Math.min(.7f,outputLid*.7f)),rotations,translations,buffer.scales);
  Matrix4f[]result=buffer.result;for(int i=0;i<result.length;i++){var b=bones.get(i);var r=rotations[i];var p=b.pivot();if(b.parent()<0)result[i].identity();else result[i].set(result[b.parent()]);result[i].translate(translations[i]).translate(p).rotateZYX((float)Math.toRadians(r.z),(float)Math.toRadians(r.y),(float)Math.toRadians(r.x)).scale(buffer.scales[i]).translate(-p.x,-p.y,-p.z);}return result;
 }
 private static void apply(List<Track>tracks,float time,Vector3f[]rotations,Vector3f[]positions,Vector3f[]scales){
  for(var track:tracks){var points=track.points();if(points.length==0)continue;float[]a=points[0],b=a;for(var point:points){if(point[0]<=time)a=point;if(point[0]>=time){b=point;break;}b=point;}float mix=b[0]<=a[0]?0:Math.max(0,Math.min(1,(time-a[0])/(b[0]-a[0])));if(a.length>4&&a[4]>0)mix=0;float x=a[1]+(b[1]-a[1])*mix,y=a[2]+(b[2]-a[2])*mix,z=a[3]+(b[3]-a[3])*mix;if(track.channel().equals("rotation"))rotations[track.bone()].add(x,y,z);else if(track.channel().equals("scale"))scales[track.bone()].set(x,y,z);else if(track.channel().equals("position"))positions[track.bone()].add(x/16,y/16,z/16);}
 }
 public void workpiece(PoseStack poses,Matrix4f[]matrices){if(workpiece>=0){poses.last().pose().mul(matrices[workpiece]);poses.translate(bones.get(workpiece).pivot().x,bones.get(workpiece).pivot().y,bones.get(workpiece).pivot().z);}}
 public void draw(PoseStack poses,MultiBufferSource buffers,int light,int overlay,Matrix4f[]matrices,boolean glass){
  if(matrices==restPose()){drawFrozen(poses,buffers,light,overlay,glass);return;}
  for(int texture=0;texture<models.length;texture++){
   if((texture==3)!=glass)continue;var type=glass?DwfRenderTypes.glass(textures[texture]):RenderType.entityCutoutNoCull(textures[texture]);var batch=!glass&&buffers instanceof MultiBufferSource.BufferSource?Adv01Gpu.batch(models[texture]):null;if(batch!=null)batch.begin();
   for(var part:parts)if(part.texture()==texture&&!bones.get(part.bone()).name().startsWith("Stored_Output_")){poses.pushPose();poses.last().pose().mul(matrices[part.bone()]);poses.last().normal().mul(new Matrix3f(matrices[part.bone()]));if(batch!=null)batch.pose(part.cube(),poses);else emit(poses,buffers.getBuffer(type),MeshRenderData.get(part.cube()),light,overlay);poses.popPose();}
   if(batch!=null)batch.draw(type,light,overlay,1,1,1,1);
  }
 }
 private static void emit(PoseStack poses,VertexConsumer consumer,MeshRenderData data,int light,int overlay){if(MeshBulkWriter.draw(consumer,data,poses.last().pose(),poses.last().normal(),light,overlay,1,1,1,1))return;var m=poses.last().pose();var n=poses.last().normal();var v=data.values;for(int at=0;at<v.length;at+=23){var normal=n.transform(new Vector3f(v[at],v[at+1],v[at+2]));for(int i=at+3;i<at+23;i+=5){var p=m.transformPosition(new Vector3f(v[i],v[i+1],v[i+2]));consumer.vertex(p.x,p.y,p.z,1,1,1,1,v[i+3],v[i+4],overlay,light,normal.x,normal.y,normal.z);}}}
}
