package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record FiberCableCutEffectMessage(
        int droneEntityId,
        UUID sessionId,
        double x,
        double y,
        double z
) {
    public static void encode(FiberCableCutEffectMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.droneEntityId);
        buffer.writeUUID(message.sessionId);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }

    public static FiberCableCutEffectMessage decode(FriendlyByteBuf buffer) {
        return new FiberCableCutEffectMessage(
                buffer.readVarInt(),
                buffer.readUUID(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public static void handle(FiberCableCutEffectMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DroneFiberOpticClient.handleCutEffect(message);
        contextSupplier.get().setPacketHandled(true);
    }
}
