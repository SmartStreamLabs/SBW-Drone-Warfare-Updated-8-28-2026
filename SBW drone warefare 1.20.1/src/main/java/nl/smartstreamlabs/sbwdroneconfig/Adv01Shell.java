package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.projectile.SmallCannonShellEntity;
import com.atsuishio.superbwarfare.tools.CustomExplosion;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;
import net.minecraft.nbt.CompoundTag;
import java.util.*;

/** Native SBW small-shell flight, with one server-side swept proximity fuse and one explosion. */
public class Adv01Shell extends SmallCannonShellEntity {
    private static final boolean HAS_MOVE_HOOK=hasMoveHook();
    private static boolean hasMoveHook(){try{com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile.class.getDeclaredMethod("projectileMove",Level.class);return true;}catch(NoSuchMethodException absent){return false;}}
    private static final Map<ServerLevel,Set<UUID>> ACTIVE=new WeakHashMap<>();
    private UUID principal,sourceVehicle;private String principalName="";private boolean sourceChecked,detonated,automatic=true,registered;
    public Adv01Shell(EntityType<? extends Adv01Shell> type,Level level){super(type,level);}
    public UUID principal(){return principal;}public String principalName(){return principalName;}public UUID sourceVehicle(){return sourceVehicle;}
    public boolean automaticShot(){return automatic;}
    public static int activeCount(ServerLevel l){return ACTIVE.getOrDefault(l,Set.of()).size();}
    private void identifySource() {
        if(sourceChecked||level().isClientSide)return;sourceChecked=true;
        Entity owner=getOwner();Adv01Entity source=owner instanceof Adv01Entity a?a:owner!=null&&owner.getVehicle() instanceof Adv01Entity a?a:null;
        if(source==null) {
            double nearest=4;for(var a:level().getEntitiesOfClass(Adv01Entity.class,getBoundingBox().inflate(4))) {
                double d=a.getShootPos("Cannon",1).distanceToSqr(position());
                if(d<nearest&&a.recentShotBy(owner)){source=a;nearest=d;}
            }
        }
        if(source!=null){principal=source.owner();principalName=source.ownerName();sourceVehicle=source.getUUID();automatic=source.automatic();}
        else if(owner instanceof net.minecraft.world.entity.player.Player p){principal=p.getUUID();principalName=p.getScoreboardName();automatic=false;}
    }
    @Override public void tick(){if(!HAS_MOVE_HOOK&&sweep(level()))return;super.tick();}
    
    private boolean sweep(Level level) {
        if(detonated)return true;identifySource();
        if(level instanceof ServerLevel l) {
            if(!registered){registered=true;ACTIVE.computeIfAbsent(l,k->new HashSet<>()).add(getUUID());}
            Vec3 start=position(),end=start.add(getDeltaMovement());
            if(!Hgd01Lasers.loaded(l,start,end)){discard();return true;}
            var wall=l.clip(new net.minecraft.world.level.ClipContext(start,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));
            Vec3 clipped=wall.getType()==HitResult.Type.MISS?end:wall.getLocation();
            var target=Adv01Targets.fuseTarget(l,this,start,clipped);
            if(target!=null) {
                var p=target.getBoundingBox().getCenter();double t=Adv01Frames.closestFraction(start.x,start.y,start.z,clipped.x,clipped.y,clipped.z,p.x,p.y,p.z);
                burst(start.lerp(clipped,t));return true;
            }
        }
        return false;
    }
    @Override public void onHitEntity(EntityHitResult hit){burst(hit.getLocation());}
    @Override public void onHitBlock(BlockHitResult hit){burst(hit.getLocation());}
    public void performDamage(Entity target,float amount,boolean headshot) { /* One explosion replaces direct hit damage. */ }
    @Override public void destroyBlock(BlockHitResult hit) { /* ADV flak never destroys terrain. */ }
    public void afterHitEntity(EntityHitResult hit){burst(hit.getLocation());}
    public void afterHitBlock(BlockHitResult hit){burst(hit.getLocation());}
    @Override public void causeExplode(Vec3 at){burst(at);}
    private void burst(Vec3 at) {
        if(level().isClientSide||detonated||isRemoved())return;identifySource();detonated=true;
        var l=(ServerLevel)level();
        var type=l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
            .getHolderOrThrow(com.atsuishio.superbwarfare.init.ModDamageTypes.PROJECTILE_EXPLOSION);
        var damage=new net.minecraft.world.damagesource.DamageSource(type,this,getOwner());
        new CustomExplosion.Builder(this).directSource(this).source(this).attacker(getOwner()).position(at)
            .damageSource(damage).damage(Adv01Config.DAMAGE.get().floatValue()).radius(3.5f).keepBlock().fireTime(0).explode();
        discard();
    }
    @Override public boolean forceLoadChunk(){return false;}
    @Override public void remove(RemovalReason reason){if(level() instanceof ServerLevel l){var set=ACTIVE.get(l);if(set!=null){set.remove(getUUID());if(set.isEmpty())ACTIVE.remove(l);}}super.remove(reason);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(principal!=null)n.putUUID("AdvPrincipal",principal);if(sourceVehicle!=null)n.putUUID("AdvSource",sourceVehicle);n.putString("AdvPrincipalName",principalName);n.putBoolean("AdvDetonated",detonated);n.putBoolean("AdvAutomatic",automatic);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);principal=n.hasUUID("AdvPrincipal")?n.getUUID("AdvPrincipal"):null;sourceVehicle=n.hasUUID("AdvSource")?n.getUUID("AdvSource"):null;principalName=n.getString("AdvPrincipalName");detonated=n.getBoolean("AdvDetonated");automatic=n.getBoolean("AdvAutomatic");sourceChecked=principal!=null;}
}
