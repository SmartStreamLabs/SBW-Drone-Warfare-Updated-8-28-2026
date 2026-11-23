package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.*;import net.minecraft.world.item.context.UseOnContext;import net.minecraft.world.level.Level;import net.minecraft.network.chat.Component;import java.util.List;
public final class NtItem extends Item {
 public NtItem(Properties p){super(p);}
 @Override public InteractionResult useOn(UseOnContext c){if(c.getLevel().isClientSide()){if(c.getPlayer()!=null&&c.getPlayer().isShiftKeyDown())NtClient.open();else NtClient.point(c.getClickLocation());}return InteractionResult.sidedSuccess(c.getLevel().isClientSide());}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){if(l.isClientSide())NtClient.open();return InteractionResultHolder.sidedSuccess(p.getItemInHand(h),l.isClientSide());}
 @Override public void appendHoverText(ItemStack s,Level l,List<Component> t,TooltipFlag f){t.add(Component.translatable("tooltip.sbwdroneconfig.nt01.1"));t.add(Component.translatable("tooltip.sbwdroneconfig.nt01.2"));}
}
