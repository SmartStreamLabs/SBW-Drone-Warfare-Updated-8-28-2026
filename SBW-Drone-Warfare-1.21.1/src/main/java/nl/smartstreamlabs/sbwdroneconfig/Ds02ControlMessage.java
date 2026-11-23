package nl.smartstreamlabs.sbwdroneconfig;
public record Ds02ControlMessage(net.minecraft.core.BlockPos pos,int action) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<Ds02ControlMessage> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"ds02_control"));
 @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 public static void handle(Ds02ControlMessage m,net.neoforged.neoforge.network.handling.IPayloadContext c){if(c.player() instanceof net.minecraft.server.level.ServerPlayer p)apply(m,p);}

    private static void apply(Ds02ControlMessage m,net.minecraft.server.level.ServerPlayer p){
        if(p==null || m.action<0 || m.action>2 || p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(m.pos))>64 || !p.level().hasChunkAt(m.pos))return;
        if(p.level().getBlockEntity(m.pos) instanceof Ds02BlockEntity be)be.control(p,m.action);
    }
    public static void encode(Ds02ControlMessage m,net.minecraft.network.FriendlyByteBuf b){b.writeBlockPos(m.pos);b.writeByte(m.action);}
    public static Ds02ControlMessage decode(net.minecraft.network.FriendlyByteBuf b){return new Ds02ControlMessage(b.readBlockPos(),b.readUnsignedByte());}
}
