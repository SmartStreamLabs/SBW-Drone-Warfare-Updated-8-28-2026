package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;import java.util.*;import java.io.*;import net.minecraft.server.MinecraftServer;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.*;import net.minecraftforge.registries.ForgeRegistries;

/** Immutable catalog rebuilt on datapack sync, never by per-machine tick scans. */
public final class DwfRecipes{
 public record Requirement(Ingredient ingredient,long count){public Requirement{if(count<1)throw new IllegalArgumentException();}}
 public record Entry(ResourceLocation id,ItemStack output,List<Requirement>inputs,int ticks,String category){
  public Entry{output=output.copy();inputs=groupRequirements(inputs);if(output.isEmpty()||inputs.isEmpty()||ticks<1||inputs.size()>64)throw new IllegalArgumentException("Invalid fabricator recipe "+id);long units=0;for(var input:inputs)units=Math.addExact(units,input.count());}
  public ItemStack product(){return output.copy();}
 }
 public record Catalog(List<Entry>entries,Map<ResourceLocation,Entry>byId,List<ResourceLocation>missing,long version){public Catalog{entries=List.copyOf(entries);byId=Map.copyOf(byId);missing=List.copyOf(missing);}}
 private static final Set<String>DEPENDENCIES=Set.of("superbwarfare:monitor","superbwarfare:battery","superbwarfare:small_battery_pack","superbwarfare:large_battery_pack","superbwarfare:cell","superbwarfare:small_shell_aa");
 private static long sequence;
 public static Catalog build(MinecraftServer server){
  var entries=new LinkedHashMap<ResourceLocation,Entry>();var priorities=new HashMap<ResourceLocation,Integer>();var resources=server.getResourceManager();
  for(var recipe:server.getRecipeManager().getRecipes()){
   var output=recipe.getResultItem(server.registryAccess());if(output.isEmpty()||!own(output))continue;
   var requirements=new ArrayList<Requirement>();for(var ingredient:recipe.getIngredients())if(!ingredient.isEmpty())requirements.add(new Requirement(ingredient,1));
   if(!requirements.isEmpty())entries.put(recipe.getId(),new Entry(recipe.getId(),output,requirements,duration(output),category(output)));
  }
  // SBW's native assembling recipes encode an ENTITY result, not an ItemStack.
  // Preserve their authored costs and initialize the normal container item.
  for(var resource:resources.listResources("recipes",p->p.getPath().endsWith(".json")&&(p.getNamespace().equals(SbwDroneRangeConfig.MOD_ID)||p.getNamespace().equals(SbwDroneRangeConfig.SBW_MOD_ID))).entrySet()){
   try(var reader=resource.getValue().openAsReader()){
    var json=JsonParser.parseReader(reader).getAsJsonObject();if(!json.has("type")||!json.get("type").getAsString().equals("superbwarfare:vehicle_assembling"))continue;
    var result=json.getAsJsonObject("result");if(result==null)continue;ItemStack output;if(result.has("entity")){if(!result.get("entity").getAsString().startsWith(SbwDroneRangeConfig.MOD_ID+":"))continue;var entity=ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(result.get("entity").getAsString()));if(entity==null)throw new IllegalArgumentException("Unknown result entity");
    output=com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(entity);}else if(result.has("item")){String product=result.get("item").getAsString();if(!product.startsWith(SbwDroneRangeConfig.MOD_ID+":")&&!DEPENDENCIES.contains(product))continue;output=result(result);}else continue;var requirements=new ArrayList<Requirement>();
    for(var value:json.getAsJsonArray("inputs")){String input=value.getAsString().trim();long count=1;int split=input.indexOf(' ');if(split>0){count=Long.parseLong(input.substring(0,split));input=input.substring(split+1).trim();}var ingredient=new JsonObject();ingredient.addProperty(input.startsWith("#")?"tag":"item",input.startsWith("#")?input.substring(1):input);requirements.add(new Requirement(Ingredient.fromJson(ingredient),count));}
    String path=resource.getKey().getPath();var id=ResourceLocation.tryParse(resource.getKey().getNamespace()+":"+path.substring(8,path.length()-5));entries.put(id,new Entry(id,output,requirements,2400,"Vehicles"));priorities.put(id,1);
   }catch(IOException|RuntimeException failure){throw new IllegalStateException("Invalid native vehicle recipe "+resource.getKey(),failure);}
  }
  var aliases=new LinkedHashMap<ResourceLocation,JsonObject>();
  for(var resource:resources.listResources("dwf_assembly",p->p.getPath().endsWith(".json")).entrySet())try(var reader=resource.getValue().openAsReader()){
   var json=JsonParser.parseReader(reader).getAsJsonObject();String path=resource.getKey().getPath();var id=ResourceLocation.tryParse(resource.getKey().getNamespace()+":"+path.substring(13,path.length()-5));
   if(json.has("source")){aliases.put(id,json);continue;}entries.put(id,explicit(id,json));priorities.put(id,2);
  }catch(IOException|RuntimeException failure){throw new IllegalStateException("Invalid fabricator recipe "+resource.getKey(),failure);}
  for(var alias:aliases.entrySet()){
   var json=alias.getValue();var source=entries.get(ResourceLocation.tryParse(json.get("source").getAsString()));if(source==null)throw new IllegalStateException("Missing source recipe for "+alias.getKey());
   var output=result(json.getAsJsonObject("result"));entries.put(alias.getKey(),new Entry(alias.getKey(),output,source.inputs(),json.has("ticks")?json.get("ticks").getAsInt():source.ticks(),category(output)));
  }
  var covered=new HashSet<ResourceLocation>();for(var e:entries.values())covered.add(ForgeRegistries.ITEMS.getKey(e.output().getItem()));
  var missing=new ArrayList<ResourceLocation>();for(var id:ForgeRegistries.ITEMS.getKeys())if(id.getNamespace().equals(SbwDroneRangeConfig.MOD_ID)&&!covered.contains(id))missing.add(id);
  for(String dependency:DEPENDENCIES){var id=ResourceLocation.tryParse(dependency);if(ForgeRegistries.ITEMS.containsKey(id)&&!covered.contains(id))missing.add(id);}
  // Inspect generic registration types, not creative-tab order and never by
  // instantiating a gameplay entity just to discover its class.
  var vehicles=new HashSet<ResourceLocation>();for(var entry:entries.values()){var stack=entry.output();var itemId=ForgeRegistries.ITEMS.getKey(stack.getItem());if(itemId!=null&&ForgeRegistries.ENTITY_TYPES.containsKey(itemId))vehicles.add(itemId);var data=net.minecraft.world.item.BlockItem.getBlockEntityData(stack); if(data!=null){var vehicle=ResourceLocation.tryParse(data.getString("EntityType"));if(vehicle!=null)vehicles.add(vehicle);}}
  for(var field:AddonEntities.class.getFields())if(vehicleType(field.getGenericType()))try{Object value=field.get(null);if(value instanceof java.util.function.Supplier<?>supplier&&supplier.get()instanceof net.minecraft.world.entity.EntityType<?>type){var id=ForgeRegistries.ENTITY_TYPES.getKey(type);if(id!=null&&!vehicles.contains(id)&&!missing.contains(id))missing.add(id);}}catch(ReflectiveOperationException failure){throw new IllegalStateException("Cannot audit vehicle registration "+field.getName(),failure);}
  missing.sort(Comparator.comparing(Object::toString));// Keep every recipe ID resolvable for existing saved orders, but display one
  // canonical recipe per full product variant. Legacy item aliases are migration-only.
  var variants=new HashMap<DwfItemKey,DwfItemKey>();for(var alias:aliases.entrySet())if(alias.getKey().getPath().startsWith("legacy_")){var old=entries.get(alias.getKey());var current=entries.get(ResourceLocation.tryParse(alias.getValue().get("source").getAsString()));if(old!=null&&current!=null)variants.put(productKey(old.output()),productKey(current.output()));}
  var canonical=new LinkedHashMap<DwfItemKey,Entry>();var ordered=new ArrayList<>(entries.values());ordered.sort(Comparator.comparing(e->e.id().toString()));
  for(var entry:ordered){if(entry.id().getNamespace().equals(SbwDroneRangeConfig.MOD_ID)&&entry.id().getPath().startsWith("legacy_"))continue;var raw=productKey(entry.output());var key=variants.getOrDefault(raw,raw);var previous=canonical.get(key);boolean currentForm=raw.equals(key);boolean previousForm=previous!=null&&productKey(previous.output()).equals(key);if(previous==null||currentForm&&!previousForm||currentForm==previousForm&&priorities.getOrDefault(entry.id(),0)>priorities.getOrDefault(previous.id(),0))canonical.put(key,entry);}
  var list=new ArrayList<>(canonical.values());list.sort(Comparator.comparing(e->e.id().toString()));
  if(!missing.isEmpty())SbwDroneRangeConfig.LOGGER.error("DWF recipe coverage missing {} registered products (items or vehicles): {}",missing.size(),missing);
  return new Catalog(list,entries,missing,++sequence);
 }
 // The native container writer adds its block-entity type ID; legacy crafting
 // results omit that redundant ID. It is serialization metadata, not a skin.
 // Normalize only the catalogue comparison, never storage keys or crafted stacks.
 private static DwfItemKey productKey(ItemStack stack){
  var copy=stack.copy();var id=ForgeRegistries.ITEMS.getKey(copy.getItem());
  if(id!=null&&id.toString().equals("superbwarfare:container")){
   var tag=net.minecraft.world.item.BlockItem.getBlockEntityData(copy);
   if(tag!=null&&tag.getString("id").equals("superbwarfare:container"))tag.remove("id");
  }
  return new DwfItemKey(copy);
 }
 private static Entry explicit(ResourceLocation id,JsonObject json){
  var output=result(json.getAsJsonObject("result"));var inputs=new ArrayList<Requirement>();for(var value:json.getAsJsonArray("ingredients")){var input=value.getAsJsonObject();inputs.add(new Requirement(Ingredient.fromJson(input.get("ingredient")),input.has("count")?input.get("count").getAsLong():1));}
  return new Entry(id,output,inputs,json.get("ticks").getAsInt(),json.has("category")?json.get("category").getAsString():category(output));
 }
 private static ItemStack result(JsonObject json){var stack=net.minecraftforge.common.crafting.CraftingHelper.getItemStack(json,true);if(stack.isEmpty()||stack.getCount()<1)throw new IllegalArgumentException("Invalid product");return stack;}
 private static boolean own(ItemStack stack){var id=ForgeRegistries.ITEMS.getKey(stack.getItem());if(id!=null&&(id.getNamespace().equals(SbwDroneRangeConfig.MOD_ID)||DEPENDENCIES.contains(id.toString())))return true;var n=stack.getTag();return n!=null&&n.getCompound("BlockEntityTag").getString("EntityType").startsWith(SbwDroneRangeConfig.MOD_ID+":");}
 private static String category(ItemStack stack){var key=ForgeRegistries.ITEMS.getKey(stack.getItem());String name=key==null?"":key.getPath();if(stack.getItem()instanceof ArmorItem)return "Equipment";if(name.equals("container")||key!=null&&ForgeRegistries.ENTITY_TYPES.containsKey(key))return "Vehicles";if(stack.getItem()instanceof BlockItem)return "Installations";if(name.contains("shell")||name.contains("grenade")||name.contains("missile")||name.contains("payload")||name.contains("launcher"))return "Payloads and weapons";return "Components";}
 private static int duration(ItemStack stack){return category(stack).equals("Vehicles")?1200:category(stack).equals("Installations")?600:200;}
 private static boolean vehicleType(java.lang.reflect.Type type){if(type instanceof Class<?>c)return com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity.class.isAssignableFrom(c);if(type instanceof java.lang.reflect.WildcardType w)for(var bound:w.getUpperBounds())if(vehicleType(bound))return true;if(type instanceof java.lang.reflect.ParameterizedType p)for(var arg:p.getActualTypeArguments())if(vehicleType(arg))return true;return false;}
 public static List<Requirement> groupRequirements(List<Requirement> inputs){
  var groups=new LinkedHashMap<String,Requirement>();for(var input:inputs){var ingredient=input.ingredient();String key=ingredient.toJson().toString();var previous=groups.get(key);groups.put(key,previous==null?input:new Requirement(previous.ingredient(),Math.addExact(previous.count(),input.count())));}return List.copyOf(groups.values());
 }
 private DwfRecipes(){}
}
