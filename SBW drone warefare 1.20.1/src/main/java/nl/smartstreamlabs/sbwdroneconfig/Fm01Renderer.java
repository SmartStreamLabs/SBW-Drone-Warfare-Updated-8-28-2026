package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import java.util.*;
/** Shared cached mesh; only seven dynamic objects are submitted by the block entity renderer. */
public final class Fm01Renderer implements BlockEntityRenderer<FuelMixerBlockEntity> {
 private record Part(String group,String name,float[] vertices){}
 private static List<Part> parts;
 private static JsonObject bones,pivots;
 private static ResourceLocation id(String s){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,s);}
 private static final RenderType BODY=RenderType.entitySolid(id("textures/block/fuel_mixer_fm01.png"));
 public static void clear(){parts=null;bones=null;pivots=null;}
 private static JsonElement read(String name)throws java.io.IOException{try(var r=Minecraft.getInstance().getResourceManager().getResourceOrThrow(id(name)).openAsReader()){return JsonParser.parseReader(r);}}
 private static List<Part> mesh(){
  if(parts==null)try{
   var result=new ArrayList<Part>();
   for(var node:read("meshes/fm01.mesh.json").getAsJsonArray()){
    var o=node.getAsJsonObject();var data=o.getAsJsonArray("v");float[] v=new float[data.size()];for(int i=0;i<v.length;i++)v[i]=data.get(i).getAsFloat();result.add(new Part(o.get("group").getAsString(),o.get("name").getAsString(),v));
   }
   bones=read("animations/fuel_mixer_fm01.animation.json").getAsJsonObject().getAsJsonObject("animations").getAsJsonObject("animation.fm01.mixing").getAsJsonObject("bones");
   pivots=read("meshes/fm01_integration.json").getAsJsonObject().getAsJsonObject("obj_part_pivots_blocks");parts=List.copyOf(result);
  }catch(java.io.IOException ex){throw new IllegalStateException("FM01 supplied mesh could not be loaded",ex);}
  return parts;
 }
 private static float value(JsonObject keys,float time,int axis){
  float before=-1,after=Float.MAX_VALUE;JsonArray a=null,b=null;
  for(var entry:keys.entrySet()){float t=Float.parseFloat(entry.getKey());if(t<=time&&t>before){before=t;a=entry.getValue().getAsJsonArray();}if(t>=time&&t<after){after=t;b=entry.getValue().getAsJsonArray();}}
  if(a==null)a=b;if(b==null)b=a;float av=a.get(axis).getAsFloat();return after==before||after==Float.MAX_VALUE?av:av+(b.get(axis).getAsFloat()-av)*(time-before)/(after-before);
 }
 @Override public void render(FuelMixerBlockEntity be,float partial,PoseStack stack,MultiBufferSource buffers,int light,int overlay){
  var objects=mesh();boolean active=be.getBlockState().getValue(FuelMixerBlock.LIT);float time=be.fm01Time(partial);
  stack.pushPose();stack.translate(.5,0,.5);stack.mulPose(Axis.YP.rotationDegrees(180-be.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot()));
  var consumer=buffers.getBuffer(BODY);
  for(Part p:objects){stack.pushPose();
   if(bones.has(p.group)){
    var channels=bones.getAsJsonObject(p.group);var pivot=pivots.getAsJsonArray(p.group);double x=pivot.get(0).getAsDouble(),y=pivot.get(1).getAsDouble(),z=pivot.get(2).getAsDouble();
    if(channels.has("position")){var keys=channels.getAsJsonObject("position");stack.translate(value(keys,time,0)/16,value(keys,time,1)/16,value(keys,time,2)/16);}
    if(channels.has("rotation")){var keys=channels.getAsJsonObject("rotation");stack.translate(x,y,z);stack.mulPose(Axis.ZP.rotationDegrees(value(keys,time,2)));stack.mulPose(Axis.YP.rotationDegrees(value(keys,time,1)));stack.mulPose(Axis.XP.rotationDegrees(value(keys,time,0)));stack.translate(-x,-y,-z);}
   }
   boolean lamp=active?p.name.equals("lamp_4"):p.name.equals("lamp_5");
   emit(p,stack,buffers.getBuffer(BODY),lamp?LightTexture.FULL_BRIGHT:light,overlay);
   if(p.name.equals("display")){
    float minX=10,maxX=-10,minY=10,maxY=-10,minZ=10;for(int i=0;i<p.vertices.length;i+=8){minX=Math.min(minX,p.vertices[i]);maxX=Math.max(maxX,p.vertices[i]);minY=Math.min(minY,p.vertices[i+1]);maxY=Math.max(maxY,p.vertices[i+1]);minZ=Math.min(minZ,p.vertices[i+2]);}
    var font=Minecraft.getInstance().font;String label=active?"MIXING":"IDLE";float scale=Math.min((maxX-minX)*.85f/font.width("MIXING"),(maxY-minY)/24);
    stack.pushPose();stack.translate(maxX-(maxX-minX)*.07,maxY-(maxY-minY)*.15,minZ-.001);stack.scale(-scale,-scale,scale);
    font.drawInBatch("FM-01",0,0,0xffbddb91,false,stack.last().pose(),buffers,Font.DisplayMode.NORMAL,0,LightTexture.FULL_BRIGHT);
    font.drawInBatch(label,0,10,active?0xfff0c460:0xffbddb91,false,stack.last().pose(),buffers,Font.DisplayMode.NORMAL,0,LightTexture.FULL_BRIGHT);stack.popPose();
   }
   stack.popPose();
  }stack.popPose();
 }
 private static void emit(Part p,PoseStack s,VertexConsumer consumer,int light,int overlay){
  float[] a=p.vertices;var pose=s.last();boolean screen=p.name.equals("display");
  for(int i=0;i<a.length;i+=8)consumer.vertex(pose.pose(),a[i],a[i+1],a[i+2]).color(screen?.12f:1,screen?.16f:1,screen?.12f:1,1).uv(screen?.001f:a[i+3],screen?.001f:a[i+4]).overlayCoords(overlay).uv2(light).normal(pose.normal(),a[i+5],a[i+6],a[i+7]).endVertex();
 }
}
