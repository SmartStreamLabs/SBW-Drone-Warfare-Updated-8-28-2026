package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record VehicleAntiDroneNetStateMessage(int entityId, boolean active,net.minecraft.nbt.CompoundTag panels) {
    public VehicleAntiDroneNetStateMessage(int entityId,boolean active){this(entityId,active,new net.minecraft.nbt.CompoundTag());}
    public static void encode(VehicleAntiDroneNetStateMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeBoolean(message.active);
        buffer.writeNbt(message.panels);
    }

    public static VehicleAntiDroneNetStateMessage decode(FriendlyByteBuf buffer) {
        return new VehicleAntiDroneNetStateMessage(buffer.readVarInt(), buffer.readBoolean(), java.util.Objects.requireNonNullElseGet(buffer.readNbt(),net.minecraft.nbt.CompoundTag::new));
    }

    public static void handle(VehicleAntiDroneNetStateMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> VehicleAntiDroneNetClient.handleVehicleNetState(message)
        ));
        context.setPacketHandled(true);
    }
}
