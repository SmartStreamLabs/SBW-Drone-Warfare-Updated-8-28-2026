package nl.smartstreamlabs.sbwdroneconfig;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Compare only explicit server interaction results, never inventory ticks or launch handoffs. */
public final class MonitorLinkFeedback {
    public record Before(String main, String off) {}

    public static Before begin(ServerPlayer player) {
        return new Before(identity(player.getMainHandItem()), identity(player.getOffhandItem()));
    }

    public static String identity(ItemStack stack) {
        return SbwCompat.isMonitor(stack) ? identity(PlatformMonitor.tag(stack)) : null;
    }

    static String identity(CompoundTag tag) {
        if (tag.getBoolean(SbwCompat.TAG_LINKED)) {
            String id = tag.getString(SbwCompat.TAG_LINKED_DRONE);
            try { return "entity:" + UUID.fromString(id); }
            catch (IllegalArgumentException ignored) { return null; }
        }
        if (tag.hasUUID("NdcCarrier") && tag.hasUUID("NdcLoad") && (!tag.hasUUID("VatagUUID") || tag.getUUID("NdcCarrier").equals(tag.getUUID("VatagUUID"))))
            return "bay:" + tag.getString("NdcDimension") + ':' + tag.getUUID("NdcCarrier") + ':' + tag.getInt("NdcSlot") + ':' + tag.getUUID("NdcLoad");
        if (tag.hasUUID("VatagUUID")) return "entity:" + tag.getUUID("VatagUUID");
        if (tag.hasUUID("PadLoad"))
            return "pad:" + tag.getString("PadDimension") + ':' + tag.getLong("PadPosition") + ':' + tag.getUUID("PadLoad");
        return null;
    }

    static boolean changed(String before, String after) {
        return after != null && !Objects.equals(before, after);
    }

    public static void finish(ServerPlayer player, Before before) {
        if (before == null || player.hasDisconnected() || !player.isAlive() || player.isSpectator()) return;
        if (changed(before.main(), identity(player.getMainHandItem())) || changed(before.off(), identity(player.getOffhandItem())))
            AddonNetwork.monitorLinked(player, new MonitorLinkSoundPacket(UUID.randomUUID()));
    }

    private MonitorLinkFeedback() {}
}
