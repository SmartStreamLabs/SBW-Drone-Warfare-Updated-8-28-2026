package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.world.level.Level;import net.minecraft.world.level.block.Blocks;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.phys.AABB;import java.util.*;
/** Bounded placement transaction: clear replaceable dry vegetation, preserve solids and restore every changed cell on failure. Loader placement snapshots still see every write. */
public final class BunkerVegetation {
 public static boolean replaceable(Level l,BlockPos p){var s=l.getBlockState(p);return s.isAir()||(s.canBeReplaced()||s.getBlock() instanceof net.minecraft.world.level.block.BushBlock&&s.getCollisionShape(l,p).isEmpty())&&s.getFluidState().isEmpty()&&!s.hasBlockEntity();}
 public static net.minecraft.world.item.context.BlockPlaceContext at(net.minecraft.world.item.context.BlockPlaceContext c,BlockPos root){return new net.minecraft.world.item.context.BlockPlaceContext(c.getLevel(),c.getPlayer(),c.getHand(),c.getItemInHand(),new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(root),net.minecraft.core.Direction.UP,root,false)){
  @Override public BlockPos getClickedPos(){return root;}
  @Override public boolean canPlace(){return replaceable(getLevel(),root);}
  @Override public boolean replacingClickedOnBlock(){return true;}
 };}
 public static final class Transaction {
  private final Level level;private final LinkedHashMap<BlockPos,BlockState> before=new LinkedHashMap<>();
  public Transaction(Level l){level=l;}
  public boolean set(BlockPos p,BlockState s){before.putIfAbsent(p.immutable(),level.getBlockState(p));return level.setBlock(p,s,3);}
  public boolean clear(AABB box){var pending=new ArrayList<BlockPos>();for(var p:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX-1e-5,box.maxY-1e-5,box.maxZ-1e-5))){if(!replaceable(level,p))return false;if(!level.getBlockState(p).isAir()){var pos=p.immutable();before.putIfAbsent(pos,level.getBlockState(pos));pending.add(pos);}}for(var p:pending)if(!level.setBlock(p,Blocks.AIR.defaultBlockState(),2)){rollback();return false;}return true;}
  public void rollback(){var entries=new ArrayList<>(before.entrySet());Collections.reverse(entries);for(var e:entries)level.setBlock(e.getKey(),e.getValue(),2);for(var e:entries)level.updateNeighborsAt(e.getKey(),e.getValue().getBlock());before.clear();}
 }
 private BunkerVegetation(){}
}
