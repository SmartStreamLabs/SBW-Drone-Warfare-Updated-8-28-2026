package nl.smartstreamlabs.sbwdroneconfig;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiberCableCutMessageTest {
    private static final UUID SESSION_ID = UUID.fromString("92cb8252-e97f-45ca-a8fa-4f7a28de3d7d");

    @Test
    void cutRequestRoundTripsCompactPayload() {
        FiberCableCutRequestMessage original = new FiberCableCutRequestMessage(
                42,
                SESSION_ID,
                17,
                3,
                new Vec3(1.25D, 64.5D, -8.75D)
        );

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        FiberCableCutRequestMessage.encode(original, buffer);
        FiberCableCutRequestMessage decoded = FiberCableCutRequestMessage.decode(buffer);

        assertEquals(original.droneEntityId(), decoded.droneEntityId());
        assertEquals(original.sessionId(), decoded.sessionId());
        assertEquals(original.claimedRevision(), decoded.claimedRevision());
        assertEquals(original.claimedSegmentIndex(), decoded.claimedSegmentIndex());
        assertEquals(original.hitX(), decoded.hitX(), 1.0E-6D);
        assertEquals(original.hitY(), decoded.hitY(), 1.0E-6D);
        assertEquals(original.hitZ(), decoded.hitZ(), 1.0E-6D);
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void cutEffectRoundTripsCutPosition() {
        FiberCableCutEffectMessage original = new FiberCableCutEffectMessage(42, SESSION_ID, 1.0D, 2.0D, 3.0D);

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        FiberCableCutEffectMessage.encode(original, buffer);
        FiberCableCutEffectMessage decoded = FiberCableCutEffectMessage.decode(buffer);

        assertEquals(original.droneEntityId(), decoded.droneEntityId());
        assertEquals(original.sessionId(), decoded.sessionId());
        assertEquals(original.x(), decoded.x(), 1.0E-6D);
        assertEquals(original.y(), decoded.y(), 1.0E-6D);
        assertEquals(original.z(), decoded.z(), 1.0E-6D);
        assertEquals(0, buffer.readableBytes());
    }
}
