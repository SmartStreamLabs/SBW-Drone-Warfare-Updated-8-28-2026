package nl.smartstreamlabs.sbwdroneconfig;
import software.bernie.geckolib.cache.object.GeoCube;
import java.util.*;
/** Immutable native geometry, shared across instances; no poses or entity references in the cache.
 * Weak keys allow obsolete models to be collected after a resource reload.
 */
final class MeshRenderData {
 private static final Map<GeoCube,MeshRenderData> CACHE=Collections.synchronizedMap(new WeakHashMap<>());
 final float[] values;
 final boolean flipX,flipY,flipZ;
 private MeshRenderData(GeoCube cube){
  var quads=cube.quads();int count=0;for(var q:quads)if(q!=null)count++;
  values=new float[count*23];int at=0;
  for(var q:quads){if(q==null)continue;if(q.vertices().length!=4)throw new IllegalArgumentException("Expected baked quad");
   values[at++]=q.normal().x();values[at++]=q.normal().y();values[at++]=q.normal().z();
   for(var v:q.vertices()){values[at++]=v.position().x();values[at++]=v.position().y();values[at++]=v.position().z();values[at++]=v.texU();values[at++]=v.texV();}
  }
  var size=cube.size();flipX=size.y==0||size.z==0;flipY=size.x==0||size.z==0;flipZ=size.x==0||size.y==0;
 }
 static void register(GeoCube cube){CACHE.put(cube,new MeshRenderData(cube));}
 static MeshRenderData get(GeoCube cube){return CACHE.get(cube);}
}
