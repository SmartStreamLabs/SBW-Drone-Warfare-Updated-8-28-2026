package nl.smartstreamlabs.sbwdroneconfig;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiPredicate;

/** Server-thread weak tracking by instance: Minecraft reuses entity IDs on player respawn. */
final class Sk01IdentityWeakMap<K,V> {
 private final ReferenceQueue<K> collected=new ReferenceQueue<>();
 private final HashMap<Key<K>,V> entries=new HashMap<>();
 private static final class Key<K> extends WeakReference<K> {
  final int hash;
  Key(K key,ReferenceQueue<K> queue){super(Objects.requireNonNull(key),queue);hash=System.identityHashCode(key);}
  @Override public int hashCode(){return hash;}
  @Override public boolean equals(Object other){if(this==other)return true;var key=get();return key!=null&&other instanceof Key<?> k&&key==k.get();}
 }
 private void drain(){Reference<? extends K> key;for(int budget=64;budget>0&&(key=collected.poll())!=null;budget--)entries.remove(key);}
 void prune(){drain();}
 V put(K key,V value){drain();return entries.put(new Key<>(key,collected),Objects.requireNonNull(value));}
 V get(K key){drain();return entries.get(new Key<>(key,null));}
 V remove(K key){drain();return entries.remove(new Key<>(key,null));}
 boolean containsKey(K key){return get(key)!=null;}
 int size(){drain();return entries.size();}
 void removeIf(BiPredicate<K,V> predicate){drain();entries.entrySet().removeIf(entry->{var key=entry.getKey().get();return key==null||predicate.test(key,entry.getValue());});}
}
