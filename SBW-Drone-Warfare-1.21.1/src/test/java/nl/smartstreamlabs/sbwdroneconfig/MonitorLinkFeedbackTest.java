package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MonitorLinkFeedbackTest {
 private static final UUID A=UUID.fromString("00000000-0000-0000-0000-000000000001"),B=UUID.fromString("00000000-0000-0000-0000-000000000002");
 @Test void missingAndFailedLinksAreSilent(){assertNull(MonitorLinkFeedback.identity(new CompoundTag()));assertFalse(MonitorLinkFeedback.changed(null,null));assertFalse(MonitorLinkFeedback.changed("entity:"+A,null));}
 @Test void confirmedChangeOnly(){assertTrue(MonitorLinkFeedback.changed(null,"entity:"+A));assertTrue(MonitorLinkFeedback.changed("entity:"+A,"entity:"+B));assertFalse(MonitorLinkFeedback.changed("entity:"+A,"entity:"+A));}
 @Test void nativeAndGroundRepresentSameEntity(){var n=new CompoundTag();n.putUUID("VatagUUID",A);String before=MonitorLinkFeedback.identity(n);n.putBoolean(SbwCompat.TAG_LINKED,true);n.putString(SbwCompat.TAG_LINKED_DRONE,A.toString());assertEquals(before,MonitorLinkFeedback.identity(n));}
 @Test void usagePositionAndCameraDoNotChangeIdentity(){var n=new CompoundTag();n.putBoolean(SbwCompat.TAG_LINKED,true);n.putString(SbwCompat.TAG_LINKED_DRONE,A.toString());String before=MonitorLinkFeedback.identity(n);n.putBoolean(SbwCompat.TAG_USING,true);n.putDouble("PosX",900);n.putUUID("PadPilotSession",B);assertEquals(before,MonitorLinkFeedback.identity(n));}
 @Test void platformTracksSpecificLoadAndDimension(){var n=new CompoundTag();n.putUUID("PadLoad",A);n.putLong("PadPosition",42);n.putString("PadDimension","minecraft:overworld");String before=MonitorLinkFeedback.identity(n);n.putUUID("PadLoad",B);assertTrue(MonitorLinkFeedback.changed(before,MonitorLinkFeedback.identity(n)));n.putUUID("PadLoad",A);assertEquals(before,MonitorLinkFeedback.identity(n));n.putString("PadDimension","minecraft:the_nether");assertTrue(MonitorLinkFeedback.changed(before,MonitorLinkFeedback.identity(n)));}
 @Test void navalTracksSpecificSlotAndLoad(){var n=new CompoundTag();n.putUUID("NdcCarrier",A);n.putUUID("NdcLoad",B);n.putInt("NdcSlot",0);String before=MonitorLinkFeedback.identity(n);n.putInt("NdcSlot",1);assertTrue(MonitorLinkFeedback.changed(before,MonitorLinkFeedback.identity(n)));}
 @Test void malformedNativeLinkCannotConfirm(){var n=new CompoundTag();n.putBoolean(SbwCompat.TAG_LINKED,true);n.putString(SbwCompat.TAG_LINKED_DRONE,"none");assertNull(MonitorLinkFeedback.identity(n));}
}
