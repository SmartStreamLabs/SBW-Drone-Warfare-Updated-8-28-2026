package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.phys.*;import java.util.*;
/** Creates precisely the existing registered PM-01. No alternate missile or flight implementation. */
public final class PatriotShots{
 private static final Set<UUID>BUSY=new HashSet<>();
 public static Pm01Entity spawn(PatriotLauncher source,ServerPlayer actor,Entity target,Vec3 origin,Vec3 direction){var l=source.defenseLevel();if(!BUSY.add(source.defenseId()))return null;try{if(!Mpl01Entity.airDrone(target)||Adv01Targets.relation(l,source.defenseOwner(),source.defenseOwnerName(),target)!=Adv01Targets.HOSTILE||target.position().distanceToSqr(origin)>Math.pow(Mpl01Config.RANGE.get(),2)||Pm01Entity.active(l)>=Mpl01Config.MAX_MISSILES.get())return null;var missile=AddonEntities.PM01.get().create(l);if(missile==null)return null;missile.launch(source,actor,target,origin,direction);if(!l.addFreshEntity(missile)){Pm01Entity.track(l,missile.getUUID(),false);return null;}return missile;}finally{BUSY.remove(source.defenseId());}}
 public static int assigned(ServerLevel l,UUID target){return Pm01Entity.assigned(l,target);}
}
