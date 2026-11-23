package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
public final class RubbleClearingItem extends Item {
    public RubbleClearingItem(){super(new Properties().durability(2048));}
    @Override public InteractionResult useOn(UseOnContext c){if(c.getPlayer()instanceof net.minecraft.server.level.ServerPlayer p)RubbleTools.area(p,c.getClickedPos());return InteractionResult.sidedSuccess(c.getLevel().isClientSide);}
    @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level l,net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand hand){if(p.isShiftKeyDown()&&p instanceof net.minecraft.server.level.ServerPlayer sp)RubbleTools.area(sp,p.blockPosition());return net.minecraft.world.InteractionResultHolder.sidedSuccess(p.getItemInHand(hand),l.isClientSide);}
    @Override public void appendHoverText(ItemStack s,Item.TooltipContext l,java.util.List<Component> lines,TooltipFlag f){lines.add(Component.translatable("tooltip.sbwdroneconfig.rubble_clearing_tool_rct01"));}
}
