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

public class CubedFpvDroneItem extends Item {
    public CubedFpvDroneItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.cubed_fpv_drone.1").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.cubed_fpv_drone.2").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.cubed_fpv_drone.3").withStyle(ChatFormatting.DARK_GREEN));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.cubed_fpv_drone.4").withStyle(ChatFormatting.DARK_GREEN));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockState blockState = level.getBlockState(clickedPos);
        BlockPos spawnPos = blockState.getCollisionShape(level, clickedPos).isEmpty() ? clickedPos : clickedPos.relative(direction);

        Entity entity = AddonEntities.CUBED_FPV_DRONE.get().spawn(serverLevel, stack, context.getPlayer(), spawnPos, MobSpawnType.SPAWN_EGG, true, !Objects.equals(clickedPos, spawnPos) && direction == Direction.UP);
        if (entity != null) {
            SbwCompat.assignDroneOwner(entity, context.getPlayer());
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, clickedPos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        }

        BlockPos blockPos = hitResult.getBlockPos();
        if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos, hitResult.getDirection(), stack)) {
            return InteractionResultHolder.fail(stack);
        }

        Entity entity = AddonEntities.CUBED_FPV_DRONE.get().spawn(serverLevel, stack, player, blockPos, MobSpawnType.SPAWN_EGG, false, false);
        if (entity == null) {
            return InteractionResultHolder.pass(stack);
        }
        SbwCompat.assignDroneOwner(entity, player);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
        return InteractionResultHolder.consume(stack);
    }
}
