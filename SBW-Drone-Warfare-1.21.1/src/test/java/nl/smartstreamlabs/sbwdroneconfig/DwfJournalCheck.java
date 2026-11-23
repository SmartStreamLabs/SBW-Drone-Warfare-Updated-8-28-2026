package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.nio.file.*;import java.nio.charset.*;
public final class DwfJournalCheck{
 static Map<String,Long> replay(Path file,UUID generation,DwfArchive.Limits limits)throws Exception{var values=new HashMap<String,Long>();DwfJournal.read(file,new DwfArchive.Reader(){public void state(byte[]s){}public void entry(int chest,byte[]key,long count){values.put(chest+":"+new String(key,StandardCharsets.UTF_8),count);}},limits,generation);return values;}
 public static void main(String[]args)throws Exception{
  Path dir=Files.createTempDirectory("dwf-journal-test"),file=dir.resolve("items.wal");var limits=new DwfArchive.Limits(64000000,65536,4096,4096);UUID a=UUID.randomUUID(),b=UUID.randomUUID();DwfArchive.Encoder<String>codec=k->k.getBytes(StandardCharsets.UTF_8);
  DwfJournal.append(file,List.of(new DwfJournal.Change<>(0,"steel",20L)),new byte[]{0},codec,limits,a);byte[]first=Files.readAllBytes(file);
  DwfJournal.append(file,List.of(new DwfJournal.Change<>(0,"steel",18L),new DwfJournal.Change<>(1,"drone",1L)),new byte[]{1},codec,limits,a);byte[]full=Files.readAllBytes(file);
  var values=replay(file,a,limits);if(values.get("0:steel")!=18||values.get("1:drone")!=1)throw new AssertionError("Atomic two-chest transaction");
  Files.write(file,Arrays.copyOf(full,full.length-3));values=replay(file,a,limits);if(values.get("0:steel")!=20||values.containsKey("1:drone"))throw new AssertionError("Torn transaction must roll back BOTH halves");
  var result=DwfJournal.read(file,new DwfArchive.Reader(){public void state(byte[]s){}public void entry(int c,byte[]k,long n){}},limits,a);if(!result.tornTail()||result.validBytes()!=first.length)throw new AssertionError("Tail recovery position");DwfJournal.repairTail(file,result.validBytes());
  DwfJournal.append(file,List.of(new DwfJournal.Change<>(0,"steel",18L),new DwfJournal.Change<>(1,"drone",1L)),new byte[]{1},codec,limits,a);if(replay(file,a,limits).get("1:drone")!=1)throw new AssertionError("Append after tail repair");
  if(!replay(file,b,limits).isEmpty())throw new AssertionError("A replaced checkpoint must never replay an older generation journal");
  DwfJournal.append(file,List.of(new DwfJournal.Change<>(0,"steel",16L),new DwfJournal.Change<>(1,"drone",2L)),new byte[]{2},codec,limits,b);values=replay(file,b,limits);if(values.get("0:steel")!=16||values.get("1:drone")!=2)throw new AssertionError("New checkpoint generation");
  byte[]bad=Files.readAllBytes(file);bad[bad.length-1]^=1;Files.write(file,bad);boolean rejected=false;try{replay(file,b,limits);}catch(java.io.IOException e){rejected=true;}if(!rejected)throw new AssertionError("CRC corruption must not be published");
  System.out.println("PASS incremental journal, atomic two-chest frame, torn-tail rollback and repair, checkpoint-generation crash recovery, CRC corruption rejection");
 }
}
