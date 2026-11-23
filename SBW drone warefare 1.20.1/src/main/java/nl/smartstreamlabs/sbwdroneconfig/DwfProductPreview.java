package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.MultiBufferSource;import net.minecraft.world.item.*;import software.bernie.geckolib.model.GeoModel;

/** Select once when the product changes. Known entity-backed legacy item
 * renderers are replaced by their real static geometry, never a proxy entity. */
public final class DwfProductPreview{
 public interface Preview{void draw(PoseStack poses,MultiBufferSource buffers,int light,int overlay);default void phase(float progress){}}
 private static final GeoModel<?>GAD=new Gad01Model(),SD=new Sd01Model(),PM=new Pm01Model(),HG=new Hg01Model();private static long epoch;private static java.util.Map<net.minecraft.resources.ResourceLocation,Descriptor>vehicles;
 private record Descriptor(net.minecraft.resources.ResourceLocation model,net.minecraft.resources.ResourceLocation texture){}
 public static void clear(){DwfStaticGeo.clear();vehicles=null;epoch++;}
 private static final class Geo implements Preview{
  private final GeoModel<?>provider;private float progress=1;public void phase(float value){progress=value;}private long generation=-1;private DwfStaticGeo mesh;
  Geo(GeoModel<?>provider){this.provider=provider;}
  public void draw(PoseStack poses,MultiBufferSource buffers,int light,int overlay){if(generation!=epoch){mesh=DwfStaticGeo.get(provider);generation=epoch;}mesh.draw(poses,buffers,light,overlay,progress<.3f?0:progress<.5f?1:progress<.65f?2:3);}
 }
 private static net.minecraft.resources.ResourceLocation id(String path){return net.minecraft.resources.ResourceLocation.tryParse("sbwdroneconfig:"+path);}
 private static void descriptors(){if(vehicles!=null)return;var result=new java.util.HashMap<net.minecraft.resources.ResourceLocation,Descriptor>();var resources=Minecraft.getInstance().getResourceManager();for(var entry:resources.listResources("sbw/vehicles",key->key.getPath().endsWith(".json")&&key.getNamespace().equals(SbwDroneRangeConfig.MOD_ID)).entrySet())try(var reader=entry.getValue().openAsReader()){var root=com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();if(!root.has("Model")||!root.get("Model").isJsonObject())continue;var model=root.getAsJsonObject("Model");if(!model.has("Model")||!model.has("Texture"))continue;var path=entry.getKey().getPath();var vehicle=net.minecraft.resources.ResourceLocation.tryParse(entry.getKey().getNamespace()+":"+path.substring("sbw/vehicles/".length(),path.length()-5));result.put(vehicle,new Descriptor(net.minecraft.resources.ResourceLocation.tryParse(model.get("Model").getAsString()),net.minecraft.resources.ResourceLocation.tryParse(model.get("Texture").getAsString())));}catch(java.io.IOException failure){throw new IllegalStateException("Cannot read assembly vehicle metadata "+entry.getKey(),failure);}vehicles=java.util.Map.copyOf(result);}
 private static Preview resources(net.minecraft.resources.ResourceLocation model,net.minecraft.resources.ResourceLocation texture){return new Preview(){float progress=1;public void phase(float value){progress=value;}long generation=-1;DwfStaticGeo mesh;public void draw(PoseStack poses,MultiBufferSource buffers,int light,int overlay){if(generation!=epoch){mesh=DwfStaticGeo.get(model,texture);generation=epoch;}mesh.draw(poses,buffers,light,overlay,progress<.3f?0:progress<.5f?1:progress<.65f?2:3);}};}
 private static Preview binary(String folder,String name,String texture){return new Preview(){float progress=1;public void phase(float value){progress=value;}long generation=-1;DwfStaticGeo mesh;public void draw(PoseStack poses,MultiBufferSource buffers,int light,int overlay){if(generation!=epoch){mesh=DwfStaticGeo.binary(id(folder+"/"+name+".json"),id(folder+"/"+name+".bin"),id(texture));generation=epoch;}mesh.draw(poses,buffers,light,overlay,progress<.3f?0:progress<.5f?1:progress<.65f?2:3);}};}
 private static Preview vehicle(String key){
  if(key.equals("sbwdroneconfig:naval_drone_carrier_ndc01"))return binary("ndc01","carrier","ndc01/carrier.png");
  if(key.equals("sbwdroneconfig:flamingo_fl01"))return binary("fl01","missile","fl01/missile.png");
  if(key.equals("sbwdroneconfig:submarine_drone_sd01"))return new Geo(SD);if(key.equals("sbwdroneconfig:patriot_missile_pm01"))return new Geo(PM);if(key.equals("sbwdroneconfig:hx2_drone"))return new Geo(new Hx2Model());
  if(key.equals("sbwdroneconfig:"+CubedFpvDroneDefinition.ID))return resources(CubedFpvDroneDefinition.model(),CubedFpvDroneDefinition.texture());
  if(key.equals("sbwdroneconfig:"+LucasDroneDefinition.ID))return resources(LucasDroneDefinition.model(),LucasDroneDefinition.texture());
  if(key.equals("sbwdroneconfig:"+DeliveryDroneDefinition.ID))return resources(DeliveryDroneDefinition.model(),DeliveryDroneDefinition.texture());
  if(key.equals("sbwdroneconfig:"+Geranium2Definition.ID))return resources(id("geo/lp02_geranium_reference.geo.json"),id("textures/item/geranium_2.png"));
  if(key.equals("sbwdroneconfig:"+HeavyBomberDroneDefinition.ID))return resources(HeavyBomberDroneDefinition.model(),HeavyBomberDroneDefinition.texture());
  if(key.equals("sbwdroneconfig:"+BayraktarTb2Definition.ID))return resources(BayraktarTb2Definition.model(),BayraktarTb2Definition.texture());
  descriptors();var descriptor=vehicles.get(net.minecraft.resources.ResourceLocation.tryParse(key));return descriptor==null?null:resources(descriptor.model(),descriptor.texture());
 }
 public static boolean gad(ItemStack stack){if(stack.getItem()instanceof Gad01Item)return true;var tag=net.minecraft.world.item.BlockItem.getBlockEntityData(stack);return tag!=null&&tag.getString("EntityType").equals("sbwdroneconfig:ground_assault_drone_gad01");}
 public static Preview resolve(ItemStack product){
  if(product.isEmpty())return (p,b,l,o)->{};
  var data=net.minecraft.world.item.BlockItem.getBlockEntityData(product);String type=data==null?"":data.getString("EntityType");if(type.isEmpty())type=net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(product.getItem()).toString();var vehicle=vehicle(type);if(vehicle!=null)return vehicle;
  if(product.getItem()instanceof Hg01Item)return new Geo(HG);if(product.getItem()instanceof Gad01Item)return new Geo(GAD);if(product.getItem()instanceof Sd01Item)return new Geo(SD);if(product.getItem()instanceof PatriotItem)return new Geo(PM);
  var stack=product.copy();stack.setCount(1);
  return (p,b,l,o)->{var m=Minecraft.getInstance();p.pushPose();p.scale(1.5f,1.5f,1.5f);p.translate(0,.5,0);m.getItemRenderer().renderStatic(stack,ItemDisplayContext.FIXED,l,o,p,b,m.level,0);p.popPose();};
 }
 private DwfProductPreview(){}
}
