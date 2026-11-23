package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.level.Explosion;
import nl.smartstreamlabs.sbwdroneconfig.ExplosionVfxClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientPacketListener.class, remap = false)
public abstract class DroneExplosionPacketVfxMixin {
    // Retain finalizeExplosion itself (block updates/sound) and all subsequent knockback handling.
    @Redirect(method = "handleExplosion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Explosion;finalizeExplosion(Z)V"), require = 1)
    private void sbwdroneconfig$onlyReplaceVisuals(Explosion explosion, boolean particles, ClientboundExplodePacket packet) {
        boolean replace = ExplosionVfxClient.consumeMarker(packet.getX(), packet.getY(), packet.getZ());
        nl.smartstreamlabs.sbwdroneconfig.ExplosionAudioGate.run(replace,()->explosion.finalizeExplosion(particles && !replace));
    }
}
