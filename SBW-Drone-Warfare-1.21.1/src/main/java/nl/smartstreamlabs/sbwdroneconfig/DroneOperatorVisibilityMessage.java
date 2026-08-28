package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record DroneOperatorVisibilityMessage(UUID playerId, boolean hidden) {
    public static void encode(DroneOperatorVisibilityMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.playerId);
        buffer.writeBoolean(message.hidden);
    }

    public static DroneOperatorVisibilityMessage decode(FriendlyByteBuf buffer) {
        return new DroneOperatorVisibilityMessage(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(DroneOperatorVisibilityMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DroneThermalVisionClient.handleDroneOperatorVisibility(message)));
        context.setPacketHandled(true);
    }
}
