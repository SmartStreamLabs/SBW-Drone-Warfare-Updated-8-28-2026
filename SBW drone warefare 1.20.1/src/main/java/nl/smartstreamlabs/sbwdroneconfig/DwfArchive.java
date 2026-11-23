package nl.smartstreamlabs.sbwdroneconfig;
import java.io.*;import java.nio.file.*;import java.util.*;import java.util.zip.*;

/** Streaming sidecar checkpoint. World/inventory access is intentionally absent.
 * Save accepts frozen snapshots and an encoder of PRE-ENCODED immutable variants.
 * Load callbacks must feed a bounded queue; decode/apply Minecraft stacks on server. */
public final class DwfArchive {
    private static final int MAGIC=0x44574632,VERSION=1;
    public interface Encoder<K>{byte[] encoded(K key);}
    public interface Reader{
        void state(byte[] state)throws IOException;
        void entry(int chest,byte[] variant,long amount)throws IOException;
    }
    public record Limits(long items,int variants,int variantBytes,int stateBytes){
        public Limits{if(items<1||variants<1||variantBytes<1||stateBytes<1)throw new IllegalArgumentException();}
    }
    public static <K> void save(Path path,DwfBulkStore.Cursor<K> input,DwfBulkStore.Cursor<K> output,byte[] state,Encoder<K> encoder,Limits limits)throws IOException{
        Objects.requireNonNull(path);if(state.length>limits.stateBytes())throw new IOException("Machine state too large");
        Files.createDirectories(path.toAbsolutePath().getParent());Path temp=Files.createTempFile(path.toAbsolutePath().getParent(),path.getFileName().toString(),".pending");boolean committed=false;
        try{
            try(var file=new FileOutputStream(temp.toFile());var gzip=new GZIPOutputStream(file,65536);var out=new DataOutputStream(gzip)){
                out.writeInt(MAGIC);out.writeInt(VERSION);out.writeInt(state.length);out.write(state);
                writeStore(out,input,encoder,limits);writeStore(out,output,encoder,limits);
                out.writeInt(MAGIC);out.flush();gzip.finish();file.getFD().sync();
            }
            // Refuse non-atomic replacement rather than exposing a half-written checkpoint.
            Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);committed=true;
        }finally{if(!committed)Files.deleteIfExists(temp);}
    }
    private static <K> void writeStore(DataOutputStream out,DwfBulkStore.Cursor<K> cursor,Encoder<K> encoder,Limits limits)throws IOException{
        long total=0;int variants=0;
        while(!cursor.done())for(var e:cursor.next(256)){
            byte[] variant=Objects.requireNonNull(encoder.encoded(e.key()));
            if(variant.length<1||variant.length>limits.variantBytes()||e.count()<1||e.count()>limits.items()-total||++variants>limits.variants())throw new IOException("Storage exceeds checkpoint limits");
            total+=e.count();out.writeInt(variant.length);out.write(variant);out.writeLong(e.count());
        }
        out.writeInt(0);out.writeLong(total);out.writeInt(variants);
    }
    /** Cooperative reader: keep the gzip cursor between bounded worker turns.
     * A large file cannot monopolize an IO worker while its server queue is full. */
    public static final class Source implements AutoCloseable{
        private final DataInputStream in;private final Limits limits;private final byte[]state;
        private int chest,variants;private long total;private boolean done;
        public Source(Path path,Limits limits)throws IOException{
            this.limits=limits;in=new DataInputStream(new GZIPInputStream(Files.newInputStream(path),65536));
            try{if(in.readInt()!=MAGIC||in.readInt()!=VERSION)throw new IOException("Unknown fabricator checkpoint");int length=in.readInt();if(length<0||length>limits.stateBytes())throw new IOException("Invalid state size");state=in.readNBytes(length);if(state.length!=length)throw new EOFException();}catch(IOException|RuntimeException failure){in.close();throw failure;}
        }
        public byte[]state(){return state.clone();}public boolean done(){return done;}
        public void next(Reader reader,int limit)throws IOException{
            if(limit<1)throw new IllegalArgumentException();int records=0;
            while(!done&&records<limit){
                if(chest==2){if(in.readInt()!=MAGIC||in.read()!=-1)throw new IOException("Truncated or trailing checkpoint");done=true;return;}
                int bytes=in.readInt();if(bytes==0){if(in.readLong()!=total||in.readInt()!=variants)throw new IOException("Storage totals mismatch");chest++;total=0;variants=0;continue;}
                if(bytes<1||bytes>limits.variantBytes()||++variants>limits.variants())throw new IOException("Invalid variant limit");byte[]key=in.readNBytes(bytes);if(key.length!=bytes)throw new EOFException();long count=in.readLong();if(count<1||count>limits.items()-total)throw new IOException("Invalid item count");total+=count;reader.entry(chest,key,count);records++;
            }
        }
        public void close()throws IOException{in.close();}
    }
    public static void read(Path path,Reader reader,Limits limits)throws IOException{
        try(var source=new Source(path,limits)){reader.state(source.state());while(!source.done())source.next(reader,256);}
        // Never publish a loading machine until the whole stream and CRC succeed.
    }
    private DwfArchive(){}
}
