package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record OpenSirenConfigMessage(BlockPos blockPos, List<String> blacklistedGamertags) {
    public static void encode(OpenSirenConfigMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeCollection(message.blacklistedGamertags, FriendlyByteBuf::writeUtf);
    }

    public static OpenSirenConfigMessage decode(FriendlyByteBuf buffer) {
        return new OpenSirenConfigMessage(buffer.readBlockPos(), buffer.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(OpenSirenConfigMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DroneDetectionSirenScreen.open(message)));
        context.setPacketHandled(true);
    }
}
