package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class OtherModsConfigScreen extends ConfigBaseScreen {
 private final Screen parent;private final List<OtherModsConfigAccess.Entry> entries;private final List<AbstractWidget> rows=new ArrayList<>();private final ConfigModIcons icons=new ConfigModIcons();private List<OtherModsConfigAccess.Entry> filtered;private String query="",error="";private int first;
 public OtherModsConfigScreen(Screen parent){super(t("other_mods"));this.parent=parent;List<OtherModsConfigAccess.Entry> found;try{found=OtherModsConfigAccess.entries();}catch(RuntimeException e){found=List.of();error=e.getMessage();}entries=found;filtered=entries;}
 private static Component t(String key){return ConfigEnglish.text("config.sbwdroneconfig.ui."+key);}
 @Override protected void init(){clearWidgets();rows.clear();var search=addRenderableWidget(new EditBox(font,12,31,width-24,20,t("search_mods")));search.setMaxLength(256);search.setValue(query);search.setHint(t("search_mods"));search.setResponder(value->{query=value;String term=value.toLowerCase(Locale.ROOT);filtered=entries.stream().filter(e->(e.name()+" "+e.id()).toLowerCase(Locale.ROOT).contains(term)).toList();first=0;layout();});addRenderableWidget(ConfigPanelButton.create(t("back"),b->onClose()).bounds(12,height-29,width-24,20).build());layout();}
 private int visible(){return Math.max(1,(height-98)/31);}
 private void layout(){for(var row:rows)removeWidget(row);rows.clear();first=Math.max(0,Math.min(first,Math.max(0,filtered.size()-visible())));for(int i=first;i<Math.min(filtered.size(),first+visible());i++){var entry=filtered.get(i);icons.prepare(entry);var button=ConfigPanelButton.create(Component.literal(entry.name()),b->open(entry)).bounds(40,60+(i-first)*31,width-52,25).build();button.setTooltip(Tooltip.create(Component.literal(entry.id()+"\n").append(t(OtherModsConfigAccess.nativeAllowed(entry)?"native_mod_screen":"registered_specs"))));rows.add(addRenderableWidget(button));}}
 private void open(OtherModsConfigAccess.Entry entry){try{if(OtherModsConfigAccess.nativeAllowed(entry))minecraft.setScreen(OtherModsConfigAccess.nativeScreen(entry,this));else minecraft.setScreen(new OtherModFilesScreen(this,entry));}catch(RuntimeException e){error=Objects.toString(e.getMessage(),e.getClass().getSimpleName());}}
 @Override public boolean mouseScrolled(double x,double y,double delta){first+=delta<0?1:-1;layout();return true;}
 @Override public void render(GuiGraphics g,int mx,int my,float tick){g.fill(0,0,width,height,0xe5101b23);g.drawCenteredString(font,title,width/2,12,0xd8e9f0);for(int i=first;i<Math.min(filtered.size(),first+visible());i++){var entry=filtered.get(i);int y=60+(i-first)*31;g.fill(12,y,35,y+25,0xff263d49);if(!icons.draw(g,entry.id(),12,y+1,23))g.drawCenteredString(font,entry.name().substring(0,1).toUpperCase(Locale.ROOT),23,y+8,0xbcd7e5);}g.drawString(font,font.plainSubstrByWidth(error,width-24),12,height-44,0xff9999,false);super.render(g,mx,my,tick);}
 @Override public void removed(){icons.close();super.removed();}
 @Override public void onClose(){minecraft.setScreen(parent);}
}
