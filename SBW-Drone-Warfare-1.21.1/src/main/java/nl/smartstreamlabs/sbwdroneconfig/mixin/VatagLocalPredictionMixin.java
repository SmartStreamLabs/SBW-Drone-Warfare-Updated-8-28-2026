package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.phys.Vec3;import net.minecraft.client.Minecraft;import nl.smartstreamlabs.sbwdroneconfig.VatagClient;
/** A remote camera unloads the body's client terrain. Suppress local movement prediction only;
 * the real server player stays vulnerable at the monitor. No body relocation or fake passenger. */
@Mixin(value=LivingEntity.class,remap=false)
public abstract class VatagLocalPredictionMixin {
 @Inject(method="travel",at=@At("HEAD"),cancellable=true,remap=false)
 private void vatag$holdClientPrediction(Vec3 input,CallbackInfo ci){if((VatagClient.holdBodyPrediction()||nl.smartstreamlabs.sbwdroneconfig.Rd01Client.holdBodyPrediction()||nl.smartstreamlabs.sbwdroneconfig.Id01Client.holdBodyPrediction())&&(Object)this==Minecraft.getInstance().player){((LivingEntity)(Object)this).setDeltaMovement(Vec3.ZERO);ci.cancel();}}
}
