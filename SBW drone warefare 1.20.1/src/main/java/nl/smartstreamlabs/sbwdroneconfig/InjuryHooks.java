package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
public final class InjuryHooks {
    public static boolean downed(Entity e) {
        if (!(e instanceof Player p) || !p.isAlive() || p.isSpectator() || p.isCreative()) return false;
        if (p.level().isClientSide) { var v=InjuryClientState.get(p.getUUID()); return v!=null&&v.downed(); }
        return p instanceof net.minecraft.server.level.ServerPlayer s && InjuryServer.downed(s);
    }
    public static boolean prone(Entity e) { return downed(e) && !e.isPassenger(); }
    private InjuryHooks() {}
}
