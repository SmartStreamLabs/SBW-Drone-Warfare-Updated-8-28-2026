package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

/** Native SBW damage and existing rubble/VFX, constructed WITHOUT an entity or TNT proxy. */
public final class Lm01Explosion {
 public static void explode(ServerLevel l,Vec3 at){
  boolean blocks=Lm01Config.WORLD_DAMAGE.get()&&com.atsuishio.superbwarfare.config.server.ExplosionConfig.EXPLOSION_DESTROY.get();
  var e=new com.atsuishio.superbwarfare.tools.CustomExplosion(l,null,new net.minecraft.world.damagesource.DamageSource(l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(com.atsuishio.superbwarfare.init.ModDamageTypes.CUSTOM_EXPLOSION),at),Lm01Config.DAMAGE.get().floatValue(),at.x,at.y,at.z,Lm01Config.RADIUS.get().floatValue(),blocks?Explosion.BlockInteraction.DESTROY:Explosion.BlockInteraction.KEEP);
  if(!blocks)((Lm01ExplosionPolicy)(Object)e).lm01$denyTerrain();
  if(net.minecraftforge.event.ForgeEventFactory.onExplosionStart(l,e))return;
  if(blocks)RubbleEvents.mark(e);
  e.explode();e.finalizeExplosion(true); // Shared finalization owns the single VFX/audio event.
 }
 private Lm01Explosion(){}
}
