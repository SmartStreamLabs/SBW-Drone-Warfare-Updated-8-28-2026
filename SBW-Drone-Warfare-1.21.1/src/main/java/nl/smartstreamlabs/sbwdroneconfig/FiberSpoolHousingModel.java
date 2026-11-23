package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** Shared immutable housing mesh: loaded once, no procedural model construction per frame. */
final class FiberSpoolHousingModel {
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","textures/entity/fiber_spool_housing.png");
 private static final float[] MESH=load();
 private static float[] load(){try(var in=FiberSpoolHousingModel.class.getResourceAsStream("/assets/sbwdroneconfig/spool/fiber_spool_housing.json")){if(in==null)throw new IllegalStateException("Missing fiber spool mesh");return new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),float[].class);}catch(IOException e){throw new IllegalStateException("Cannot load spool mesh",e);}}
 static void render(PoseStack stack,MultiBufferSource buffer,int light){var pose=stack.last();var out=buffer.getBuffer(RenderType.entitySolid(TEXTURE));for(int i=0;i<MESH.length;i+=8){out.addVertex(pose.pose(),MESH[i],MESH[i+1],MESH[i+2]).setColor(255,255,255,255).setUv(MESH[i+3],MESH[i+4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,MESH[i+5],MESH[i+6],MESH[i+7]);}}
}
