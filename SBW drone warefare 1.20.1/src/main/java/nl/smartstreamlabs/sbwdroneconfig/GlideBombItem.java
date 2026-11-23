package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Objects;

public class GlideBombItem extends Item {
    public GlideBombItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.glide_bomb.1").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.glide_bomb.2").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.glide_bomb.3").withStyle(ChatFormatting.DARK_GREEN));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {var level=context.getLevel();var player=context.getPlayer();var pos=context.getClickedPos();var state=level.getBlockState(pos);if(player!=null&&state.getBlock()instanceof GlidePlatformBlock)return GlidePlatformBlock.interact(level,pos,state,player,context.getHand());if(player!=null&&!level.isClientSide)player.displayClientMessage(Component.translatable("message.sbwdroneconfig.platform_required"),true);return InteractionResult.FAIL;}

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {var stack=player.getItemInHand(hand);var hit=getPlayerPOVHitResult(level,player,ClipContext.Fluid.NONE);if(hit.getType()==HitResult.Type.BLOCK){var state=level.getBlockState(hit.getBlockPos());if(state.getBlock()instanceof GlidePlatformBlock)return new InteractionResultHolder<>(GlidePlatformBlock.interact(level,hit.getBlockPos(),state,player,hand),stack);}if(!level.isClientSide)player.displayClientMessage(Component.translatable("message.sbwdroneconfig.platform_required"),true);return InteractionResultHolder.fail(stack);}
}
