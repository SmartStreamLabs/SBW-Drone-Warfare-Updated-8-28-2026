package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.*;
import net.minecraft.network.chat.Component;
import java.util.*;
public final class Vc01Events {
 @SubscribeEvent public static void grenadeDamage(net.minecraftforge.event.entity.living.LivingHurtEvent e){
  var source=e.getSource();if(e.getEntity().level().isClientSide||!source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION))return;
  var direct=source.getDirectEntity();if(direct==null)return;
  String id=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(direct.getType()).toString();
  if(direct instanceof Hg01Entity||id.equals("superbwarfare:hand_grenade")||id.equals("superbwarfare:rgo_grenade"))e.setAmount((float)(e.getAmount()*Hg01Config.ENTITY_SCALE.get()));
 }

 @SubscribeEvent public static void track(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)VehicleAntiDroneNetSyncSystem.startTracking(p,e.getTarget());}
 @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppedEvent e){Vc01Damage.clear();Vc01Intercept.clear();}
 @SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload e){if(e.getLevel() instanceof ServerLevel level)Vc01Damage.unload(level);}
 // SBW 0.8.9.1 NeoForge hurts/removes vehicles INSIDE its NORMAL Detonate handler.
 @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST) public static void explosion(ExplosionEvent.Detonate e){if(e.getLevel() instanceof ServerLevel l)Vc01Damage.capture(l,e.getExplosion(),e.getAffectedEntities());}
 @SubscribeEvent public static void entity(PlayerInteractEvent.EntityInteract e){var proxy=new PlayerInteractEvent.RightClickItem(e.getEntity(),e.getHand());repair(proxy);if(proxy.isCanceled()){e.setCanceled(true);e.setCancellationResult(proxy.getCancellationResult());}}
 @SubscribeEvent public static void entityAt(PlayerInteractEvent.EntityInteractSpecific e){var proxy=new PlayerInteractEvent.RightClickItem(e.getEntity(),e.getHand());repair(proxy);if(proxy.isCanceled()){e.setCanceled(true);e.setCancellationResult(proxy.getCancellationResult());}}
 @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock e){var proxy=new PlayerInteractEvent.RightClickItem(e.getEntity(),e.getHand());repair(proxy);if(proxy.isCanceled()){e.setCanceled(true);e.setCancellationResult(proxy.getCancellationResult());}}
 @SubscribeEvent public static void repair(PlayerInteractEvent.RightClickItem event){
  var player=event.getEntity();if(!(player.level() instanceof ServerLevel level)||PlayerDroneAnchorManager.getCapturedOriginalPosition((net.minecraft.server.level.ServerPlayer)player)!=null||!player.isShiftKeyDown()||!event.getItemStack().is(Items.IRON_BARS)||!SbwCompat.getActiveLinkedMonitor(player).isEmpty())return;
  Vec3 from=player.getEyePosition(),to=from.add(player.getLookAngle().scale(5));Entity selected=null;String key=null;double nearest=Double.POSITIVE_INFINITY;
  for(Entity vehicle:level.getEntities(player,player.getBoundingBox().inflate(7),SbwCompat::hasVehicleAntiDroneNetUpgrade)){
   var profile=Vc01Geometry.profile(vehicle);if(profile==null)continue;
   for(int b=0;b<profile.branches().size();b++){var branch=profile.branches().get(b);var matrix=Vc01Geometry.matrix(vehicle,branch,1);for(int p=0;p<branch.panels().size();p++){
    var triangle=Vc01Geometry.triangle(branch.panels().get(p),matrix);double t=NtGeometry.sweep(new AABB(from,from).inflate(.005),to.subtract(from),triangle);
    if(Double.isFinite(t)&&t<nearest){nearest=t;selected=vehicle;key=Vc01Panels.id(b,branch.panelIds().get(p));}
   }}
  }
  if(selected==null)return;
  var obstruction=level.clip(new net.minecraft.world.level.ClipContext(from,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,player));if(obstruction.getType()!=net.minecraft.world.phys.HitResult.Type.MISS&&from.distanceToSqr(obstruction.getLocation())+1e-5<25*nearest*nearest)return;
  event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
  if(!player.mayBuild()||!level.mayInteract(player,selected.blockPosition()))return;
  var actor=(net.minecraft.server.level.ServerPlayer)player;boolean denied=false;
  if(selected instanceof VatagEntity)denied=!((VatagEntity)selected).permitted(actor);
  if(selected instanceof RescueVehicle)denied|=!((RescueVehicle)selected).permitted(actor);
  if(selected instanceof Mj03Entity){var truck=(Mj03Entity)selected;denied|=truck.owner()!=null&&!truck.owner().equals(actor.getUUID())&&!actor.hasPermissions(2);}
  if(denied){player.displayClientMessage(Component.literal("VC-01: repair access denied."),true);return;}

  var owner=selected.getPersistentData();if(owner.hasUUID(SbwCompat.TAG_DRONE_OWNER_UUID)&&!owner.getUUID(SbwCompat.TAG_DRONE_OWNER_UUID).equals(player.getUUID())&&!player.hasPermissions(2)){player.displayClientMessage(Component.literal("VC-01: only the owner may repair this cage."),true);return;}
  var health=Vc01Panels.health(selected,key);int cost=Vc01Settings.COST.get();
  if(health.fraction()>=1){player.displayClientMessage(Component.literal("VC-01 "+Vc01Panels.label(key)+": intact. Damaged panels cost "+cost+" iron bars."),true);return;}
  if(!player.getAbilities().instabuild&&event.getItemStack().getCount()<cost){player.displayClientMessage(Component.literal("VC-01 "+Vc01Panels.label(key)+": "+Math.round(health.fraction()*100)+"% — repair costs "+cost+" iron bars in this hand."),true);return;}
  Vc01Panels.set(selected,key,new Vc01Panels.Health(health.maximum(),health.maximum()));if(!player.getAbilities().instabuild)event.getItemStack().shrink(cost);
  player.displayClientMessage(Component.literal("VC-01 "+Vc01Panels.label(key)+": repaired (100%)."),true);
 }
}
