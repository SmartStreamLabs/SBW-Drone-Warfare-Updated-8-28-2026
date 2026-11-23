package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.world.phys.*;import net.minecraft.network.syncher.*;import net.minecraft.nbt.CompoundTag;import net.minecraft.server.level.ServerLevel;import java.util.UUID;
import software.bernie.geckolib.animatable.GeoEntity;import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;import software.bernie.geckolib.core.animation.AnimatableManager;import software.bernie.geckolib.util.GeckoLibUtil;
/** An inert render proxy until armed by an authoritative throw/drop. No contact damage. */
public class Hg01Entity extends Entity implements GeoEntity{
 private static final EntityDataAccessor<Boolean> ARMED=SynchedEntityData.defineId(Hg01Entity.class,EntityDataSerializers.BOOLEAN);private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
 private int remaining,bounces,steps;private UUID owner;private boolean spent;private double lx,ly,lz;
 public Hg01Entity(EntityType<? extends Hg01Entity> type,Level level){super(type,level);}
 protected void defineSynchedData(){entityData.define(ARMED,false);}
 public boolean armed(){return entityData.get(ARMED);}public int remaining(){return remaining;}public int bounces(){return bounces;}
 public void arm(net.minecraft.world.entity.player.Player p,Vec3 at,Vec3 velocity){if(level().isClientSide||armed()||spent)return;owner=p==null?null:p.getUUID();moveTo(at.x,at.y,at.z,p==null?0:p.getYRot(),0);setDeltaMovement(velocity);remaining=Hg01Config.FUSE.get();entityData.set(ARMED,true);}
 @Override public void tick(){super.tick();if(level().isClientSide){if(steps>0){setPos(getX()+(lx-getX())/steps,getY()+(ly-getY())/steps,getZ()+(lz-getZ())/steps);--steps;}return;}if(!armed()||spent)return;
  if(--remaining<=0){detonate();return;}Vec3 before=position(),v=getDeltaMovement().add(0,-.045,0);if(v.lengthSqr()>9)v=v.normalize().scale(3);move(MoverType.SELF,v);Vec3 actual=position().subtract(before);double x=Math.abs(actual.x-v.x)>1e-6?-v.x*.28:v.x,z=Math.abs(actual.z-v.z)>1e-6?-v.z*.28:v.z,y=v.y;
  if(Math.abs(actual.y-v.y)>1e-6){boolean bounce=v.y<-.18&&bounces<4;y=bounce?-v.y*.28:0;if(bounce){bounces++;if(tickCount%3==0)level().playSound(null,getX(),getY(),getZ(),net.minecraft.sounds.SoundEvents.STONE_HIT,net.minecraft.sounds.SoundSource.PLAYERS,.18f,.9f);}}
  double drag=onGround()?.58:.985;if(onGround()&&x*x+z*z<.0001){x=0;z=0;}setDeltaMovement(x*drag,y,z*drag);
 }
 public boolean detonate(){if(spent||!armed()||!(level() instanceof ServerLevel l))return false;spent=true;remaining=0;var p=owner==null?null:l.getPlayerByUUID(owner);discard();try(var scope=DroneAttackDamage.scope(this)){new com.atsuishio.superbwarfare.tools.CustomExplosion.Builder(this).directSource(this).source(this).attacker(p).damageSource(damageSources().explosion(this,p)).position(position().add(0,.06,0)).damage(Hg01Config.DAMAGE.get().floatValue()).radius(Hg01Config.RADIUS.get().floatValue()).keepBlock().fireTime(0).explode();}return true;}
 @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int ticks,boolean teleport){lx=x;ly=y;lz=z;steps=Math.max(1,ticks);setYRot(yaw);setXRot(pitch);}
 protected void addAdditionalSaveData(CompoundTag n){n.putBoolean("Armed",armed());n.putBoolean("Spent",spent);n.putInt("Fuse",remaining);n.putInt("Bounces",bounces);if(owner!=null)n.putUUID("Owner",owner);}
 protected void readAdditionalSaveData(CompoundTag n){spent=n.getBoolean("Spent");remaining=n.contains("Fuse")?Math.max(1,Math.min(200,n.getInt("Fuse"))):Hg01Config.FUSE.get();bounces=Math.min(4,n.getInt("Bounces"));owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;entityData.set(ARMED,n.getBoolean("Armed"));if(spent)discard();}
 @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket(){return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);}
 public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}public void registerControllers(AnimatableManager.ControllerRegistrar controllers){}
 @Override public net.minecraft.world.item.ItemStack getPickResult(){return new net.minecraft.world.item.ItemStack(AddonItems.HG01.get());}
}
