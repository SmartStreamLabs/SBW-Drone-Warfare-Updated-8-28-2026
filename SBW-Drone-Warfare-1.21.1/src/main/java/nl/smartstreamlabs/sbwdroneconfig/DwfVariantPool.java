package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.lang.ref.*;import java.io.*;import net.minecraft.world.item.ItemStack;

/** Share immutable variant metadata across machines on ONE server. Weak values
 * plus bounded reference-queue cleanup release metadata after unload/last use.
 * Repeated checkpoints of the same variant need no repeated Minecraft decoding. */
public final class DwfVariantPool{
 private static final class Ref extends WeakReference<DwfItemKey>{final int nativeHash,encodedHash;Ref(DwfItemKey key,ReferenceQueue<DwfItemKey>queue){super(key,queue);nativeHash=key.hashCode();encodedHash=key.encodedHash();}}
 private final Thread owner=Thread.currentThread();private final ReferenceQueue<DwfItemKey>dead=new ReferenceQueue<>();
 private final Map<Integer,List<Ref>>nativeKeys=new HashMap<>(),encodedKeys=new HashMap<>();
 private void check(){if(Thread.currentThread()!=owner)throw new IllegalStateException("Variant hydration must run on the server thread");}
 public DwfItemKey find(ItemStack stack){check();if(stack.isEmpty())return null;var probe=DwfItemKey.probe(stack);var bucket=nativeKeys.get(probe.hashCode());if(bucket!=null)for(var ref:bucket){var key=ref.get();if(key!=null&&key.matches(stack))return key;}return null;}
 public DwfItemKey intern(ItemStack stack){check();clean(16);var known=find(stack);if(known!=null)return known;var key=new DwfItemKey(stack);var ref=new Ref(key,dead);nativeKeys.computeIfAbsent(ref.nativeHash,k->new ArrayList<>()).add(ref);encodedKeys.computeIfAbsent(ref.encodedHash,k->new ArrayList<>()).add(ref);return key;}
 public DwfItemKey decode(byte[] data)throws IOException{
  check();clean(16);var bucket=encodedKeys.get(Arrays.hashCode(data));if(bucket!=null)for(var ref:bucket){var key=ref.get();if(key!=null&&key.encodedEquals(data))return key;}
  try(var in=new DataInputStream(new ByteArrayInputStream(data))){var tag=net.minecraft.nbt.NbtIo.read(in,net.minecraft.nbt.NbtAccounter.create(2097152L));if(tag==null||in.read()!=-1)throw new IOException("Invalid variant encoding");var stack=ItemStack.parse(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess(),tag).orElse(ItemStack.EMPTY);if(stack.isEmpty())throw new IOException("Stored item unavailable; restore its mod before loading this machine");return intern(stack);}
 }
 public void clean(int limit){check();if(limit<0)throw new IllegalArgumentException();for(int i=0;i<limit;i++){Ref ref=(Ref)dead.poll();if(ref==null)break;remove(nativeKeys,ref.nativeHash,ref);remove(encodedKeys,ref.encodedHash,ref);}}
 private static void remove(Map<Integer,List<Ref>>map,int hash,Ref ref){var bucket=map.get(hash);if(bucket!=null){bucket.remove(ref);if(bucket.isEmpty())map.remove(hash);}}
 public void clear(){check();nativeKeys.clear();encodedKeys.clear();while(dead.poll()!=null){}}
}
