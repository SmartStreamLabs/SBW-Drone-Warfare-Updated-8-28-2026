package nl.smartstreamlabs.sbwdroneconfig;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberRouteMessageTest {
    private static final UUID DRONE_ID = UUID.fromString("7af194b1-3525-45ea-8e2d-1b08e1b0a0d1");
    private static final UUID SESSION_ID = UUID.fromString("78dd42cd-106e-4504-b58f-c1dba1b0a9ed");

    @Test
    void snapshotDecodeConsumesOversizedPayloadButRetainsOnlyHardLimit() {
        List<FiberCableRouteState.RoutePoint> points = new ArrayList<>();
        for (int index = 0; index < 2050; index++) {
            points.add(point(index, 64.0D, index));
        }

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        FiberRouteSnapshotMessage.encode(new FiberRouteSnapshotMessage(DRONE_ID, 42, SESSION_ID, 7, points), buffer);

        FiberRouteSnapshotMessage decoded = FiberRouteSnapshotMessage.decode(buffer);

        assertEquals(2048, decoded.points().size());
        assertEquals(2047.0D, decoded.points().get(decoded.points().size() - 1).x(), 1.0E-6D);
        assertEquals(0, buffer.readableBytes(), "Decode should consume the whole encoded payload even after the retain cap.");
    }

    @Test
    void appendDecodeConsumesOversizedPayloadButRetainsOnlyHardLimit() {
        List<FiberCableRouteState.RoutePoint> points = new ArrayList<>();
        for (int index = 0; index < 130; index++) {
            points.add(point(index, 64.0D, index));
        }

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        FiberRouteAppendMessage.encode(new FiberRouteAppendMessage(DRONE_ID, 42, SESSION_ID, 6, 7, points), buffer);

        FiberRouteAppendMessage decoded = FiberRouteAppendMessage.decode(buffer);

        assertEquals(128, decoded.points().size());
        assertEquals(127.0D, decoded.points().get(decoded.points().size() - 1).x(), 1.0E-6D);
        assertEquals(0, buffer.readableBytes(), "Decode should consume the whole encoded payload even after the retain cap.");
    }

    @Test
    void routeMessageDecodersDropMalformedPoints() {
        List<FiberCableRouteState.RoutePoint> points = List.of(
                point(0.0D, 64.0D, 0.0D),
                point(Double.NaN, 64.0D, 1.0D),
                point(2.0D, Double.POSITIVE_INFINITY, 2.0D)
        );

        FriendlyByteBuf snapshotBuffer = new FriendlyByteBuf(Unpooled.buffer());
        FiberRouteSnapshotMessage.encode(new FiberRouteSnapshotMessage(DRONE_ID, 42, SESSION_ID, 1, points), snapshotBuffer);

        FriendlyByteBuf appendBuffer = new FriendlyByteBuf(Unpooled.buffer());
        FiberRouteAppendMessage.encode(new FiberRouteAppendMessage(DRONE_ID, 42, SESSION_ID, 1, 2, points), appendBuffer);

        FiberRouteSnapshotMessage decodedSnapshot = FiberRouteSnapshotMessage.decode(snapshotBuffer);
        FiberRouteAppendMessage decodedAppend = FiberRouteAppendMessage.decode(appendBuffer);

        assertEquals(1, decodedSnapshot.points().size());
        assertEquals(1, decodedAppend.points().size());
        assertTrue(decodedSnapshot.points().get(0).isFinite());
        assertTrue(decodedAppend.points().get(0).isFinite());
    }

    private static FiberCableRouteState.RoutePoint point(double x, double y, double z) {
        return new FiberCableRouteState.RoutePoint(x, y, z);
    }
}
