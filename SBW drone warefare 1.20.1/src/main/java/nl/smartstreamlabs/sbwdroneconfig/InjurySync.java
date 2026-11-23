package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
public record InjurySync(int entity,UUID id,int stage,int bleeding,int recovery) {
    public static void encode(InjurySync m,FriendlyByteBuf b){b.writeInt(m.entity);b.writeUUID(m.id);b.writeVarInt(m.stage);b.writeVarInt(m.bleeding);b.writeVarInt(m.recovery);}
    public static InjurySync decode(FriendlyByteBuf b){int entity=b.readInt();UUID id=b.readUUID();int s=b.readVarInt(),bleed=b.readVarInt(),r=b.readVarInt();if(s<0||s>4||bleed<0||bleed>2||r<0||r>72000)throw new IllegalArgumentException("Medical state");return new InjurySync(entity,id,s,bleed,r);}
    public static void receive(InjurySync m){InjuryClientState.put(new InjuryClientState.View(m.entity,m.id,InjuryState.Stage.values()[m.stage],m.bleeding,m.recovery));}
    public static void handle(InjurySync m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->receive(m));c.setPacketHandled(true);}
}
