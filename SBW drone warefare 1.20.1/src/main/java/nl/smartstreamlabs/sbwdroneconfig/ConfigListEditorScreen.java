package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import java.util.*;
/** A detached, virtualized list editor. Done stages the whole validated list; Escape discards it. */
public final class ConfigListEditorScreen extends ConfigBaseScreen {
 private final Screen parent;private final ConfigCatalog.Definition definition;private final ConfigEditDraft draft;
 private final List<String> values=new ArrayList<>();private final List<AbstractWidget> rows=new ArrayList<>();private final boolean strings;private int first;private String error="";
 public ConfigListEditorScreen(Screen parent,ConfigCatalog.Definition definition,ConfigEditDraft draft){super(ConfigPresentation.name(definition));this.parent=parent;this.definition=definition;this.draft=draft;var current=(List<?>)draft.current(definition.id());strings=current.isEmpty()?definition.rules().test(List.of("")):current.stream().allMatch(v->v instanceof String);for(Object v:current)values.add(strings?v.toString():new com.google.gson.Gson().toJson(v));}
 private static Component t(String key,Object... args){return ConfigEnglish.text("config.sbwdroneconfig.ui."+key,args);}
 @Override protected void init(){clearWidgets();rows.clear();int w=Math.max(30,(width-24)/3);addRenderableWidget(ConfigPanelButton.create(t("add_entry"),b->{values.add("");first=Math.max(0,values.size()-count());layout();}).bounds(8,height-26,w,20).build());addRenderableWidget(ConfigPanelButton.create(t("cancel"),b->onClose()).bounds(12+w,height-26,w,20).build());addRenderableWidget(ConfigPanelButton.create(t("stage_list"),b->{try{draft.set(definition.id(),strings?new com.google.gson.Gson().toJson(values):"["+String.join(",",values)+"]");minecraft.setScreen(parent);}catch(RuntimeException e){error=Objects.toString(e.getMessage(),"Invalid list");}}).bounds(16+w*2,height-26,w,20).build());layout();}
 private int count(){return Math.max(1,(height-100)/24);}
 private void layout(){for(var r:rows)removeWidget(r);rows.clear();first=Math.max(0,Math.min(first,Math.max(0,values.size()-count())));for(int i=first;i<Math.min(values.size(),first+count());i++){final int index=i;int y=49+(i-first)*24;var edit=new EditBox(font,10,y,Math.max(30,width-58),20,t("list_entry",i+1));edit.setMaxLength(Integer.MAX_VALUE);edit.setValue(values.get(i));edit.setResponder(v->values.set(index,v));rows.add(addRenderableWidget(edit));rows.add(addRenderableWidget(ConfigPanelButton.create(Component.literal("×"),b->{values.remove(index);layout();}).bounds(width-42,y,30,20).build()));}}
 @Override public boolean mouseScrolled(double x,double y,double delta){first+=delta<0?1:-1;layout();return true;}
 @Override public void render(GuiGraphics g,int mx,int my,float tick){g.fill(0,0,width,height,0xff10212a);g.drawString(font,font.plainSubstrByWidth(title.getString(),width-20),10,10,0xe9f0f3,false);g.drawString(font,t("list_count",values.size()),10,28,0xa0b8c5,false);g.drawString(font,font.plainSubstrByWidth(error,width-20),10,height-42,0xff8888,false);super.render(g,mx,my,tick);}
 @Override public void onClose(){minecraft.setScreen(parent);}
}
