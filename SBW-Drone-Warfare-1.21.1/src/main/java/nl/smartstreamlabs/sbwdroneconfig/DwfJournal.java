package nl.smartstreamlabs.sbwdroneconfig;
import java.io.*;import java.nio.file.*;import java.util.*;import java.util.zip.CRC32;

/** Absolute-count transaction journal. One CRC-protected frame contains BOTH
 * chests and the matching production state. A torn final frame is rolled back. */
public final class DwfJournal{
 private static final int MAGIC=0x44574a32,MAX_FRAME=134217728;
 public record Change<K>(int chest,K key,long count){public Change{if(chest<0||chest>1||count<0)throw new IllegalArgumentException();Objects.requireNonNull(key);}}
 public record ReadResult(long validBytes,int frames,boolean tornTail){}
 public static <K> void append(Path path,List<Change<K>>changes,byte[]state,DwfArchive.Encoder<K>encoder,DwfArchive.Limits limits)throws IOException{
  append(path,changes,state,encoder,limits,new UUID(0,0));
 }
 public static <K> void append(Path path,List<Change<K>>changes,byte[]state,DwfArchive.Encoder<K>encoder,DwfArchive.Limits limits,UUID generation)throws IOException{
  if(state.length>limits.stateBytes()||changes.size()>limits.variants()*2L)throw new IOException("Journal state/record limit");
  byte[]payload;
  try(var bytes=new ByteArrayOutputStream();var out=new DataOutputStream(bytes)){
   out.writeInt(MAGIC);out.writeInt(state.length);out.write(state);out.writeInt(changes.size());
   for(var c:changes){byte[]variant=encoder.encoded(c.key());if(variant.length<1||variant.length>limits.variantBytes()||c.count()>limits.items())throw new IOException("Journal variant/count limit");out.writeByte(c.chest());out.writeInt(variant.length);out.write(variant);out.writeLong(c.count());if(bytes.size()>MAX_FRAME)throw new IOException("Journal frame limit");}
   out.flush();payload=bytes.toByteArray();
  }
  var crc=new CRC32();crc.update(payload);Files.createDirectories(path.toAbsolutePath().getParent());
  try(var file=new RandomAccessFile(path.toFile(),"rw")){
   if(file.length()<24||!header(file).equals(generation)){file.setLength(0);file.seek(0);var bytes=new ByteArrayOutputStream();var h=new DataOutputStream(bytes);h.writeInt(MAGIC);h.writeLong(generation.getMostSignificantBits());h.writeLong(generation.getLeastSignificantBits());byte[]raw=bytes.toByteArray();var check=new CRC32();check.update(raw);file.write(raw);file.writeInt((int)check.getValue());}
   // The caller serializes all writes for this machine. Repair a torn last frame
   // once on opening/recovery, not by scanning the journal for every transfer.
   file.seek(file.length());file.writeInt(payload.length);file.writeInt((int)crc.getValue());file.write(payload);file.getFD().sync();
  }
 }
 public static ReadResult read(Path path,DwfArchive.Reader reader,DwfArchive.Limits limits)throws IOException{
  return read(path,reader,limits,new UUID(0,0));
 }
 public static ReadResult read(Path path,DwfArchive.Reader reader,DwfArchive.Limits limits,UUID generation)throws IOException{
  if(!Files.exists(path))return new ReadResult(0,0,false);long valid=0;int frames=0;
  try(var file=new RandomAccessFile(path.toFile(),"r")){
   if(file.length()==0)return new ReadResult(0,0,false);if(file.length()<24)return new ReadResult(0,0,true);
   if(!header(file).equals(generation))return new ReadResult(file.length(),0,false);
   valid=24;
   while(file.getFilePointer()<file.length()){
    if(file.length()-file.getFilePointer()<8)return new ReadResult(valid,frames,true);
    int length=file.readInt(),expected=file.readInt();if(length<1||length>MAX_FRAME)throw new IOException("Invalid journal frame size");
    if(file.length()-file.getFilePointer()<length)return new ReadResult(valid,frames,true);
    byte[]payload=new byte[length];file.readFully(payload);var crc=new CRC32();crc.update(payload);if((int)crc.getValue()!=expected)throw new IOException("Corrupt journal transaction");
    // Fully validate before sending any part of this transaction to a loader.
    var records=new ArrayList<Raw>();byte[]state;
    try(var in=new DataInputStream(new ByteArrayInputStream(payload))){
     if(in.readInt()!=MAGIC)throw new IOException("Invalid journal header");int size=in.readInt();if(size<0||size>limits.stateBytes())throw new IOException("Journal state limit");state=in.readNBytes(size);if(state.length!=size)throw new EOFException();
     int count=in.readInt();if(count<0||count>limits.variants()*2L)throw new IOException("Journal record limit");
     for(int i=0;i<count;i++){int chest=in.readUnsignedByte(),bytes=in.readInt();if(chest>1||bytes<1||bytes>limits.variantBytes())throw new IOException("Journal variant limit");byte[]key=in.readNBytes(bytes);if(key.length!=bytes)throw new EOFException();long amount=in.readLong();if(amount<0||amount>limits.items())throw new IOException("Journal count limit");records.add(new Raw(chest,key,amount));}
     if(in.read()!=-1)throw new IOException("Trailing journal bytes");
    }
    reader.state(state);for(var r:records)reader.entry(r.chest(),r.key(),r.amount());valid=file.getFilePointer();frames++;
   }
  }return new ReadResult(valid,frames,false);
 }
 private record Raw(int chest,byte[]key,long amount){}
 private static UUID header(RandomAccessFile file)throws IOException{file.seek(0);byte[]raw=new byte[20];file.readFully(raw);var crc=new CRC32();crc.update(raw);if(file.readInt()!=(int)crc.getValue())throw new IOException("Journal header checksum mismatch");try(var in=new DataInputStream(new ByteArrayInputStream(raw))){if(in.readInt()!=MAGIC)throw new IOException("Journal header format");return new UUID(in.readLong(),in.readLong());}}
 public static void repairTail(Path path,long bytes)throws IOException{try(var file=new RandomAccessFile(path.toFile(),"rw")){file.setLength(bytes);file.getFD().sync();}}
 private DwfJournal(){}
}
