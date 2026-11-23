package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class NetBulkPlacementServer {
    private static final int IMMEDIATE_PLACEMENT_LIMIT = 256;
    private static final int RECENT_SESSION_LIMIT = 16;
    private static final Map<UUID, PlacementJob> ACTIVE_JOBS = new HashMap<>();
    private static final Map<UUID, LastUndo> LAST_UNDO = new HashMap<>();
    private static final Map<UUID, ArrayDeque<UUID>> RECENT_SESSIONS = new HashMap<>();

    private NetBulkPlacementServer() {
    }

    public static void handleConfirm(ServerPlayer player, NetBulkPlacementConfirmMessage message) {
        if (player == null || message == null || !player.isAlive()) {
            return;
        }
        if (!AddonConfig.netBulkPlacementEnabled()) {
            fail(player, NetBulkPlacementSupport.MESSAGE_DISABLED);
            return;
        }
        if (!player.level().dimension().location().equals(message.dimension())) {
            fail(player, NetBulkPlacementSupport.MESSAGE_INVALID_PLANE);
            return;
        }
        if (isDuplicateSession(player.getUUID(), message.sessionId())) {
            return;
        }
        if (ACTIVE_JOBS.containsKey(player.getUUID())) {
            fail(player, NetBulkPlacementSupport.MESSAGE_BUSY);
            return;
        }

        Optional<NetBulkPlacementKind> kind = NetBulkPlacementKind.fromItemId(message.itemId());
        if (kind.isEmpty()) {
            fail(player, NetBulkPlacementSupport.MESSAGE_INVALID_PLANE);
            return;
        }

        InteractionHand heldHand = findHeldHand(player, kind.get());
        if (heldHand == null) {
            fail(player, NetBulkPlacementSupport.MESSAGE_INVALID_PLANE);
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!withinReach(player, message.pos1()) || !withinReach(player, message.pos2())) {
            fail(player, NetBulkPlacementSupport.MESSAGE_TOO_LARGE);
            return;
        }

        NetBulkPlacementPlan plan;
        try {
            plan = NetBulkPlacementSupport.buildPlan(
                    kind.get(),
                    message.pos1(),
                    message.pos2(),
                    message.planeAxis(),
                    message.facing(),
                    level,
                    AddonConfig.netBulkPlacementMaxWidth(),
                    AddonConfig.netBulkPlacementMaxHeight(),
                    AddonConfig.netBulkPlacementMaxBlocks()
            );
        } catch (NetBulkPlacementSupport.InvalidSelectionException exception) {
            fail(player, exception.messageKey());
            return;
        }

        Validation validation = validateTargets(level, player, heldHand, plan);
        boolean skipBlocked = shouldSkipBlocked(plan);
        if (validation.blockedCount() > 0 && !skipBlocked) {
            fail(player, NetBulkPlacementSupport.MESSAGE_BLOCKED);
            return;
        }
        if (validation.validTargets().isEmpty()) {
            fail(player, NetBulkPlacementSupport.MESSAGE_BLOCKED);
            return;
        }

        int requiredItems = validation.validTargets().size();
        boolean creative = player.getAbilities().instabuild;
        if (!creative) {
            int ownedItems = countMatchingItems(player, kind.get());
            if (ownedItems < requiredItems) {
                int missing = requiredItems - ownedItems;
                player.displayClientMessage(Component.translatable(
                        NetBulkPlacementSupport.MESSAGE_NOT_ENOUGH_DETAILED,
                        missing,
                        new ItemStack(kind.get().item()).getHoverName()
                ).withStyle(ChatFormatting.RED), true);
                playFailureSound(player);
                return;
            }
            consumeMatchingItems(player, kind.get(), requiredItems);
        }

        rememberSession(player.getUUID(), message.sessionId());
        PlacementJob job = new PlacementJob(
                player.getUUID(),
                level.dimension(),
                kind.get(),
                message.sessionId(),
                message.facing(),
                validation.validTargets(),
                validation.blockedCount(),
                skipBlocked,
                creative ? 0 : requiredItems,
                level.getGameTime()
        );

        if (validation.validTargets().size() <= IMMEDIATE_PLACEMENT_LIMIT) {
            PlacementResult result = job.tick(level, player, validation.validTargets().size());
            if (!result.success()) {
                job.rollback(level, player);
                fail(player, result.messageKey());
                return;
            }
            finishJob(level, player, job);
            return;
        }

        ACTIVE_JOBS.put(player.getUUID(), job);
        player.displayClientMessage(Component.translatable(NetBulkPlacementSupport.MESSAGE_STARTED, validation.validTargets().size()).withStyle(ChatFormatting.GREEN), true);
        level.playSound(null, player.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.65F, 1.0F);
    }

    public static int undoLast(ServerPlayer player) {
        if (player == null) {
            return 0;
        }
        LastUndo undo = LAST_UNDO.remove(player.getUUID());
        if (undo == null) {
            player.displayClientMessage(Component.translatable(NetBulkPlacementSupport.MESSAGE_UNDO_EMPTY).withStyle(ChatFormatting.YELLOW), true);
            return 0;
        }
        ServerLevel level = player.server.getLevel(undo.dimension());
        if (level == null || level.getGameTime() > undo.expiresAtGameTime()) {
            player.displayClientMessage(Component.translatable(NetBulkPlacementSupport.MESSAGE_UNDO_EMPTY).withStyle(ChatFormatting.YELLOW), true);
            return 0;
        }

        int restored = 0;
        List<PlacedBlock> entries = undo.entries();
        for (int index = entries.size() - 1; index >= 0; index--) {
            PlacedBlock entry = entries.get(index);
            if (!level.hasChunkAt(entry.pos())) {
                continue;
            }
            BlockState current = level.getBlockState(entry.pos());
            if (!current.equals(entry.placedState())) {
                continue;
            }
            level.setBlock(entry.pos(), entry.originalState(), Block.UPDATE_ALL);
            restored++;
        }

        if (restored > 0 && !player.getAbilities().instabuild) {
            refundItems(player, undo.kind(), restored);
        }
        player.displayClientMessage(Component.translatable(NetBulkPlacementSupport.MESSAGE_UNDO_SUCCESS, restored).withStyle(ChatFormatting.GREEN), true);
        level.playSound(null, player.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.65F, 0.75F);
        return restored;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null || ACTIVE_JOBS.isEmpty()) {
            return;
        }

        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<UUID, PlacementJob>> iterator = ACTIVE_JOBS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PlacementJob> entry = iterator.next();
            PlacementJob job = entry.getValue();
            ServerLevel level = server.getLevel(job.dimension());
            ServerPlayer player = server.getPlayerList().getPlayer(job.playerId());
            if (level == null || player == null || player.serverLevel() != level) {
                if (level != null) {
                    job.rollback(level, player);
                }
                iterator.remove();
                continue;
            }

            PlacementResult result = job.tick(level, player, AddonConfig.netBulkPlacementBlocksPerTick());
            if (!result.success()) {
                job.rollback(level, player);
                fail(player, result.messageKey());
                iterator.remove();
                continue;
            }
            if (job.complete()) {
                finishJob(level, player, job);
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelActiveJob(player);
            LAST_UNDO.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelActiveJob(player);
        }
    }

    private static void cancelActiveJob(ServerPlayer player) {
        PlacementJob job = ACTIVE_JOBS.remove(player.getUUID());
        if (job != null) {
            ServerLevel level = player.server.getLevel(job.dimension());
            if (level != null) {
                job.rollback(level, player);
            }
        }
    }

    private static void finishJob(ServerLevel level, ServerPlayer player, PlacementJob job) {
        int unusedReservedItems = Math.max(0, job.reservedItems() - job.placedBlocks().size());
        if (unusedReservedItems > 0 && !player.getAbilities().instabuild) {
            refundItems(player, job.kind(), unusedReservedItems);
        }

        long undoTicks = Math.max(0, AddonConfig.netBulkPlacementUndoSeconds()) * 20L;
        if (!job.placedBlocks().isEmpty() && undoTicks > 0L) {
            LAST_UNDO.put(player.getUUID(), new LastUndo(
                    job.dimension(),
                    job.kind(),
                    List.copyOf(job.placedBlocks()),
                    level.getGameTime() + undoTicks
            ));
        }
        Component message = job.skippedCount() > 0
                ? Component.translatable(NetBulkPlacementSupport.MESSAGE_SUCCESS, job.placedBlocks().size())
                .append(Component.literal(" / " + job.skippedCount() + " skipped"))
                : Component.translatable(NetBulkPlacementSupport.MESSAGE_SUCCESS, job.placedBlocks().size());
        player.displayClientMessage(message.copy().withStyle(ChatFormatting.GREEN), true);
        level.playSound(null, job.placedBlocks().isEmpty() ? player.blockPosition() : job.placedBlocks().get(0).pos(), SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.9F, 1.0F);
    }

    private static Validation validateTargets(ServerLevel level, ServerPlayer player, InteractionHand hand, NetBulkPlacementPlan plan) {
        List<NetBulkPlacementPlan.Target> validTargets = new ArrayList<>();
        int blocked = 0;
        for (NetBulkPlacementPlan.Target target : plan.targets()) {
            if (canPlaceTarget(level, player, hand, plan.kind(), target, plan.facing())) {
                validTargets.add(target);
            } else {
                blocked++;
            }
        }
        return new Validation(List.copyOf(validTargets), blocked);
    }

    private static boolean shouldSkipBlocked(NetBulkPlacementPlan plan) {
        return AddonConfig.netBulkPlacementSkipBlocked()
                || !AddonConfig.netBulkPlacementAllOrNothing()
                || (plan != null && plan.targets().size() > 1);
    }

    static boolean canPlaceTarget(
            ServerLevel level,
            ServerPlayer player,
            InteractionHand hand,
            NetBulkPlacementKind kind,
            NetBulkPlacementPlan.Target target,
            Direction facing
    ) {
        if (level == null || player == null || kind == null || target == null || target.pos() == null) {
            return false;
        }
        BlockPos pos = target.pos();
        if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState existing = level.getBlockState(pos);
        if (existing.hasBlockEntity() || level.getBlockEntity(pos) != null) {
            return false;
        }
        InteractionHand contextHand = hand == null ? InteractionHand.MAIN_HAND : hand;
        ItemStack heldStack = hand == null ? ItemStack.EMPTY : player.getItemInHand(hand);
        ItemStack placementStack = kind.matches(heldStack) ? heldStack : new ItemStack(kind.item());
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, facing, placementStack)) {
            return false;
        }
        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), facing, pos, false);
        BlockPlaceContext context = new BlockPlaceContext(player, contextHand, placementStack, hitResult);
        if (!existing.isAir() && !existing.canBeReplaced(context)) {
            return false;
        }
        return target.state().canSurvive(level, pos);
    }

    private static boolean withinReach(ServerPlayer player, BlockPos pos) {
        double reach = AddonConfig.netBulkPlacementMaximumReach();
        return player.getEyePosition().distanceTo(Vec3.atCenterOf(pos)) <= reach + 1.0D;
    }

    private static InteractionHand findHeldHand(ServerPlayer player, NetBulkPlacementKind kind) {
        if (kind.matches(player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        if (kind.matches(player.getOffhandItem())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static int countMatchingItems(ServerPlayer player, NetBulkPlacementKind kind) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (kind.matches(stack)) {
                count += stack.getCount();
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (kind.matches(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeMatchingItems(ServerPlayer player, NetBulkPlacementKind kind, int amount) {
        int remaining = amount;
        remaining = consumeFromList(player.getInventory().items, kind, remaining);
        consumeFromList(player.getInventory().offhand, kind, remaining);
        player.getInventory().setChanged();
    }

    private static int consumeFromList(List<ItemStack> stacks, NetBulkPlacementKind kind, int amount) {
        int remaining = amount;
        for (ItemStack stack : stacks) {
            if (remaining <= 0) {
                break;
            }
            if (!kind.matches(stack)) {
                continue;
            }
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }
        return remaining;
    }

    private static void refundItems(ServerPlayer player, NetBulkPlacementKind kind, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            ItemStack refund = new ItemStack(kind.item(), Math.min(remaining, kind.item().getDefaultInstance().getMaxStackSize()));
            remaining -= refund.getCount();
            if (!player.getInventory().add(refund)) {
                player.drop(refund, false);
            }
        }
        player.getInventory().setChanged();
    }

    private static boolean isDuplicateSession(UUID playerId, UUID sessionId) {
        return sessionId == null || RECENT_SESSIONS.getOrDefault(playerId, new ArrayDeque<>()).contains(sessionId);
    }

    private static void rememberSession(UUID playerId, UUID sessionId) {
        ArrayDeque<UUID> sessions = RECENT_SESSIONS.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        sessions.addLast(sessionId);
        while (sessions.size() > RECENT_SESSION_LIMIT) {
            sessions.removeFirst();
        }
    }

    private static void fail(ServerPlayer player, String messageKey) {
        if (player != null) {
            player.displayClientMessage(Component.translatable(messageKey).withStyle(ChatFormatting.RED), true);
            playFailureSound(player);
        }
    }

    private static void playFailureSound(ServerPlayer player) {
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 0.45F, 0.55F);
    }

    private record Validation(List<NetBulkPlacementPlan.Target> validTargets, int blockedCount) {
    }

    private record PlacedBlock(BlockPos pos, BlockState placedState, BlockState originalState) {
    }

    private record LastUndo(ResourceKey<Level> dimension, NetBulkPlacementKind kind, List<PlacedBlock> entries, long expiresAtGameTime) {
    }

    private record PlacementResult(boolean success, String messageKey) {
        static PlacementResult ok() {
            return new PlacementResult(true, "");
        }

        static PlacementResult failed(String messageKey) {
            return new PlacementResult(false, messageKey);
        }
    }

    private static final class PlacementJob {
        private final UUID playerId;
        private final ResourceKey<Level> dimension;
        private final NetBulkPlacementKind kind;
        private final UUID sessionId;
        private final Direction facing;
        private final List<NetBulkPlacementPlan.Target> targets;
        private int skippedCount;
        private final boolean skipBlocked;
        private final int reservedItems;
        private final long startedGameTime;
        private final List<PlacedBlock> placedBlocks = new ArrayList<>();
        private int nextIndex;

        private PlacementJob(
                UUID playerId,
                ResourceKey<Level> dimension,
                NetBulkPlacementKind kind,
                UUID sessionId,
                Direction facing,
                List<NetBulkPlacementPlan.Target> targets,
                int skippedCount,
                boolean skipBlocked,
                int reservedItems,
                long startedGameTime
        ) {
            this.playerId = playerId;
            this.dimension = dimension;
            this.kind = kind;
            this.sessionId = sessionId;
            this.facing = facing;
            this.targets = targets;
            this.skippedCount = skippedCount;
            this.skipBlocked = skipBlocked;
            this.reservedItems = reservedItems;
            this.startedGameTime = startedGameTime;
        }

        UUID playerId() {
            return playerId;
        }

        ResourceKey<Level> dimension() {
            return dimension;
        }

        NetBulkPlacementKind kind() {
            return kind;
        }

        List<PlacedBlock> placedBlocks() {
            return placedBlocks;
        }

        int skippedCount() {
            return skippedCount;
        }

        int reservedItems() {
            return reservedItems;
        }

        boolean complete() {
            return nextIndex >= targets.size();
        }

        PlacementResult tick(ServerLevel level, ServerPlayer player, int maxBlocks) {
            int limit = Math.max(1, maxBlocks);
            int placedThisTick = 0;
            while (nextIndex < targets.size() && placedThisTick < limit) {
                NetBulkPlacementPlan.Target target = targets.get(nextIndex++);
                if (!canPlaceTarget(level, player, findHeldHand(player, kind), kind, target, facing)) {
                    if (skipBlocked) {
                        skippedCount++;
                        continue;
                    }
                    return PlacementResult.failed(NetBulkPlacementSupport.MESSAGE_BLOCKED);
                }
                BlockState originalState = level.getBlockState(target.pos());
                if (!level.setBlock(target.pos(), target.state(), Block.UPDATE_ALL)) {
                    if (skipBlocked) {
                        skippedCount++;
                        continue;
                    }
                    return PlacementResult.failed(NetBulkPlacementSupport.MESSAGE_BLOCKED);
                }
                placedBlocks.add(new PlacedBlock(target.pos(), target.state(), originalState));
                placedThisTick++;
            }
            return PlacementResult.ok();
        }

        void rollback(ServerLevel level, ServerPlayer player) {
            for (int index = placedBlocks.size() - 1; index >= 0; index--) {
                PlacedBlock placed = placedBlocks.get(index);
                if (level.hasChunkAt(placed.pos()) && level.getBlockState(placed.pos()).equals(placed.placedState())) {
                    level.setBlock(placed.pos(), placed.originalState(), Block.UPDATE_ALL);
                }
            }
            if (player != null && reservedItems > 0) {
                refundItems(player, kind, reservedItems);
            }
            SbwDroneRangeConfig.LOGGER.info(
                    "Cancelled anti-drone net bulk placement job player={} session={} placed={} startedTick={}",
                    playerId,
                    sessionId,
                    placedBlocks.size(),
                    startedGameTime
            );
        }
    }
}
