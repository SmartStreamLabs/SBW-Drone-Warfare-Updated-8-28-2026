package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Server-owned leases, work and payouts. Both players consume the same indivisible rubble units. */
public final class RubbleTools {
    public static void reset(){SESSIONS.clear();AREAS.clear();LAST_WORK.clear();}
    private static final class Session {final ServerPlayer player;final ItemStack tool;BlockPos target;long last;Session(ServerPlayer p,BlockPos t){player=p;target=t;tool=p.getMainHandItem();last=p.serverLevel().getGameTime();}}
    private static final class Area {final ServerPlayer player;final ServerLevel level;final BlockPos centre;final int radius;final boolean admin;final ArrayDeque<BlockPos> todo;boolean running;int cleared;Area(ServerPlayer p,BlockPos c,int r,boolean a){player=p;level=p.serverLevel();centre=c;radius=r;admin=a;todo=new ArrayDeque<>(piles(level,c,r));}}
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();private static final Map<UUID,Area> AREAS=new HashMap<>();
    private static final Map<RubbleBlockEntity,Long> LAST_WORK=new WeakHashMap<>();
    public static boolean shovel(ItemStack s){return !s.isEmpty()&&(s.getItem()instanceof ShovelItem||s.is(net.minecraft.tags.ItemTags.SHOVELS));}
    public static boolean target(ServerPlayer p,BlockPos pos){if(p.distanceToSqr(Vec3.atCenterOf(pos))>36||!p.serverLevel().hasChunkAt(pos))return false;var hit=p.serverLevel().clip(new ClipContext(p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(6)),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));return hit.getType()==HitResult.Type.BLOCK&&hit.getBlockPos().equals(pos);}
    public static void heartbeat(ServerPlayer p,RubbleUse m){if(!m.held()){SESSIONS.remove(p.getUUID());return;}if(!p.isAlive()||p.isSpectator()||InjuryServer.cannotOperate(p)||!shovel(p.getMainHandItem())||!target(p,m.pos())||!(p.serverLevel().getBlockEntity(m.pos())instanceof RubbleBlockEntity))return;var old=SESSIONS.get(p.getUUID());if(old==null||old.tool!=p.getMainHandItem()||!old.target.equals(m.pos()))SESSIONS.put(p.getUUID(),new Session(p,m.pos()));else old.last=p.serverLevel().getGameTime();}
    public static List<BlockPos> piles(ServerLevel l,BlockPos centre,int radius){var out=new ArrayList<BlockPos>();int minX=(centre.getX()-radius)>>4,maxX=(centre.getX()+radius)>>4,minZ=(centre.getZ()-radius)>>4,maxZ=(centre.getZ()+radius)>>4;for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){var chunk=l.getChunkSource().getChunkNow(x,z);if(chunk==null)continue;for(var e:chunk.getBlockEntities().entrySet())if(e.getValue()instanceof RubbleBlockEntity&&Math.abs(e.getKey().getX()-centre.getX())<=radius&&Math.abs(e.getKey().getY()-centre.getY())<=radius&&Math.abs(e.getKey().getZ()-centre.getZ())<=radius)out.add(e.getKey().immutable());}out.sort(Comparator.comparingDouble(p->p.distSqr(centre)));return out;}
    public static void area(ServerPlayer p,BlockPos centre){if(InjuryServer.cannotOperate(p)||p.getMainHandItem().getItem()!=AddonItems.RCT01.get()||p.distanceToSqr(Vec3.atCenterOf(centre))>36)return;var old=AREAS.get(p.getUUID());if(p.isShiftKeyDown()){AREAS.remove(p.getUUID());reply(p,centre,0,0,0,0,0,0);return;}if(old!=null&&old.level==p.serverLevel()&&old.centre.equals(centre)){old.running=!old.running;sendArea(old);return;}var a=new Area(p,centre,RubbleConfig.AREA.get(),false);AREAS.put(p.getUUID(),a);sendArea(a);}
    private static void sendArea(Area a){reply(a.player,a.centre,0,RubbleConfig.CLEAR_TICKS.get(),a.todo.size(),a.cleared,a.radius,a.running?2:1);}
    private static void reply(ServerPlayer p,BlockPos c,int work,int required,int units,int cleared,int radius,int mode){AddonNetwork.rubbleReply(p,new RubbleReply(c,work,required,units,cleared,radius,mode));}
    public static boolean clearUnit(ServerPlayer p,RubbleBlockEntity pile){
        if(pile.work+1>=RubbleConfig.CLEAR_TICKS.get()&&!RubbleServer.canClear(p,pile.getBlockPos()))return false;
        var l=p.serverLevel();long now=l.getGameTime();if(LAST_WORK.getOrDefault(pile,-1L)==now)return false;LAST_WORK.put(pile,now);
        if(pile.units()==0)return false;
        if(++pile.work<RubbleConfig.CLEAR_TICKS.get()){pile.setChanged();return false;}
        var unit=pile.take();if(unit==null)return false;
        p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
        // The unit is consumed before inventory/drop calls. No original block loot is also issued.
        if(l.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)&&salvage(unit.receipt(),RubbleConfig.RECOVERY.get())){var item=RubbleMaterials.recovery(unit.state());if(item!=Items.AIR){var stack=new ItemStack(item);if(!p.getInventory().add(stack))p.drop(stack,false);}}
        int damage=RubbleConfig.DURABILITY.get();if(damage>0&&!p.isCreative())p.getMainHandItem().hurtAndBreak(damage,p,actor->actor.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
        return true;
    }
    static boolean salvage(UUID id,double fraction){return RubbleEconomy.salvage(id,fraction);}
    public static void tick(ServerLevel l){
        var it=SESSIONS.values().iterator();while(it.hasNext()){var s=it.next();if(s.player.serverLevel()!=l)continue;if(s.player.hasDisconnected()||!s.player.isAlive()||InjuryServer.cannotOperate(s.player)||s.player.getMainHandItem()!=s.tool||!shovel(s.tool)||l.getGameTime()-s.last>8||!target(s.player,s.target)||!(l.getBlockEntity(s.target)instanceof RubbleBlockEntity)){it.remove();continue;}var pile=(RubbleBlockEntity)l.getBlockEntity(s.target);clearUnit(s.player,pile);if(l.getGameTime()%4==0)reply(s.player,s.target,pile.work,RubbleConfig.CLEAR_TICKS.get(),pile.units(),0,0,3);}
        var ai=AREAS.values().iterator();while(ai.hasNext()){var a=ai.next();if(a.level!=l)continue;if(a.player.hasDisconnected()||!a.player.isAlive()||InjuryServer.cannotOperate(a.player)){ai.remove();continue;}if(!a.admin&&(a.player.getMainHandItem().getItem()!=AddonItems.RCT01.get()||a.player.distanceToSqr(Vec3.atCenterOf(a.centre))>144)){a.running=false;continue;}if(!a.running)continue;
            int steps=a.admin?16:1;while(steps-->0&&!a.todo.isEmpty()){BlockPos pos=a.todo.peek();if(!l.hasChunkAt(pos)){a.running=false;break;}if(!(l.getBlockEntity(pos)instanceof RubbleBlockEntity pile)){a.todo.remove();continue;}if(!l.mayInteract(a.player,pos)){a.running=false;break;}if(a.admin){l.removeBlock(pos,false);a.todo.remove();a.cleared++;}else if(clearUnit(a.player,pile)){a.cleared++;if(pile.units()==0)a.todo.remove();}}
            if(l.getGameTime()%5==0)sendArea(a);if(a.todo.isEmpty()){sendArea(a);ai.remove();}
        }
    }
    public static void commands(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> d){
        var C=net.minecraft.commands.Commands.literal("rubble").requires(s->s.hasPermission(2));
        C.then(net.minecraft.commands.Commands.literal("preview").then(net.minecraft.commands.Commands.argument("radius",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,32)).executes(c->{var p=c.getSource().getPlayerOrException();var a=new Area(p,p.blockPosition(),com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"radius"),true);AREAS.put(p.getUUID(),a);sendArea(a);p.sendSystemMessage(Component.literal("Rubble only: "+a.todo.size()+" piles. /rubble clear confirm; /rubble stop"));return a.todo.size();})));
        C.then(net.minecraft.commands.Commands.literal("clear").then(net.minecraft.commands.Commands.literal("confirm").executes(c->{var p=c.getSource().getPlayerOrException();var a=AREAS.get(p.getUUID());if(a==null||!a.admin||a.level!=p.serverLevel())return 0;a.running=true;sendArea(a);return 1;})));
        C.then(net.minecraft.commands.Commands.literal("stop").executes(c->{var p=c.getSource().getPlayerOrException();AREAS.remove(p.getUUID());reply(p,p.blockPosition(),0,0,0,0,0,0);return 1;}));d.register(C);
    }
    private RubbleTools(){}
}
