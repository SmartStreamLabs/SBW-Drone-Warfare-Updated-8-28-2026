package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenJammerConfigMessage(BlockPos blockPos, int range, int minRange, int maxRange, boolean longRangeTower, int energy, int maxEnergy, boolean charging, boolean operational, List<String> whitelistedGamertags) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenJammerConfigMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "open_jammer_config"));

    public OpenJammerConfigMessage(BlockPos blockPos, int range, List<String> whitelistedGamertags) {
        this(blockPos, range, JammerBlockEntity.MIN_RANGE, JammerBlockEntity.MAX_RANGE, false, 0, 0, false, false, whitelistedGamertags);
    }

    public static OpenJammerConfigMessage fromBlockEntity(BlockPos blockPos, ConfigurableJammerBlockEntity blockEntity) {
        int energy = 0;
        int maxEnergy = 0;
        boolean charging = false;
        boolean operational = false;
        if (blockEntity instanceof RadarJammerTowerBlockEntity tower) {
            energy = tower.getEnergy();
            maxEnergy = tower.getMaxEnergy();
            charging = tower.isCharging();
            operational = tower.isOperational();
        }
        return new OpenJammerConfigMessage(
                blockPos,
                blockEntity.getJammerRange(),
                blockEntity.getMinJammerRange(),
                blockEntity.getMaxJammerRange(),
                blockEntity.isLongRangeTower(),
                energy,
                maxEnergy,
                charging,
                operational,
                blockEntity.getWhitelistedGamertags()
        );
    }

    public static void encode(OpenJammerConfigMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeVarInt(message.range);
        buffer.writeVarInt(message.minRange);
        buffer.writeVarInt(message.maxRange);
        buffer.writeBoolean(message.longRangeTower);
        buffer.writeVarInt(message.energy);
        buffer.writeVarInt(message.maxEnergy);
        buffer.writeBoolean(message.charging);
        buffer.writeBoolean(message.operational);
        buffer.writeCollection(message.whitelistedGamertags, FriendlyByteBuf::writeUtf);
    }

    public static OpenJammerConfigMessage decode(FriendlyByteBuf buffer) {
        return new OpenJammerConfigMessage(
                buffer.readBlockPos(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readList(FriendlyByteBuf::readUtf)
        );
    }

    public static void handle(OpenJammerConfigMessage message, IPayloadContext context) {
        JammerScreen.open(message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
