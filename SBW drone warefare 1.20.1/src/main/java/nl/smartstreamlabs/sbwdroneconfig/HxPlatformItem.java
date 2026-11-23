package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.chat.Component;import net.minecraft.world.item.*;import net.minecraft.world.level.Level;import java.util.List;
public class HxPlatformItem extends BlockItem{
 public HxPlatformItem(Properties p){super(AddonBlocks.HX_PLATFORM.get(),p);}
 @Override public void appendHoverText(ItemStack s,Level l,List<Component> lines,TooltipFlag f){lines.add(Component.translatable("hx2.platform_bio"));lines.add(Component.translatable("hx2.platform_controls"));}
}
