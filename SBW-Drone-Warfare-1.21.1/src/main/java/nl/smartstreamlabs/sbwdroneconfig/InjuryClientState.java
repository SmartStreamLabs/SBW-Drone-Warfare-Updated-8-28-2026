package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
/** Common-safe presentation data; no client classes loaded on a dedicated server. */
public final class InjuryClientState {
    public record View(int entity, UUID id, InjuryState.Stage stage, int bleeding, int recovery) {
        public boolean downed() { return stage == InjuryState.Stage.SEVERE || stage == InjuryState.Stage.STABILIZED; }
    }
    private static final Map<UUID, View> PLAYERS = new HashMap<>();
    public static View get(UUID id) { return PLAYERS.get(id); }
    public static void put(View v) { PLAYERS.put(v.id(), v); }
    public static void clear() { PLAYERS.clear(); }
    private InjuryClientState() {}
}
