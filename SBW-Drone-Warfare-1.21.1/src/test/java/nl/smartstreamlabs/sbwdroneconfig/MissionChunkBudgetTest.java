package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MissionChunkBudgetTest {
 static class World implements MissionChunkBudget.Backend<Integer>{Set<Integer> tickets=new HashSet<>(),loaded=new HashSet<>();int adds,removes;public void add(Integer k){assertTrue(tickets.add(k));adds++;}public void remove(Integer k){assertTrue(tickets.remove(k));removes++;}public boolean ready(Integer k){return loaded.contains(k);}}
 @Test void overlapsSurviveFirstOwnerEnding(){var w=new World();var p=new MissionChunkBudget<Integer>(w);var a=UUID.randomUUID();var b=UUID.randomUUID();p.follow(a,Set.of(1,2));p.follow(b,Set.of(2,3));p.tick(9,9,9);assertEquals(3,w.adds);p.release(a);assertEquals(Set.of(2,3),w.tickets);p.release(b);assertTrue(w.tickets.isEmpty());assertEquals(3,w.removes);}
 @Test void startsPendingAndGlobalCapAreIndependent(){var w=new World();var p=new MissionChunkBudget<Integer>(w);p.follow(UUID.randomUUID(),Set.of(1,2,3,4,5));p.tick(2,3,4);assertEquals(2,p.count());p.tick(2,3,4);assertEquals(3,p.count());w.loaded.addAll(w.tickets);p.tick(2,3,4);assertEquals(4,p.count());}
 @Test void oneOwnerCannotStarveAnother(){var w=new World();var p=new MissionChunkBudget<Integer>(w);p.follow(UUID.randomUUID(),Set.of(1,2,3));p.follow(UUID.randomUUID(),Set.of(9));p.tick(1,9,9);p.tick(1,9,9);assertTrue(w.tickets.contains(9));}
 @Test void movingAreaReleasesOldBeforeAddingNew(){var w=new World();var p=new MissionChunkBudget<Integer>(w);var id=UUID.randomUUID();p.follow(id,Set.of(1,2));p.tick(9,9,2);p.follow(id,Set.of(2,3));assertEquals(Set.of(2),w.tickets);p.tick(9,9,2);assertEquals(Set.of(2,3),w.tickets);p.clear();assertEquals(0,p.count());assertEquals(0,p.owners());}
 @Test void readyRequiresEveryChunkAndNeverCreatesTickets(){var w=new World();var p=new MissionChunkBudget<Integer>(w);var id=UUID.randomUUID();p.follow(id,Set.of(1,2));assertFalse(p.ready(id));p.tick(9,9,9);w.loaded.add(1);assertFalse(p.ready(id));w.loaded.add(2);assertTrue(p.ready(id));assertEquals(2,w.adds);}
 @Test void rejectsOversizedRoutes(){var w=new World();var p=new MissionChunkBudget<Integer>(w);assertThrows(IllegalArgumentException.class,()->p.follow(UUID.randomUUID(),Set.of(1,2,3,4,5,6,7,8,9,10)));assertEquals(0,p.count());}
}
