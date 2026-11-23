package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;
public record InjuryFeedback(int elapsed,int duration,String status,boolean hit) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<InjuryFeedback> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"injuryfeedback"));
    @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
    public static void encode(InjuryFeedback m,FriendlyByteBuf b){b.writeVarInt(m.elapsed);b.writeVarInt(m.duration);b.writeUtf(m.status,64);b.writeBoolean(m.hit);}
    public static InjuryFeedback decode(FriendlyByteBuf b){int e=b.readVarInt(),d=b.readVarInt();String s=b.readUtf(64);boolean h=b.readBoolean();if(e<0||d<0||e>7200||d>7200)throw new IllegalArgumentException("Medical progress");return new InjuryFeedback(e,d,s,h);}
    public static void handle(InjuryFeedback m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->InjuryClient.feedback(m));}
}
