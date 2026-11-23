package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class MissionJournalTest {
 @TempDir Path dir;
 static byte[] data(String s){return s.getBytes(java.nio.charset.StandardCharsets.UTF_8);}
 static void await(CountDownLatch latch)throws IOException{try{if(!latch.await(10,TimeUnit.SECONDS))throw new IOException("fixture timeout");}catch(InterruptedException e){throw new IOException(e);}}
 @Test void unchangedSnapshotWritesOnce()throws Exception{
  AtomicInteger writes=new AtomicInteger();try(var j=new MissionJournal(b->writes.incrementAndGet())){for(int i=0;i<1000;i++)j.saveAndWait(data("same"));}assertEquals(1,writes.get());
 }
 @Test void asyncDoesNotWaitForBlockedDiskAndCoalesces()throws Exception{
  var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var writes=new CopyOnWriteArrayList<String>();
  var j=new MissionJournal(b->{writes.add(new String(b));if(writes.size()==1){entered.countDown();await(release);}});
  try{j.saveAsync(data("first"));assertTrue(entered.await(2,TimeUnit.SECONDS));
   var producer=CompletableFuture.runAsync(()->{try{for(int i=0;i<1000;i++)j.saveAsync(data("v"+i));}catch(IOException e){throw new CompletionException(e);}});
   producer.get(2,TimeUnit.SECONDS);assertEquals(List.of("first"),writes);
  }finally{release.countDown();j.close();}
  assertEquals(List.of("first","v999"),writes);
 }
 @Test void criticalCommitWaitsForDurability()throws Exception{
  var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var j=new MissionJournal(b->{entered.countDown();await(release);});
  var commit=CompletableFuture.runAsync(()->{try{j.saveAndWait(data("spawned"));}catch(IOException e){throw new CompletionException(e);}});
  try{assertTrue(entered.await(2,TimeUnit.SECONDS));assertFalse(commit.isDone());release.countDown();commit.get(2,TimeUnit.SECONDS);}finally{release.countDown();j.close();}
 }
 @Test void queuedPeriodicCannotOverwriteNewerTombstone()throws Exception{
  var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var written=new AtomicReference<String>();var first=new AtomicBoolean(true);
  var j=new MissionJournal(b->{if(first.getAndSet(false)){entered.countDown();await(release);}written.set(new String(b));});
  try{j.saveAsync(data("in flight"));assertTrue(entered.await(2,TimeUnit.SECONDS));j.saveAsync(data("older position"));
   var done=CompletableFuture.runAsync(()->{try{j.saveAndWait(data("Reached"));}catch(IOException e){throw new CompletionException(e);}});
   release.countDown();done.get(2,TimeUnit.SECONDS);
  }finally{release.countDown();j.close();}assertEquals("Reached",written.get());
 }
 @Test void callerMutationCannotChangePendingSnapshot()throws Exception{
  var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var written=new CopyOnWriteArrayList<String>();var j=new MissionJournal(b->{if(written.isEmpty()){entered.countDown();await(release);}written.add(new String(b));});
  try{j.saveAsync(data("first"));assertTrue(entered.await(2,TimeUnit.SECONDS));byte[] bytes=data("safe");j.saveAsync(bytes);bytes[0]='X';}finally{release.countDown();j.close();}assertEquals("safe",written.get(written.size()-1));
 }
 @Test void failureIsLatchedAndCriticalFailsClosed()throws Exception{
  AtomicInteger attempts=new AtomicInteger();try(var j=new MissionJournal(b->{attempts.incrementAndGet();throw new IOException("disk full");})){
   assertThrows(IOException.class,()->j.saveAndWait(data("reserve")));assertNotNull(j.failure());assertThrows(IOException.class,()->j.saveAsync(data("retry")));assertEquals(1,attempts.get());
  }
 }
 @Test void closeFlushesLatestAndRejectsMoreWork()throws Exception{
  AtomicReference<String> last=new AtomicReference<>();var j=new MissionJournal(b->last.set(new String(b)));j.saveAsync(data("last"));j.close();assertEquals("last",last.get());assertThrows(IOException.class,()->j.saveAsync(data("late")));
 }
 @Test void actualAtomicFileCanBeReadAfterReopening()throws Exception{
  Path path=dir.resolve("data/sbwdrone_missions.json");try(var j=new MissionJournal(path)){j.saveAndWait(data("[{\"state\":\"Reached\"}]"));}
  assertEquals("[{\"state\":\"Reached\"}]",Files.readString(path));assertFalse(Files.exists(path.resolveSibling(path.getFileName()+".tmp")));
  try(var j=new MissionJournal(path)){j.saveAndWait(data("[]"));}assertEquals("[]",Files.readString(path));
 }
}
