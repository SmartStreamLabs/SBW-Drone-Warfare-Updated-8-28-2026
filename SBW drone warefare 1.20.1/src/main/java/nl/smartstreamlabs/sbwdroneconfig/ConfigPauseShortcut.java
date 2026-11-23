package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ScreenEvent;
import java.util.*;

@Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=Dist.CLIENT)
public final class ConfigPauseShortcut {
 private ConfigPauseShortcut(){}
 @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void init(ScreenEvent.Init.Post event){
  if(!(event.getScreen() instanceof PauseScreen screen))return;
  var widgets=event.getListenersList().stream().filter(c->c instanceof AbstractWidget).map(c->(AbstractWidget)c).toList();
  if(widgets.stream().anyMatch(c->c instanceof Shortcut))return;
  var place=placement(screen,widgets);event.addListener(new Shortcut(place[0],place[1],screen));
 }
 // A later Init.Post handler may add another mod's button after ours. Reconcile before drawing.
 @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void render(ScreenEvent.Render.Pre event){
  if(!(event.getScreen() instanceof PauseScreen screen))return;
  var widgets=screen.children().stream().filter(c->c instanceof AbstractWidget).map(c->(AbstractWidget)c).toList();
  var shortcut=widgets.stream().filter(c->c instanceof Shortcut).findFirst().orElse(null);if(shortcut==null)return;
  var place=placement(screen,widgets);shortcut.visible=place!=null;shortcut.active=place!=null;
  if(place!=null&&(shortcut.getX()!=place[0]||shortcut.getY()!=place[1])){shortcut.setX(place[0]);shortcut.setY(place[1]);}
 }
 private static int[] placement(PauseScreen screen,List<AbstractWidget> widgets){
  String caption=Component.translatable("gui.advancements").getString();
  var anchor=widgets.stream().filter(c->!(c instanceof Shortcut)&&c.getMessage().getString().equals(caption)).findFirst().orElse(null);if(anchor==null)return new int[]{Math.max(4,screen.width/2-126),Math.max(4,screen.height/4+8)};
  // Stay on the Advancements row, preferring the immediately adjacent left position.
  for(int offset=24;offset<screen.width;offset+=24){int left=anchor.getX()-offset,right=anchor.getX()+anchor.getWidth()+offset-20,y=anchor.getY();if(fits(screen,widgets,left,y))return new int[]{left,y};if(fits(screen,widgets,right,y))return new int[]{right,y};}
  // On narrow screens reserve a real slot on the Advancements row instead of hiding.
  if(anchor.getWidth()>48){int x=anchor.getX();anchor.setX(x+24);anchor.setWidth(anchor.getWidth()-24);return new int[]{x,anchor.getY()};}
  return new int[]{Math.max(4,Math.min(screen.width-24,anchor.getX()-24)),anchor.getY()};
 }
 private static boolean fits(PauseScreen screen,List<AbstractWidget> widgets,int x,int y){
  if(x<4||y<4||x+20>screen.width-4||y+20>screen.height-4)return false;
  for(var widget:widgets)if(!(widget instanceof Shortcut)&&widget.visible&&x<widget.getX()+widget.getWidth()+2&&x+22>widget.getX()&&y<widget.getY()+widget.getHeight()+2&&y+22>widget.getY())return false;
  return true;
 }
 private static final class Shortcut extends ConfigPanelButton {
  private final ItemStack icon=new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(SbwDroneRangeConfig.MONITOR_ITEM_ID)));
  Shortcut(int x,int y,PauseScreen parent){super(x,y,20,20,Component.empty(),b->Minecraft.getInstance().setScreen(new ConfigLandingScreen(parent)));setTooltip(Tooltip.create(Component.literal("Drone Warfare Config")));}
  @Override protected void renderWidget(GuiGraphics g,int mx,int my,float tick){super.renderWidget(g,mx,my,tick);g.renderItem(icon,getX()+2,getY()+2);}
 }
}
