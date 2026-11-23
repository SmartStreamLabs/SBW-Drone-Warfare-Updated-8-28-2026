package nl.smartstreamlabs.sbwdroneconfig;
/** Client particles only; engine instances are owned and stopped by DroneAudioManager. */
public final class Fl01Effects{
 public static void step(Fl01Entity e){var m=net.minecraft.client.Minecraft.getInstance();if(m.player==null||e.spent()||e.fuel()<=0||m.player.distanceToSqr(e)>16384||e.tickCount%2!=0)return;var at=Fl01Geometry.point(Fl01Geometry.frame(e,1),new net.minecraft.world.phys.Vec3(0,1.3,5.35));var v=e.getDeltaMovement().normalize().scale(-.12);e.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,at.x,at.y,at.z,v.x,v.y,v.z);if(e.tickCount%4==0)e.level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,at.x,at.y,at.z,v.x*.4,.02,v.z*.4);}
}
