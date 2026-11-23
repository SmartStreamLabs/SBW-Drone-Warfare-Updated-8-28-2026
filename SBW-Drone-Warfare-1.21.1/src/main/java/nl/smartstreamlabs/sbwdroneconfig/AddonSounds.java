package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AddonSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, SbwDroneRangeConfig.MOD_ID);
    public static final java.util.function.Supplier<SoundEvent> WD01_DRIVE=SOUND_EVENTS.register("wd01_drive",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"wd01_drive")));
    public static final java.util.function.Supplier<SoundEvent> UF01_MUSIC=SOUND_EVENTS.register("uf01_music",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"uf01_music")));
    public static final java.util.Map<String,java.util.function.Supplier<SoundEvent>> EXPLOSION_AUDIO=new java.util.HashMap<>();
    static {
        for(String bank:new String[]{"small","heavy"}) for(String layer:new String[]{"near","mid","far","veryfar"}) for(int v=0;v<3;v++) {
            String name=bank+"_"+layer+"_"+v;String key="explosion_audio_"+name;
            EXPLOSION_AUDIO.put(name,SOUND_EVENTS.register(key,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, key))));
        }
    }
    public static final DeferredHolder<SoundEvent, SoundEvent> CUBED_FPV_DRONE_ENGINE = SOUND_EVENTS.register(
            "cubed_fpv_drone_engine",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "cubed_fpv_drone_engine"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CUBED_FPV_DRONE_ENGINE_NEAR = SOUND_EVENTS.register(
            "cubed_fpv_drone_engine_near",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "cubed_fpv_drone_engine_near"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CUBED_FPV_DRONE_ENGINE_MID = SOUND_EVENTS.register(
            "cubed_fpv_drone_engine_mid",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "cubed_fpv_drone_engine_mid"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CUBED_FPV_DRONE_ENGINE_FAR = SOUND_EVENTS.register(
            "cubed_fpv_drone_engine_far",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "cubed_fpv_drone_engine_far"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCAS_DRONE_ENGINE = SOUND_EVENTS.register(
            "lucas_drone_engine",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "lucas_drone_engine"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DELIVERY_DRONE_ENGINE = SOUND_EVENTS.register(
            "delivery_drone_engine",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "delivery_drone_engine"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> GLIDE_BOMB_ENGINE = SOUND_EVENTS.register(
            "glide_bomb_engine",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "glide_bomb_engine"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCAS_DRONE_ENGINE_NEAR = SOUND_EVENTS.register(
            "lucas_drone_engine_near",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "lucas_drone_engine_near"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCAS_DRONE_ENGINE_MID = SOUND_EVENTS.register(
            "lucas_drone_engine_mid",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "lucas_drone_engine_mid"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCAS_DRONE_ENGINE_FAR = SOUND_EVENTS.register(
            "lucas_drone_engine_far",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "lucas_drone_engine_far"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DRONE_CLOSE_EXPLOSION = SOUND_EVENTS.register(
            "drone_close_explosion",
            () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_close_explosion"), 96.0F)
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DRONE_DISTANT_EXPLOSION = SOUND_EVENTS.register(
            "drone_distant_explosion",
            () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_distant_explosion"), 192.0F)
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DRONE_5000_BLOCK_EXPLOSION = SOUND_EVENTS.register(
            "drone_5000_block_explosion",
            () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_5000_block_explosion"), 5000.0F)
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DRONE_DETECTION_SIREN = SOUND_EVENTS.register(
            "drone_detection_siren",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_detection_siren"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DRONE_RADAR_BEEP = SOUND_EVENTS.register(
            "drone_radar_beep",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_radar_beep"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> RADAR_JAMMER_TOWER_LOOP = SOUND_EVENTS.register(
            "radar_jammer_tower_loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "radar_jammer_tower_loop"))
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> DS02_ALARM=SOUND_EVENTS.register("drone_warning_siren_ds02_alarm",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"drone_warning_siren_ds02_alarm")));

    public static final java.util.function.Supplier<SoundEvent> INJURY_HEARTBEAT=SOUND_EVENTS.register("injury_heartbeat",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"injury_heartbeat")));
    public static final java.util.function.Supplier<SoundEvent> DISTANCE_WAR_BOOM=SOUND_EVENTS.register("distance_war_boom",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"distance_war_boom")));
    public static final java.util.function.Supplier<SoundEvent> DISTANCE_WAR_02=SOUND_EVENTS.register("distance_war_02",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"distance_war_02")));
    public static final java.util.function.Supplier<SoundEvent> DISTANCE_WAR_03=SOUND_EVENTS.register("distance_war_03",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"distance_war_03")));
    public static final java.util.List<java.util.function.Supplier<SoundEvent>> DISTANCE_WAR_CLIPS=java.util.List.of(DISTANCE_WAR_BOOM,DISTANCE_WAR_02,DISTANCE_WAR_03);
    public static final java.util.function.Supplier<SoundEvent> MONITOR_LINK_SUCCESS=SOUND_EVENTS.register("monitor_drone_link_success",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"monitor_drone_link_success")));
    private AddonSounds() {
    }

 public static final java.util.function.Supplier<SoundEvent>DWF_MOTOR=SOUND_EVENTS.register("dwf01_motor",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"dwf01_motor")));
}
