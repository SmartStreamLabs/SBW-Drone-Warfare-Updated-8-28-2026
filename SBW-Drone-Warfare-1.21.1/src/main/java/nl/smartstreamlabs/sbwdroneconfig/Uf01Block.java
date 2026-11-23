package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.context.BlockPlaceContext;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.world.level.block.state.properties.*;import net.minecraft.world.phys.shapes.*;
/** One main renderer, unticked physical helpers and root-owned loot. No cloth helper entities. */
public final class Uf01Block extends BaseEntityBlock {
 public static final com.mojang.serialization.MapCodec<Uf01Block> CODEC=simpleCodec(Uf01Block::new);
 @Override protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final IntegerProperty PART=IntegerProperty.create("part",0,Uf01Layout.CELLS.length-1);
 static final ThreadLocal<Boolean> MUTATING=ThreadLocal.withInitial(()->false);
 public Uf01Block(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.EAST).setValue(PART,0));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection());}
 public static BlockPos root(BlockPos p,BlockState s){return Uf01Layout.root(p,s.getValue(FACING),s.getValue(PART));}
 @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity e,net.minecraft.world.item.ItemStack stack){super.setPlacedBy(l,p,s,e,stack);if(!l.isClientSide&&l.getBlockEntity(p)instanceof Uf01BlockEntity b){b.owner=e==null?null:e.getUUID();b.ownerName=e==null?"":e.getScoreboardName();b.range=UfMusicConfig.RANGE.get();b.setChanged();}}
 @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,net.minecraft.world.phys.BlockHitResult hit){if(player instanceof net.minecraft.server.level.ServerPlayer sp)UfMusicServer.open(sp,root(p,s));return net.minecraft.world.InteractionResult.sidedSuccess(l.isClientSide);}
 @Override protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState s,Level l,BlockPos p,Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){if(player instanceof net.minecraft.server.level.ServerPlayer sp)UfMusicServer.open(sp,root(p,s));return net.minecraft.world.ItemInteractionResult.sidedSuccess(l.isClientSide);}
 @Override public void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){super.onPlace(s,l,p,old,moving);if(!l.isClientSide&&!old.is(this)&&(s.getValue(PART)==0||Uf01Layout.CELLS[s.getValue(PART)][0]!=0||Uf01Layout.CELLS[s.getValue(PART)][2]!=0))l.scheduleTick(p,this,200);}
 @Override public void tick(BlockState s,net.minecraft.server.level.ServerLevel l,BlockPos p,net.minecraft.util.RandomSource r){
  BlockPos base=root(p,s);Direction f=s.getValue(FACING);int part=s.getValue(PART);
  if(part==0){for(int i=0;i<Uf01Layout.CELLS.length;i++){BlockPos cell=Uf01Layout.cell(p,f,i);if(!l.hasChunkAt(cell))continue;BlockState got=l.getBlockState(cell);if(!got.is(this)||got.getValue(PART)!=i||got.getValue(FACING)!=f||Uf01Layout.CELLS[i][1]==0&&!l.getBlockState(cell.below()).isFaceSturdy(l,cell.below(),Direction.UP)){l.destroyBlock(p,true);return;}}}
  else if(l.hasChunkAt(base)){BlockState b=l.getBlockState(base);if(!b.is(this)||b.getValue(PART)!=0||b.getValue(FACING)!=f){l.removeBlock(p,false);return;}}
  l.scheduleTick(p,this,200);
 }
 @Override public void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos changed,boolean moving){if(l.isClientSide||MUTATING.get())return;BlockPos base=root(p,s);if(Uf01Layout.CELLS[s.getValue(PART)][1]==0&&!l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP)){if(l.hasChunkAt(base)&&l.getBlockState(base).is(this))l.destroyBlock(base,true);else l.removeBlock(p,false);}}
 @Override public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){if(!l.isClientSide&&player.isCreative()&&s.getValue(PART)!=0){BlockPos base=root(p,s);if(l.hasChunkAt(base)&&l.getBlockState(base).is(this))l.destroyBlock(base,false);}return super.playerWillDestroy(l,p,s,player);}
 @Override public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){
  if(!s.is(next.getBlock())&&!l.isClientSide&&!MUTATING.get()){
   MUTATING.set(true);try{BlockPos base=root(p,s);Direction f=s.getValue(FACING);if(s.getValue(PART)!=0&&l.hasChunkAt(base)){BlockState b=l.getBlockState(base);if(b.is(this)&&b.getValue(PART)==0&&b.getValue(FACING)==f)l.destroyBlock(base,true);}
    for(int i=1;i<Uf01Layout.CELLS.length;i++){BlockPos cell=Uf01Layout.cell(base,f,i);if(!l.hasChunkAt(cell))continue;BlockState got=l.getBlockState(cell);if(got.is(this)&&got.getValue(PART)==i&&got.getValue(FACING)==f)l.removeBlock(cell,false);}
   }finally{MUTATING.set(false);}
  }super.onRemove(s,l,p,next,moving);
 }
 @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Uf01Layout.shape(s.getValue(FACING),s.getValue(PART));}
 @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return getShape(s,l,p,c);}
 @Override public VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
 @Override public RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(PART)==0?new Uf01BlockEntity(p,s):null;}
 @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
