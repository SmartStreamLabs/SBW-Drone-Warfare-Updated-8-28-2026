package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DroneChunkLoadingTest {
    static class World implements DroneChunkLeases.Backend {
        Set<Long> tickets=new HashSet<>(),ready=new HashSet<>(); int adds,removes;
        public void add(long p) { assertTrue(tickets.add(p),"duplicate request"); adds++; }
        public void remove(long p) { assertTrue(tickets.remove(p),"removed another owner's ticket"); removes++; }
        public boolean ready(long p) { return ready.contains(p); }
        void finish() { ready.addAll(tickets); }
    }
    @Test void negativeCoordinatesUseFloor() {
        assertEquals(-1,DroneChunkGeometry.chunk(-0.01));
        assertEquals(-1,DroneChunkGeometry.chunk(-16));
        assertEquals(-2,DroneChunkGeometry.chunk(-16.01));
        assertEquals(0,DroneChunkGeometry.chunk(15.99));
        long packed=DroneChunkGeometry.pack(-2,-3);
        assertEquals(-2,(int)packed); assertEquals(-3,(int)(packed>>>32));
        assertEquals(81,DroneChunkGeometry.square(-2,-3,4).size());
    }
    @Test void requestsAndUnfinishedWorkAreBounded() {
        var leases=new DroneChunkLeases<String>(12,4); var world=new World();
        var area=DroneChunkGeometry.square(0,0,4);
        for(int tick=0;tick<30;tick++) {
            leases.touch("a",area,tick); int before=world.adds;
            leases.tick(tick,2,3,world);
            assertTrue(world.adds-before<=2); assertTrue(leases.pending(world)<=3); assertTrue(leases.tickets()<=12);
            if(tick%3==0) world.finish();
        }
        assertEquals(12,leases.tickets());
    }
    @Test void ownersShareTicketsAndOneSessionCannotReleaseAnother() {
        var leases=new DroneChunkLeases<String>(16,4); var world=new World();
        leases.touch("a",Set.of(1L,2L),0); leases.touch("b",Set.of(2L,3L),0);
        leases.tick(0,16,16,world); world.finish(); assertEquals(3,world.adds);
        leases.release("a");
        for(int tick=1;tick<=30;tick++) { leases.touch("b",Set.of(2L,3L),tick); leases.tick(tick,16,16,world); }
        assertEquals(Set.of(2L,3L),world.tickets);
        leases.release("b"); leases.tick(51,16,16,world); assertEquals(0,leases.tickets());
    }
    @Test void switchingCameraCancelsUnstartedWorkAndPendingOldRequests() {
        var leases=new DroneChunkLeases<String>(16,4); var world=new World();
        leases.touch("camera",DroneChunkGeometry.square(0,0,1),0); leases.tick(0,1,1,world);
        leases.touch("camera",Set.of(999L),1); leases.tick(1,1,1,world);
        assertEquals(Set.of(999L),world.tickets); assertEquals(2,world.adds);
    }
    @Test void leaseExpiryCleansAbandonedSessionsAndShutdownClearsState() {
        var leases=new DroneChunkLeases<String>(16,4); var world=new World();
        leases.touch("lost-connection",Set.of(1L),0); leases.tick(0,4,8,world); world.finish();
        for(int t=1;t<=32;t++) leases.tick(t,4,8,world);
        assertEquals(0,leases.owners()); assertEquals(0,leases.tickets());
        leases.touch("reconnect",Set.of(2L),33); leases.tick(33,4,8,world);
        leases.clear(world); assertTrue(world.tickets.isEmpty()); assertEquals(0,leases.owners());
    }
    @Test void borderOscillationRetainsReadyChunksWithoutRepeatedRequests() {
        var leases=new DroneChunkLeases<String>(16,4); var world=new World();
        for(int t=0;t<20;t++) {
            leases.touch("drone",Set.of((long)(t%2)),t); leases.tick(t,4,8,world); world.finish();
        }
        assertEquals(2,world.adds); assertEquals(0,world.removes);
    }
    @Test void excessiveOwnerAndAreaRequestsAreRejected() {
        var leases=new DroneChunkLeases<String>(16,1); var world=new World();
        leases.touch("authorized",Set.of(1L),0); leases.touch("excess",Set.of(2L),0);
        leases.tick(0,16,16,world); assertEquals(Set.of(1L),world.tickets);
        Set<Long> huge=new HashSet<>(); for(long n=0;n<82;n++) huge.add(n);
        assertThrows(IllegalArgumentException.class,()->leases.touch("authorized",huge,0));
    }
}
