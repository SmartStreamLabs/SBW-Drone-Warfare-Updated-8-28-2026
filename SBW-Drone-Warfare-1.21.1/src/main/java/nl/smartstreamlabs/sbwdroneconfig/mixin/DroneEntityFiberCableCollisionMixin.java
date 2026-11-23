package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import nl.smartstreamlabs.sbwdroneconfig.FiberOpticCableSegmentEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.atsuishio.superbwarfare.entity.vehicle.DroneEntity")
public abstract class DroneEntityFiberCableCollisionMixin {
    @Inject(method = "hitEntityCrash", at = @At("HEAD"), remap = false, require = 0, cancellable = true)
    private void sbwdroneconfig$ignoreFiberCableCrashTargets(Player player, Entity target, CallbackInfo ci) {
        // Native drone crash detection uses broad entity bounds, separately from
        // vehicle OBB collisions. Honor the same short deck/departure lease here.
        if (target instanceof FiberOpticCableSegmentEntity) {
            ci.cancel();
        } else if (nl.smartstreamlabs.sbwdroneconfig.Dc01Docking.sameCarrier((Entity)(Object)this, target)) {
            ci.cancel();
        }
    }
}
