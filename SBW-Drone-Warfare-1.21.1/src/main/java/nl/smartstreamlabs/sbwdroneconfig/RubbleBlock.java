package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.entity.player.Player;

/** Chunk-batched rubble; no ticker, dropped-block duplication or automatic decay. */
public final class RubbleBlock extends Block implements EntityBlock,SimpleWaterloggedBlock {
    public static final IntegerProperty LAYERS=IntegerProperty.create("layers",1,8);
    public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
    private static final VoxelShape[] SHAPES=new VoxelShape[9];
    static {for(int i=1;i<=8;i++)SHAPES[i]=Shapes.or(box(1,0,1,15,i*1.45,15),box(0,0,4,16,i*.85,12),box(4,0,0,12,i*.75,16));}
    public RubbleBlock(){super(Properties.of().strength(-1,3600000).sound(SoundType.GRAVEL).noOcclusion());registerDefaultState(stateDefinition.any().setValue(LAYERS,1).setValue(WATERLOGGED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LAYERS,WATERLOGGED);}
    @Override public net.minecraft.world.level.material.FluidState getFluidState(BlockState s){return s.getValue(WATERLOGGED)?net.minecraft.world.level.material.Fluids.WATER.getSource(false):super.getFluidState(s);}
    @Override public BlockState updateShape(BlockState s,Direction d,BlockState next,LevelAccessor l,BlockPos p,BlockPos other){if(s.getValue(WATERLOGGED))l.scheduleTick(p,net.minecraft.world.level.material.Fluids.WATER,net.minecraft.world.level.material.Fluids.WATER.getTickDelay(l));return super.updateShape(s,d,next,l,p,other);}
    @Override public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new RubbleBlockEntity(p,s);}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPES[s.getValue(LAYERS)];}
    @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(LAYERS)<=2?Shapes.empty():SHAPES[s.getValue(LAYERS)];}
    @Override public float getDestroyProgress(BlockState s,Player p,BlockGetter l,BlockPos pos){return 0;}
}
