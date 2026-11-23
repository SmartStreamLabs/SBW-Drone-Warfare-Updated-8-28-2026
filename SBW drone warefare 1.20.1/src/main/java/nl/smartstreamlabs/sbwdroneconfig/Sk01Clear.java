package nl.smartstreamlabs.sbwdroneconfig;

import java.util.UUID;import net.minecraft.network.FriendlyByteBuf;
/** Only the requested ID crosses the wire; aim, distance, tool and claims are server verified. */
public record Sk01Clear(UUID id) {
 public static void encode(Sk01Clear p,FriendlyByteBuf b){b.writeUUID(p.id);}
 public static Sk01Clear decode(FriendlyByteBuf b){return new Sk01Clear(b.readUUID());}
 public static void handle(Sk01Clear p,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>s){var c=s.get();c.enqueueWork(()->{if(c.getSender()!=null)Sk01Server.clear(c.getSender(),p.id);});c.setPacketHandled(true);}
}
