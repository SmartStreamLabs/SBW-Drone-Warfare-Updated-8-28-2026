package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** A plain baked block: no BlockEntity, Entity, random tick, ticker, or idle detection scan. */
public final class Lm01Block extends Block {
 public enum Stage implements StringRepresentable { ARMING,ARMED,SAFE,TRIGGERED;
  public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
 }
 public static final EnumProperty<Stage> STAGE=EnumProperty.create("stage",Stage.class);
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 private static final VoxelShape[] OUTLINE=new VoxelShape[4];
 private static final AABB[] TRIGGER=new AABB[4];
 static {for(var direction:Direction.Plane.HORIZONTAL){int index=direction.get2DDataValue();double minX=32,minZ=32,maxX=-32,maxZ=-32;for(double x:new double[]{1.3,14.7})for(double z:new double[]{1.3,15.3}){double rx=x-8,rz=z-8;for(int i=0;i<index;i++){double a=rx;rx=-rz;rz=a;}minX=Math.min(minX,rx+8);maxX=Math.max(maxX,rx+8);minZ=Math.min(minZ,rz+8);maxZ=Math.max(maxZ,rz+8);}OUTLINE[index]=box(minX,0,minZ,maxX,3.34,maxZ);TRIGGER[index]=new AABB(minX/16,0,minZ/16,maxX/16,3.5/16,maxZ/16);}}
 public Lm01Block(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(STAGE,Stage.ARMING).setValue(FACING,Direction.NORTH));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(STAGE,FACING);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){var s=defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());return canSurvive(s,c.getLevel(),c.getClickedPos())?s:null;}
 @Override public boolean canSurvive(BlockState s,LevelReader l,BlockPos p){return l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP);}
 @Override public void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){if(!old.is(this)&&!l.isClientSide&&s.getValue(STAGE)==Stage.ARMING)l.scheduleTick(p,this,Lm01Config.ARM_TICKS.get());}
 @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity placer,ItemStack stack){if(l instanceof ServerLevel server){server.getBlockTicks().clearArea(new net.minecraft.world.level.levelgen.structure.BoundingBox(p));Lm01Budget.cancel(server,p);server.setBlock(p,s.setValue(STAGE,Stage.ARMING),Block.UPDATE_CLIENTS);server.scheduleTick(p,this,Lm01Config.ARM_TICKS.get());}}
 @Override public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){
  if(!next.is(this)&&l instanceof ServerLevel server){server.getBlockTicks().clearArea(new net.minecraft.world.level.levelgen.structure.BoundingBox(p));Lm01Budget.cancel(server,p);}super.onRemove(s,l,p,next,moving);
 }
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState next,LevelAccessor l,BlockPos p,BlockPos other){
  if(d==Direction.DOWN&&!canSurvive(s,l,p)){if(l instanceof ServerLevel server){var current=server.getBlockState(p);if(current.is(this)&&server.removeBlock(p,false)&&current.getValue(STAGE)!=Stage.TRIGGERED)Block.dropResources(current,server,p);}return Blocks.AIR.defaultBlockState();}return super.updateShape(s,d,next,l,p,other);
 }
 @Override public void tick(BlockState supplied,ServerLevel l,BlockPos p,RandomSource random){
  var s=l.getBlockState(p);if(!s.is(this))return;
  if(s.getValue(STAGE)==Stage.ARMING){if(canSurvive(s,l,p))l.setBlock(p,s.setValue(STAGE,Stage.ARMED),Block.UPDATE_CLIENTS);else l.destroyBlock(p,true);return;}
  if(s.getValue(STAGE)!=Stage.TRIGGERED)return;
  if(!Lm01Budget.claim(l,p)){l.scheduleTick(p,this,Lm01Budget.reserve(l,p));return;}
  long started=System.nanoTime();try{if(l.removeBlock(p,false))Lm01Explosion.explode(l,Vec3.atBottomCenterOf(p).add(0,.10,0));}finally{Lm01Budget.elapsed(l,started);}
 }
 public static AABB triggerBox(BlockPos p,BlockState s){return TRIGGER[s.getValue(FACING).get2DDataValue()].move(p);}
 public static boolean contact(ServerLevel l,BlockPos p,Entity e){
  var s=l.getBlockState(p);if(!s.is(AddonBlocks.LM01.get())||s.getValue(STAGE)!=Stage.ARMED||e.isRemoved()||e.isSpectator())return false;
  boolean allowed=e instanceof Player?Lm01Config.PLAYERS.get():e instanceof LivingEntity&&!(e instanceof net.minecraft.world.entity.decoration.ArmorStand)&&Lm01Config.MOBS.get();
  if(!allowed||e instanceof LivingEntity living&&!living.isAlive()||!triggerBox(p,s).intersects(e.getBoundingBox()))return false;
  return trigger(l,p);
 }
 public static boolean trigger(ServerLevel l,BlockPos p){
  var s=l.getBlockState(p);if(!s.is(AddonBlocks.LM01.get())||s.getValue(STAGE)!=Stage.ARMED)return false;
  if(!l.setBlock(p,s.setValue(STAGE,Stage.TRIGGERED),Block.UPDATE_CLIENTS))return false;
  l.scheduleTick(p,s.getBlock(),Lm01Budget.reserve(l,p));return true;
 }
 @Override public void stepOn(Level level,BlockPos pos,BlockState state,Entity entity){if(level instanceof ServerLevel server)contact(server,pos,entity);super.stepOn(level,pos,state,entity);}
 @Override public void entityInside(BlockState s,Level l,BlockPos p,Entity e){if(l instanceof ServerLevel server)contact(server,p,e);}
 @Override public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
  if(!player.getItemInHand(hand).is(AddonItems.RS01.get()))return InteractionResult.PASS;
  if(l.isClientSide)return InteractionResult.SUCCESS;
  return player instanceof ServerPlayer actor&&interact(actor,p,hand)?InteractionResult.CONSUME:InteractionResult.FAIL;
 }
 public static boolean interact(ServerPlayer p,BlockPos pos,InteractionHand hand){
  var l=p.serverLevel();var s=l.getBlockState(pos);
  if(!s.is(AddonBlocks.LM01.get())||s.getValue(STAGE)==Stage.TRIGGERED||!p.isAlive()||p.isSpectator()||InjuryServer.cannotOperate(p)||!p.getItemInHand(hand).is(AddonItems.RS01.get())||!p.getAbilities().mayBuild||p.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos))>36||!p.mayUseItemAt(pos,Direction.UP,p.getItemInHand(hand))||!RubbleServer.canClear(p,pos))return false;
  var target=l.clip(new ClipContext(p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(6)),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
  if(target.getType()!=HitResult.Type.BLOCK||!target.getBlockPos().equals(pos))return false;
  if(s.getValue(STAGE)!=Stage.SAFE){l.getBlockTicks().clearArea(new net.minecraft.world.level.levelgen.structure.BoundingBox(pos));if(!l.setBlock(pos,s.setValue(STAGE,Stage.SAFE),Block.UPDATE_CLIENTS))return false;p.displayClientMessage(net.minecraft.network.chat.Component.literal("LM-01 disarmed. Right-click again with RS-01 to recover."),true);return true;}
  if(!l.removeBlock(pos,false))return false;
  if(!p.isCreative()){var item=new ItemStack(AddonItems.LM01.get());if(!p.getInventory().add(item))Block.popResource(l,pos,item);}
  return true;
 }
 @Override public boolean onDestroyedByPlayer(BlockState s,Level l,BlockPos p,Player player,boolean harvest,FluidState fluid){
  if(!player.isCreative()&&(s.getValue(STAGE)==Stage.ARMED||s.getValue(STAGE)==Stage.TRIGGERED)){if(l instanceof ServerLevel server)trigger(server,p);return false;}
  return super.onDestroyedByPlayer(s,l,p,player,harvest,fluid);
 }
 @Override public boolean dropFromExplosion(Explosion e){return false;}
 @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return OUTLINE[s.getValue(FACING).get2DDataValue()];}
 @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
 @Override public VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
 @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
