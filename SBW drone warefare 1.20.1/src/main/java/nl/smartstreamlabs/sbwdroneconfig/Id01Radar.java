package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import java.util.*;
/** Reuses portable radar adapters; target eligibility remains a server decision. */
public final class Id01Radar {
    public record Contact(UUID id,String name,float distance,boolean hostile){}
    public static boolean hostile(Id01Entity interceptor,Entity target){
        if(target==null||target==interceptor||!target.isAlive()||target.isRemoved()||target.level()!=interceptor.level()||!SbwCompat.isDrone(target))return false;
        if(!(target.level() instanceof ServerLevel l)||interceptor.ownerId()==null)return false;
        if(!(target instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v)||v.isWreck())return false;
        var o=SbwCompat.resolveDroneOwner(l,target);
        if(!o.hasUuid()||interceptor.ownerId().equals(o.uuid()))return false; // unknown allegiance is protected
        var players=l.getServer().getPlayerList();var a=players.getPlayer(interceptor.ownerId());var b=players.getPlayer(o.uuid());
        if(a!=null&&b!=null&&a.isAlliedTo(b))return false;
        // Stored owner names also preserve scoreboard protection while a player is offline.
        String an=a==null?SbwCompat.resolveDroneOwner(l,interceptor).name():a.getScoreboardName();
        String bn=b==null?o.name():b.getScoreboardName();
        var at=an==null?null:l.getScoreboard().getPlayersTeam(an);var bt=bn==null?null:l.getScoreboard().getPlayersTeam(bn);
        return at==null||bt==null||!at.isAlliedTo(bt);
    }
    public static List<Contact> scan(Id01Entity drone,net.minecraft.server.level.ServerPlayer observer){
        return PortableFpvDetectorServer.scanFrom(observer,drone.position(),Id01Config.RANGE.get()).stream()
            .filter(t->!t.droneId().equals(drone.getUUID()))
            .limit(16).map(t->new Contact(t.droneId(),t.displayName(),(float)t.distance(),hostile(drone,observer.serverLevel().getEntity(t.droneId())))).toList();
    }
}
