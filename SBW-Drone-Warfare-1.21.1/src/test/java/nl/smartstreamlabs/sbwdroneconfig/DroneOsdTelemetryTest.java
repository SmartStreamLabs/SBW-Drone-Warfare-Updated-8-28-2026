package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import java.util.*;
public class DroneOsdTelemetryTest {
    private DroneOsdTelemetry value(List<Integer> markers){return new DroneOsdTelemetry(UUID.fromString("12345678-1234-4321-abcd-123456789000"),123,"minecraft:overworld","AUTO / COORD","PAYLOAD EMPTY","RF",123.5,64,-42.5,350,12000,52.5f,true,false,false,650,-1,Float.NaN,markers);}
    @Test void serverSnapshotRoundTripsIncludingUnknownAndExplicitOrigin(){var b=new FriendlyByteBuf(Unpooled.buffer());try{var before=value(List.of(1,3,5));DroneOsdTelemetry.encode(before,b);var after=DroneOsdTelemetry.decode(b);assertEquals(before,after);assertEquals(0,b.readableBytes());assertEquals(123.5,after.originX());assertTrue(Float.isNaN(after.signal()));}finally{b.release();}}
    @Test void markerListIsBoundedAndImmutable(){var list=new ArrayList<Integer>();list.add(1);var v=value(list);list.add(2);assertEquals(List.of(1),v.markers());assertThrows(UnsupportedOperationException.class,()->v.markers().add(4));assertThrows(IllegalArgumentException.class,()->value(Collections.nCopies(17,1)));}
}
