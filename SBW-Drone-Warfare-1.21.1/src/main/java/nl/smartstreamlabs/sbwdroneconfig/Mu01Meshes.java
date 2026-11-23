package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.*;
import org.joml.Vector3f;
import java.io.*;import java.util.*;
/** Immutable cached original meshes. No reference-player geometry, poses or entity instances. */
public final class Mu01Meshes {
 static final Map<String,Piece> CACHE=new HashMap<>();
 public record Mesh(int limb,int texture,GeoCube cube){}
 public record Piece(List<Mesh> meshes,Map<Integer,BakedGeoModel> models,int triangles){}
 static Piece get(String name){return CACHE.computeIfAbsent(name,Mu01Meshes::read);}
 public static void clear(){for(var p:CACHE.values())for(var m:p.models().values())Adv01Gpu.release(m);CACHE.clear();}
 private static Piece read(String name){
  var location=ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"mu01/"+name+".bin");
  try(var in=new DataInputStream(Minecraft.getInstance().getResourceManager().getResourceOrThrow(location).open())){
   if(in.readInt()!=0x4d553031)throw new IOException("Invalid MU-01 mesh header");int count=in.readInt();if(count<1||count>12)throw new IOException("Invalid MU-01 limb count");
   List<Mesh> meshes=new ArrayList<>();Map<Integer,List<GeoBone>> roots=new HashMap<>();int total=0;
   for(int i=0;i<count;i++){
    int limb=in.readInt(),texture=in.readInt(),faces=in.readInt();if(limb<0||limb>5||texture<0||texture>1||faces<1||faces>50000)throw new IOException("Invalid MU-01 limb mesh");GeoQuad[] quads=new GeoQuad[faces];total+=faces;
    for(int q=0;q<faces;q++){
     float nx=in.readFloat(),ny=in.readFloat(),nz=in.readFloat();GeoVertex[] v=new GeoVertex[4];
     for(int at=0;at<4;at++)v[at]=new GeoVertex(in.readFloat(),in.readFloat(),in.readFloat()).withUVs(in.readFloat(),in.readFloat());
     quads[q]=new GeoQuad(v,new Vector3f(nx,ny,nz),Direction.getNearest(nx,ny,nz));
    }
    var cube=new GeoCube(quads,Vec3.ZERO,Vec3.ZERO,new Vec3(1,1,1),0,false);MeshRenderData.register(cube);meshes.add(new Mesh(limb,texture,cube));
    var bone=new GeoBone(null,"mu01_"+name+"_"+limb,false,0d,false,false);bone.getCubes().add(cube);roots.computeIfAbsent(texture,k->new ArrayList<>()).add(bone);
   }
   if(in.read()!=-1)throw new IOException("Trailing MU-01 mesh bytes");Map<Integer,BakedGeoModel> models=new HashMap<>();roots.forEach((k,v)->models.put(k,new BakedGeoModel(v,null)));
   return new Piece(List.copyOf(meshes),Map.copyOf(models),total);
  }catch(IOException e){throw new IllegalStateException("Cannot load original MU-01 mesh "+name,e);}
 }
}
