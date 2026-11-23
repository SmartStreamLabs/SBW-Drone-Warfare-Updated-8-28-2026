package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import java.util.*;

/** Single owner of environmental changes. Never invokes an explosion or applies blast damage. */
public final class RubbleServer {
    public static void reset(){MOVING.clear();}
    private static final Map<ServerLevel,Set<UUID>> MOVING=new WeakHashMap<>();
    public static int moving(ServerLevel l){return MOVING.computeIfAbsent(l,k->new HashSet<>()).size();}
    public static void moving(ServerLevel l,UUID id,boolean add){var s=MOVING.computeIfAbsent(l,k->new HashSet<>());if(add)s.add(id);else s.remove(id);}
    public static boolean allowed(ServerLevel l,BlockPos p,BlockState s,UUID owner){
        if(!l.hasChunkAt(p)||!l.getWorldBorder().isWithinBounds(p)||!RubbleMaterials.eligible(s)||s.getDestroySpeed(l,p)<0)return false;
        var actor=owner==null?null:l.getServer().getPlayerList().getPlayer(owner);
        if(actor==null)actor=net.neoforged.neoforge.common.util.FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(owner==null?UUID.fromString("f3b1d8fe-a92d-46b1-8adb-92fe26f155d1"):owner,"[Drone Warfare]"));
        if(!l.mayInteract(actor,p))return false;
        boolean cancelled=net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(l,p,s,actor)).isCanceled();return !cancelled;
    }
    public static boolean canClear(ServerPlayer p,BlockPos pos){return p.serverLevel().mayInteract(p,pos)&&!net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(p.serverLevel(),pos,p.serverLevel().getBlockState(pos),p)).isCanceled();}
    public static void queue(ServerLevel l,Vec3 at,UUID owner,Collection<BlockPos> positions,double core,Set<BlockPos> denied){queue(l,at,owner,positions,core,denied,false);}
    public static void queue(ServerLevel l,Vec3 at,UUID owner,Collection<BlockPos> positions,double core,Set<BlockPos> denied,boolean reinforced){
        var d=RubbleTasks.get(l);if(d.jobs.size()>=RubbleConfig.JOBS.get())return;
        var j=new RubbleTasks.Job(BlockPos.containing(at),owner);j.core=core;j.securitycraft=reinforced;j.denied.addAll(denied);int max=RubbleConfig.BLOCKS.get();
        var selected=new ArrayList<BlockPos>();var glass=new ArrayList<BlockPos>();for(var p:positions)if(l.hasChunkAt(p)&&p.distSqr(j.centre)<=RubbleConfig.RADIUS.get()*RubbleConfig.RADIUS.get()&&RubbleMaterials.eligible(l.getBlockState(p),j.securitycraft)){(RubbleMaterials.kind(l.getBlockState(p),j.securitycraft)==RubbleMaterials.Kind.GLASS?glass:selected).add(p.immutable());}selected.sort(Comparator.comparingDouble(p->p.distSqr(j.centre)));glass.sort(Comparator.comparingDouble(p->p.distSqr(j.centre)));int reserve=Math.min(max/4,glass.size());if(selected.size()>max-reserve)selected.subList(max-reserve,selected.size()).clear();for(var p:glass)if(selected.size()<max)selected.add(p);for(var p:selected){var s=l.getBlockState(p);j.pending.put(p,s);j.before.put(p,s);}j.origin=at;
        if(!j.pending.isEmpty()){d.jobs.add(j);d.setDirty();}
    }
    private static boolean detach(ServerLevel l,RubbleTasks d,RubbleTasks.Job j,BlockPos p,BlockState expected,int chain,RubbleSupport.Budget budget){
        if(j.securitycraft&&SecurityCraftBlastCompat.selected(expected))return detachReinforced(l,d,j,p,expected,chain,budget);
        if(j.denied.contains(p)||d.deposits.size()>=RubbleConfig.JOBS.get()*RubbleConfig.BLOCKS.get()||!l.getBlockState(p).equals(expected)||!allowed(l,p,expected,j.owner))return false;
        if(chain==0&&!RubbleBlast.exposed(l,j,p,expected,budget))return false;
        var id=UUID.randomUUID();var escrow=new RubbleTasks.Deposit(id,p,expected,j.owner);d.deposits.add(escrow);d.setDirty();
        // No normal loot: the single persistent escrow owns all future salvage.
        if(!l.removeBlock(p,false)){d.deposits.remove(escrow);return false;}
        for(Direction dir:Direction.values())if(j.seeds.size()<RubbleConfig.BLOCKS.get()*6){j.checked.remove(p.relative(dir));j.seeds.add(new RubbleTasks.Seed(p.relative(dir),chain));}
        RubbleEvents.burst(l,p,expected,chain==0?0:1);
        if(moving(l)<RubbleConfig.PIECES.get()){
            var e=new RubbleFragmentEntity(AddonEntities.RUBBLE_FRAGMENT.get(),l);e.init(escrow,Vec3.atCenterOf(p),j.centre);
            if(l.addFreshEntity(e))d.deposits.remove(escrow);
        }
        return true;
    }

    private static boolean detachReinforced(ServerLevel l,RubbleTasks d,RubbleTasks.Job j,BlockPos p,BlockState expected,int chain,RubbleSupport.Budget budget){
        if(j.denied.contains(p)||d.deposits.size()>=RubbleConfig.JOBS.get()*RubbleConfig.BLOCKS.get()||!l.getBlockState(p).equals(expected))return false;
        if(chain==0&&!RubbleBlast.exposed(l,j,p,expected,budget))return false;
        var parts=new LinkedHashMap<BlockPos,BlockState>();parts.put(p,expected);
        var mate=SecurityCraftBlastCompat.partner(p,expected);
        if(mate!=null){if(!l.hasChunkAt(mate))return false;var state=l.getBlockState(mate);if(state.getBlock()==expected.getBlock()&&state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)&&state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)!=expected.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF))parts.put(mate,state);}
        if(budget.left<32*(parts.size()-1))return false;
        for(var part:parts.entrySet())if(j.denied.contains(part.getKey())||!SecurityCraftBlastCompat.selected(part.getValue())||!claimAllowed(l,part.getKey(),part.getValue(),j.owner))return false;
        if(j.before.size()+parts.keySet().stream().filter(at->!j.before.containsKey(at)).count()>RubbleConfig.BLOCKS.get()*4)return false;
        for(var part:parts.entrySet())j.before.putIfAbsent(part.getKey(),part.getValue());
        budget.left-=32*(parts.size()-1);
        var snapshots=new ArrayList<net.neoforged.neoforge.common.util.BlockSnapshot>();
        for(var at:parts.keySet())snapshots.add(net.neoforged.neoforge.common.util.BlockSnapshot.create(l.dimension(),l,at));
        var material=SecurityCraftBlastCompat.debris(expected);
        var escrow=new RubbleTasks.Deposit(UUID.randomUUID(),p,material,j.owner);d.deposits.add(escrow);d.setDirty();
        // Suppress intermediate neighbour callbacks: SC's lower door otherwise drops a second door item.
        int changed=0;for(var part:parts.entrySet()){
            if(!l.setBlock(part.getKey(),part.getValue().getFluidState().createLegacyBlock(),18)){
                for(int i=0;i<changed;i++)snapshots.get(i).restore(18);
                d.deposits.remove(escrow);return false;
            }changed++;
        }
        for(var part:parts.entrySet()){
            var at=part.getKey();l.updateNeighborsAt(at,part.getValue().getBlock());l.getBlockState(at).updateNeighbourShapes(l,at,3);
            for(Direction dir:Direction.values())if(j.seeds.size()<RubbleConfig.BLOCKS.get()*6){j.checked.remove(at.relative(dir));j.seeds.add(new RubbleTasks.Seed(at.relative(dir),chain));}
            RubbleEvents.burst(l,at,SecurityCraftBlastCompat.debris(part.getValue()),chain==0?0:1);
        }
        if(moving(l)<RubbleConfig.PIECES.get()){var e=new RubbleFragmentEntity(AddonEntities.RUBBLE_FRAGMENT.get(),l);e.init(escrow,Vec3.atCenterOf(p),j.centre);if(l.addFreshEntity(e))d.deposits.remove(escrow);}
        return true;
    }
    private static boolean claimAllowed(ServerLevel l,BlockPos p,BlockState state,UUID owner){
        if(!l.hasChunkAt(p)||!l.getWorldBorder().isWithinBounds(p))return false;
        var actor=owner==null?null:l.getServer().getPlayerList().getPlayer(owner);
        if(actor==null)actor=net.neoforged.neoforge.common.util.FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(owner==null?UUID.fromString("f3b1d8fe-a92d-46b1-8adb-92fe26f155d1"):owner,"[Drone Warfare]"));
        return l.mayInteract(actor,p)&&!net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(l,p,state,actor)).isCanceled();
    }
    public static void tick(ServerLevel l){
        var d=RubbleTasks.get(l);var budget=new RubbleSupport.Budget(RubbleConfig.WORK.get());
        // Existing airborne material must settle even when new destruction has been disabled.
        if(l.getGameTime()%10==0){int n=Math.min(d.deposits.size(),8);for(int i=0;i<n&&budget.left>200;i++){var v=d.deposits.remove();if(!deposit(l,v,budget))d.deposits.add(advance(l,v,budget));d.setDirty();}}
        if(!RubbleConfig.ENABLED.get()||RubbleConfig.MODE.get()==RubbleConfig.Mode.VISUAL)return;
        int count=d.jobs.size();for(int loop=0;loop<count&&budget.left>64;loop++){
            var j=d.jobs.remove();boolean blocked=false;
            int removed=0;var it=j.pending.entrySet().iterator();while(it.hasNext()&&budget.left>64&&removed<8){var e=it.next();if(!l.hasChunkAt(e.getKey())){blocked=true;break;}it.remove();budget.left-=32;if(detach(l,d,j,e.getKey(),e.getValue(),0,budget))removed++;}
            if(j.pending.isEmpty()&&RubbleConfig.MODE.get()==RubbleConfig.Mode.COLLAPSE){
                while(!j.seeds.isEmpty()&&budget.left>=512){var seed=j.seeds.remove();if(seed.chain>=RubbleConfig.CHAIN.get()||j.checked.contains(seed.pos))continue;if(!l.hasChunkAt(seed.pos)){j.seeds.add(seed);blocked=true;break;}j.checked.add(seed.pos);if(j.checked.size()>RubbleConfig.BLOCKS.get()*8)break;
                    var state=l.getBlockState(seed.pos);if(!RubbleMaterials.eligible(state,j.securitycraft))continue;
                    var before=new RubbleSupport.World(){public boolean loaded(BlockPos p){return l.hasChunkAt(p);}public BlockState at(BlockPos p){return j.before.getOrDefault(p,l.getBlockState(p));}};
                    var after=new RubbleSupport.World(){public boolean loaded(BlockPos p){return l.hasChunkAt(p);}public BlockState at(BlockPos p){return l.getBlockState(p);}};
                    var was=RubbleSupport.check(before,seed.pos,j.centre,RubbleConfig.RADIUS.get(),budget,j.securitycraft);
                    if(was!=RubbleSupport.Result.SUPPORTED)continue;
                    var now=RubbleSupport.check(after,seed.pos,j.centre,RubbleConfig.RADIUS.get(),budget,j.securitycraft);
                    if(now==RubbleSupport.Result.UNSUPPORTED&&j.before.size()<RubbleConfig.BLOCKS.get()*4){j.before.put(seed.pos,state);detach(l,d,j,seed.pos,state,seed.chain+1,budget);}
                }
            }
            if(!j.pending.isEmpty()||RubbleConfig.MODE.get()==RubbleConfig.Mode.COLLAPSE&&!j.seeds.isEmpty())d.jobs.add(j);
            d.setDirty();if(blocked)break;
        }
    }
    public static boolean deposit(ServerLevel l,RubbleTasks.Deposit source,RubbleSupport.Budget budget){
        int[][] offsets={{0,0},{1,0},{-1,0},{0,1},{0,-1}};
        for(var delta:offsets)for(int y=0;y>=-24;y--){if(budget.left--<=0)return false;BlockPos p=source.pos().offset(delta[0],y,delta[1]);if(p.getY()<l.getMinBuildHeight()+1||!l.hasChunkAt(p))break;if(!l.getWorldBorder().isWithinBounds(p))break;var s=l.getBlockState(p);
            if(l.getBlockEntity(p)instanceof RubbleBlockEntity pile){if(pile.contains(source.id()))return true;if(!pile.accepts())break;var area=new AABB(p).setMaxY(p.getY()+Math.min(1,(pile.layers()+1)*.1));if(!l.getEntitiesOfClass(LivingEntity.class,area).isEmpty())break;if(!permitDeposit(l,p,source.owner()))break;if(pile.add(source.id(),source.state()))return true;break;}
            if(!s.isAir()&&!s.is(Blocks.WATER))break;
            var below=p.below();if(!l.getBlockState(below).isFaceSturdy(l,below,Direction.UP))continue;
            var shape=new AABB(p).setMaxY(p.getY()+.18);if(!l.getEntitiesOfClass(LivingEntity.class,shape).isEmpty())continue;
            // Protection is checked at the destination as well; no user building is replaced.
            var snapshot=net.neoforged.neoforge.common.util.BlockSnapshot.create(l.dimension(),l,p);
            if(!l.setBlock(p,AddonBlocks.RUBBLE.get().defaultBlockState().setValue(RubbleBlock.WATERLOGGED,s.is(Blocks.WATER)),3))continue;
            if(!permitDeposit(l,p,source.owner(),snapshot)){snapshot.restore(3);continue;}
            if(l.getBlockEntity(p)instanceof RubbleBlockEntity pile)return pile.add(source.id(),source.state());
        }
        return false;
    }
    private static boolean permitDeposit(ServerLevel l,BlockPos p,UUID owner){return permitDeposit(l,p,owner,net.neoforged.neoforge.common.util.BlockSnapshot.create(l.dimension(),l,p));}
    private static boolean permitDeposit(ServerLevel l,BlockPos p,UUID owner,net.neoforged.neoforge.common.util.BlockSnapshot snapshot){
        var actor=owner==null?null:l.getServer().getPlayerList().getPlayer(owner);
        if(actor==null)actor=net.neoforged.neoforge.common.util.FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(owner==null?UUID.fromString("f3b1d8fe-a92d-46b1-8adb-92fe26f155d1"):owner,"[Drone Warfare]"));
        return l.mayInteract(actor,p)&&!net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(snapshot,l.getBlockState(p.below()),actor)).isCanceled();
    }
    private static RubbleTasks.Deposit advance(ServerLevel l,RubbleTasks.Deposit d,RubbleSupport.Budget budget){for(int y=1;y<=16;y++){if(budget.left--<=0)return d;var p=d.pos().below(y);if(p.getY()<=l.getMinBuildHeight()+1||!l.hasChunkAt(p)||!l.getBlockState(p).getCollisionShape(l,p).isEmpty())return d;}return new RubbleTasks.Deposit(d.id(),d.pos().below(16),d.state(),d.owner());}
    public static void rest(ServerLevel l,RubbleTasks.Deposit d){var budget=new RubbleSupport.Budget(180);if(!deposit(l,d,budget)){RubbleTasks.get(l).deposits.add(d);RubbleTasks.get(l).setDirty();}}
    private RubbleServer(){}
}
