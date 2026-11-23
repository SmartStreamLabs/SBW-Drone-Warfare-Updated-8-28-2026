package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.util.*;

/** Batches the complete native mesh. No entities, scans, sounds or saved state are created. */
public final class Pd01ItemRenderer extends BlockEntityWithoutLevelRenderer {
    private record Triangle(float[] xyz,float[] uv,float nx,float ny,float nz,boolean scan){}
    private final List<Triangle> triangles=new ArrayList<>();private BakedModel sourceModel;
    public Pd01ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    private static ResourceLocation id(String path){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,path);}
    private void load(ItemStack stack){
        var mc=Minecraft.getInstance();var model=mc.getItemRenderer().getModel(stack,mc.level,null,0);if(model==sourceModel)return;
        triangles.clear();
        try(var in=mc.getResourceManager().openAsReader(id("meshes/portable_fpv_detector_pd01.json"))){
            var bones=JsonParser.parseReader(in).getAsJsonObject().getAsJsonArray("bones");
            for(var v:bones){var b=v.getAsJsonObject();if(!b.has("poly_mesh"))continue;var mesh=b.getAsJsonObject("poly_mesh");var points=mesh.getAsJsonArray("positions");var uv=mesh.getAsJsonArray("uvs");var normals=mesh.getAsJsonArray("normals");
                for(var f:mesh.getAsJsonArray("polys")){var poly=f.getAsJsonArray();float[] xyz=new float[9],tex=new float[6];int normal=0;
                    for(int i=0;i<3;i++){var ref=poly.get(i).getAsJsonArray();int p=ref.get(0).getAsInt()*3,t=ref.get(2).getAsInt()*2;normal=ref.get(1).getAsInt()*3;xyz[i*3]=-points.get(p).getAsFloat()/16;xyz[i*3+1]=points.get(p+1).getAsFloat()/16;xyz[i*3+2]=points.get(p+2).getAsFloat()/16;tex[i*2]=uv.get(t).getAsFloat();tex[i*2+1]=1-uv.get(t+1).getAsFloat();}
                    triangles.add(new Triangle(xyz,tex,-normals.get(normal).getAsFloat(),normals.get(normal+1).getAsFloat(),normals.get(normal+2).getAsFloat(),b.get("name").getAsString().equals("scan_line")));
                }
            }sourceModel=model;
        }catch(java.io.IOException e){throw new IllegalStateException("Cannot load PD-01 mesh",e);}
    }
    static int visualState(boolean active,boolean held,boolean targets){return !active?0:held&&targets?2:1;}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        load(stack);var mc=Minecraft.getInstance();var data=stack.getTag();boolean active=data!=null&&data.getBoolean(DroneJammerItem.TAG_ACTIVE);
        boolean held=mc.player!=null&&(stack==mc.player.getMainHandItem()||stack==mc.player.getOffhandItem());int state=Pd01VisualState.state(active,held,DroneJammerClient.pd01HasTargets());
        String suffix=state==0?"_standby":state==2?"_alert":"";var out=buffers.getBuffer(RenderType.entityCutoutNoCull(id("textures/item/portable_fpv_detector_pd01"+suffix+".png")));
        p.pushPose();p.translate(.5,.5,.5); // cancel ItemRenderer's [-.5,-.5,-.5] centering once
        boolean first=context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean third=context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if(first)p.translate(0,.16,0); // expose the complete handheld body above the hotbar
        if(first||third||(context==ItemDisplayContext.FIXED&&held)){p.mulPose(Axis.YP.rotationDegrees(180));if(third){p.mulPose(Axis.XP.rotationDegrees(-90));p.translate(0,0,.18);}p.translate(0,-1.5/16,-.62/16);}
        else {p.translate(0,-3.52875/16,0);}
        double time=mc.level==null?0:mc.level.getGameTime();float scan=(float)Pd01VisualState.sweep(time);
        for(var t:triangles){if(t.scan&&state!=1)continue;float dy=t.scan?scan:0;var pose=p.last();
            for(int j=0;j<4;j++){int i=Math.min(j,2);out.vertex(pose.pose(),t.xyz[i*3],t.xyz[i*3+1]+dy,t.xyz[i*3+2]).color(255,255,255,255).uv(t.uv[i*2],t.uv[i*2+1]).overlayCoords(overlay).uv2(light).normal(pose.normal(),t.nx,t.ny,t.nz).endVertex();}
        }p.popPose();
    }
}
