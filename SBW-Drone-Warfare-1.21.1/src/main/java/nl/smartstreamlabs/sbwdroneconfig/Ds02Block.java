package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** One root entity, reserved physical upper footprint, one item drop for the whole siren. */
public class Ds02Block extends BaseEntityBlock {
 public static final com.mojang.serialization.MapCodec<Ds02Block> CODEC=simpleCodec(Ds02Block::new);
 @Override protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE=BlockStateProperties.LIT;
    public static final IntegerProperty PART=IntegerProperty.create("part",0,Ds02Layout.CELLS.length-1);
    private static final ThreadLocal<Boolean> REMOVING=ThreadLocal.withInitial(()->false);
    public Ds02Block(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH).setValue(ACTIVE,false).setValue(PART,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,ACTIVE,PART);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){
        Level l=c.getLevel();BlockPos root=c.getClickedPos();Direction f=c.getHorizontalDirection();
        if(!l.getBlockState(root.below()).isFaceSturdy(l,root.below(),Direction.UP))return fail(c);
        for(int i=0;i<Ds02Layout.CELLS.length;i++){
            BlockPos p=Ds02Layout.cell(root,f,i);
            if(l.isOutsideBuildHeight(p) || !l.getWorldBorder().isWithinBounds(p) || !l.hasChunkAt(p) || !l.getBlockState(p).canBeReplaced(c)
                || c.getPlayer()!=null && (!l.mayInteract(c.getPlayer(),p) || !c.getPlayer().mayUseItemAt(p,Direction.UP,c.getItemInHand())))return fail(c);
        }
        // The cabinet door swings outward; this is clearance only, never an extra ticking entity.
        for(int height=0;height<=1;height++){
            BlockPos door=root.relative(f).above(height);
            if(!l.hasChunkAt(door) || !l.getBlockState(door).canBeReplaced(c))return fail(c);
        }
        return defaultBlockState().setValue(FACING,f);
    }
    private BlockState fail(BlockPlaceContext c){if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.translatable("ds02.clearance"),true);return null;}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity who,ItemStack stack){
        if(l.isClientSide)return;
        for(int i=1;i<Ds02Layout.CELLS.length;i++)if(!l.setBlock(Ds02Layout.cell(p,s.getValue(FACING),i),s.setValue(PART,i),3)){l.destroyBlock(p,true);return;}
        if(l.getBlockEntity(p) instanceof Ds02BlockEntity be && who!=null)be.setOwnerUuid(who.getUUID());
    }
    public static BlockPos root(BlockPos p,BlockState s){return Ds02Layout.root(p,s.getValue(FACING),s.getValue(PART));}
    @Override public void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){
        super.onPlace(s,l,p,old,moving);if(!l.isClientSide && !old.is(this))l.scheduleTick(p,this,20);
    }
    @Override public void tick(BlockState s,net.minecraft.server.level.ServerLevel l,BlockPos p,net.minecraft.util.RandomSource random){
        BlockPos r=root(p,s);
        if(s.getValue(PART)!=0){
            if(l.hasChunkAt(r) && (!l.getBlockState(r).is(this) || l.getBlockState(r).getValue(PART)!=0)){l.removeBlock(p,false);return;}
        }else for(int i=1;i<Ds02Layout.CELLS.length;i++){
            BlockPos cell=Ds02Layout.cell(r,s.getValue(FACING),i);if(!l.hasChunkAt(cell))continue;
            BlockState part=l.getBlockState(cell);
            if(!part.is(this) || part.getValue(PART)!=i || part.getValue(FACING)!=s.getValue(FACING)){l.destroyBlock(r,true);return;}
        }
        l.scheduleTick(p,this,100);
    }
    @Override public InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(l.getBlockEntity(root(p,s)) instanceof Ds02BlockEntity be && player instanceof ServerPlayer sp){
            if(!be.canConfigure(sp))sp.displayClientMessage(Component.translatable("message.sbwdroneconfig.siren_not_owner"),true);
            else{be.control(sp,0);AddonNetwork.openSirenConfig(sp,new OpenSirenConfigMessage(be.getBlockPos(),be.getBlacklistedGamertags()));}
        }
        return InteractionResult.CONSUME;
    }
    @Override public void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos n,boolean moving){
        if(!l.isClientSide && !REMOVING.get()){
            BlockPos r=root(p,s);
            if(s.getValue(PART)==0 && !l.getBlockState(r.below()).isFaceSturdy(l,r.below(),Direction.UP))l.destroyBlock(r,true);
            else if(s.getValue(PART)!=0 && l.hasChunkAt(r) && !l.getBlockState(r).is(this))l.removeBlock(p,false);
        }
    }
    /** A creative helper break must not turn root cleanup into a survival loot drop. */
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        if(!level.isClientSide&&player.isCreative()&&state.getValue(PART)!=0){
            var anchor=root(pos,state);
            if(level.hasChunkAt(anchor)){var base=level.getBlockState(anchor);if(base.is(this)&&base.getValue(PART)==0&&base.getValue(FACING)==state.getValue(FACING))level.removeBlock(anchor,false);}
        }
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){
        if(!s.is(next.getBlock()) && !l.isClientSide && !REMOVING.get()){
            REMOVING.set(true);
            try{
                BlockPos r=root(p,s);Direction f=s.getValue(FACING);
                if(s.getValue(PART)!=0 && l.hasChunkAt(r) && l.getBlockState(r).is(this))l.destroyBlock(r,true);
                for(int i=1;i<Ds02Layout.CELLS.length;i++){
                    BlockPos part=Ds02Layout.cell(r,f,i);if(!l.hasChunkAt(part))continue;
                    BlockState ps=l.getBlockState(part);if(ps.is(this) && ps.getValue(PART)==i && ps.getValue(FACING)==f)l.removeBlock(part,false);
                }
            }finally{REMOVING.set(false);}
        }
        super.onRemove(s,l,p,next,moving);
    }
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Ds02Layout.shape(s.getValue(FACING),s.getValue(PART));}
    @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return getShape(s,l,p,c);}
    @Override public VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override public RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(PART)==0?new Ds02BlockEntity(p,s):null;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){
        return l.isClientSide?createTickerHelper(t,AddonBlockEntities.DS02.get(),(a,b,c,be)->Ds02Audio.tick(be))
            :createTickerHelper(t,AddonBlockEntities.DS02.get(),Ds02BlockEntity::serverTick);
    }
    @Override public boolean isSignalSource(BlockState s){return s.getValue(PART)==0;}
    @Override public int getSignal(BlockState s,BlockGetter l,BlockPos p,Direction d){return s.getValue(PART)==0 && s.getValue(ACTIVE) && AddonConfig.droneSirenRedstoneOutput()?15:0;}
    @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
