package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.context.BlockPlaceContext;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.world.level.block.state.properties.*;import net.minecraft.world.phys.*;import net.minecraft.world.phys.shapes.*;
public final class Dvt01Block extends BaseEntityBlock {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public Dvt01Block(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity e,net.minecraft.world.item.ItemStack stack){if(!l.isClientSide&&l.getBlockEntity(p)instanceof Dvt01BlockEntity b){b.owner=e.getUUID();b.changed();}}
 @Override public boolean canSurvive(BlockState s,LevelReader l,BlockPos p){if(l.getBlockEntity(p)instanceof Dvt01BlockEntity d&&d.pbRoot!=null)return Pb01Dvt.supported(d);return l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP);}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor l,BlockPos p,BlockPos op){return d==Direction.DOWN&&!canSurvive(s,l,p)?Blocks.AIR.defaultBlockState():super.updateShape(s,d,other,l,p,op);}
 @Override public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(!l.isClientSide&&player instanceof net.minecraft.server.level.ServerPlayer sp)DvtServer.open(sp,p);return InteractionResult.sidedSuccess(l.isClientSide);}
 private static final java.util.Map<Direction,VoxelShape> OPEN=new java.util.EnumMap<>(Direction.class),CLOSED=new java.util.EnumMap<>(Direction.class);
 static{for(var d:Direction.Plane.HORIZONTAL){CLOSED.put(d,rotated(d,-1.3,0,1.6,17.3,7.4,14.4));OPEN.put(d,Shapes.or(CLOSED.get(d),rotated(d,-1,7.4,12.1,17,17,14.7)));}}
 private static VoxelShape rotated(Direction d,double x,double y,double z,double X,double Y,double Z){double a=Math.toRadians(180-d.toYRot()),sn=Math.sin(a),cs=Math.cos(a),minX=100,minZ=100,maxX=-100,maxZ=-100;for(double px:new double[]{x,X})for(double pz:new double[]{z,Z}){double rx=8+(px-8)*cs+(pz-8)*sn,rz=8-(px-8)*sn+(pz-8)*cs;minX=Math.min(minX,rx);maxX=Math.max(maxX,rx);minZ=Math.min(minZ,rz);maxZ=Math.max(maxZ,rz);}return box(minX,y,minZ,maxX,Y,maxZ);}
 @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){boolean open=!(l.getBlockEntity(p)instanceof Dvt01BlockEntity b)||b.closed(0)<.95;var result=(open?OPEN:CLOSED).get(s.getValue(FACING));return l.getBlockEntity(p)instanceof Dvt01BlockEntity d?Pb01Dvt.shape(d,result):result;}
 @Override public VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
 @Override public RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Dvt01BlockEntity(p,s);}
 @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 @Override public java.util.List<net.minecraft.world.item.ItemStack>getDrops(BlockState s,net.minecraft.world.level.storage.loot.LootParams.Builder params){var be=params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);return be instanceof Dvt01BlockEntity d&&d.pbRoot!=null?java.util.List.of(Pb01Dvt.stack(d)):super.getDrops(s,params);}
 @Override public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){var be=l.getBlockEntity(p);super.onRemove(s,l,p,next,moving);if(!s.is(next.getBlock()))Pb01Dvt.removed(l,p,be);}
}
