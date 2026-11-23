package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.*;

public final class OtherModFilesScreen extends ConfigBaseScreen {
 private final Screen parent;private final OtherModsConfigAccess.Entry entry;private final List<AbstractWidget> rows=new ArrayList<>();private int first;private String error="";
 public OtherModFilesScreen(Screen parent,OtherModsConfigAccess.Entry entry){super(Component.literal(entry.name()));this.parent=parent;this.entry=entry;if(entry.configs().isEmpty())error=t("external_remote_readonly").getString();}
 private static Component t(String key){return ConfigEnglish.text("config.sbwdroneconfig.ui."+key);}
 @Override protected void init(){clearWidgets();rows.clear();addRenderableWidget(ConfigPanelButton.create(t("back"),b->onClose()).bounds(10,height-28,width-20,20).build());layout();}
 private int visible(){return Math.max(1,(height-88)/32);}
 private void layout(){for(var row:rows)removeWidget(row);rows.clear();first=Math.max(0,Math.min(first,Math.max(0,entry.configs().size()-visible())));for(int i=first;i<Math.min(entry.configs().size(),first+visible());i++){var config=entry.configs().get(i);var button=ConfigPanelButton.create(Component.literal(config.getType()+" — "+config.getFileName()),b->{try{if(!(config.getSpec() instanceof ForgeConfigSpec spec))throw new IllegalStateException(t("unsupported_type").getString());var definitions=ConfigCatalog.describe("external/"+entry.id()+"/"+config.getFileName(),config.getFileName(),config.getType(),spec);minecraft.setScreen(new ConfigWorkspaceScreen(this,config,definitions));}catch(RuntimeException e){error=Objects.toString(e.getMessage(),e.getClass().getSimpleName());}}).bounds(12,39+(i-first)*32,width-24,26).build();button.setTooltip(Tooltip.create(Component.literal(config.getFileName()).append("\n").append(t("external_scope_help"))));rows.add(addRenderableWidget(button));}}
 @Override public boolean mouseScrolled(double x,double y,double delta){first+=delta<0?1:-1;layout();return true;}
 @Override public void render(GuiGraphics g,int mx,int my,float tick){g.fill(0,0,width,height,0xe5101b23);g.drawCenteredString(font,title,width/2,12,0xd8e9f0);g.drawString(font,font.plainSubstrByWidth(error,width-24),12,height-44,0xff9999,false);super.render(g,mx,my,tick);}
 @Override public void onClose(){minecraft.setScreen(parent);}
}
