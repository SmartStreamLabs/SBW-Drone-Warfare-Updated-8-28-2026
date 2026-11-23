package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
public record Id01State(int entity,UUID token,boolean active,String target,float distance,List<Id01Radar.Contact> contacts) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<Id01State> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"id01state"));
    @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
    public Id01State {if(contacts.size()>16)throw new IllegalArgumentException("radar limit");target=clip(target);contacts=contacts.stream().map(c->new Id01Radar.Contact(c.id(),clip(c.name()),c.distance(),c.hostile())).toList();}
    private static String clip(String s){return s.length()>256?s.substring(0,256):s;}
    public static void encode(Id01State m,net.minecraft.network.FriendlyByteBuf b){b.writeInt(m.entity);b.writeUUID(m.token);b.writeBoolean(m.active);b.writeUtf(m.target,256);b.writeFloat(m.distance);b.writeVarInt(m.contacts.size());for(var t:m.contacts){b.writeUUID(t.id());b.writeUtf(t.name(),256);b.writeFloat(t.distance());b.writeBoolean(t.hostile());}}
    public static Id01State decode(net.minecraft.network.FriendlyByteBuf b){int entity=b.readInt();UUID token=b.readUUID();boolean active=b.readBoolean();String target=b.readUtf(256);float distance=b.readFloat();int n=b.readVarInt();if(n<0||n>16)throw new IllegalArgumentException("radar limit");List<Id01Radar.Contact> contacts=new ArrayList<>();for(int i=0;i<n;i++)contacts.add(new Id01Radar.Contact(b.readUUID(),b.readUtf(256),b.readFloat(),b.readBoolean()));return new Id01State(entity,token,active,target,distance,contacts);}
    public static void handle(Id01State m,net.neoforged.neoforge.network.handling.IPayloadContext c){Id01Client.state(m);}
}
