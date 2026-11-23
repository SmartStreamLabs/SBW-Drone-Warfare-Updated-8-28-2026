package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.atsuishio.superbwarfare.entity.projectile.MissileProjectile;import nl.smartstreamlabs.sbwdroneconfig.Dil01MissileAccess;import net.minecraft.nbt.CompoundTag;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=MissileProjectile.class,remap=false)
public abstract class Dil01MissileSaveMixin {
 @Inject(method="addAdditionalSaveData",remap=false,at=@At("TAIL"))private void dil01$save(CompoundTag t,CallbackInfo ci){if((Object)this instanceof Dil01MissileAccess a&&a.dil01())t.putBoolean("DIL01Guidance",true);}
 @Inject(method="readAdditionalSaveData",remap=false,at=@At("TAIL"))private void dil01$load(CompoundTag t,CallbackInfo ci){if((Object)this instanceof Dil01MissileAccess a)a.dil01(t.getBoolean("DIL01Guidance"));}
}
