package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import java.util.*;

/** Stationary geometry stays chunk-baked. Only four shared mesh batches move/change texture. */
public final class Rj01Renderer implements BlockEntityRenderer<RadarJammerTowerBlockEntity> {
    private record Batch(String group,float[] vertices){}
    private record Key(float time,float value){}
    private record Clip(float length,List<Key> keys){
        float angle(float seconds){
            float time=seconds%length;Key a=keys.get(0),b=keys.get(keys.size()-1);
            for(Key key:keys){if(key.time<=time)a=key;if(key.time>=time){b=key;break;}}
            return b.time==a.time?a.value:a.value+(b.value-a.value)*(time-a.time)/(b.time-a.time);
        }
    }
    private static List<Batch> batches;
    private static Map<String,float[]> pivots;
    private static Clip yaw,tilt,fan;
    private static ResourceLocation id(String path){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,path);}
    private static final RenderType STANDBY=RenderType.entitySolid(id("textures/block/radar_jammer_tower_rj01.png"));
    private static final RenderType ACTIVE=RenderType.entitySolid(id("textures/block/radar_jammer_tower_rj01_active.png"));
    public static void clear(){batches=null;pivots=null;yaw=tilt=fan=null;}
    private static JsonObject read(String path)throws java.io.IOException{
        try(var in=Minecraft.getInstance().getResourceManager().openAsReader(id(path))){return JsonParser.parseReader(in).getAsJsonObject();}
    }
    private static Clip clip(JsonObject animations,String name,String bone,int axis){
        var a=animations.getAsJsonObject("animation.rj01."+name);var keys=new ArrayList<Key>();
        for(var e:a.getAsJsonObject("bones").getAsJsonObject(bone).getAsJsonObject("rotation").entrySet())keys.add(new Key(Float.parseFloat(e.getKey()),e.getValue().getAsJsonArray().get(axis).getAsFloat()));
        keys.sort(Comparator.comparingDouble(Key::time));return new Clip(a.get("animation_length").getAsFloat(),List.copyOf(keys));
    }
    private static void load(){
        if(batches!=null)return;
        try{
            var mesh=read("meshes/rj01.mesh.json");var result=new ArrayList<Batch>();
            for(var e:mesh.getAsJsonArray("batches")){var o=e.getAsJsonObject();var a=o.getAsJsonArray("v");float[] vertices=new float[a.size()];for(int i=0;i<vertices.length;i++)vertices[i]=a.get(i).getAsFloat();result.add(new Batch(o.get("group").getAsString(),vertices));}
            var meta=read("meshes/rj01_integration.json");var p=new HashMap<String,float[]>();
            for(var e:meta.getAsJsonObject("obj_part_pivots_blocks").entrySet()){var a=e.getValue().getAsJsonArray();p.put(e.getKey(),new float[]{a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat()});}
            var animations=read("animations/radar_jammer_tower_rj01.animation.json").getAsJsonObject("animations");
            yaw=clip(animations,"head_yaw","head_yaw",1);tilt=clip(animations,"head_tilt_preview","head_tilt",0);fan=clip(animations,"fan","cooling_fan",1);
            pivots=Map.copyOf(p);batches=List.copyOf(result);
        }catch(java.io.IOException e){throw new IllegalStateException("Cannot load supplied RJ-01 mesh",e);}
    }
    @Override public int getViewDistance(){return 128;}
    @Override public void render(RadarJammerTowerBlockEntity be,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        load();float time=be.rj01Time(partial);boolean active=be.rj01Active();var out=buffers.getBuffer(active?ACTIVE:STANDBY);
        p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(180-be.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot()));
        for(Batch b:batches){
            p.pushPose();
            if(b.group.equals("head_yaw")||b.group.equals("head_tilt"))pivot(p,"head_yaw",yaw.angle(time),false);
            if(b.group.equals("head_tilt"))pivot(p,"head_tilt",tilt.angle(time),true);
            if(b.group.equals("cooling_fan"))pivot(p,"cooling_fan",fan.angle(time),false);
            var pose=p.last();float[] v=b.vertices;
            for(int i=0;i<v.length;i+=8)out.vertex(pose.pose(),v[i],v[i+1],v[i+2]).color(255,255,255,255).uv(v[i+3],v[i+4]).overlayCoords(overlay).uv2(light).normal(pose.normal(),v[i+5],v[i+6],v[i+7]).endVertex();
            p.popPose();
        }
        p.popPose();
    }
    private static void pivot(PoseStack p,String bone,float angle,boolean xAxis){
        float[] v=pivots.get(bone);p.translate(v[0],v[1],v[2]);p.mulPose((xAxis?Axis.XP:Axis.YP).rotationDegrees(angle));p.translate(-v[0],-v[1],-v[2]);
    }
}
