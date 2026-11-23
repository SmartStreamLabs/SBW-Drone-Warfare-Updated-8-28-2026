package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PortableFpvDetectorOpenMessage() {
    public static void encode(PortableFpvDetectorOpenMessage message, FriendlyByteBuf buffer) {
    }

    public static PortableFpvDetectorOpenMessage decode(FriendlyByteBuf buffer) {
        return new PortableFpvDetectorOpenMessage();
    }

    public static void handle(PortableFpvDetectorOpenMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> DroneJammerClient::openDetectorScreen));
        contextSupplier.get().setPacketHandled(true);
    }
}
