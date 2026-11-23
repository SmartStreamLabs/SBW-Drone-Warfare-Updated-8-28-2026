package nl.smartstreamlabs.sbwdroneconfig;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Brief client-side material context for break effects arriving after block removal. */
public final class RubbleParticleSource {
    private record Entry(BlockState source, long expires) {}
    private static WeakReference<Level> world = new WeakReference<>(null);
    private static final Map<Long, Entry> RECENT = new LinkedHashMap<>() {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, Entry> entry) {
            return size() > 512;
        }
    };

    public static synchronized void remember(Level level, BlockPos pos, BlockState source) {
        if (!level.isClientSide) return;
        if (world.get() != level) {
            RECENT.clear();
            world = new WeakReference<>(level);
        }
        RECENT.put(pos.asLong(), new Entry(source, level.getGameTime() + 40));
    }

    public static synchronized BlockState find(BlockAndTintGetter view, BlockPos pos) {
        if (!(view instanceof Level level) || world.get() != level) return null;
        var entry = RECENT.get(pos.asLong());
        if (entry == null) return null;
        if (entry.expires() <= level.getGameTime()) {
            RECENT.remove(pos.asLong());
            return null;
        }
        return entry.source();
    }

    private RubbleParticleSource() {}
}
