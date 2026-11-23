package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.phys.Vec3;import net.minecraft.core.Direction;import software.bernie.geckolib.cache.object.*;import org.joml.Vector3f;import java.io.*;import java.util.*;
/** Cached, merged limb meshes with no reference-player geometry or example-skin material. */
public final class Us01Meshes{
 private static final Map<String,Piece>CACHE=new HashMap<>();public record Mesh(int limb,int optional,GeoCube cube){}public record Piece(List<Mesh>meshes,BakedGeoModel model,int triangles){}
 public static Piece get(String name){return CACHE.computeIfAbsent(name,Us01Meshes::read);}public static void clear(){for(var p:CACHE.values())Adv01Gpu.release(p.model());CACHE.clear();}
 private static Piece read(String name){try(var in=new DataInputStream(Minecraft.getInstance().getResourceManager().getResourceOrThrow(ResourceLocation.tryParse("sbwdroneconfig:us01/"+name+".bin")).open())){
  if(in.readInt()!=0x55533031)throw new IOException("Invalid US01 mesh header");int count=in.readInt();if(count<1||count>12)throw new IOException("Invalid US01 limb count");var meshes=new ArrayList<Mesh>();var roots=new ArrayList<GeoBone>();int triangles=0;
  for(int i=0;i<count;i++){int limb=in.readInt(),optional=in.readInt(),faces=in.readInt();if(limb<0||limb>5||optional<0||optional>3||faces<1||faces>50000)throw new IOException("Invalid US01 mesh part");var quads=new GeoQuad[faces];triangles+=faces;
   for(int f=0;f<faces;f++){var normal=new Vector3f(in.readFloat(),in.readFloat(),in.readFloat());var vertices=new GeoVertex[4];for(int k=0;k<4;k++)vertices[k]=new GeoVertex(in.readFloat(),in.readFloat(),in.readFloat()).withUVs(in.readFloat(),in.readFloat());quads[f]=new GeoQuad(vertices,normal,Direction.getNearest(normal.x,normal.y,normal.z));}
   var cube=new GeoCube(quads,Vec3.ZERO,Vec3.ZERO,new Vec3(1,1,1),0,false);MeshRenderData.register(cube);meshes.add(new Mesh(limb,optional,cube));var bone=new GeoBone(null,"us01_"+name+"_"+limb+"_"+optional,false,0d,false,false);bone.getCubes().add(cube);roots.add(bone);
  }if(in.read()!=-1)throw new IOException("Trailing US01 mesh bytes");return new Piece(List.copyOf(meshes),new BakedGeoModel(roots,null),triangles);
 }catch(IOException e){throw new IllegalStateException("Cannot load US01 REV02 original mesh "+name,e);}}
}
