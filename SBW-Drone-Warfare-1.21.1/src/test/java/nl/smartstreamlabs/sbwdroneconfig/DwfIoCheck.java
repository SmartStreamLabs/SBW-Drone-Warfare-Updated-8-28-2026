package nl.smartstreamlabs.sbwdroneconfig;
import java.nio.file.*;import java.nio.charset.*;import java.util.*;
public final class DwfIoCheck{
 public static void main(String[]args)throws Exception{
  Path dir=Files.createTempDirectory("dwf-io-test");var limits=new DwfArchive.Limits(64000000,65536,1024,1024);var input=new DwfBulkStore<String>(64000000,65536);var output=new DwfBulkStore<String>(64000000,65536);for(int i=0;i<1000;i++)input.insert("variant "+i,10,false);
  try(var io=new DwfIo<String>(k->k.getBytes(StandardCharsets.UTF_8),limits)){
   UUID id=UUID.randomUUID();Path file=dir.resolve("machine.dwf");
   for(int i=0;i<20;i++){input.insert("added",1,false);io.save(id,file,input.snapshot(),output.snapshot(),new byte[]{(byte)i});}
   var load=io.load(id,file);long total=0;int records=0;long until=System.nanoTime()+10_000_000_000L;
   while(!load.drained()&&System.nanoTime()<until){DwfIo.Record r;while((r=load.poll())!=null){total+=r.amount();records++;}Thread.sleep(1);}
   if(!load.drained()||load.failure!=null||load.state[0]!=19||total!=10020||records!=1001)throw new AssertionError("Coalesced latest save, ordered load or bounded streaming failed: "+load.failure);
   for(int n=0;n<10;n++){UUID next=UUID.randomUUID();Path p=dir.resolve(next+".dwf");io.save(next,p,input.snapshot(),output.snapshot(),new byte[]{1});io.load(next,p);}
   // Closing must cancel undrained bounded loads, freeing workers to flush saves.
  }
  System.out.println("PASS coalesced latest checkpoints, load ordering, bounded streaming, save/load deadlock prevention and shutdown cleanup");
 }
}
