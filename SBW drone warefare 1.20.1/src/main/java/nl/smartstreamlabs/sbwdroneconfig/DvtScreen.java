package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.client.Minecraft;import net.minecraft.client.gui.*;import net.minecraft.client.gui.components.*;import net.minecraft.client.gui.screens.Screen;import net.minecraft.core.BlockPos;import net.minecraft.network.chat.Component;
/** A shared read-only texture, without a persistent player camera or control session. */
public final class DvtScreen extends Screen {
 public final BlockPos pos;private final List<DvtReply.Entry> choices=new ArrayList<>();private int index,x,y,w,vh;private UUID choice;private EditBox player;private Button select,lid,watch;public boolean fullscreen;
 public DvtScreen(BlockPos pos){super(Component.literal("DVT-01 · Drone Video Terminal"));this.pos=pos.immutable();var r=DvtClient.state(pos);if(r!=null){choices.addAll(r.choices());choice=r.selected();}locate();}
 private void locate(){index=0;for(int i=0;i<choices.size();i++)if(choices.get(i).id().equals(choice))index=i;if(choice==null&&!choices.isEmpty())choice=choices.get(0).id();}
 public void updateChoices(DvtReply r){if(!r.choices().isEmpty()||r.notice().equals("Drone list refreshed.")){choices.clear();choices.addAll(r.choices());locate();if(select!=null)select.setMessage(Component.literal(choiceName()));}}
 @Override public boolean isPauseScreen(){return false;}
 @Override protected void init(){w=Math.min(380,width-16);vh=Math.max(28,Math.min(150,height-146));x=(width-w)/2;y=Math.max(22,(height-(110+vh))/2);int third=(w-8)/3;
 select=addRenderableWidget(Button.builder(Component.literal(choiceName()),b->{if(!choices.isEmpty()){index=(index+1)%choices.size();choice=choices.get(index).id();b.setMessage(Component.literal(choiceName()));}}).bounds(x,y,w-64,20).build());addRenderableWidget(Button.builder(Component.literal("Refresh"),b->DvtClient.request(pos,1,null,"")).bounds(x+w-60,y,60,20).build());
 addRenderableWidget(Button.builder(Component.literal("Connect"),b->DvtClient.request(pos,2,choice,"")).bounds(x,y+24,third,20).build());addRenderableWidget(Button.builder(Component.literal("Unlink"),b->DvtClient.request(pos,2,null,"")).bounds(x+third+4,y+24,third,20).build());lid=addRenderableWidget(Button.builder(Component.literal("Open / close lid"),b->DvtClient.request(pos,3,null,"")).bounds(x+2*(third+4),y+24,w-2*(third+4),20).build());
 watch=addRenderableWidget(Button.builder(Component.literal("Watch fullscreen"),b->{fullscreen=true;widgets(false);}).bounds(x,y+52+vh,w,20).build());int py=y+76+vh;player=addRenderableWidget(new EditBox(font,x,py,w-168,20,Component.literal("Online player name")));player.setMaxLength(16);player.setHint(Component.literal("Player name"));addRenderableWidget(Button.builder(Component.literal("Grant view"),b->DvtClient.request(pos,4,choice,player.getValue())).bounds(x+w-164,py,80,20).build());addRenderableWidget(Button.builder(Component.literal("Revoke view"),b->DvtClient.request(pos,5,choice,player.getValue())).bounds(x+w-80,py,80,20).build());widgets(!fullscreen);}
 private String choiceName(){String name=choices.isEmpty()?"No authorized drones":choices.get(index).name()+"  ["+choices.get(index).status()+"]";return font==null?name:font.plainSubstrByWidth(name,Math.max(80,w-78));}
 private void widgets(boolean show){for(var r:renderables)if(r instanceof AbstractWidget a)a.visible=show;}
 @Override public void tick(){var m=Minecraft.getInstance();if(m.level==null||!(m.level.getBlockEntity(pos)instanceof Dvt01BlockEntity b)||m.player==null||m.player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>48*48){onClose();return;}if(b.open)DvtClient.watch(pos);}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){var r=DvtClient.state(pos);g.fill(0,0,width,height,0xe0081015);var f=r!=null&&r.lid()?DvtClient.feed(r):null;
  if(fullscreen){int fw=Math.min(width,height*16/9),fh=fw*9/16;DvtClient.draw(g,f,(width-fw)/2,(height-fh)/2,fw,fh);g.drawString(font,"Esc · Return to terminal",8,height-14,0xffc7d5d2);return;}
  g.drawCenteredString(font,title,width/2,y-16,0xffe0e8e5);g.fill(x,y+48,x+w,y+48+vh,0xff000000);int fw=Math.min(w,vh*16/9),fh=fw*9/16;DvtClient.draw(g,f,x+(w-fw)/2,y+48+(vh-fh)/2,fw,fh);
  if(r!=null){watch.active=r.lid()&&r.selected()!=null;lid.setMessage(Component.literal(r.lid()?"Close lid":"Open lid"));if(!r.notice().isBlank())g.drawCenteredString(font,r.notice(),width/2,y+100+vh,0xffead6a1);}super.render(g,mx,my,partial);
 }
 @Override public boolean keyPressed(int key,int scan,int modifiers){if(fullscreen&&key==256){fullscreen=false;widgets(true);return true;}return super.keyPressed(key,scan,modifiers);}
 @Override public void onClose(){Minecraft.getInstance().setScreen(null);}
}
