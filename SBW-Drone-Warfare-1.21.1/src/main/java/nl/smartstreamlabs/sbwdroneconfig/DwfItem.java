package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.network.chat.Component;import net.minecraft.world.*;import net.minecraft.world.item.*;import net.minecraft.world.item.context.BlockPlaceContext;import net.minecraft.world.level.*;
public final class DwfItem extends BlockItem{
 public DwfItem(net.minecraft.world.level.block.Block block,Properties p){super(block,p);}
 public static BlockPos anchor(BlockPos target,Direction facing){return target.relative(facing.getOpposite(),4);}
 @Override public BlockPlaceContext updatePlacementContext(BlockPlaceContext c){if(c.getClickedFace()!=Direction.UP)return null;var facing=c.getPlayer()==null?Direction.NORTH:c.getPlayer().getDirection().getOpposite();var root=anchor(c.getClickedPos(),facing);return new BlockPlaceContext(c.getLevel(),c.getPlayer(),c.getHand(),c.getItemInHand(),new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(root),Direction.UP,root,false));}
 @Override public InteractionResult place(BlockPlaceContext c){c=updatePlacementContext(c);if(c==null)return InteractionResult.FAIL;var level=c.getLevel();var root=c.getClickedPos();var player=c.getPlayer();if(player==null)return InteractionResult.FAIL;var facing=player.getDirection().getOpposite();
  for(int i=0;i<DwfGeometry.CELLS.length;i++){var at=DwfGeometry.cell(root,facing,i);if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||!level.mayInteract(player,at)||!player.mayUseItemAt(at,c.getClickedFace(),c.getItemInHand())||!level.getBlockState(at).canBeReplaced()||!level.getWorldBorder().isWithinBounds(at))return fail(player,"Not enough clear space for DWF-01 (15 x 6 x 7 blocks)");}
  for(int i=0;i<DwfGeometry.CELLS.length;i++)if(DwfGeometry.CELLS[i][1]==0){var support=DwfGeometry.cell(root,facing,i).below();if(!level.getBlockState(support).isFaceSturdy(level,support,Direction.UP))return fail(player,"DWF-01 needs a solid, level foundation");}
  if(!level.getEntities((net.minecraft.world.entity.Entity)null,DwfGeometry.bounds(root,facing),entity->!entity.isSpectator()&&!entity.isRemoved()&&!(entity instanceof net.minecraft.world.entity.ExperienceOrb)).isEmpty())return fail(player,"Placement area is occupied; move clear of DWF-01");
  if(level.isClientSide)return InteractionResult.SUCCESS;var server=(net.minecraft.server.level.ServerLevel)level;var tag=c.getItemInHand().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();var id=tag!=null&&tag.hasUUID("DwfStorage")?tag.getUUID("DwfStorage"):java.util.UUID.randomUUID();var claims=DwfClaims.get(server);
  if(tag!=null&&tag.hasUUID("DwfWorld")&&!tag.getUUID("DwfWorld").equals(claims.worldId))return fail(player,"Packed DWF-01 stock belongs to another world");
  if(!claims.claim(id,server.dimension().location().toString(),root.asLong()))return fail(player,"This packed fabricator is already placed");
  // Capture the complete replaceable footprint before any mutation. A rejected
  // setBlock or initialization failure must not leave a partial machine or claim.
  var snapshots=new java.util.ArrayList<net.neoforged.neoforge.common.util.BlockSnapshot>(DwfGeometry.CELLS.length);
  for(int i=0;i<DwfGeometry.CELLS.length;i++)snapshots.add(net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(),level,DwfGeometry.cell(root,facing,i),3));
  DwfBlock.MUTATING.set(true);
  try{
   var state=getBlock().defaultBlockState().setValue(DwfBlock.FACING,facing);
   for(int i=0;i<DwfGeometry.CELLS.length;i++)if(!level.setBlock(DwfGeometry.cell(root,facing,i),state.setValue(DwfBlock.PART,i),3))throw new IllegalStateException("Fabricator footprint placement rejected");
   if(level.getBlockEntity(root)instanceof DwfBlockEntity b)b.initialize(id,player.getUUID());else throw new IllegalStateException("Fabricator controller missing");
  }catch(RuntimeException failure){
   for(int i=snapshots.size()-1;i>=0;i--)if(!snapshots.get(i).restore(3))SbwDroneRangeConfig.LOGGER.error("DWF footprint rollback failed at {}",snapshots.get(i).getPos());
   claims.release(id);SbwDroneRangeConfig.LOGGER.error("DWF placement cancelled at {}",root,failure);
   return fail(player,"DWF-01 could not be placed; no item was consumed");
  }finally{DwfBlock.MUTATING.set(false);}

  if(!player.isCreative())c.getItemInHand().shrink(1);level.playSound(null,root,net.minecraft.sounds.SoundEvents.METAL_PLACE,net.minecraft.sounds.SoundSource.BLOCKS,.7f,1);return InteractionResult.CONSUME;
 }
 private static InteractionResult fail(net.minecraft.world.entity.player.Player p,String text){if(!p.level().isClientSide)p.displayClientMessage(Component.literal(text),true);return InteractionResult.FAIL;}
@Override public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions>c){c.accept(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions(){public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){return DwfItemRenderer.instance();}});}
}
