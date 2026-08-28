package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record LucasFuelSyncMessage(UUID droneId, int currentFuel, int maxFuel, boolean emptyFuel, boolean lowFuel) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LucasFuelSyncMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "lucas_fuel_sync"));

    public static void encode(LucasFuelSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeVarInt(message.currentFuel);
        buffer.writeVarInt(message.maxFuel);
        buffer.writeBoolean(message.emptyFuel);
        buffer.writeBoolean(message.lowFuel);
    }

    public static LucasFuelSyncMessage decode(FriendlyByteBuf buffer) {
        return new LucasFuelSyncMessage(
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    public static void handle(LucasFuelSyncMessage message, IPayloadContext context) {
        LucasFuelClient.handleSync(message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
