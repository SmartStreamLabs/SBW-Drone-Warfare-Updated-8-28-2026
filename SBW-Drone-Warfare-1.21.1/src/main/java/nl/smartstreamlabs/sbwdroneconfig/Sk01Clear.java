package nl.smartstreamlabs.sbwdroneconfig;

import java.util.UUID;import net.minecraft.network.FriendlyByteBuf;
/** Only the requested ID crosses the wire; aim, distance, tool and claims are server verified. */
public record Sk01Clear(UUID id) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<Sk01Clear> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","sk01clear"));
 @Override public Type<Sk01Clear>type(){return TYPE;}
 public static void encode(Sk01Clear p,FriendlyByteBuf b){b.writeUUID(p.id);}
 public static Sk01Clear decode(FriendlyByteBuf b){return new Sk01Clear(b.readUUID());}
 public static void handle(Sk01Clear p,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->{if(c.player()instanceof net.minecraft.server.level.ServerPlayer player)Sk01Server.clear(player,p.id());});}
}
