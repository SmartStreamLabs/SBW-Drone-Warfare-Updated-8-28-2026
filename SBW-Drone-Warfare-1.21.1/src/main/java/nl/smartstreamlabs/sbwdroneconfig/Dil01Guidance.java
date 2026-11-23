package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.entity.projectile.JavelinMissileEntity;import net.minecraft.world.entity.Entity;import net.minecraft.world.entity.projectile.Projectile;import net.minecraft.world.entity.player.Player;
/** Native movement, collision and damage run first. Only DIL-marked Javelins receive this air intercept correction. */
public final class Dil01Guidance {
 public static void step(JavelinMissileEntity missile){Entity m=missile;if(m.isRemoved())return;Entity target=null;var level=m.level();
  if(level.isClientSide){target=com.atsuishio.superbwarfare.tools.EntityFindUtil.findEntity(level,missile.getTargetUUID());}
  else {try{target=((net.minecraft.server.level.ServerLevel)level).getEntity(java.util.UUID.fromString(missile.getTargetUUID()));}catch(IllegalArgumentException ignored){}if(!(((Projectile)m).getOwner() instanceof Player p)||!Dil01System.hostile(p,target)){target=null;missile.setTargetUuid("none");}}
  if(target!=null){var delta=target.getBoundingBox().getCenter().subtract(m.position());var velocity=target.getDeltaMovement();double lead=Dil01Lead.time(delta.x,delta.y,delta.z,velocity.x,velocity.y,velocity.z,Math.max(2,m.getDeltaMovement().length()));var aim=target.getBoundingBox().getCenter().add(velocity.scale(lead));missile.turn(aim.subtract(m.position()).normalize(),12f);}
  m.setDeltaMovement(m.getDeltaMovement().add(m.getLookAngle()).scale(.8));
 }
 private Dil01Guidance(){}
}
