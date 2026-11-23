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

public class LucasPlatformBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART=IntegerProperty.create("part",0,LucasPlatformLayout.PARTS-1);
    private static final ThreadLocal<Boolean> REMOVING=ThreadLocal.withInitial(()->false);
    public LucasPlatformBlock(Properties properties) {super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH).setValue(PART,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level=context.getLevel();var anchor=context.getClickedPos();var facing=context.getHorizontalDirection();
        for(int i=0;i<LucasPlatformLayout.PARTS;i++) {
            var p=LucasPlatformLayout.cell(anchor,facing,i);
            if(!level.hasChunkAt(p) || !level.getBlockState(p).canBeReplaced(context)
                    || i<6 && !level.getBlockState(p.below()).isFaceSturdy(level,p.below(),Direction.UP)
                    || context.getPlayer()!=null && !context.getPlayer().mayUseItemAt(p,Direction.UP,context.getItemInHand()))return null;
        }
        return defaultBlockState().setValue(FACING,facing);
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity player,ItemStack stack) {
        if(level.isClientSide)return;
        for(int i=1;i<LucasPlatformLayout.PARTS;i++)level.setBlock(LucasPlatformLayout.cell(pos,state.getValue(FACING),i),state.setValue(PART,i),3);
    }
    public static InteractionResult interact(Level level,BlockPos pos,BlockState state,Player player,InteractionHand hand) {
        if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;
        var anchor=LucasPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(level.getBlockEntity(anchor) instanceof LucasPlatformBlockEntity platform) {
            var stack=player.getItemInHand(hand);
            if(PlatformMonitor.bind(platform,player,stack)){}
            else if(player.isShiftKeyDown())platform.requestReturn(player);
            else if(stack.is(AddonItems.LUCAS_DRONE.get()))platform.loadDrone(player,stack);
            else if(stack.is(AddonItems.GASOLINE_JERRYCAN.get()))platform.refuel(player,hand);
            else if(stack.isEmpty() && player instanceof ServerPlayer serverPlayer)platform.open(serverPlayer);
            else player.displayClientMessage(net.minecraft.network.chat.Component.translatable("lp01.empty_hand"),true);
        }
        return InteractionResult.CONSUME;
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {return interact(level,pos,state,player,hand);}
    @Override public void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos neighbor,boolean moving) {
        if(!level.isClientSide && !REMOVING.get() && state.getValue(PART)<6 && !level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP))
            level.destroyBlock(LucasPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART)),true);
    }
    /** A creative helper break must not turn root cleanup into a survival loot drop. */
    @Override public void playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        if(!level.isClientSide&&player.isCreative()&&state.getValue(PART)!=0){
            var anchor=LucasPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
            if(level.hasChunkAt(anchor)){var base=level.getBlockState(anchor);if(base.is(this)&&base.getValue(PART)==0&&base.getValue(FACING)==state.getValue(FACING))level.removeBlock(anchor,false);}
        }
        super.playerWillDestroy(level,pos,state,player);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState replacement,boolean moving) {
        if(!state.is(replacement.getBlock()) && !level.isClientSide) {
            if(state.getValue(PART)==0 && level.getBlockEntity(pos) instanceof LucasPlatformBlockEntity platform)platform.dropContents();
            if(!REMOVING.get()) {
                REMOVING.set(true);
                try {
                    var anchor=LucasPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
                    if(state.getValue(PART)!=0 && level.getBlockState(anchor).is(this))level.destroyBlock(anchor,true);
                    for(int i=1;i<LucasPlatformLayout.PARTS;i++) {
                        var p=LucasPlatformLayout.cell(anchor,state.getValue(FACING),i);var part=level.getBlockState(p);
                        if(part.is(this) && part.getValue(PART)==i && part.getValue(FACING)==state.getValue(FACING))level.destroyBlock(p,false);
                    }
                } finally {REMOVING.set(false);}
            }
        }
        super.onRemove(state,level,pos,replacement,moving);
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return LucasPlatformShapes.selection(state.getValue(FACING),state.getValue(PART));}
    @Override public VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        if(context instanceof EntityCollisionContext collision && collision.getEntity() instanceof LucasDroneEntity drone) {
            var root=LucasPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
            if(level.getBlockEntity(root) instanceof LucasPlatformBlockEntity platform && platform.departing(drone))return Shapes.empty();
        }
        return LucasPlatformShapes.collision(state.getValue(FACING),state.getValue(PART));
    }
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override public VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(PART)==0?new LucasPlatformBlockEntity(pos,state):null;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,AddonBlockEntities.LUCAS_PLATFORM.get(),LucasPlatformBlockEntity::serverTick);}
    @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
