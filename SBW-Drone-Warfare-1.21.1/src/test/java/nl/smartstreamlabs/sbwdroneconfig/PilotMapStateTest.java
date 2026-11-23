package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import java.util.*;
class PilotMapStateTest {
 static final UUID A=UUID.fromString("00000000-0000-0000-0000-000000000001"),B=UUID.fromString("00000000-0000-0000-0000-000000000002");
 PilotMapState active(){var s=new PilotMapState();s.connection(new Object());return s;}
 @Test void defaultDoesNotHideAnyPlayer(){var s=active();assertFalse(s.hidden(A));assertFalse(s.hidden(null));}
 @Test void serverDeltaOnlyHidesSelectedUuid(){var s=active();s.apply(new PilotMapPacket(false,true,List.of(A)));assertTrue(s.hidden(A));assertFalse(s.hidden(B));}
 @Test void stopRestoresOneWithoutRevealingOtherPilots(){var s=active();s.apply(new PilotMapPacket(true,true,List.of(A,B)));s.apply(new PilotMapPacket(false,false,List.of(A)));assertFalse(s.hidden(A));assertTrue(s.hidden(B));}
 @Test void lateJoinSnapshotHasAllActivePilots(){var s=active();s.apply(new PilotMapPacket(true,true,List.of(A,B)));assertTrue(s.hidden(A)&&s.hidden(B));}
 @Test void emptySnapshotRemovesStaleHiddenPlayers(){var s=active();s.apply(new PilotMapPacket(true,true,List.of(A)));s.apply(new PilotMapPacket(true,true,List.of()));assertFalse(s.hidden(A));}
 @Test void reconnectNeverKeepsPreviousServerState(){var s=active();s.apply(new PilotMapPacket(true,true,List.of(A)));s.connection(new Object());assertFalse(s.hidden(A));}
 @Test void disconnectClearsAndRejectsLateMessages(){var s=active();s.apply(new PilotMapPacket(true,true,List.of(A)));s.connection(null);s.apply(new PilotMapPacket(false,true,List.of(B)));assertFalse(s.hidden(A)||s.hidden(B));}
 @Test void sameConnectionRetainsStateAcrossWorldObjectChanges(){var s=new PilotMapState();var connection=new Object();s.connection(connection);s.apply(new PilotMapPacket(false,true,List.of(A)));s.connection(connection);assertTrue(s.hidden(A));}
 @Test void duplicateChangesAreIdempotent(){var s=active();var hide=new PilotMapPacket(false,true,List.of(A));s.apply(hide);s.apply(hide);assertTrue(s.hidden(A));var show=new PilotMapPacket(false,false,List.of(A));s.apply(show);s.apply(show);assertFalse(s.hidden(A));}
 @Test void snapshotPacketCopiesInputAndBoundsSize(){var ids=new ArrayList<>(List.of(A));var p=new PilotMapPacket(true,true,ids);ids.clear();assertEquals(List.of(A),p.players());assertThrows(IllegalArgumentException.class,()->new PilotMapPacket(true,true,Collections.nCopies(PilotMapPacket.LIMIT+1,A)));}
}
