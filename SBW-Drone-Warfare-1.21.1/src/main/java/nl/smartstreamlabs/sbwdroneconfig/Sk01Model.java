package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.io.*;import net.minecraft.resources.ResourceLocation;import net.minecraft.client.*;import net.minecraft.client.renderer.*;import com.mojang.blaze3d.vertex.*;import software.bernie.geckolib.cache.object.*;import org.joml.Vector3f;
/** Original posed mesh/UVs, one cached GPU buffer per supplied detail level. */
public final class Sk01Model {
 private record Key(String model,boolean lod){}
 private static final Map<Key,GeoCube>CACHE=new HashMap<>();private static final Set<Key>FAILED=new HashSet<>();
 public static void clear(){for(var cube:CACHE.values())DwfStaticGpu.release(cube);CACHE.clear();FAILED.clear();}
 private static GeoCube load(Key key){var root=ResourceLocation.tryParse(key.model);if(root==null)return null;var path=ResourceLocation.fromNamespaceAndPath(root.getNamespace(),root.getPath()+"/"+(key.lod?"lod":"soldier")+".bin");
  try(var in=new DataInputStream(new BufferedInputStream(Minecraft.getInstance().getResourceManager().getResourceOrThrow(path).open()))){if(in.readInt()!=0x534b3031)throw new IOException("Invalid SK-01 mesh");int n=in.readInt();if(n<1||n>50000)throw new IOException("Invalid SK-01 face count");var quads=new GeoQuad[n];for(int i=0;i<n;i++){var normal=new Vector3f(in.readFloat(),in.readFloat(),in.readFloat());var vertices=new GeoVertex[4];for(int j=0;j<4;j++)vertices[j]=new GeoVertex(in.readFloat(),in.readFloat(),in.readFloat()).withUVs(in.readFloat(),in.readFloat());quads[i]=new GeoQuad(vertices,normal,net.minecraft.core.Direction.getNearest(normal.x,normal.y,normal.z));}if(in.read()!=-1)throw new IOException("Trailing SK-01 mesh data");var cube=new GeoCube(quads,net.minecraft.world.phys.Vec3.ZERO,net.minecraft.world.phys.Vec3.ZERO,new net.minecraft.world.phys.Vec3(1,1,1),0,false);MeshRenderData.register(cube);return cube;
  }catch(IOException ex){SbwDroneRangeConfig.LOGGER.error("Cannot load SK-01 model {}",path,ex);FAILED.add(key);return null;}}
 public static void draw(Sk01Entry e,boolean lod,PoseStack poses,MultiBufferSource buffers,int light){var key=new Key(e.model(),lod);if(FAILED.contains(key))return;var cube=CACHE.computeIfAbsent(key,Sk01Model::load);if(cube==null)return;var root=ResourceLocation.tryParse(e.model());var texture=ResourceLocation.fromNamespaceAndPath(root.getNamespace(),root.getPath()+"/texture.png");var type=RenderType.entityCutoutNoCull(texture);if(DwfStaticGpu.draw(cube,type,poses,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY))return;
  var out=buffers.getBuffer(type);var data=MeshRenderData.get(cube).values;for(int at=0;at<data.length;at+=23)for(int i=at+3;i<at+23;i+=5)out.addVertex(poses.last().pose(),data[i],data[i+1],data[i+2]).setColor(255,255,255,255).setUv(data[i+3],data[i+4]).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(poses.last(),data[at],data[at+1],data[at+2]);
 }
 private Sk01Model(){}
}
