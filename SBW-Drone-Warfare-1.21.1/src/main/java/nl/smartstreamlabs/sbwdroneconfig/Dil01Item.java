package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.item.gun.GunGeoItem;import com.atsuishio.superbwarfare.data.gun.*;import com.atsuishio.superbwarfare.entity.projectile.JavelinMissileEntity;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.entity.Entity;import net.minecraft.world.item.*;import net.minecraft.network.chat.Component;import net.minecraft.world.level.Level;import java.util.*;
/** Native SBW gun data, reload controls, real Javelin ammunition and native projectile/damage. */
public class Dil01Item extends GunGeoItem {
 public Dil01Item(){super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));}
 public static boolean is(ItemStack s){return s.getItem() instanceof Dil01Item;}
 @Override public boolean useSpecialFireProcedure(GunData d){return false;}
 @Override public void tick(Entity holder,GunData data,boolean selected){Dil01System.tick(holder,data,selected);}
 @Override public boolean canShoot(GunData d,Entity holder){if(!super.canShoot(d,holder)||d.ammo.get()!=1)return false;if(holder instanceof ServerPlayer p)return Dil01System.locked(p,d)!=null;return d.data().getBoolean("DILLocked");}
 @Override public void shoot(ShootParameters args){
  if(!(args.shooter instanceof ServerPlayer p)||!args.zoom||!canShoot(args.data,p))return;var d=args.data;var target=Dil01System.locked(p,d);if(target==null)return;long now=p.level().getGameTime();if(d.data().contains("DILShot")&&d.data().getLong("DILShot")==now)return;
  var spawn=Dil01AimMath.muzzle(p,true);var muzzleRay=p.level().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),spawn,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));if(muzzleRay.getType()!=net.minecraft.world.phys.HitResult.Type.MISS){p.displayClientMessage(Component.translatable("dil01.blocked"),true);return;}
  var m=new JavelinMissileEntity(p,p.level(),d.get(GunProp.DAMAGE).floatValue(),d.get(GunProp.EXPLOSION_DAMAGE).floatValue(),d.get(GunProp.EXPLOSION_RADIUS).floatValue(),0,target.position());
  ((Dil01MissileAccess)m).dil01(true);m.setAttackMode(false);m.setTargetUuid(target.getStringUUID());m.setLife(d.get(GunProp.PROJECTILE_LIFE));((Entity)m).setPos(spawn);var dir=p.getLookAngle();((net.minecraft.world.entity.projectile.Projectile)m).shoot(dir.x,dir.y,dir.z,3f,0);
  // Commit before spawning: event reentry sees an empty launcher. A rejected spawn restores exactly one loaded round.
  d.ammo.set(0);d.data().putLong("DILShot",now);d.save();if(!p.serverLevel().addFreshEntity(m)){d.ammo.set(1);d.save();return;}
  Dil01System.fired(p,d);d.shootAnimationTimer.set(8);d.save();playFireSounds(d,p,true);
 }
 @Override public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(net.minecraft.world.entity.LivingEntity p,net.minecraft.world.InteractionHand hand,ItemStack stack){Dil01Pose.capture(p);return Dil01Pose.firstHand?net.minecraft.client.model.HumanoidModel.ArmPose.EMPTY:net.minecraft.client.model.HumanoidModel.ArmPose.CROSSBOW_HOLD;}
 @Override public void registerControllers(software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar c){}
 @Override public java.util.function.Supplier<? extends software.bernie.geckolib.renderer.GeoItemRenderer<? extends Item>> getRenderer(){return Dil01ItemRenderer::instance;}
 @Override public void appendHoverText(ItemStack stack,Item.TooltipContext level,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,level,lines,flag);lines.add(Component.translatable("dil01.tooltip"));}
}
