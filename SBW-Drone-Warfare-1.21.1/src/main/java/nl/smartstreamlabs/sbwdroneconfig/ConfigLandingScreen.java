package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;

public final class ConfigLandingScreen extends ConfigBaseScreen {
 private final Screen parent;
 public ConfigLandingScreen(Screen parent){super(Component.literal("SBW: Drone Warfare"));this.parent=parent;}
 private static Component t(String key){return ConfigEnglish.text("config.sbwdroneconfig.ui."+key);}
 @Override protected void init(){
  int w=Math.min(340,Math.max(150,width-80)),x=(width-w)/2;int h=Math.max(20,Math.min(30,(height-116)/4)),gap=Math.max(6,Math.min(12,height/40)),start=Math.max(74,(height-(h*3+gap*2))/2+10);
  addRoute(ModConfig.Type.CLIENT,"client_config",x,start,w,h);
  addRoute(ModConfig.Type.COMMON,"common_config",x,start+h+gap,w,h);
  addRoute(ModConfig.Type.SERVER,"server_config",x,start+(h+gap)*2,w,h);
  addRenderableWidget(ConfigPanelButton.create(t("other_mods"),b->minecraft.setScreen(new OtherModsConfigScreen(this))).bounds(x,Math.max(start+(h+gap)*3+6,height-h-16),w,h).build());
  var back=ConfigPanelButton.create(Component.literal("<<"),b->onClose()).bounds(Math.max(6,x-33),start+h+gap,26,26).build();back.setTooltip(Tooltip.create(t("back")));addRenderableWidget(back);
 }
 private void addRoute(ModConfig.Type scope,String label,int x,int y,int w,int h){
  boolean present=ConfigCatalog.definitions().stream().anyMatch(d->!d.managed()&&ConfigAvailability.active(d)&&d.scope()==scope);boolean available=present&&(scope!=ModConfig.Type.SERVER||minecraft.level!=null&&minecraft.getConnection()!=null);
  var button=ConfigPanelButton.create(t(label),b->minecraft.setScreen(new ConfigWorkspaceScreen(this,scope))).bounds(x,y,w,h).build();button.active=available;button.setTooltip(Tooltip.create(!present?t("no_config_type"):!available?t("join_world"):scope==ModConfig.Type.SERVER?t("server_route_help"):t("local_route_help")));addRenderableWidget(button);
 }
 @Override public void render(GuiGraphics g,int mx,int my,float tick){
  g.fill(0,0,width,height,0xb0101820);g.fill(0,8,width,65,0xc0081017);g.fill(0,7,width,8,0xff506977);g.fill(0,65,width,66,0xff506977);
  g.drawCenteredString(font,t("access_mod"),width/2,17,0x9fbccf);float scale=Math.min(2f,(width-24f)/Math.max(1,font.width(title)));g.pose().pushPose();try{g.pose().translate(width/2f,35,0);g.pose().scale(scale,scale,1);g.drawCenteredString(font,title,0,0,0xe6f1f5);}finally{g.pose().popPose();}super.render(g,mx,my,tick);
 }
 @Override public void onClose(){minecraft.setScreen(parent);}
}
