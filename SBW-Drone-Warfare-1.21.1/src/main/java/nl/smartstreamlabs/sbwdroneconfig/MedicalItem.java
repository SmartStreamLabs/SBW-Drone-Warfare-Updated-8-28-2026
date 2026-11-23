package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import java.util.List;

public final class MedicalItem extends Item {
    private final boolean trauma;
    public MedicalItem(boolean trauma, Properties p) { super(p); this.trauma=trauma; }
    public boolean trauma() { return trauma; }
    private InteractionResult start(Player actor, Player patient, InteractionHand hand) {
        if(actor.level().isClientSide){var v=InjuryClientState.get(patient.getUUID());if(v!=null&&(v.stage()==InjuryState.Stage.HEALTHY||v.stage()==InjuryState.Stage.RECOVERING||!trauma&&v.bleeding()==0))return InteractionResult.FAIL;}
        if (!actor.level().isClientSide && actor instanceof net.minecraft.server.level.ServerPlayer a && patient instanceof net.minecraft.server.level.ServerPlayer p && !InjuryServer.begin(a,p,hand,this)) return InteractionResult.FAIL;
        actor.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var result=start(player,player,hand);
        return new InteractionResultHolder<>(result,player.getItemInHand(hand));
    }
    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player actor, LivingEntity target, InteractionHand hand) {
        return target instanceof Player p ? start(actor,p,hand) : InteractionResult.PASS;
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return 72000; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int left) {
        if(user instanceof net.minecraft.server.level.ServerPlayer p) InjuryServer.cancel(p,true);
    }
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable(trauma?"injury.medkit.info":"injury.bandage.info").withStyle(net.minecraft.ChatFormatting.GRAY));
        if(trauma)lines.add(Component.translatable("injury.medkit.consumption").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable("injury.item.use").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
}
