package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Called once immediately before the existing jammer accumulator is advanced. */
public final class Dj01System {
    private static final int MAX_TARGETS=64,REFRESH_TICKS=4;
    private record Cache(ServerLevel level,long refresh,List<Entity> targets,ItemStack stack){}
    private static final Map<UUID,Cache> CACHES=new HashMap<>();
    public static Vec3 muzzle(Player p){return p.getEyePosition().add(p.getLookAngle().scale(1.35));}
    // Check the complete chunk path before Level.clip, whose block reads must never load terrain for this item.
    private static boolean loadedRay(ServerLevel l,Vec3 a,Vec3 b){
        int x=net.minecraft.util.Mth.floor(a.x)>>4,z=net.minecraft.util.Mth.floor(a.z)>>4,endX=net.minecraft.util.Mth.floor(b.x)>>4,endZ=net.minecraft.util.Mth.floor(b.z)>>4;
        double dx=b.x-a.x,dz=b.z-a.z;int sx=Double.compare(dx,0),sz=Double.compare(dz,0);
        double tx=sx==0?Double.POSITIVE_INFINITY:(((x+(sx>0?1:0))*16d)-a.x)/dx,tz=sz==0?Double.POSITIVE_INFINITY:(((z+(sz>0?1:0))*16d)-a.z)/dz;
        double stepX=sx==0?Double.POSITIVE_INFINITY:16/Math.abs(dx),stepZ=sz==0?Double.POSITIVE_INFINITY:16/Math.abs(dz);
        for(int i=0;i<128;i++){
            if(!l.hasChunk(x,z))return false;if(x==endX&&z==endZ)return true;
            if(Math.abs(tx-tz)<1e-12){if(!l.hasChunk(x+sx,z)||!l.hasChunk(x,z+sz))return false;x+=sx;z+=sz;tx+=stepX;tz+=stepZ;}
            else if(tx<tz){x+=sx;tx+=stepX;}else{z+=sz;tz+=stepZ;}
        }
        return false;
    }
    private static boolean visible(ServerLevel level,Entity user,Vec3 a,Vec3 b){
        if(!loadedRay(level,a,b))return false;
        return level.clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,user)).getType()==HitResult.Type.MISS;
    }
    public static String cannotUse(Player p,ItemStack stack){
        if(!p.isAlive()||p.isSpectator()||p instanceof ServerPlayer sp&&sp.hasDisconnected()||!(p.level() instanceof ServerLevel))return "dj01.unavailable";
        if(!Dj01Config.ENABLED.get()||!AddonConfig.enableDroneJammer())return "dj01.disabled";
        if(Dj01Item.energy(stack)<Dj01Config.COST.get())return "dj01.empty";
        var l=(ServerLevel)p.level();Vec3 muzzle=muzzle(p);
        if(!visible(l,p,p.getEyePosition(),muzzle)||!visible(l,p,muzzle,muzzle.add(p.getLookAngle().scale(.45))))return "dj01.blocked";
        return null;
    }
    public static boolean friendlyAllowed(ServerPlayer p,Entity drone){
        var owner=SbwCompat.resolveDroneOwner(p.serverLevel(),drone);UUID controller=SbwCompat.getDroneControllerUuid(drone);
        if(owner.hasUuid()&&owner.uuid().equals(p.getUUID())||p.getUUID().equals(controller))return Dj01Config.AFFECT_OWN.get();
        boolean allied=drone.isAlliedTo(p);
        if(owner.hasUuid()){var other=p.server.getPlayerList().getPlayer(owner.uuid());if(other!=null&&other!=p&&p.isAlliedTo(other))allied=true;}
        if(owner.hasName()&&p.getTeam()!=null&&p.server.getScoreboard().getPlayersTeam(owner.name())==p.getTeam())allied=true;
        if(controller!=null){var other=p.server.getPlayerList().getPlayer(controller);if(other!=null&&other!=p&&p.isAlliedTo(other))allied=true;}
        return !allied||Dj01Config.AFFECT_ALLIES.get();
    }
    public static boolean eligible(ServerPlayer p,Entity drone,Vec3 source,Vec3 direction,double range,double cone){
        if(drone.level()!=p.level()||drone.isRemoved()||!JammerRules.affects(p.serverLevel(),drone,null,Set.of())||FiberOpticLinkSystem.isImmuneToNormalJammers(drone)||!friendlyAllowed(p,drone))return false;
        Vec3 target=drone.getBoundingBox().getCenter(),delta=target.subtract(source);
        return Dj01Cone.contains(delta.x,delta.y,delta.z,direction.x,direction.y,direction.z,range,cone)&&visible(p.serverLevel(),p,source,target);
    }
    public static void tick(MinecraftServer server){
        var active=new HashSet<UUID>();
        for(ServerPlayer p:server.getPlayerList().getPlayers()){
            if(!p.isUsingItem()||p.getUsedItemHand()!=InteractionHand.MAIN_HAND||p.getUseItem()!=p.getMainHandItem()||!Dj01Item.is(p.getUseItem()))continue;
            ItemStack stack=p.getUseItem();var previous=CACHES.get(p.getUUID());if(previous!=null&&previous.level()!=p.level()){Dj01Item.emitting(stack,false);p.stopUsingItem();continue;}String reason=cannotUse(p,stack);
            if(reason!=null){Dj01Item.emitting(stack,false);p.stopUsingItem();p.displayClientMessage(Component.translatable(reason),true);p.getCooldowns().addCooldown(stack.getItem(),20);continue;}
            var storage=Dj01Item.storage(stack);int cost=Dj01Config.COST.get();
            if(storage==null||storage.extractEnergy(cost,true)!=cost||storage.extractEnergy(cost,false)!=cost){Dj01Item.emitting(stack,false);p.stopUsingItem();continue;}
            Dj01Item.emitting(stack,true);active.add(p.getUUID());
            Vec3 source=muzzle(p),direction=p.getLookAngle();double range=Dj01Config.RANGE.get(),cone=Dj01Config.CONE.get();long now=p.serverLevel().getGameTime();
            Cache cache=CACHES.get(p.getUUID());if(cache!=null&&cache.stack()!=stack){Dj01Item.emitting(cache.stack(),false);cache=null;}
            if(cache==null||cache.level()!=p.level()||now>=cache.refresh()){
                var targets=p.serverLevel().getEntities((Entity)null,new AABB(source,source).inflate(range),e->eligible(p,e,source,direction,range,cone));
                targets.sort(Comparator.comparingDouble(e->e.distanceToSqr(source)));if(targets.size()>MAX_TARGETS)targets=targets.subList(0,MAX_TARGETS);
                cache=new Cache(p.serverLevel(),now+REFRESH_TICKS,List.copyOf(targets),stack);CACHES.put(p.getUUID(),cache);
            }
            // Revalidate every tick: releasing, turning, moving behind a wall and fiber changes never leave a cached beam active.
            for(Entity target:cache.targets())if(eligible(p,target,source,direction,range,cone))DroneJamStateManager.recordInfluence(p.serverLevel(),target,target.getBoundingBox().getCenter().distanceTo(source),range);
            p.getInventory().setChanged();
            if(Dj01Item.energy(stack)<cost){Dj01Item.emitting(stack,false);p.stopUsingItem();}
        }
        CACHES.entrySet().removeIf(entry->{if(active.contains(entry.getKey()))return false;Dj01Item.emitting(entry.getValue().stack(),false);return true;});
    }
    public static void clear(){for(var cache:CACHES.values())Dj01Item.emitting(cache.stack(),false);CACHES.clear();} private Dj01System(){}
}
