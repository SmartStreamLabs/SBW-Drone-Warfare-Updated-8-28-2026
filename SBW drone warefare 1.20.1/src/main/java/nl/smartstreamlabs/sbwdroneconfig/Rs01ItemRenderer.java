package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import com.mojang.blaze3d.vertex.PoseStack;

/** All supplied source triangles and UVs; the immutable array is loaded once. */
public final class Rs01ItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE=new ResourceLocation("sbwdroneconfig","textures/item/rubble_shovel_rs01.png");
    private static final float[] MESH=load();
    private static float[] load(){try(var in=Rs01ItemRenderer.class.getResourceAsStream("/assets/sbwdroneconfig/rs01/mesh.json")){if(in==null)throw new IllegalStateException("Missing RS-01 mesh");return new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),float[].class);}catch(java.io.IOException e){throw new IllegalStateException(e);}}
    public Rs01ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack p,MultiBufferSource b,int light,int overlay){
        p.pushPose();p.translate(.5,.5,.5);var pose=p.last();var out=b.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for(int i=0;i<MESH.length;i+=8)out.vertex(pose.pose(),MESH[i],MESH[i+1],MESH[i+2]).color(255,255,255,255).uv(MESH[i+3],MESH[i+4]).overlayCoords(overlay).uv2(light).normal(pose.normal(),MESH[i+5],MESH[i+6],MESH[i+7]).endVertex();
        p.popPose();
    }
}
