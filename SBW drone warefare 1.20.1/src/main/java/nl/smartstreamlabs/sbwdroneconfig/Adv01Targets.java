package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import java.util.*;

/** Existing flying-drone identity/owner adapters; unknown allegiance is never an automatic enemy. */
public final class Adv01Targets {
    public static final int UNKNOWN=0,FRIEND=1,HOSTILE=2;
    public static boolean drone(Entity e) {
        return e!=null&&e.isAlive()&&!e.isRemoved()&&SbwCompat.isDrone(e)
            &&(!(e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v)||!v.isWreck());
    }
    public static int relation(ServerLevel l,UUID owner,String name,Entity target) {
        if(owner==null||!drone(target)||target.level()!=l)return UNKNOWN;
        var other=SbwCompat.resolveDroneOwner(l,target);if(!other.hasUuid())return UNKNOWN;
        if(owner.equals(other.uuid())||owner.equals(SbwCompat.getDroneControllerUuid(target)))return FRIEND;
        var a=l.getServer().getPlayerList().getPlayer(owner);var b=l.getServer().getPlayerList().getPlayer(other.uuid());
        if(a!=null&&(target.isAlliedTo(a)||b!=null&&a.isAlliedTo(b)))return FRIEND;
        String an=a!=null?a.getScoreboardName():name,bn=b!=null?b.getScoreboardName():other.name();
        if(an==null||an.isBlank()||bn==null||bn.isBlank())return UNKNOWN;
        var at=l.getScoreboard().getPlayersTeam(an);var bt=l.getScoreboard().getPlayersTeam(bn);
        return at!=null&&bt!=null&&at.isAlliedTo(bt)?FRIEND:HOSTILE;
    }
    public static boolean clear(ServerLevel l,Entity source,Vec3 a,Vec3 b) {
        return Hgd01Lasers.loaded(l,a,b)&&l.clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,source)).getType()==HitResult.Type.MISS;
    }
    public static Entity fuseTarget(ServerLevel l,Adv01Shell shell,Vec3 start,Vec3 end) {
        double radius=Adv01Config.FUSE_RADIUS.get();Entity best=null;double fraction=2;
        for(var e:l.getEntities(shell,new AABB(start,end).inflate(radius),Adv01Targets::drone)) {
            if(relation(l,shell.principal(),shell.principalName(),e)!=HOSTILE)continue;
            var p=e.getBoundingBox().getCenter();double t=Adv01Frames.closestFraction(start.x,start.y,start.z,end.x,end.y,end.z,p.x,p.y,p.z);
            var at=start.lerp(end,t);
            if(t<fraction&&at.distanceToSqr(p)<=radius*radius&&clear(l,shell,at,p)){best=e;fraction=t;}
        }
        return best;
    }
    private Adv01Targets() {}
}
