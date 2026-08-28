package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import nl.smartstreamlabs.sbwdroneconfig.SbwCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.atsuishio.superbwarfare.item.Drone")
public abstract class DroneItemOwnerMixin {
    @Inject(method = "spawnDeployedEntity", at = @At("RETURN"), remap = false, require = 0)
    private void sbwdroneconfig$assignOwnerOnPlacement(Level level, Player player, CallbackInfoReturnable<Entity> cir) {
        Entity drone = cir.getReturnValue();
        if (drone != null && player != null) {
            SbwCompat.assignDroneOwner(drone, player);
        }
    }
}
