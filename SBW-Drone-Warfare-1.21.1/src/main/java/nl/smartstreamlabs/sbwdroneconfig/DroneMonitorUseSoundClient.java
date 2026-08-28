package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class DroneMonitorUseSoundClient {
    private static MonitorUseSoundInstance activeSound;

    private DroneMonitorUseSoundClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(DroneMonitorUseSoundClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneMonitorUseSoundClient.class);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            stopActiveSound();
            return;
        }

        boolean shouldPlay = SbwCompat.isUsingLinkedMonitor(SbwCompat.getActiveLinkedMonitor(player));

        if (!shouldPlay) {
            stopActiveSound();
            return;
        }

        if (activeSound == null || activeSound.isStopped()) {
            activeSound = new MonitorUseSoundInstance(player);
            minecraft.getSoundManager().play(activeSound);
            return;
        }

        activeSound.setPlayer(player);
    }

    private static void stopActiveSound() {
        if (activeSound != null) {
            activeSound.requestStop();
            activeSound = null;
        }
    }

    private static final class MonitorUseSoundInstance extends AbstractTickableSoundInstance {
        private LocalPlayer player;
        private boolean stopped;

        private MonitorUseSoundInstance(LocalPlayer player) {
            super(SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, player.level().getRandom());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.relative = true;
            this.attenuation = Attenuation.NONE;
            this.volume = getConfiguredVolume();
            this.pitch = 1.2F;
        }

        @Override
        public void tick() {
            if (this.stopped || this.player == null || !this.player.isAlive()) {
                stopInternal();
                return;
            }

            boolean stillUsing = SbwCompat.isUsingLinkedMonitor(SbwCompat.getActiveLinkedMonitor(this.player));
            if (!stillUsing) {
                stopInternal();
                return;
            }

            this.volume = getConfiguredVolume();
            this.pitch = 1.2F;
        }

        private float getConfiguredVolume() {
            return (float) (0.34F * AddonConfig.droneMonitorHumVolumeMultiplier());
        }

        private void setPlayer(LocalPlayer player) {
            this.player = player;
        }

        private void requestStop() {
            this.stopped = true;
        }

        private void stopInternal() {
            if (this.stopped) {
                this.stop();
                return;
            }

            this.stopped = true;
            this.stop();
        }

        @Override
        public boolean isStopped() {
            return this.stopped;
        }
    }
}
