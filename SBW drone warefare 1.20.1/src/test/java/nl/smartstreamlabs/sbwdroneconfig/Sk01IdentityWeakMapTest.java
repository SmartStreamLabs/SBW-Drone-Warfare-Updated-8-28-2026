package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Sk01IdentityWeakMapTest {
 private static final class EntityIdentity {
  int id;
  EntityIdentity(int id){this.id=id;}
  @Override public int hashCode(){return id;}
  @Override public boolean equals(Object other){return other instanceof EntityIdentity e&&e.id==id;}
 }
 @Test void respawnReusingEntityIdCannotRetractOriginalDeath(){
  var original=new EntityIdentity(7);var respawn=new EntityIdentity(7);assertEquals(original,respawn);
  var tracked=new Sk01IdentityWeakMap<EntityIdentity,String>();tracked.put(original,"original death");
  assertNull(tracked.remove(respawn));assertEquals("original death",tracked.get(original));
  tracked.put(respawn,"new life");assertEquals(2,tracked.size());
  assertEquals("new life",tracked.remove(respawn));assertEquals("original death",tracked.get(original));
  assertEquals("original death",tracked.remove(original));assertEquals(0,tracked.size());
 }
 @Test void changingEntityIdCannotLoseSameInstanceRecovery(){
  var entity=new EntityIdentity(7);var tracked=new Sk01IdentityWeakMap<EntityIdentity,String>();
  tracked.put(entity,"provisional");entity.id=12;assertTrue(tracked.containsKey(entity));
  assertEquals("provisional",tracked.remove(entity));assertEquals(0,tracked.size());
  tracked.put(entity,"other dimension");tracked.removeIf((key,value)->key==entity);assertEquals(0,tracked.size());
 }
}
