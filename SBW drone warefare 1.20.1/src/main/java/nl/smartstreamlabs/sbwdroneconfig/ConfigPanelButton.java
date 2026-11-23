package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Original dark panel styling; no Create classes or assets are required. */
public class ConfigPanelButton extends Button {
 protected ConfigPanelButton(int x,int y,int width,int height,Component text,OnPress action){super(x,y,width,height,text,action,DEFAULT_NARRATION);}
 public static Builder create(Component label,OnPress action){return new Builder(label,action);}
 public static final class Builder {
  private final Component label;private final OnPress action;private int x,y,width=150,height=20;
  private Builder(Component label,OnPress action){this.label=label;this.action=action;}
  public Builder bounds(int x,int y,int width,int height){this.x=x;this.y=y;this.width=width;this.height=height;return this;}
  public ConfigPanelButton build(){return new ConfigPanelButton(x,y,width,height,label,action);}
 }
 @Override protected void renderWidget(GuiGraphics g,int mx,int my,float tick){
  boolean hover=active&&isHoveredOrFocused();int edge=hover?0xff91b6c9:active?0xff506977:0xff34424b;
  int x=getX(),y=getY(),w=getWidth(),h=getHeight();g.fill(x,y,x+w,y+h,edge);g.fill(x+1,y+1,x+w-1,y+h-1,hover?0xef20343f:0xe8081017);
  var font=Minecraft.getInstance().font;float scale=Math.min(1f,(w-10f)/Math.max(1,font.width(getMessage())));
  g.pose().pushPose();try{g.pose().translate(x+w/2f,y+(h-9*scale)/2f,0);g.pose().scale(scale,scale,1);g.drawCenteredString(font,getMessage(),0,0,active?0xd5e8f0:0x73848d);}finally{g.pose().popPose();}
 }
}
