package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.*;
import net.minecraft.world.phys.*;
import java.util.*;

/** Protection mods see all candidates in the original Detonate event before finalisation consumes them. */
public final class RubbleEvents {
    private static final class Context {
        final String source;final UUID owner;final boolean securitycraft;double core=3;final Set<BlockPos> proposed=new HashSet<>();
        final Set<BlockPos> staged=java.util.concurrent.ConcurrentHashMap.newKeySet();
        final java.util.concurrent.atomic.AtomicInteger received=new java.util.concurrent.atomic.AtomicInteger();
        Context(String s,UUID u){this(s,u,false);}
        Context(String s,UUID u,boolean reinforced){source=s;owner=u;securitycraft=reinforced&&SecurityCraftBlastCompat.enabled();}
    }
    private static final Map<Explosion,Context> CONTEXTS=Collections.synchronizedMap(new WeakHashMap<>());
    /** SBW computes these positions in worker tasks, then clears its shared list before Detonate.
     * Only collect immutable coordinates here; all world reads/changes stay on the server thread. */
    public static boolean stage(Explosion e,Collection<BlockPos> blocks){var c=CONTEXTS.get(e);if(c==null||!RubbleConfig.ENABLED.get())return false;int limit=RubbleConfig.BLOCKS.get()*8;for(var p:blocks)if(!c.staged.contains(p)&&c.received.getAndIncrement()<limit)c.staged.add(p.immutable());return true;}
    private record Visual(ServerLevel level,Vec3 pos,long expires,float scale){}
    private static final ArrayDeque<Visual> VISUALS=new ArrayDeque<>();
    private static final class Scan {long time=-1;int left;}
    private static final Map<ServerLevel,Scan> SCANS=new WeakHashMap<>();
    private static final Map<ServerLevel,ArrayList<RubbleBurst.Sample>> BURSTS=new WeakHashMap<>();
    public static void mark(Explosion e){var a=DroneAttackDamage.current();CONTEXTS.put(e,new Context(a==null?"":a.drone(),uuid(a==null?"":a.owner()),true));}
    public static void markPayload(Explosion e,net.minecraft.world.entity.Entity payload){var a=DroneAttackDamage.current();if(a==null)a=DroneAttackDamage.identify(payload,null);String id=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(payload.getType()).toString();UUID owner=e.getIndirectSourceEntity()==null?null:e.getIndirectSourceEntity().getUUID();var attacker=((nl.smartstreamlabs.sbwdroneconfig.mixin.Vc01ExplosionAccess)e).vc01$source().getEntity();if(owner==null&&attacker!=null)owner=attacker.getUUID();if(a!=null){id=a.drone();if(!a.payload().isEmpty()&&RubbleConfig.hasProfile(a.payload()))id=a.payload();if(owner==null)owner=uuid(a.owner());}CONTEXTS.put(e,new Context(id,owner,true));}
    public static float visualScale(ServerLevel l,Vec3 pos){var it=VISUALS.descendingIterator();while(it.hasNext()){var v=it.next();if(v.level==l&&v.expires>=l.getGameTime()&&v.pos.distanceToSqr(pos)<.04)return v.scale*RubbleConfig.VISUAL.get().floatValue();}return RubbleConfig.VISUAL.get().floatValue();}
    public static boolean owns(Explosion e){return RubbleConfig.ENABLED.get()&&(CONTEXTS.containsKey(e)||DroneExplosionVfx.isAttributed(e.getDirectSourceEntity()));}
    private static UUID uuid(String s){try{return UUID.fromString(s);}catch(Exception e){return null;}}
    public static boolean visible(ServerLevel l,Vec3 origin,BlockPos target,Set<BlockPos> removed){
        if(!l.hasChunksAt(BlockPos.containing(origin),target))return false;
        var hit=l.clip(new ClipContext(origin,Vec3.atCenterOf(target),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,(net.minecraft.world.entity.Entity)null){
            @Override public net.minecraft.world.phys.shapes.VoxelShape getBlockShape(BlockState s,net.minecraft.world.level.BlockGetter world,BlockPos p){return removed.contains(p)&&RubbleMaterials.eligible(s)?net.minecraft.world.phys.shapes.Shapes.empty():super.getBlockShape(s,world,p);}
        });
        return hit.getType()==HitResult.Type.MISS||hit.getBlockPos().equals(target);
    }
    public static void prepare(Explosion e){
        
        if(!(((nl.smartstreamlabs.sbwdroneconfig.mixin.RubbleExplosionAccess)e).rubble$level()instanceof ServerLevel l)||!RubbleConfig.ENABLED.get())return;
        var source=e.getDirectSourceEntity();
        if(!CONTEXTS.containsKey(e)&&!DroneExplosionVfx.isAttributed(source)&&!(source instanceof AntiVehicleMortarPayloadEntity))return;
        if(!CONTEXTS.containsKey(e)){var a=DroneAttackDamage.current();String id=source==null?"":net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(source.getType()).toString();UUID owner=source==null?null:SbwCompat.getDroneControllerUuid(source);if(owner==null&&a!=null)owner=uuid(a.owner());var indirect=e.getIndirectSourceEntity();if(owner==null&&indirect!=null)owner=indirect.getUUID();CONTEXTS.put(e,new Context(id,owner));}
        var context=CONTEXTS.get(e);context.proposed.clear();e.getToBlow().clear();
        // These sources explicitly expose environmental-damage switches; native KEEP alone is
        // insufficient because HG-01 deliberately delegates its terrain pass to this system.
        if(source instanceof Fl01Entity&&(!Fl01Config.BLOCK_DAMAGE.get()||!AddonConfig.enableDroneCrashExplosionDamage())
            ||source instanceof Id01Entity&&!AddonConfig.enableDroneCrashExplosionDamage())return;
        if(VISUALS.size()>=64)VISUALS.removeFirst();VISUALS.add(new Visual(l,e.center(),l.getGameTime()+2,(float)RubbleConfig.visual(context.source)));
        if(source instanceof net.minecraft.world.entity.Mob&&!l.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))return;
        if(RubbleConfig.MODE.get()!=RubbleConfig.Mode.VISUAL&&RubbleConfig.RESPECT_SBW.get()&&e instanceof com.atsuishio.superbwarfare.tools.CustomExplosion&&!com.atsuishio.superbwarfare.config.server.ExplosionConfig.EXPLOSION_DESTROY.get())return;
        float nativeRadius=((nl.smartstreamlabs.sbwdroneconfig.mixin.RubbleExplosionAccess)e).rubble$radius();
        double core=Math.min(RubbleConfig.RADIUS.get(),Math.max(0,nativeRadius*RubbleConfig.scale(context.source)));context.core=core;
        var at=e.center();int radius=(int)Math.ceil(Math.min(RubbleConfig.RADIUS.get(),core*RubbleConfig.GLASS.get()));
        var centre=BlockPos.containing(at);var scan=SCANS.computeIfAbsent(l,k->new Scan());if(scan.time!=l.getGameTime()){scan.time=l.getGameTime();scan.left=RubbleConfig.WORK.get();}
        for(var offset:RubbleBlast.OFFSETS){
            if(offset.distance()>radius*radius||scan.left--<=0||e.getToBlow().size()>=RubbleConfig.BLOCKS.get()*4)break;
            var p=centre.offset(offset.x(),offset.y(),offset.z());if(context.proposed.contains(p)||!l.hasChunkAt(p))continue;
            var state=l.getBlockState(p);if(!RubbleMaterials.eligible(state,context.securitycraft))continue;
            if(p.distToCenterSqr(at)>core*core&&RubbleMaterials.kind(state,context.securitycraft)!=RubbleMaterials.Kind.GLASS)continue;
            var mate=context.securitycraft?SecurityCraftBlastCompat.partner(p,state):null;
            if(mate!=null){
                if(scan.left--<=0||e.getToBlow().size()+2>RubbleConfig.BLOCKS.get()*4||!l.hasChunkAt(mate))continue;
                var paired=l.getBlockState(mate);
                if(paired.getBlock()!=state.getBlock()||!paired.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)||paired.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)==state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF))mate=null;
                else if(!RubbleMaterials.eligible(paired,true))continue;
            }
            if(context.proposed.add(p))e.getToBlow().add(p);
            if(mate!=null&&context.proposed.add(mate))e.getToBlow().add(mate);
        }
        context.proposed.addAll(e.getToBlow());if(RubbleConfig.MODE.get()==RubbleConfig.Mode.VISUAL){int n=0;for(var p:e.getToBlow()){if(n++>=16)break;burst(l,p,l.getBlockState(p),0);}e.getToBlow().clear();}
        
    }
    /** Invoked once at the actual finalisation boundary, after all event listeners have filtered blocks. */
    public static void finish(Explosion e){
        
        Context context=CONTEXTS.remove(e);if(context==null||!RubbleConfig.ENABLED.get())return;
        var l=((nl.smartstreamlabs.sbwdroneconfig.mixin.RubbleExplosionAccess)e).rubble$level();if(!(l instanceof ServerLevel server))return;
        var blocks=new ArrayList<>(e.getToBlow());e.getToBlow().clear();
        var denied=new HashSet<>(context.proposed);denied.removeAll(blocks);if(RubbleConfig.MODE.get()!=RubbleConfig.Mode.VISUAL)RubbleServer.queue(server,e.center(),context.owner,blocks,context.core,denied,context.securitycraft);
    }
    public static void burst(ServerLevel l,BlockPos p,BlockState state,int kind){var a=BURSTS.computeIfAbsent(l,k->new ArrayList<>());if(a.size()<96)a.add(new RubbleBurst.Sample(p,net.minecraft.world.level.block.Block.getId(state),kind));}
    @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null)return;for(var l:server.getAllLevels()){
        RubbleServer.tick(l);RubbleTools.tick(l);
        var list=BURSTS.remove(l);if(list!=null&&!list.isEmpty()){var m=new RubbleBurst(l.dimension().location(),l.getGameTime(),List.copyOf(list));for(var p:l.players())if(list.stream().anyMatch(s->DroneClientChunkSyncManager.trackingPosition(p).distanceToSqr(Vec3.atCenterOf(s.pos()))<192*192))AddonNetwork.rubbleBurst(p,m);}
    }}
    @SubscribeEvent public static void joined(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){if(e.getLevel()instanceof ServerLevel l&&e.getEntity()instanceof RubbleFragmentEntity)RubbleServer.moving(l,e.getEntity().getUUID(),true);}
    @SubscribeEvent public static void left(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent e){if(e.getLevel()instanceof ServerLevel l&&e.getEntity()instanceof RubbleFragmentEntity)RubbleServer.moving(l,e.getEntity().getUUID(),false);}
    @SubscribeEvent public static void commands(net.neoforged.neoforge.event.RegisterCommandsEvent e){RubbleTools.commands(e.getDispatcher());}
    @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){CONTEXTS.clear();SCANS.clear();BURSTS.clear();VISUALS.clear();RubbleTools.reset();RubbleServer.reset();SecurityCraftBlastCompat.clear();}
    @SubscribeEvent public static void tags(net.neoforged.neoforge.event.TagsUpdatedEvent e){SecurityCraftBlastCompat.clear();}
    private RubbleEvents(){}
}
