package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiGraphics;import net.minecraft.network.chat.Component;import net.minecraft.world.entity.Entity;
public final class MoistureClient {
 public static void receive(MoisturePacket m){var mc=Minecraft.getInstance();if(mc.level==null||!mc.level.dimension().location().toString().equals(m.dimension()))return;var e=mc.level.getEntity(m.entity());if(e==null||!e.getUUID().equals(m.id()))return;var n=e.getPersistentData();n.putFloat("DwClientWet",Math.max(0,Math.min(100,m.wet())));n.putFloat("DwClientLens",Math.max(0,Math.min(100,m.lens())));n.putInt("DwClientStage",Math.max(0,Math.min(4,m.stage())));n.putBoolean("DwClientRain",m.rain());n.putLong("DwClientTime",mc.level.getGameTime());}
 public static void draw(GuiGraphics g,Entity e,int x,int y,int w,int h){if(e==null||e.level().getGameTime()-e.getPersistentData().getLong("DwClientTime")>60)return;var n=e.getPersistentData();float wet=n.getFloat("DwClientWet"),lens=n.getFloat("DwClientLens");int stage=n.getInt("DwClientStage");long t=e.level().getGameTime();if(n.contains("DwClientWipeTime"))lens*=Math.max(0,Math.min(1,(t-n.getLong("DwClientWipeTime"))/200f));
  g.enableScissor(x,y,x+w,y+h);
  try{if(lens>1&&MoistureClientConfig.DROPS.get()){int count=MoistureClientConfig.LIGHT.get()?3:7;for(int i=0;i<count;i++){int px=x+(int)(w*(.13+(i*.173)% .77)),py=y+(int)(h*(.18+((i*.271)% .58)));int a=(int)(lens*1.1);int color=(Math.min(110,a)<<24)|0xa8c7d8;g.fill(px,py,px+3,py+7,color);g.fill(px+1,py+7,px+3,py+9,color);}}
   if(stage>=2&&t%Math.max(40,140-stage*25)<stage)g.fill(x+5,y+h/2,x+w-5,y+h/2+2,0x4455aacc);
   if(wet>0){String key=stage>=4?"moisture.severe":stage>=2?"moisture.fault":n.getBoolean("DwClientRain")?"moisture.rain":"moisture.drying";var text=Component.translatable(key).append(" / ").append(Component.translatable("moisture.level",Math.round(wet)));var font=Minecraft.getInstance().font;float scale=Math.min(1,(w-12f)/Math.max(1,font.width(text)));g.pose().pushPose();g.pose().translate(x+w/2f,y+Math.max(24,h*.20),0);g.pose().scale(scale,scale,1);g.drawCenteredString(font,text,0,0,stage>=4?0xffff7568:0xff99cdeb);g.pose().popPose();}
  }finally{g.disableScissor();}
 }
 private MoistureClient(){}
}
