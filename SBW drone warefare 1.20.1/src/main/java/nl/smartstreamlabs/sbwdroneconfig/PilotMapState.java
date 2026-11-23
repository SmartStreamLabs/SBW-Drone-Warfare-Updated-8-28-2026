package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
/** Volatile, connection-scoped UUID set; never tied to an individual world or saved to disk. */
public final class PilotMapState {
 private Object connection;private final Set<UUID> hidden=new HashSet<>();
 public void connection(Object next){if(connection!=next){hidden.clear();connection=next;}}
 public void apply(PilotMapPacket p){if(connection==null)return;if(p.replace())hidden.clear();if(p.hidden())hidden.addAll(p.players());else hidden.removeAll(p.players());}
 public boolean hidden(UUID id){return id!=null&&hidden.contains(id);}
}
