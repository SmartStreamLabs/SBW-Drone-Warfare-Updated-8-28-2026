package nl.smartstreamlabs.sbwdroneconfig;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiberOpticHudSyncMessageTest {
    @Test
    void encodeAndDecodePreserveFiberHudState() {
        DroneFiberOpticSyncMessage original = new DroneFiberOpticSyncMessage(
                UUID.fromString("12345678-1234-5678-9abc-def012345678"),
                412,
                DroneLinkMode.FIBER_OPTIC,
                true,
                87.5D,
                256,
                0.42F,
                93,
                14,
                12.5D,
                64.0D,
                -3.5D
        );

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        DroneFiberOpticSyncMessage.encode(original, buffer);

        DroneFiberOpticSyncMessage decoded = DroneFiberOpticSyncMessage.decode(buffer);

        assertEquals(original.droneId(), decoded.droneId());
        assertEquals(original.droneEntityId(), decoded.droneEntityId());
        assertEquals(original.linkMode(), decoded.linkMode());
        assertEquals(original.fiberSessionActive(), decoded.fiberSessionActive());
        assertEquals(original.cableLength(), decoded.cableLength(), 1.0E-6D);
        assertEquals(original.maxCableLength(), decoded.maxCableLength());
        assertEquals(original.cableTension(), decoded.cableTension(), 1.0E-6F);
        assertEquals(original.spoolPercent(), decoded.spoolPercent());
        assertEquals(original.segmentCount(), decoded.segmentCount());
        assertEquals(original.anchorX(), decoded.anchorX(), 1.0E-6D);
        assertEquals(original.anchorY(), decoded.anchorY(), 1.0E-6D);
        assertEquals(original.anchorZ(), decoded.anchorZ(), 1.0E-6D);
    }
}
