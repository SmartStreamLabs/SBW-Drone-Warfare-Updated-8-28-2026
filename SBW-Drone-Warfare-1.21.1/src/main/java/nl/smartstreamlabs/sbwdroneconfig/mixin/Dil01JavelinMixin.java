package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.atsuishio.superbwarfare.entity.projectile.*;import nl.smartstreamlabs.sbwdroneconfig.*;import net.minecraft.network.syncher.*;import net.minecraft.world.entity.Entity;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=JavelinMissileEntity.class,remap=false)
public abstract class Dil01JavelinMixin implements Dil01MissileAccess {
 @Unique private static final EntityDataAccessor<Boolean> dil01$MARK=SynchedEntityData.defineId(JavelinMissileEntity.class,EntityDataSerializers.BOOLEAN);
 @Inject(method="defineSynchedData",remap=false,at=@At("TAIL"))private void dil01$data(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(dil01$MARK,false);}
 @Override public boolean dil01(){return ((Entity)(Object)this).getEntityData().get(dil01$MARK);}
 @Override public void dil01(boolean enabled){((Entity)(Object)this).getEntityData().set(dil01$MARK,enabled);}
 @Inject(method="tick",remap=false,at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/projectile/MissileProjectile;tick()V",shift=At.Shift.AFTER,remap=false),cancellable=true)
 private void dil01$flight(CallbackInfo ci){if(!dil01())return;var missile=(JavelinMissileEntity)(Object)this;if(!((Entity)(Object)this).isRemoved()){missile.mediumTrail();Dil01Guidance.step(missile);}ci.cancel();}
}
