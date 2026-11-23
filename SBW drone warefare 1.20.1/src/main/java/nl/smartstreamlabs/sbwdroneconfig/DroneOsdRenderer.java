package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.math.Axis;
import java.util.Locale;

/** One pixel OSD layout for aircraft, rovers and the receiver's real camera viewport. */
public final class DroneOsdRenderer {
    private static final int WHITE=0xfff4f4f4,BLACK=0xff171717;
    private static final String[] CARDINALS={"N","E","S","W"};
    private static final String[] PITCH=new String[17];
    static {for(int i=0;i<PITCH.length;i++)PITCH[i]=Integer.toString((i-8)*10);}
    private DroneOsdRenderer(){}
    static void render(GuiGraphics g,DroneOsdClient.Sample s,DroneOsdClient.Orientation o,int x,int y,int width,int height,boolean preview){
        if(width<40||height<30)return;
        double requested=DroneOsdConfig.SCALE.get()*(s.entity instanceof LucasDroneEntity?AddonConfig.lucasHudScale():1);
        float scale=DroneOsdMath.scale(width,height,requested,preview);int w=(int)(width/scale),h=(int)(height/scale),pad=preview?7:16,step=preview?11:13;
        g.enableScissor(x,y,x+width,y+height);g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);
        try {
            if(!preview)centre(g,fit(s.name,(int)(w*.48)),w/2,10);
            int top=preview?7:29;
            text(g,fit(s.alt,(int)(w*.31)),pad,top);text(g,fit(s.velocity,(int)(w*.31)),pad,top+step);text(g,fit(s.dist,(int)(w*.31)),pad,top+step*2);
            if(!preview)text(g,fit(s.range,(int)(w*.31)),pad,top+step*3);
            right(g,DroneThermalVisionClient.modeLabel(),w-pad,preview?7:10);
            if(preview){right(g,fit(s.shortPower,(int)(w*.48)),w-pad,top+step);right(g,fit(s.link,(int)(w*.4)),w-pad,top+step*2);right(g,fit(s.range,(int)(w*.4)),w-pad,top+step*3);}
            else {right(g,fit(s.power,(int)(w*.42)),w-pad,top);right(g,fit(s.fuel,(int)(w*.31)),w-pad,top+step);right(g,fit(s.link,(int)(w*.4)),w-pad,top+step*2);right(g,fit(s.health,(int)(w*.31)),w-pad,top+step*3);}
            if(o.id!=null&&o.id.equals(s.id)){
                horizon(g,o,x,y,w,h,scale,preview);
                if(!preview)centre(g,o.headingText,w/2,29);
                compass(g,o,w,h,preview);
                DroneOsdTargets.render(g,s,o,w,h);
            }
            reticle(g,w/2,h/2);
            int base=h-(preview?44:62);
            if(preview){text(g,fit(s.coordinates,w-pad*2),pad,base);}
            else for(int i=0;i<3;i++)text(g,fit(s.xyz[i],(int)(w*.3)),pad,base+i*step);
            if(!preview){
                right(g,fit(s.payload,(int)(w*.35)),w-pad,h-65);right(g,fit(s.bodyHeading,(int)(w*.31)),w-pad,h-39);
                centre(g,DroneOsdClient.perspective(s.entity),w/2,h-65);
            }
            centre(g,fit(s.mode,(int)(w*.44)),w/2,h-(preview?30:49));
            if(preview)centre(g,fit(s.warning.isEmpty()?s.payload:s.warning,w-pad*2),w/2,h-57);else if(!s.warning.isEmpty())centre(g,fit(s.warning,(int)(w*.52)),w/2,57);
            if(!preview&&s.entity instanceof Sd01Entity sd)Sd01Client.hud(g,sd,w,h);
            var d=s.fresh();if(!preview&&s.entity instanceof Id01Entity&&Id01Client.snapshot()!=null&&!Id01Client.snapshot().target().isEmpty())right(g,fit("TGT "+Id01Client.snapshot().target(),(int)(w*.35)),w-pad,h-78);
        } finally {g.pose().popPose();g.disableScissor();}
    }
    private static void horizon(GuiGraphics g,DroneOsdClient.Orientation o,int x,int y,int w,int h,float scale,boolean preview){
        int cx=w/2,cy=h/2,half=preview?42:Math.min(110,w/5),vertical=preview?10:Math.min(65,h/5);
        g.enableScissor(x+Math.round((cx-half-22)*scale),y+Math.round((cy-vertical)*scale),x+Math.round((cx+half+22)*scale),y+Math.round((cy+vertical)*scale));
        g.pose().pushPose();g.pose().translate(cx,cy,0);g.pose().mulPose(Axis.ZP.rotationDegrees((float)o.roll));
        try {
            double focal=h*Math.abs(o.projection.m11())*.5;
            for(int mark=-80;mark<=80;mark+=10){
                double elevation=DroneOsdMath.horizonY(o.pitch,mark,focal);if(!Double.isFinite(elevation)||Math.abs(elevation)>Math.hypot(half+22,vertical))continue;
                int yy=(int)Math.round(elevation),length=mark==0?half:preview?15:30;
                line(g,-length,yy,-5,yy);line(g,5,yy,length,yy);
                if(mark!=0){line(g,-length,yy,-length,yy+(mark<0?-3:3));line(g,length,yy,length,yy+(mark<0?-3:3));if(!preview){text(g,PITCH[(mark+80)/10],-length-21,yy-4);text(g,PITCH[(mark+80)/10],length+6,yy-4);}}
            }
        }finally{g.pose().popPose();g.disableScissor();}
    }
    private static void compass(GuiGraphics g,DroneOsdClient.Orientation o,int w,int h,boolean preview){
        if(!Double.isFinite(o.heading))return;int cx=w/2,y=h-7,half=preview?48:Math.min(112,w/5);double pixels=half/60.0;
        line(g,cx-half,y,cx+half,y);line(g,cx,y-3,cx,y+2);
        for(int mark=0;mark<360;mark+=10){double delta=DroneOsdMath.difference(mark,o.heading);if(Math.abs(delta)>60)continue;int xx=cx+(int)Math.round(delta*pixels);line(g,xx,y,xx,y-3);if(mark%90==0)centre(g,CARDINALS[mark/90],xx,y-12);}
    }
    private static void reticle(GuiGraphics g,int x,int y){line(g,x-3,y,x-1,y);line(g,x+1,y,x+3,y);line(g,x,y-3,x,y-1);line(g,x,y+1,x,y+3);}
    static void text(GuiGraphics g,String value,int x,int y){if(value==null||value.isEmpty())return;var f=Minecraft.getInstance().font;g.drawString(f,value,x-1,y,BLACK,false);g.drawString(f,value,x+1,y,BLACK,false);g.drawString(f,value,x,y-1,BLACK,false);g.drawString(f,value,x,y+1,BLACK,false);g.drawString(f,value,x,y,WHITE,false);}
    private static void right(GuiGraphics g,String value,int x,int y){text(g,value,x-Minecraft.getInstance().font.width(value),y);}
    private static void centre(GuiGraphics g,String value,int x,int y){text(g,value,x-Minecraft.getInstance().font.width(value)/2,y);}
    static void line(GuiGraphics g,int x1,int y1,int x2,int y2){int l=Math.min(x1,x2),r=Math.max(x1,x2),t=Math.min(y1,y2),b=Math.max(y1,y2);g.fill(l-1,t-1,r+2,b+2,BLACK);g.fill(l,t,r+1,b+1,WHITE);}
    static String fit(String value,int width){if(value==null)return "";var f=Minecraft.getInstance().font;if(f.width(value)<=width)return value;return f.plainSubstrByWidth(value,Math.max(0,width-6))+"…";}
}
