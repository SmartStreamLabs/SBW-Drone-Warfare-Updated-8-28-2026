package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.config.ModConfig;
import java.util.*;

/** Only visible cards have widgets. Filtering and layout run on changes, never on each render frame. */
public final class ConfigWorkspaceScreen extends ConfigBaseScreen {
 private final Screen parent;
 private final List<ConfigCatalog.Definition> definitions;
 private final ConfigEditDraft draft;
 private final Map<String,ConfigCatalog.Definition> byId=new HashMap<>();
 private final ModConfig external;
 private final Map<String,String> searchIndex=new HashMap<>(),invalid=new HashMap<>(),rawInputs=new HashMap<>();
 private final Map<Object,Long> baselines=new IdentityHashMap<>();
 private final Map<String,ConfigRemoteClient.State> observed=new HashMap<>();
 private final List<AbstractWidget> dynamic=new ArrayList<>();
 private final List<Card> cards=new ArrayList<>();
 private final List<String> categories;
 private List<ConfigCatalog.Definition> filtered=List.of();
 private EditBox search;private Button apply,advancedButton,changedButton;private boolean advanced,changed,initial=true;
 private String query="",category="",message="";private int first,categoryFirst,side,top,bottom,content;private long remoteSequence=-1;
 private record Card(ConfigCatalog.Definition definition,int y){}
 public ConfigWorkspaceScreen(Screen parent){this(parent,null);}
 public ConfigWorkspaceScreen(Screen parent,ModConfig.Type scope){
  this(parent,scope==null?t("title"):t(scope.name().toLowerCase(Locale.ROOT)+"_config"),ConfigCatalog.definitions().stream().filter(d->!d.managed()&&ConfigAvailability.active(d)&&(scope==null||d.scope()==scope)).toList(),null);
 }
 public ConfigWorkspaceScreen(Screen parent,ModConfig external,List<ConfigCatalog.Definition> definitions){this(parent,Component.literal(external.getModId()+" / "+external.getFileName()),definitions.stream().filter(d->!d.managed()&&ConfigAvailability.active(d)).toList(),external);}
 private ConfigWorkspaceScreen(Screen parent,Component title,List<ConfigCatalog.Definition> definitions,ModConfig external){
  super(title);this.parent=parent;this.definitions=List.copyOf(definitions);this.external=external;draft=new ConfigEditDraft(definitions);for(var d:definitions)byId.put(d.id(),d);
  var groups=new TreeSet<String>();for(var d:definitions){String group=ConfigPresentation.category(d);groups.add(group);searchIndex.put(d.id(),(d.id()+" "+group+" "+ConfigPresentation.name(d).getString()+" "+ConfigPresentation.help(d).getString()).toLowerCase(Locale.ROOT));baselines.put(d.owner(),ConfigLifecycle.revision(d.owner()));}categories=new ArrayList<>();categories.add("");categories.addAll(groups);
 }
 private static Component t(String key,Object... args){return ConfigEnglish.text("config.sbwdroneconfig.ui."+key,args);}
 @Override protected void init(){
  clearWidgets();dynamic.clear();cards.clear();side=Math.min(154,Math.max(88,width/4));content=side+12;top=65;bottom=height-57;
  search=addRenderableWidget(new EditBox(font,8,25,Math.max(80,width-16),20,t("search")));search.setMaxLength(512);search.setValue(query);search.setHint(t("search"));search.setResponder(value->{query=value;first=0;filter();});
  int controls=Math.max(40,(width-24)/4);advancedButton=addRenderableWidget(ConfigPanelButton.create(t(advanced?"advanced":"basic"),b->{advanced=!advanced;first=0;filter();}).bounds(8,height-49,controls,20).build());
  changedButton=addRenderableWidget(ConfigPanelButton.create(t(changed?"changed_only":"all_values"),b->{changed=!changed;first=0;filter();}).bounds(12+controls,height-49,controls,20).build());
  addRenderableWidget(ConfigPanelButton.create(t("copy_preset"),b->{minecraft.keyboardHandler.setClipboard(draft.exportPreset(false));message=t("preset_copied").getString();}).bounds(16+controls*2,height-49,controls,20).build());
  addRenderableWidget(ConfigPanelButton.create(t("paste_preset"),b->importPreset()).bounds(20+controls*3,height-49,controls,20).build());
  int buttonWidth=Math.max(36,(width-24)/3);
  addRenderableWidget(ConfigPanelButton.create(t("reset_category"),b->{for(var d:definitions)if((category.isEmpty()||ConfigPresentation.category(d).equals(category))&&editable(d)){draft.reset(d.id());rawInputs.remove(d.id());invalid.remove(d.id());}filter();}).bounds(8,height-25,buttonWidth,20).build());
  addRenderableWidget(ConfigPanelButton.create(t("cancel"),b->onClose()).bounds(12+buttonWidth,height-25,buttonWidth,20).build());
  apply=addRenderableWidget(ConfigPanelButton.create(t("apply"),b->apply()).bounds(16+buttonWidth*2,height-25,buttonWidth,20).build());
  filter();if(initial){initial=false;if(external==null)ConfigRemoteClient.refresh();}
 }
 private boolean localScope(ConfigCatalog.Definition d){return d.scope()==ModConfig.Type.CLIENT||d.scope()==ModConfig.Type.COMMON&&(!ConfigRemoteClient.connected()||minecraft.getSingleplayerServer()!=null);}
 private boolean editable(ConfigCatalog.Definition d){if(!d.loaded()||!ConfigPresentation.supported(d.defaults()))return false;if(external!=null)return d.scope()==ModConfig.Type.CLIENT||d.scope()==ModConfig.Type.COMMON&&(!ConfigRemoteClient.connected()||minecraft.getSingleplayerServer()!=null);if(localScope(d))return true;var state=ConfigRemoteClient.state(d.file());return state!=null&&state.writable()&&state.values().containsKey(d.id());}
 private Component reason(ConfigCatalog.Definition d){if(d.scope().name().equals("STARTUP"))return t("startup_readonly");if(!ConfigPresentation.supported(d.defaults()))return t("unsupported_type");if(!d.loaded())return t("join_world");if(external!=null&&!editable(d))return t("external_remote_readonly");if(d.scope()==ModConfig.Type.COMMON&&!editable(d))return t("external_remote_readonly");if(d.scope()==ModConfig.Type.SERVER&&!editable(d))return t(ConfigRemoteClient.connected()?"operator_only":"join_world");return t("editable");}
 private void filter(){
  String term=query.strip().toLowerCase(Locale.ROOT);filtered=definitions.stream().filter(d->(category.isEmpty()||ConfigPresentation.category(d).equals(category))&&(term.isEmpty()||searchIndex.get(d.id()).contains(term))&&(advanced||!ConfigPresentation.advanced(d))&&(!changed||draft.changed(d.id())||!Objects.equals(draft.current(d.id()),d.defaults()))).toList();
  first=Math.max(0,Math.min(first,Math.max(0,filtered.size()-visibleCards())));advancedButton.setMessage(t(advanced?"advanced":"basic"));changedButton.setMessage(t(changed?"changed_only":"all_values"));layout();
 }
 private int visibleCards(){return Math.max(1,(bottom-top)/75);}
 private int visibleCategories(){return Math.max(1,(bottom-top)/22);}
 private void addDynamic(AbstractWidget widget){dynamic.add(addRenderableWidget(widget));}
 private void layout(){
  for(var widget:dynamic)removeWidget(widget);dynamic.clear();cards.clear();
  categoryFirst=Math.max(0,Math.min(categoryFirst,Math.max(0,categories.size()-visibleCategories())));
  for(int i=categoryFirst;i<Math.min(categories.size(),categoryFirst+visibleCategories());i++){String group=categories.get(i);var b=ConfigPanelButton.create(group.isEmpty()?t("all_categories"):ConfigPresentation.categoryName(group),button->{category=group;first=0;filter();}).bounds(8,top+(i-categoryFirst)*22,side-15,20).build();b.setTooltip(Tooltip.create(group.isEmpty()?t("all_categories"):ConfigPresentation.categoryName(group)));b.active=!category.equals(group);addDynamic(b);}
  int available=width-content-10;for(int i=first;i<Math.min(filtered.size(),first+visibleCards());i++){
   var d=filtered.get(i);int y=top+(i-first)*75;cards.add(new Card(d,y));int resetWidth=42;int inputWidth=Math.max(35,available-resetWidth-4);Object value=draft.current(d.id());
   AbstractWidget widget;
   if(value instanceof Boolean){widget=ConfigPanelButton.create(t(Boolean.TRUE.equals(value)?"on":"off"),b->{draft.set(d.id(),(!Boolean.TRUE.equals(draft.current(d.id())))+"");filter();}).bounds(content,y+19,inputWidth,20).build();}
   else if(!d.choices().isEmpty()){widget=ConfigPanelButton.create(ConfigPresentation.formatted(d,value),b->{var choices=d.choices();draft.set(d.id(),choices.get((choices.indexOf(ConfigWire.text(draft.current(d.id())))+1)%choices.size()));filter();}).bounds(content,y+19,inputWidth,20).build();}
   else if(value instanceof List<?>){widget=ConfigPanelButton.create(t("edit_list",((List<?>)value).size()),b->minecraft.setScreen(new ConfigListEditorScreen(this,d,draft))).bounds(content,y+19,inputWidth,20).build();}
   else {var field=new EditBox(font,content,y+19,inputWidth,20,ConfigPresentation.name(d));field.setMaxLength(Integer.MAX_VALUE);field.setValue(rawInputs.getOrDefault(d.id(),ConfigWire.text(value)));field.setResponder(text->{rawInputs.put(d.id(),text);try{draft.set(d.id(),text);invalid.remove(d.id());field.setTextColor(0xe8edf0);}catch(RuntimeException e){invalid.put(d.id(),Objects.toString(e.getMessage(),"Invalid value"));field.setTextColor(0xff7777);}updateApply();});widget=field;}
   widget.active=editable(d);widget.setTooltip(Tooltip.create(settingTooltip(d)));addDynamic(widget);
   var reset=ConfigPanelButton.create(t("reset"),b->{draft.reset(d.id());rawInputs.remove(d.id());invalid.remove(d.id());filter();}).bounds(content+inputWidth+4,y+19,resetWidth,20).build();reset.active=editable(d);addDynamic(reset);
   if(value instanceof Double&&isSlider(d)){
    double min=Double.parseDouble(d.minimum()),max=Double.parseDouble(d.maximum());var slider=new AbstractSliderButton(content,y+42,available,12,Component.empty(),Math.max(0,Math.min(1,(((Number)value).doubleValue()-min)/(max-min)))){
     protected void updateMessage(){}protected void applyValue(){draft.set(d.id(),Double.toString(min+this.value*(max-min)));rawInputs.remove(d.id());updateApply();}
     @Override public void onRelease(double x,double mouseY){super.onRelease(x,mouseY);layout();}
    };slider.active=editable(d);addDynamic(slider);
   }
  }
  updateApply();
 }
 private Component settingTooltip(ConfigCatalog.Definition d){
  var tooltip=Component.empty().append(ConfigPresentation.name(d)).append("\n").append(ConfigPresentation.help(d)).append("\n").append(t("configured_value",ConfigWire.text(d.current()))).append("\n").append(t(ConfigPresentation.reloadKey(d).equals("new_objects")?"new_object_value":"current_value",ConfigWire.text(ConfigRules.effective(d)))).append("\n").append(t("default_value",ConfigPresentation.formatted(d,d.defaults())));
  if(!d.minimum().isBlank()||!d.maximum().isBlank())tooltip.append("\n").append(t("bounds",d.minimum(),d.maximum()));
  if(!ConfigSemantics.details(d).unit().isBlank())tooltip.append("\n").append(t("unit_label",ConfigPresentation.unit(d)));
  if(!ConfigSemantics.details(d).requires().isEmpty())tooltip.append("\n").append(t("requires",ConfigPresentation.dependencies(d)));
  String inherited=VehicleProfilesConfig.inheritedFrom(d);if(!inherited.isEmpty()&&d.current() instanceof Number value&&value.doubleValue()==0){var general=ConfigCatalog.find(inherited);if(general!=null)tooltip.append("\n").append(t("inherited_from",ConfigPresentation.name(general)));}
  return tooltip.append("\n").append(t(ConfigPresentation.reloadKey(d))).append("\n").append(reason(d)).append("\n").append(d.file()+" / "+String.join(".",d.path()));
 }
 private static boolean isSlider(ConfigCatalog.Definition d){try{double min=Double.parseDouble(d.minimum()),max=Double.parseDouble(d.maximum());return Double.isFinite(min)&&Double.isFinite(max)&&min>=0&&max>min&&max<=100;}catch(NumberFormatException e){return false;}}
 private void updateApply(){if(apply!=null){apply.active=draft.changes()>0&&invalid.isEmpty();apply.setMessage(t("apply_count",draft.changes()));apply.setTooltip(message.isBlank()?null:Tooltip.create(Component.literal(message)));}}
 private void importPreset(){try{var incoming=new ConfigEditDraft(definitions);incoming.importPreset(minecraft.keyboardHandler.getClipboard());for(String id:incoming.pending().keySet())if(!editable(byId.get(id)))throw new SecurityException(t("preset_read_only",id).getString());draft.importPreset(minecraft.keyboardHandler.getClipboard());rawInputs.clear();invalid.clear();message=t("preset_staged").getString();filter();}catch(RuntimeException e){message=Objects.toString(e.getMessage(),"Invalid preset");}}
 private void apply(){
  try{
   if(external!=null){applyExternal();return;}
   var byFile=new LinkedHashMap<String,Map<String,String>>();var originals=new LinkedHashMap<String,Map<String,String>>();
   for(var e:draft.pending().entrySet()){var d=ConfigCatalog.find(e.getKey());if(!editable(d))throw new SecurityException(reason(d).getString());byFile.computeIfAbsent(d.file(),s->new LinkedHashMap<>()).put(d.id(),ConfigWire.text(e.getValue()));originals.computeIfAbsent(d.file(),s->new LinkedHashMap<>()).put(d.id(),ConfigWire.text(draft.original(d.id())));}
   var plans=new ArrayList<ConfigCommitPlan>();for(var entry:byFile.entrySet()){var d=ConfigCatalog.find(entry.getValue().keySet().iterator().next());if(localScope(d))plans.add(ConfigCommitPlan.prepare(baselines.get(d.owner()),ConfigLifecycle.revision(d.owner()),entry.getValue(),originals.get(entry.getKey()),v->localScope(v)&&v.loaded()));}
   for(var plan:plans){plan.execute(v->localScope(v)&&v.loaded());for(var d:plan.changes().keySet()){draft.observe(d.id(),d.current(),true);baselines.put(d.owner(),ConfigLifecycle.revision(d.owner()));rawInputs.remove(d.id());}}
   for(var entry:byFile.entrySet()){var d=ConfigCatalog.find(entry.getValue().keySet().iterator().next());if(d.scope()==ModConfig.Type.SERVER)ConfigRemoteClient.apply(entry.getKey(),entry.getValue(),originals.get(entry.getKey()));}
   message=t(draft.changes()>0?"awaiting_server":"saved").getString();filter();
  }catch(Exception e){message=Objects.toString(e.getMessage(),e.getClass().getSimpleName());filter();}
 }
 private void applyExternal()throws Exception {
  var changes=new LinkedHashMap<ConfigCatalog.Definition,Object>();
  for(var e:draft.pending().entrySet()){var d=byId.get(e.getKey());if(!editable(d))throw new SecurityException(reason(d).getString());if(!Objects.equals(d.current(),draft.original(d.id())))throw new ConcurrentModificationException("Configuration changed; reopen before applying");changes.put(d,e.getValue());}
  NativeConfigIo.commitKnown(external,changes);for(var d:changes.keySet()){draft.observe(d.id(),d.current(),true);rawInputs.remove(d.id());}message=t("saved").getString();filter();
 }
 @Override public void tick(){
  super.tick();if(external!=null||remoteSequence==ConfigRemoteClient.sequence())return;remoteSequence=ConfigRemoteClient.sequence();
  for(var d:definitions)if(d.scope()==ModConfig.Type.SERVER){var state=ConfigRemoteClient.state(d.file());if(state==null||state==observed.get(d.file()))continue;String value=state.values().get(d.id());if(value!=null){draft.observe(d.id(),d.parse(value),state.status().equals("applied"));if(!draft.changed(d.id()))rawInputs.remove(d.id());}}
  for(var d:definitions)if(d.scope()==ModConfig.Type.SERVER){var state=ConfigRemoteClient.state(d.file());if(state!=null)observed.put(d.file(),state);}message=friendlyNotice(ConfigRemoteClient.notice());filter();
 }
 private String friendlyNotice(String status){return switch(status){case "snapshot"->t("server_received").getString();case "updated"->t("server_updated").getString();case "applied"->t("saved").getString();default->{if(status.startsWith("conflict:"))yield t("server_conflict").getString();if(status.startsWith("denied:"))yield t("server_denied").getString();yield status.startsWith("error:")?t("apply_error",status.substring(6).strip()).getString():status;}};}
 @Override public boolean mouseScrolled(double x,double y,double delta){if(y>=top&&y<bottom){if(x<side+4){categoryFirst+=(delta<0?1:-1);layout();}else{first+=(delta<0?1:-1);first=Math.max(0,Math.min(first,Math.max(0,filtered.size()-visibleCards())));layout();}return true;}return super.mouseScrolled(x,y,delta);}
 @Override public boolean mouseClicked(double x,double y,int button){if(button==0&&scrollTo(x,y))return true;return super.mouseClicked(x,y,button);}
 @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){if(button==0&&scrollTo(x,y))return true;return super.mouseDragged(x,y,button,dx,dy);}
 private boolean scrollTo(double x,double y){if(y<top||y>=bottom)return false;double fraction=Math.max(0,Math.min(1,(y-top)/(bottom-top)));if(x>=side-5&&x<=side){categoryFirst=(int)Math.round(fraction*Math.max(0,categories.size()-visibleCategories()));layout();return true;}if(x>=width-6){first=(int)Math.round(fraction*Math.max(0,filtered.size()-visibleCards()));layout();return true;}return false;}
 private void scrollbar(GuiGraphics g,int x,int total,int visible,int offset){if(total<=visible)return;int track=bottom-top,thumb=Math.max(10,track*visible/total);int y=top+(track-thumb)*offset/Math.max(1,total-visible);g.fill(x,top,x+3,bottom,0xff283b45);g.fill(x,y,x+3,y+thumb,0xff6a8797);}
 @Override public void render(GuiGraphics g,int mx,int my,float tick){
  g.fill(0,0,width,height,0xff101d23);g.drawString(font,title,8,9,0xe9f0f3,false);
  g.drawString(font,t("result_count",filtered.size(),definitions.size()),content,51,0xa5b6bf,false);g.fill(side+4,top,side+5,bottom,0xff3d505a);
  for(var card:cards){var d=card.definition();int y=card.y();g.drawString(font,font.plainSubstrByWidth(ConfigPresentation.name(d).getString(),Math.max(20,width-content-12)),content,y,draft.changed(d.id())?0xffcf66:0xe6edf0,false);
   String scope=t(switch(d.scope()){case CLIENT->"local";case COMMON->"common";case SERVER->"server";default->"startup";}).getString();String info=invalid.containsKey(d.id())?invalid.get(d.id()):scope+" | "+t("default_value",ConfigPresentation.formatted(d,d.defaults())).getString()+" | "+t(ConfigPresentation.reloadKey(d)).getString();g.drawString(font,font.plainSubstrByWidth(info,Math.max(20,width-content-12)),content,y+58,invalid.containsKey(d.id())?0xff7777:0x97abb5,false);
  }
  scrollbar(g,side-4,categories.size(),visibleCategories(),categoryFirst);scrollbar(g,width-5,filtered.size(),visibleCards(),first);super.render(g,mx,my,tick);g.drawString(font,font.plainSubstrByWidth(message,Math.max(20,width-content-12)),content,Math.max(top,bottom-10),0xe1c77c,false);
 }
 @Override public void onClose(){draft.cancel();if(minecraft!=null)minecraft.setScreen(parent);}
}
