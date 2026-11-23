package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.*;
import software.bernie.geckolib.cache.object.GeoCube;
import org.joml.Matrix4f;
/** DWF-only static path. Original geometry stays on the GPU; one world-pose
 * uniform replaces repeated rigid-palette texture uploads and GL state queries.
 * No gameplay entity, per-instance mesh, or per-frame geometry rebuild. */
final class DwfStaticGpu {
 static ShaderInstance shader;
 private record Metadata(byte[]flips,byte[]stages){}
 private static final Map<GeoCube,Metadata>METADATA=new IdentityHashMap<>();
 static void metadata(GeoCube cube,byte[]flips,byte[]stages){METADATA.put(cube,new Metadata(flips.clone(),stages.clone()));}
 private static final Map<GeoCube,VertexBuffer>CACHE=new IdentityHashMap<>();
 static void release(GeoCube cube){METADATA.remove(cube);var buffer=CACHE.remove(cube);if(buffer!=null)buffer.close();}
 static boolean draw(GeoCube cube,RenderType type,PoseStack pose,int light,int overlay){return draw(cube,type,pose,light,overlay,3);}
 static boolean draw(GeoCube cube,RenderType type,PoseStack pose,int light,int overlay,int stage){return draw(cube,type,pose,light,overlay,stage,false);}
 static boolean emissive(GeoCube cube,RenderType type,PoseStack pose,int overlay){return draw(cube,type,pose,LightTexture.FULL_BRIGHT,overlay,3,true);}
 private static boolean draw(GeoCube cube,RenderType type,PoseStack pose,int light,int overlay,int stage,boolean emissive){
  if(shader==null||Boolean.getBoolean("adv01.cpuMesh"))return false;
  var vertices=CACHE.computeIfAbsent(cube,DwfStaticGpu::upload);var previous=RenderSystem.getShader();var color=RenderSystem.getShaderColor().clone();int active=GlStateManager._getActiveTexture();int[]textures={RenderSystem.getShaderTexture(0),RenderSystem.getShaderTexture(1),RenderSystem.getShaderTexture(2)};
  type.setupRenderState();
  try{RenderSystem.setShaderColor(1,1,1,1);shader.safeGetUniform("DwfAssemblyStage").set((float)stage);shader.safeGetUniform("DwfEmissive").set(emissive?1f:0f);shader.safeGetUniform("DwfPose").set(pose.last().pose());shader.safeGetUniform("DwfNormal").set(pose.last().normal());shader.safeGetUniform("AdvLight").set((float)(light&65535),(float)(light>>>16));shader.safeGetUniform("AdvOverlay").set((float)(overlay&65535),(float)(overlay>>>16));RenderSystem.setShader(()->shader);vertices.bind();vertices.drawWithShader(new Matrix4f(RenderSystem.getModelViewMatrix()),RenderSystem.getProjectionMatrix(),shader);}
  finally{VertexBuffer.unbind();BufferUploader.reset();type.clearRenderState();RenderSystem.setShader(()->previous);RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);for(int i=0;i<textures.length;i++)RenderSystem.setShaderTexture(i,textures[i]);RenderSystem.activeTexture(active);}
  return true;
 }
 private static VertexBuffer upload(GeoCube cube){var data=MeshRenderData.get(cube).values;int total=data.length/23*4;
  BufferBuilder out=new BufferBuilder(Math.max(256,total*36));out.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.NEW_ENTITY);
  var metadata=METADATA.get(cube);for(int at=0;at<data.length;at+=23){int face=at/23,flags=metadata==null?0:metadata.flips()[face],stage=metadata==null?0:metadata.stages()[face];for(int i=at+3;i<at+23;i+=5)out.vertex(data[i],data[i+1],data[i+2],stage/255f,(flags&1)!=0?1:0,(flags&2)!=0?1:0,(flags&4)!=0?1:0,data[i+3],data[i+4],0,0,data[at],data[at+1],data[at+2]);}
  VertexBuffer result=new VertexBuffer(VertexBuffer.Usage.STATIC);result.bind();result.upload(out.end());VertexBuffer.unbind();BufferUploader.reset();return result;
 }
}
