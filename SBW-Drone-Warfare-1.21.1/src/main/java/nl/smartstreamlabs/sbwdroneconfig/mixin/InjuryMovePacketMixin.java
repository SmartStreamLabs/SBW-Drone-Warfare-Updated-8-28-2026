package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ServerGamePacketListenerImpl.class,remap=false)
public abstract class InjuryMovePacketMixin {
    @Shadow public ServerPlayer player;
    @Shadow private Vec3 awaitingPositionFromClient;
    @Inject(method="handleMovePlayer",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;isPassenger()Z"),cancellable=true)
    private void injury$validate(ServerboundMovePlayerPacket packet,CallbackInfo c) {
        if(awaitingPositionFromClient==null&&InjuryServer.rejectMove(player,packet.getX(player.getX()),packet.getY(player.getY()),packet.getZ(player.getZ())))c.cancel();
    }
}
