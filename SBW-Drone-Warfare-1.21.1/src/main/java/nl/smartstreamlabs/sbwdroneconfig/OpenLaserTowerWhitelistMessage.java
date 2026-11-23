package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenLaserTowerWhitelistMessage(int entityId, List<String> whitelistedGamertags) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenLaserTowerWhitelistMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "open_laser_tower_whitelist"));

    public static void encode(OpenLaserTowerWhitelistMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeCollection(message.whitelistedGamertags, FriendlyByteBuf::writeUtf);
    }

    public static OpenLaserTowerWhitelistMessage decode(FriendlyByteBuf buffer) {
        return new OpenLaserTowerWhitelistMessage(buffer.readVarInt(), buffer.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(OpenLaserTowerWhitelistMessage message, IPayloadContext context) {
        LaserTowerWhitelistScreen.open(message.entityId(), message.whitelistedGamertags());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
