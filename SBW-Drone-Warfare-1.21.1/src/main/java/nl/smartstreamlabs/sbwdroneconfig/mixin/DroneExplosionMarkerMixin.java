package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import nl.smartstreamlabs.sbwdroneconfig.DroneExplosionVfx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Send suppression only after an attributed vanilla explosion actually finalized.
 * A canceled protection-mod explosion cannot leave a marker for an unrelated blast. */
@Mixin(value=Explosion.class,remap=false)
public abstract class DroneExplosionMarkerMixin implements nl.smartstreamlabs.sbwdroneconfig.ExplosionVisualOwner {
    @Shadow @Final private Level level;
    @Shadow @Final private Entity source;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private float radius;
    @Unique private boolean sbwdroneconfig$ownVfx;
    @Unique private boolean sbwdroneconfig$markerSent;
    @Unique private boolean sbwdroneconfig$sharedVfx;

    @Inject(method="<init>*",at=@At("TAIL"))
    private void sbwdroneconfig$attributeConstruction(CallbackInfo ci) {
        // Constructor chains must retain the claim made by their innermost constructor.
        sbwdroneconfig$ownVfx |= DroneExplosionVfx.claimExplosion(level,source,x,y,z,radius);
        if(sbwdroneconfig$ownVfx){nl.smartstreamlabs.sbwdroneconfig.Vc01Damage.mark((Explosion)(Object)this);nl.smartstreamlabs.sbwdroneconfig.RubbleEvents.mark((Explosion)(Object)this);}
        sbwdroneconfig$sharedVfx |= DroneExplosionVfx.isSbwSource(source)||(Object)this instanceof com.atsuishio.superbwarfare.tools.CustomExplosion;
    }

    @Inject(method="finalizeExplosion",at=@At("TAIL"))
    private void sbwdroneconfig$confirmPacket(boolean particles,CallbackInfo ci) { sbwdroneconfig$confirmVisuals(); }
    @Override public void sbwdroneconfig$confirmVisuals() {
        if((sbwdroneconfig$ownVfx||sbwdroneconfig$sharedVfx) && !sbwdroneconfig$markerSent && level instanceof ServerLevel server) {
            sbwdroneconfig$markerSent=true;
            DroneExplosionVfx.prepare(server,new Vec3(x,y,z));
            if((Object)this instanceof com.atsuishio.superbwarfare.tools.CustomExplosion||sbwdroneconfig$sharedVfx&&!sbwdroneconfig$ownVfx){Entity visualSource=source;Entity direct=((nl.smartstreamlabs.sbwdroneconfig.mixin.Vc01ExplosionAccess)(Object)this).vc01$source().getDirectEntity();if(DroneExplosionVfx.handGrenade(direct))visualSource=direct;if(visualSource instanceof nl.smartstreamlabs.sbwdroneconfig.Hg01Entity)DroneExplosionVfx.hg01(server,new Vec3(x,y,z));else if(DroneExplosionVfx.handGrenade(visualSource))DroneExplosionVfx.grenade(server,new Vec3(x,y,z),radius);else DroneExplosionVfx.detonate(server,new Vec3(x,y,z),radius);}
        }
    }
}
