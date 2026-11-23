package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.ChatFormatting;
/** The SBW container stores an entity ID, not the addon's item ID. Expand its own help on Shift. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class ItemBioTooltips {
 @net.minecraftforge.eventbus.api.SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void tooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent e){
  append(e.getItemStack(),e.getToolTip(),Screen.hasShiftDown());
 }
 public static void append(ItemStack stack,java.util.List<Component> lines,boolean expanded){
  ResourceLocation id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
  boolean container=stack.getItem() instanceof com.atsuishio.superbwarfare.item.container.ContainerBlockItem;
  if(container){var data=BlockItem.getBlockEntityData(stack);if(data==null)return;id=ResourceLocation.tryParse(data.getString("EntityType"));}
  if(id==null||!id.getNamespace().equals(SbwDroneRangeConfig.MOD_ID))return;
  String path=id.getPath(),key="bio.sbwdroneconfig."+path,prefix="tooltip.sbwdroneconfig."+path+".";
  String description="entity.sbwdroneconfig."+path+".description";if(!I18n.exists(key)&&!I18n.exists("info.sbwdroneconfig."+path)&&!I18n.exists(description))return;
  lines.removeIf(line->line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t&&(t.getKey().startsWith(prefix)||path.equals("hx2_drone")&&(t.getKey().equals("hx2.drone_bio")||t.getKey().equals("hx2.use_platform"))||container&&(t.getKey().equals("info.sbwdroneconfig."+path)||t.getKey().equals("des.superbwarfare.shift"))));
  if(container){
   if(!expanded){lines.add(Component.translatable("vehicle.help.shift").withStyle(ChatFormatting.AQUA));return;}
   String summary=I18n.exists("info.sbwdroneconfig."+path)?I18n.get("info.sbwdroneconfig."+path):"";
   String detail=I18n.exists(key)?I18n.get(key):I18n.exists(description)?I18n.get(description):"";
   if(!summary.isEmpty())add(lines,summary);
   if(!detail.isEmpty()&&!detail.equals(summary))add(lines,detail);
   add(lines,I18n.get("vehicle.help.exit",Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage().getString()));
  }else if(I18n.exists(key))add(lines,I18n.get(key));else if(I18n.exists(description))add(lines,I18n.get(description));
  if(path.equals("hx2_drone")||path.equals("hx2_launch_platform"))add(lines,I18n.get("hx2.flight_controls"));
  if(path.equals("quadruped_drone_qd01")&&expanded)add(lines,I18n.get("qd01.controls"));
  if(path.equals("heavy_ground_drone_hgd01"))add(lines,I18n.get("hgd01.cannon_help",VatagClient.weaponKeyName(),com.atsuishio.superbwarfare.init.ModKeyMappings.RELOAD.getTranslatedKeyMessage().getString(),com.atsuishio.superbwarfare.init.ModKeyMappings.FIRE.getTranslatedKeyMessage().getString()));
 }
 private static void add(java.util.List<Component> lines,String text){for(String paragraph:text.split("\n")){String line="";for(String word:paragraph.split(" ")){String next=line.isEmpty()?word:line+" "+word;if(!line.isEmpty()&&Minecraft.getInstance().font.width(next)>300){lines.add(Component.literal(line).withStyle(ChatFormatting.GRAY));line=word;}else line=next;}if(!line.isEmpty())lines.add(Component.literal(line).withStyle(ChatFormatting.GRAY));}}
 private ItemBioTooltips(){}
}
