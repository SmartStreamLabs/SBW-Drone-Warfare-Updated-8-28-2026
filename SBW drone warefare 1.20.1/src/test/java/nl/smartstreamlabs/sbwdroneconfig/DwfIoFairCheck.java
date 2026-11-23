package nl.smartstreamlabs.sbwdroneconfig;
import java.nio.file.*;import java.util.*;import java.nio.charset.StandardCharsets;
public final class DwfIoFairCheck{
 public static void main(String[]args)throws Exception{
  var directory=Files.createTempDirectory("dwf-fair-load");var limits=new DwfArchive.Limits(Long.MAX_VALUE,65536,2048,1024);var empty=new DwfBulkStore<String>(64000000,65536);var store=new DwfBulkStore<String>(64000000,65536);for(int i=0;i<4096;i++)store.insert("variant-"+i,1,false);
  var path=directory.resolve("large.dwf");DwfArchive.save(path,store.snapshot(),empty.snapshot(),new byte[]{1},s->s.getBytes(StandardCharsets.UTF_8),limits);
  try(var io=new DwfIo<String>(s->s.getBytes(StandardCharsets.UTF_8),limits)){
   var loads=new ArrayList<DwfIo.Load>();for(int i=0;i<12;i++)loads.add(io.load(UUID.randomUUID(),path));int[]read=new int[12];long until=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(15);
   // No client drains a whole file yet. Every file must nevertheless begin.
   while(Arrays.stream(read).anyMatch(n->n==0)&&System.nanoTime()<until){for(int i=0;i<loads.size();i++)if(read[i]==0&&loads.get(i).poll()!=null)read[i]++;Thread.sleep(2);}
   if(Arrays.stream(read).anyMatch(n->n==0))throw new AssertionError("Two large loads monopolized the IO workers");
   while(Arrays.stream(read).sum()<12*4096&&System.nanoTime()<until){for(int i=0;i<loads.size();i++)for(int n=0;n<64;n++){var record=loads.get(i).poll();if(record==null)break;read[i]++;}Thread.sleep(1);}
   for(int i=0;i<12;i++){while(!loads.get(i).complete&&System.nanoTime()<until)Thread.sleep(1);if(read[i]!=4096||!loads.get(i).drained()||loads.get(i).failure!=null)throw new AssertionError("Incorrect bounded load "+i+": "+read[i]);}
  }
  Files.delete(path);Files.delete(directory);System.out.println("PASS twelve concurrent large checkpoint loads make progress before any file is drained, preserve every record and close cleanly");
 }
}
