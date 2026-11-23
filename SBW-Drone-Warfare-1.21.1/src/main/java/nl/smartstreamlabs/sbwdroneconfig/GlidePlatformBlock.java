package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public class GlidePlatformBlock extends BaseEntityBlock {
    public static final com.mojang.serialization.MapCodec<GlidePlatformBlock> CODEC=simpleCodec(GlidePlatformBlock::new);
    @Override protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART=IntegerProperty.create("part",0,GlidePlatformLayout.PARTS-1);
    private static final ThreadLocal<Boolean> REMOVING=ThreadLocal.withInitial(()->false);
    public GlidePlatformBlock(Properties properties) {super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH).setValue(PART,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level=context.getLevel();var anchor=context.getClickedPos();var facing=context.getHorizontalDirection();
        for(int i=0;i<GlidePlatformLayout.PARTS;i++) {
            var p=GlidePlatformLayout.cell(anchor,facing,i);
            if(!level.hasChunkAt(p) || !level.getBlockState(p).canBeReplaced(context)
                    || GlidePlatformLayout.needsSupport(i) && !level.getBlockState(p.below()).isFaceSturdy(level,p.below(),Direction.UP)
                    || context.getPlayer()!=null && !context.getPlayer().mayUseItemAt(p,Direction.UP,context.getItemInHand()))return null;
        }
        if(!outerClearance(context,anchor,facing))return null;
        return defaultBlockState().setValue(FACING,facing);
    }
    private boolean outerClearance(BlockPlaceContext context,BlockPos anchor,Direction facing){
        var level=context.getLevel();var bounds=GlidePlatformLayout.openingBounds(anchor,facing);
        var lo=BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ);var hi=BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ);if(!level.hasChunksAt(lo,hi))return false;
        var reserved=new java.util.HashSet<BlockPos>();for(int i=0;i<GlidePlatformLayout.PARTS;i++)reserved.add(GlidePlatformLayout.cell(anchor,facing,i));
        for(var p:BlockPos.betweenClosed(lo,hi))if(!reserved.contains(p)){var state=level.getBlockState(p);var shape=state.getCollisionShape(level,p);if(!shape.isEmpty() && shape.bounds().move(p).intersects(bounds))return false;}
        return true;
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity player,ItemStack stack) {
        if(level.isClientSide)return;
        for(int i=1;i<GlidePlatformLayout.PARTS;i++)level.setBlock(GlidePlatformLayout.cell(pos,state.getValue(FACING),i),state.setValue(PART,i),3);
    }
    public static InteractionResult interact(Level level,BlockPos pos,BlockState state,Player player,InteractionHand hand) {
        if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;
        var anchor=GlidePlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(level.getBlockEntity(anchor) instanceof GlidePlatformBlockEntity platform) {
            var stack=player.getItemInHand(hand);
            if(PlatformMonitor.bind(platform,player,stack)){}
            else if(player.isShiftKeyDown())platform.requestReturn(player);
            else if(stack.is(AddonItems.GLIDE_BOMB.get()))platform.loadDrone(player,stack);
            else if(stack.isEmpty() && player instanceof ServerPlayer serverPlayer)platform.open(serverPlayer);
            else player.displayClientMessage(net.minecraft.network.chat.Component.translatable("lp03.only_glide"),true);
        }
        return InteractionResult.CONSUME;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){return interact(level,pos,state,player,InteractionHand.MAIN_HAND);}
    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){return interact(level,pos,state,player,hand).consumesAction()?net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide):net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
    @Override public void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos neighbor,boolean moving) {
        if(!level.isClientSide && !REMOVING.get() && GlidePlatformLayout.needsSupport(state.getValue(PART)) && !level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP))
            level.destroyBlock(GlidePlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART)),true);
    }
    /** A creative helper break must not turn root cleanup into a survival loot drop. */
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        if(!level.isClientSide&&player.isCreative()&&state.getValue(PART)!=0){
            var anchor=GlidePlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
            if(level.hasChunkAt(anchor)){var base=level.getBlockState(anchor);if(base.is(this)&&base.getValue(PART)==0&&base.getValue(FACING)==state.getValue(FACING))level.removeBlock(anchor,false);}
        }
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState replacement,boolean moving) {
        if(!state.is(replacement.getBlock()) && !level.isClientSide) {
            if(state.getValue(PART)==0 && level.getBlockEntity(pos) instanceof GlidePlatformBlockEntity platform)platform.dropContents();
            if(!REMOVING.get()) {
                REMOVING.set(true);
                try {
                    var anchor=GlidePlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
                    if(state.getValue(PART)!=0 && level.getBlockState(anchor).is(this))level.destroyBlock(anchor,true);
                    for(int i=1;i<GlidePlatformLayout.PARTS;i++) {
                        var p=GlidePlatformLayout.cell(anchor,state.getValue(FACING),i);var part=level.getBlockState(p);
                        if(part.is(this) && part.getValue(PART)==i && part.getValue(FACING)==state.getValue(FACING))level.destroyBlock(p,false);
                    }
                } finally {REMOVING.set(false);}
            }
        }
        super.onRemove(state,level,pos,replacement,moving);
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return GlidePlatformShapes.selection(state.getValue(FACING),state.getValue(PART));}
    @Override public VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        if(context instanceof EntityCollisionContext collision && collision.getEntity() instanceof GlideBombEntity drone) {
            var root=GlidePlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
            if(level.getBlockEntity(root) instanceof GlidePlatformBlockEntity platform && platform.departing(drone))return Shapes.empty();
        }
        return GlidePlatformShapes.collision(state.getValue(FACING),state.getValue(PART));
    }
    @Override public void tick(BlockState state,net.minecraft.server.level.ServerLevel level,BlockPos pos,net.minecraft.util.RandomSource random){if(state.getValue(PART)==0)extendVisualFootprint(level,pos,state);}
    private void extendVisualFootprint(net.minecraft.server.level.ServerLevel level,BlockPos anchor,BlockState state){
        // Original indices 0..17 remain valid. Add only free R2 collision cells; never erase a player's blocks or inventory.
        var facing=state.getValue(FACING);for(int i=18;i<GlidePlatformLayout.PARTS;i++){var p=GlidePlatformLayout.cell(anchor,facing,i);if(!level.hasChunkAt(p))return;var at=level.getBlockState(p);boolean own=at.is(this)&&at.getValue(PART)==i&&at.getValue(FACING)==facing;if(!own&&!at.isAir()||GlidePlatformLayout.needsSupport(i)&&!level.getBlockState(p.below()).isFaceSturdy(level,p.below(),Direction.UP)){com.mojang.logging.LogUtils.getLogger().warn("LP-03 R2 footprint at {} needs clear, supported space; retained existing platform and inventory",anchor);return;}}
        for(int i=18;i<GlidePlatformLayout.PARTS;i++){var p=GlidePlatformLayout.cell(anchor,facing,i);if(level.getBlockState(p).isAir())level.setBlock(p,state.setValue(PART,i),3);}
    }
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override public VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(PART)==0?new GlidePlatformBlockEntity(pos,state):null;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,AddonBlockEntities.GLIDE_PLATFORM.get(),GlidePlatformBlockEntity::serverTick);}
    @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
