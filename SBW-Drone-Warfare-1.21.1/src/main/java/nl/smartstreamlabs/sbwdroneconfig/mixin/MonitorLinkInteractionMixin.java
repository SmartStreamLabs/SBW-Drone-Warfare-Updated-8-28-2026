package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import nl.smartstreamlabs.sbwdroneconfig.MonitorLinkFeedback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla validates reach, permissions and the interaction before RETURN confirms a changed link. */
@Mixin(value=ServerGamePacketListenerImpl.class,remap=false)
public abstract class MonitorLinkInteractionMixin {
    @Shadow public ServerPlayer player;
    @Unique private MonitorLinkFeedback.Before monitorLink$entity;
    @Unique private MonitorLinkFeedback.Before monitorLink$block;

    @Inject(method="handleInteract", at=@At("HEAD"))
    private void monitorLink$beforeEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (player.server.isSameThread()) monitorLink$entity = MonitorLinkFeedback.begin(player);
    }
    @Inject(method="handleInteract", at=@At("RETURN"))
    private void monitorLink$afterEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (!player.server.isSameThread()) return;
        var before = monitorLink$entity;
        monitorLink$entity = null;
        MonitorLinkFeedback.finish(player, before);
    }
    @Inject(method="handleUseItemOn", at=@At("HEAD"))
    private void monitorLink$beforeBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (player.server.isSameThread()) monitorLink$block = MonitorLinkFeedback.begin(player);
    }
    @Inject(method="handleUseItemOn", at=@At("RETURN"))
    private void monitorLink$afterBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (!player.server.isSameThread()) return;
        var before = monitorLink$block;
        monitorLink$block = null;
        MonitorLinkFeedback.finish(player, before);
    }
}
