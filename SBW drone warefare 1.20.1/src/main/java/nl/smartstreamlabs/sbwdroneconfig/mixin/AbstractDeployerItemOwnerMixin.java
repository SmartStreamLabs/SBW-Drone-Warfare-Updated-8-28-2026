package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import nl.smartstreamlabs.sbwdroneconfig.SbwCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.atsuishio.superbwarfare.item.VehicleDeployerBlockItem")
public abstract class AbstractDeployerItemOwnerMixin {
    @Redirect(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            ),
            remap = false,
            require = 0
    )
    private boolean sbwdroneconfig$assignOwnerBeforeUseOnSpawn(ServerLevel level, Entity entity, UseOnContext context) {
        if (context != null && context.getPlayer() != null) {
            SbwCompat.assignDroneOwner(entity, context.getPlayer());
        }
        return level.addFreshEntity(entity);
    }

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            ),
            remap = false,
            require = 0
    )
    private boolean sbwdroneconfig$assignOwnerBeforeUseSpawn(ServerLevel level, Entity entity, Level originalLevel, Player player, InteractionHand hand) {
        if (player != null) {
            SbwCompat.assignDroneOwner(entity, player);
        }
        return level.addFreshEntity(entity);
    }
}
