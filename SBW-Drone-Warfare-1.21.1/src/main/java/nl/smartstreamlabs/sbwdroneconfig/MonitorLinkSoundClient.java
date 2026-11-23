package nl.smartstreamlabs.sbwdroneconfig;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID, value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class MonitorLinkSoundClient {
    private static ClientLevel world;
    private static final ArrayDeque<UUID> pending = new ArrayDeque<>();
    private static final LinkedHashSet<UUID> seen = new LinkedHashSet<>();
    private static Voice active;

    private static void sync() {
        var game = Minecraft.getInstance();
        if (world == game.level) return;
        if (active != null) game.getSoundManager().stop(active);
        active = null;
        pending.clear();
        seen.clear();
        world = game.level;
    }

    public static void receive(MonitorLinkSoundPacket packet) {
        sync();
        if (world == null || !seen.add(packet.id())) return;
        while (seen.size() > 128) seen.remove(seen.iterator().next());
        pending.add(packet.id());
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        sync();
        var game = Minecraft.getInstance();
        if (world == null || game.isPaused()) return;
        if (active != null) {
            boolean live = game.getSoundManager().isActive(active);
            active.started |= live;
            // The decoder/channel starts asynchronously. Do not dequeue another voice in that gap.
            if (active.isStopped() || active.started && !live || !active.started && ++active.waiting > 200)
                active = null;
        }
        if (active == null && !pending.isEmpty()) {
            pending.remove();
            active = new Voice();
            game.getSoundManager().play(active);
        }
    }

    private static final class Voice extends AbstractTickableSoundInstance {
        boolean started;
        int waiting;
        Voice() {
            super(AddonSounds.MONITOR_LINK_SUCCESS.get(), SoundSource.PLAYERS, RandomSource.create());
            relative = true;
            attenuation = Attenuation.NONE;
            looping = false;
            delay = 0;
            volume = 1;
            pitch = 1;
            x = y = z = 0;
        }
        @Override public void tick() {}
    }

    private MonitorLinkSoundClient() {}
}
