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

public class GeraniumPlatformBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART=IntegerProperty.create("part",0,GeraniumPlatformLayout.PARTS-1);
    private static final ThreadLocal<Boolean> REMOVING=ThreadLocal.withInitial(()->false);
    public GeraniumPlatformBlock(Properties properties) {super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH).setValue(PART,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level=context.getLevel();var anchor=context.getClickedPos();var facing=context.getHorizontalDirection();
        for(int i=0;i<GeraniumPlatformLayout.PARTS;i++) {
            var p=GeraniumPlatformLayout.cell(anchor,facing,i);
            if(!level.hasChunkAt(p) || !level.getBlockState(p).canBeReplaced(context)
                    || i<GeraniumPlatformLayout.GROUND_PARTS && !level.getBlockState(p.below()).isFaceSturdy(level,p.below(),Direction.UP)
                    || context.getPlayer()!=null && !context.getPlayer().mayUseItemAt(p,Direction.UP,context.getItemInHand()))return null;
        }
        if(!outerClearance(context,anchor,facing))return null;
        return defaultBlockState().setValue(FACING,facing);
    }
    private boolean outerClearance(BlockPlaceContext context,BlockPos anchor,Direction facing){
        var level=context.getLevel();var center=GeraniumPlatformLayout.center(anchor,facing);net.minecraft.world.phys.AABB bounds=null;
        for(double x:new double[]{-1.53,1.54})for(double z:new double[]{-2.66,2.50}){var p=center.add(GeraniumPlatformLayout.orient(new net.minecraft.world.phys.Vec3(x,0,z),facing));var b=new net.minecraft.world.phys.AABB(p.x,center.y,p.z,p.x,center.y+1.85,p.z);bounds=bounds==null?b:bounds.minmax(b);}
        var lo=BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ);var hi=BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ);if(!level.hasChunksAt(lo,hi))return false;
        var reserved=new java.util.HashSet<BlockPos>();for(int i=0;i<GeraniumPlatformLayout.PARTS;i++)reserved.add(GeraniumPlatformLayout.cell(anchor,facing,i));
        for(var p:BlockPos.betweenClosed(lo,hi))if(!reserved.contains(p)){var state=level.getBlockState(p);var shape=state.getCollisionShape(level,p);if(!shape.isEmpty() && shape.bounds().move(p).intersects(bounds))return false;}
        return true;
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity player,ItemStack stack) {
        if(level.isClientSide)return;
        for(int i=1;i<GeraniumPlatformLayout.PARTS;i++)level.setBlock(GeraniumPlatformLayout.cell(pos,state.getValue(FACING),i),state.setValue(PART,i),3);
    }
    public static InteractionResult interact(Level level,BlockPos pos,BlockState state,Player player,InteractionHand hand) {
        if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;
        var anchor=GeraniumPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(level.getBlockEntity(anchor) instanceof GeraniumPlatformBlockEntity platform) {
            var stack=player.getItemInHand(hand);
            if(PlatformMonitor.bind(platform,player,stack)){}
            else if(player.isShiftKeyDown())platform.requestReturn(player);
            else if(stack.is(AddonItems.GERANIUM_2.get()))platform.loadDrone(player,stack);
            else if(stack.is(AddonItems.GASOLINE_JERRYCAN.get()))platform.refuel(player,hand);
            else if(stack.isEmpty() && player instanceof ServerPlayer serverPlayer)platform.open(serverPlayer);
            else player.displayClientMessage(net.minecraft.network.chat.Component.translatable("lp02.only_geranium"),true);
        }
        return InteractionResult.CONSUME;
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {return interact(level,pos,state,player,hand);}
    @Override public void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos neighbor,boolean moving) {
        if(!level.isClientSide && !REMOVING.get() && state.getValue(PART)<GeraniumPlatformLayout.GROUND_PARTS && !level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP))
            level.destroyBlock(GeraniumPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART)),true);
    }
    /** A creative helper break must not turn root cleanup into a survival loot drop. */
    @Override public void playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        if(!level.isClientSide&&player.isCreative()&&state.getValue(PART)!=0){
            var anchor=GeraniumPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
            if(level.hasChunkAt(anchor)){var base=level.getBlockState(anchor);if(base.is(this)&&base.getValue(PART)==0&&base.getValue(FACING)==state.getValue(FACING))level.removeBlock(anchor,false);}
        }
        super.playerWillDestroy(level,pos,state,player);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState replacement,boolean moving) {
        if(!state.is(replacement.getBlock()) && !level.isClientSide) {
            if(state.getValue(PART)==0 && level.getBlockEntity(pos) instanceof GeraniumPlatformBlockEntity platform)platform.dropContents();
            if(!REMOVING.get()) {
                REMOVING.set(true);
                try {
                    var anchor=GeraniumPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
                    if(state.getValue(PART)!=0 && level.getBlockState(anchor).is(this))level.destroyBlock(anchor,true);
                    for(int i=1;i<GeraniumPlatformLayout.PARTS;i++) {
                        var p=GeraniumPlatformLayout.cell(anchor,state.getValue(FACING),i);var part=level.getBlockState(p);
                        if(part.is(this) && part.getValue(PART)==i && part.getValue(FACING)==state.getValue(FACING))level.destroyBlock(p,false);
                    }
                } finally {REMOVING.set(false);}
            }
        }
        super.onRemove(state,level,pos,replacement,moving);
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return GeraniumPlatformShapes.selection(state.getValue(FACING),state.getValue(PART));}
    @Override public VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        if(context instanceof EntityCollisionContext collision && collision.getEntity() instanceof Geranium2Entity drone) {
            var root=GeraniumPlatformLayout.anchor(pos,state.getValue(FACING),state.getValue(PART));
            if(level.getBlockEntity(root) instanceof GeraniumPlatformBlockEntity platform && platform.departing(drone))return Shapes.empty();
        }
        return GeraniumPlatformShapes.collision(state.getValue(FACING),state.getValue(PART));
    }
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override public VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(PART)==0?new GeraniumPlatformBlockEntity(pos,state):null;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,AddonBlockEntities.GERANIUM_PLATFORM.get(),GeraniumPlatformBlockEntity::serverTick);}
    @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
