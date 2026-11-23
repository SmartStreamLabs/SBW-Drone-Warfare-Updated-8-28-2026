package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Vector4f;

/** Preserves native observation markers and explicitly selected artillery points; never picks targets. */
final class DroneOsdTargets {
    private static final Vector4f POINT=new Vector4f();
    static void render(GuiGraphics g,DroneOsdClient.Sample s,DroneOsdClient.Orientation view,int w,int h){
        var m=Minecraft.getInstance();if(m.level==null)return;
        float partial=m.getFrameTime();
        if(!DroneThermalVisionClient.shouldHideDefaultTargetBoxes())for(int i=0;i<s.nativeTargetCount;i++){
            Entity e=s.nativeTargets[i];if(e!=null&&e.isAlive()&&!e.isRemoved())entity(g,e,partial,view,w,h);
        }
        var data=s.fresh();if(data!=null)for(int id:data.markers()){Entity e=m.level.getEntity(id);if(e!=null&&!e.isRemoved())entity(g,e,partial,view,w,h);}
        if(m.player!=null){var stack=m.player.getOffhandItem();String item=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();if(item.equals("artillery_indicator")||item.equals("firing_parameters")){var tag=(stack.hasTag()?stack.getTag():new net.minecraft.nbt.CompoundTag());if(tag.contains("PosX")&&tag.contains("PosY")&&tag.contains("PosZ"))point(g,tag.getDouble("PosX"),tag.getDouble("PosY"),tag.getDouble("PosZ"),view,w,h);}}
    }
    private static void entity(GuiGraphics g,Entity e,float p,DroneOsdClient.Orientation view,int w,int h){point(g,Mth.lerp(p,e.xo,e.getX()),Mth.lerp(p,e.yo,e.getY())+e.getEyeHeight(),Mth.lerp(p,e.zo,e.getZ()),view,w,h);}
    private static void point(GuiGraphics g,double x,double y,double z,DroneOsdClient.Orientation view,int w,int h){
        POINT.set((float)(x-view.position.x),(float)(y-view.position.y),(float)(z-view.position.z),1);view.view.transform(POINT);view.projection.transform(POINT);
        if(POINT.w<=0)return;float xx=POINT.x/POINT.w,yy=POINT.y/POINT.w,zz=POINT.z/POINT.w;if(Math.abs(xx)>1||Math.abs(yy)>1||Math.abs(zz)>1)return;
        int sx=Math.round((xx*.5f+.5f)*w),sy=Math.round((.5f-yy*.5f)*h),r=6;
        DroneOsdRenderer.line(g,sx-r,sy-r,sx-r+3,sy-r);DroneOsdRenderer.line(g,sx-r,sy-r,sx-r,sy-r+3);
        DroneOsdRenderer.line(g,sx+r-3,sy-r,sx+r,sy-r);DroneOsdRenderer.line(g,sx+r,sy-r,sx+r,sy-r+3);
        DroneOsdRenderer.line(g,sx-r,sy+r,sx-r+3,sy+r);DroneOsdRenderer.line(g,sx+r-3,sy+r,sx+r,sy+r);
    }
}
