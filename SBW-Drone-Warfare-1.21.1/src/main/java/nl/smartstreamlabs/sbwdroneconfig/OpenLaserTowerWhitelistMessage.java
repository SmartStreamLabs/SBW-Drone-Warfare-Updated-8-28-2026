package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record OpenLaserTowerWhitelistMessage(int entityId, List<String> whitelistedGamertags) {
    public static void encode(OpenLaserTowerWhitelistMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeVarInt(message.whitelistedGamertags.size());
        for (String entry : message.whitelistedGamertags) {
            buffer.writeUtf(entry, 64);
        }
    }

    public static OpenLaserTowerWhitelistMessage decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        int size = Math.min(buffer.readVarInt(), 64);
        List<String> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(buffer.readUtf(64));
        }
        return new OpenLaserTowerWhitelistMessage(entityId, entries);
    }

    public static void handle(OpenLaserTowerWhitelistMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                LaserTowerWhitelistScreen.open(message.entityId(), message.whitelistedGamertags())));
        context.setPacketHandled(true);
    }
}
