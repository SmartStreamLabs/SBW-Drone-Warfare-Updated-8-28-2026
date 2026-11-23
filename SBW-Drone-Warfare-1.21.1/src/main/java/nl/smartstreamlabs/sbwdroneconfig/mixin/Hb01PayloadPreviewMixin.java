package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.atsuishio.superbwarfare.entity.projectile.BasicGeoProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
/** A suspended load has its native mesh, but no firing flare or ticking engine. */
@Mixin(targets="com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer",remap=false)
public abstract class Hb01PayloadPreviewMixin {
 @Redirect(method="render",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/projectile/BasicGeoProjectileEntity;getFlareHiddenTicks()I"),require=1)
 private int hb01$inert(BasicGeoProjectileEntity projectile){return ((net.minecraft.world.entity.Entity)projectile).getPersistentData().getBoolean("sbwdroneconfigPayloadPreview")?Integer.MAX_VALUE:projectile.getFlareHiddenTicks();}
}
