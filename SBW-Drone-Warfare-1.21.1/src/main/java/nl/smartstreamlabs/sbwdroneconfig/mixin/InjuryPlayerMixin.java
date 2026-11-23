package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Player.class,remap=false)
public abstract class InjuryPlayerMixin {
    @Redirect(method="actuallyHurt",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;setHealth(F)V"))
    private void injury$health(Player p,float health,DamageSource source,float originalAmount) {
        if(p instanceof net.minecraft.server.level.ServerPlayer s)InjuryServer.commit(s,health,source);else p.setHealth(health);
    }
    @Inject(method="updatePlayerPose",at=@At("HEAD"),cancellable=true)
    private void injury$pose(CallbackInfo c) {
        var p=(Player)(Object)this;
        if(InjuryHooks.prone(p)){p.setPose(Pose.SWIMMING);c.cancel();}
    }
}
