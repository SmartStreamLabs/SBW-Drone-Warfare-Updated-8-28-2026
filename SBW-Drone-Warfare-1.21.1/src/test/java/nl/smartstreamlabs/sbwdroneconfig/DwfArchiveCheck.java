package nl.smartstreamlabs.sbwdroneconfig;
import java.nio.file.*;import java.nio.charset.*;import java.util.*;
public final class DwfArchiveCheck{
 public static void main(String[]args)throws Exception{
  Path dir=Files.createTempDirectory("dwf-checkpoint-test");Path file=dir.resolve("machine.dwf");var limits=new DwfArchive.Limits(64000000,65536,1024,1024);var left=new DwfBulkStore<String>(64000000,65536);var right=new DwfBulkStore<String>(64000000,65536);left.insert("battery:nbtA",32000000,false);left.insert("battery:nbtB",32000000,false);right.insert("product:components",1,false);
  var l=left.snapshot();var r=right.snapshot();left.extract("battery:nbtA",64000000,false);
  Thread worker=new Thread(()->{try{DwfArchive.save(file,l,r,new byte[]{3,7},k->k.getBytes(StandardCharsets.UTF_8),limits);}catch(Exception e){throw new RuntimeException(e);}});worker.start();worker.join();
  Map<String,Long> restored=new HashMap<>();DwfArchive.read(file,new DwfArchive.Reader(){public void state(byte[]s){if(!Arrays.equals(s,new byte[]{3,7}))throw new AssertionError();}public void entry(int chest,byte[]key,long count){restored.put(chest+":"+new String(key,StandardCharsets.UTF_8),count);}},limits);
  if(restored.size()!=3||restored.get("0:battery:nbtA")!=32000000||restored.get("0:battery:nbtB")!=32000000||restored.get("1:product:components")!=1)throw new AssertionError("Both stores and variant data must survive a background snapshot");
  byte[] original=Files.readAllBytes(file);boolean failed=false;try{DwfArchive.save(file,left.snapshot(),right.snapshot(),new byte[]{1},k->new byte[2048],limits);}catch(java.io.IOException e){failed=true;}if(!failed||!Arrays.equals(original,Files.readAllBytes(file)))throw new AssertionError("Failed save must retain previous checkpoint");
  Path bad=dir.resolve("truncated.dwf");Files.write(bad,Arrays.copyOf(original,original.length-5));failed=false;try{DwfArchive.read(bad,new DwfArchive.Reader(){public void state(byte[]s){}public void entry(int c,byte[]k,long n){}},limits);}catch(java.io.IOException e){failed=true;}if(!failed)throw new AssertionError("Reject truncated checkpoint before publishing loading machine");
  try(var files=Files.list(dir)){if(files.anyMatch(p->p.toString().endsWith(".pending")))throw new AssertionError("Temporary checkpoint cleanup");}
  System.out.println("PASS background coherent snapshot, both chests, full counts, variant data, atomic replacement, failed-save retention, truncated-file rejection and temporary-file cleanup");
 }
}
