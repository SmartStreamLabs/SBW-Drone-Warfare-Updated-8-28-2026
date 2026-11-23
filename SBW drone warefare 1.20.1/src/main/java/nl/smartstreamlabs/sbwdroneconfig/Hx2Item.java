package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.chat.Component;import net.minecraft.world.*;import net.minecraft.world.item.*;import net.minecraft.world.item.context.UseOnContext;import net.minecraft.world.level.Level;import java.util.List;
/** HX-2 is safely deployed and recovered through its launcher; no duplicate loose spawn route. */
public class Hx2Item extends Item {
 public Hx2Item(Properties p){super(p);}
 @Override public InteractionResult useOn(UseOnContext c){var l=c.getLevel();var s=l.getBlockState(c.getClickedPos());if(s.getBlock() instanceof HxPlatformBlock)return HxPlatformBlock.interact(l,c.getClickedPos(),s,c.getPlayer(),c.getHand());if(c.getPlayer()!=null&&!l.isClientSide)c.getPlayer().displayClientMessage(Component.translatable("hx2.use_platform"),true);return InteractionResult.sidedSuccess(l.isClientSide);}
 @Override public void appendHoverText(ItemStack s,Level l,List<Component> lines,TooltipFlag f){lines.add(Component.translatable("hx2.drone_bio"));lines.add(Component.translatable("hx2.use_platform"));}
}
