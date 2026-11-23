package nl.smartstreamlabs.sbwdroneconfig;

public record Rd01State(int entity,java.util.UUID token,boolean active) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<Rd01State> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"rd01state"));
    @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
    public static void encode(Rd01State m,net.minecraft.network.FriendlyByteBuf b){b.writeInt(m.entity);b.writeUUID(m.token);b.writeBoolean(m.active);}
    public static Rd01State decode(net.minecraft.network.FriendlyByteBuf b){return new Rd01State(b.readInt(),b.readUUID(),b.readBoolean());}
    public static void handle(Rd01State m,net.neoforged.neoforge.network.handling.IPayloadContext c){Rd01Client.state(m);}
}
