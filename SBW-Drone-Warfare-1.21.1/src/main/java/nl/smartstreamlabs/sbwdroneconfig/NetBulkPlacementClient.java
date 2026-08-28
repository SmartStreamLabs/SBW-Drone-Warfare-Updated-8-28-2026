package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NetBulkPlacementClient {
    private static ClientSelection selection;
    private static long lastHandledUseTick = Long.MIN_VALUE;

    private NetBulkPlacementClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(NetBulkPlacementClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(NetBulkPlacementClient.class);
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            return;
        }

        Optional<NetBulkPlacementKind> heldKind = NetBulkPlacementSupport.kindForItem(player.getItemInHand(event.getHand()));
        if (heldKind.isEmpty()) {
            clearSelection();
            return;
        }
        if (!AddonConfig.netBulkPlacementEnabled()) {
            return;
        }
        if (selection != null && level.getGameTime() == lastHandledUseTick) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        if (selection == null) {
            if (!player.isShiftKeyDown()) {
                return;
            }
            startSelection(player, level, event.getHand(), heldKind.get(), hitResult);
            markHandled(event, level);
            return;
        }

        if (!selection.matches(player, level, heldKind.get())) {
            clearSelection();
            return;
        }

        if (player.isShiftKeyDown()) {
            clearSelection();
            showActionBar(player, NetBulkPlacementSupport.MESSAGE_CANCELLED);
            playLocal(level, player.blockPosition(), 0.55F);
            markHandled(event, level);
            return;
        }

        if (selection.pos2() == null) {
            updatePos2(player, level, event.getHand(), hitResult);
            markHandled(event, level);
            return;
        }

        confirmSelection(player, level);
        markHandled(event, level);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            clearSelection();
            return;
        }
        if (selection != null && !selection.matchesHeldItem(minecraft.player)) {
            clearSelection();
            return;
        }
        if (selection != null && !selection.dimension().equals(minecraft.level.dimension().location())) {
            clearSelection();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS
                || selection == null
                || selection.previewTargets().isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();

        for (PreviewTarget previewTarget : selection.previewTargets()) {
            BlockPos pos = previewTarget.target().pos();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            minecraft.getBlockRenderer().renderSingleBlock(
                    previewTarget.target().state(),
                    poseStack,
                    buffers,
                    0x00F000F0,
                    OverlayTexture.NO_OVERLAY
            );
            poseStack.popPose();

            float red = previewTarget.valid() ? 0.15F : 1.0F;
            float green = previewTarget.valid() ? 1.0F : 0.1F;
            float blue = previewTarget.valid() ? 0.25F : 0.1F;
            VertexConsumer lines = buffers.getBuffer(RenderType.lines());
            LevelRenderer.renderLineBox(poseStack, lines, new AABB(pos).inflate(0.006D), red, green, blue, 0.82F);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.popPose();
        buffers.endBatch();
    }

    private static void startSelection(LocalPlayer player, ClientLevel level, InteractionHand hand, NetBulkPlacementKind kind, BlockHitResult hitResult) {
        BlockPos pos1 = placementPos(player, hand, hitResult);
        Direction facing = hitResult.getDirection();
        Direction.Axis axis;
        try {
            axis = NetBulkPlacementSupport.preferredPlaneAxis(kind, pos1, pos1, facing);
        } catch (NetBulkPlacementSupport.InvalidSelectionException exception) {
            axis = kind.isCarpet() ? Direction.Axis.Y : facing.getAxis();
        }
        selection = new ClientSelection(
                player.getUUID(),
                level.dimension().location(),
                kind,
                pos1,
                null,
                facing,
                axis,
                UUID.randomUUID(),
                List.of()
        );
        showActionBar(player, NetBulkPlacementSupport.MESSAGE_POS1_SET);
        playLocal(level, pos1, 0.8F);
    }

    private static void updatePos2(LocalPlayer player, ClientLevel level, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos pos2 = placementPos(player, hand, hitResult);
        try {
            Direction.Axis axis = NetBulkPlacementSupport.resolvePlaneAxis(
                    selection.kind(),
                    selection.pos1(),
                    pos2,
                    selection.planeAxis(),
                    selection.facing()
            );
            NetBulkPlacementPlan plan = NetBulkPlacementSupport.buildPlan(
                    selection.kind(),
                    selection.pos1(),
                    pos2,
                    axis,
                    selection.facing(),
                    level,
                    AddonConfig.netBulkPlacementMaxWidth(),
                    AddonConfig.netBulkPlacementMaxHeight(),
                    AddonConfig.netBulkPlacementMaxBlocks()
            );
            List<PreviewTarget> previewTargets = new ArrayList<>();
            for (NetBulkPlacementPlan.Target target : plan.targets()) {
                previewTargets.add(new PreviewTarget(target, canPreviewPlace(level, player, hand, target, selection.facing())));
            }
            selection = selection.withPos2(pos2, axis, List.copyOf(previewTargets));
            player.displayClientMessage(
                    Component.translatable(NetBulkPlacementSupport.MESSAGE_POS2_SET)
                            .append(Component.literal(" - "))
                            .append(Component.translatable(NetBulkPlacementSupport.MESSAGE_CONFIRM)),
                    true
            );
            playLocal(level, pos2, 1.0F);
        } catch (NetBulkPlacementSupport.InvalidSelectionException exception) {
            showActionBar(player, exception.messageKey());
            playLocal(level, pos2, 0.55F);
        }
    }

    private static void confirmSelection(LocalPlayer player, ClientLevel level) {
        if (selection == null || selection.pos2() == null) {
            return;
        }
        AddonNetwork.sendNetBulkPlacementConfirm(new NetBulkPlacementConfirmMessage(
                selection.kind().itemId(),
                selection.pos1(),
                selection.pos2(),
                selection.planeAxis(),
                selection.facing(),
                selection.dimension(),
                selection.sessionId()
        ));
        showActionBar(player, NetBulkPlacementSupport.MESSAGE_CONFIRM);
        playLocal(level, selection.pos2(), 1.2F);
        clearSelection();
    }

    private static boolean canPreviewPlace(ClientLevel level, LocalPlayer player, InteractionHand hand, NetBulkPlacementPlan.Target target, Direction facing) {
        BlockPos pos = target.pos();
        if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState existing = level.getBlockState(pos);
        if (existing.hasBlockEntity() || level.getBlockEntity(pos) != null) {
            return false;
        }
        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), facing, pos, false);
        BlockPlaceContext context = new BlockPlaceContext(player, hand, player.getItemInHand(hand), hitResult);
        if (!existing.isAir() && !existing.canBeReplaced(context)) {
            return false;
        }
        return target.state().canSurvive(level, pos);
    }

    private static BlockPos placementPos(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPlaceContext context = new BlockPlaceContext(player, hand, player.getItemInHand(hand), hitResult);
        return context.getClickedPos().immutable();
    }

    private static void markHandled(InputEvent.InteractionKeyMappingTriggered event, ClientLevel level) {
        lastHandledUseTick = level.getGameTime();
        event.setCanceled(true);
        event.setSwingHand(true);
    }

    private static void showActionBar(LocalPlayer player, String messageKey) {
        player.displayClientMessage(Component.translatable(messageKey), true);
    }

    private static void playLocal(ClientLevel level, BlockPos pos, float pitch) {
        level.playLocalSound(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                SoundEvents.WOOL_PLACE,
                SoundSource.BLOCKS,
                0.35F,
                pitch,
                false
        );
    }

    private static void clearSelection() {
        selection = null;
    }

    private record ClientSelection(
            UUID playerId,
            ResourceLocation dimension,
            NetBulkPlacementKind kind,
            BlockPos pos1,
            BlockPos pos2,
            Direction facing,
            Direction.Axis planeAxis,
            UUID sessionId,
            List<PreviewTarget> previewTargets
    ) {
        boolean matches(LocalPlayer player, ClientLevel level, NetBulkPlacementKind heldKind) {
            return player != null
                    && level != null
                    && player.getUUID().equals(playerId)
                    && level.dimension().location().equals(dimension)
                    && kind == heldKind;
        }

        boolean matchesHeldItem(LocalPlayer player) {
            return kind.matches(player.getMainHandItem()) || kind.matches(player.getOffhandItem());
        }

        ClientSelection withPos2(BlockPos newPos2, Direction.Axis newPlaneAxis, List<PreviewTarget> newPreviewTargets) {
            return new ClientSelection(playerId, dimension, kind, pos1, newPos2.immutable(), facing, newPlaneAxis, sessionId, newPreviewTargets);
        }
    }

    private record PreviewTarget(NetBulkPlacementPlan.Target target, boolean valid) {
    }
}
