package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AddonSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SbwDroneRangeConfig.MOD_ID);
    public static final RegistryObject<SoundEvent> CUBED_FPV_DRONE_ENGINE = SOUND_EVENTS.register(
            "cubed_fpv_drone_engine",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "cubed_fpv_drone_engine"))
    );
    public static final RegistryObject<SoundEvent> CUBED_FPV_DRONE_ENGINE_NEAR = SOUND_EVENTS.register(
            "cubed_fpv_drone_engine_near",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "cubed_fpv_drone_engine_near"))
    );
    public static final RegistryObject<SoundEvent> CUBED_FPV_DRONE_ENGINE_MID = SOUND_EVENTS.register(
            "cubed_fpv_drone_engine_mid",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "cubed_fpv_drone_engine_mid"))
    );
    public static final RegistryObject<SoundEvent> CUBED_FPV_DRONE_ENGINE_FAR = SOUND_EVENTS.register(
            "cubed_fpv_drone_engine_far",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "cubed_fpv_drone_engine_far"))
    );
    public static final RegistryObject<SoundEvent> LUCAS_DRONE_ENGINE = SOUND_EVENTS.register(
            "lucas_drone_engine",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "lucas_drone_engine"))
    );
    public static final RegistryObject<SoundEvent> DELIVERY_DRONE_ENGINE = SOUND_EVENTS.register(
            "delivery_drone_engine",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "delivery_drone_engine"))
    );
    public static final RegistryObject<SoundEvent> GLIDE_BOMB_ENGINE = SOUND_EVENTS.register(
            "glide_bomb_engine",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "glide_bomb_engine"))
    );
    public static final RegistryObject<SoundEvent> LUCAS_DRONE_ENGINE_NEAR = SOUND_EVENTS.register(
            "lucas_drone_engine_near",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "lucas_drone_engine_near"))
    );
    public static final RegistryObject<SoundEvent> LUCAS_DRONE_ENGINE_MID = SOUND_EVENTS.register(
            "lucas_drone_engine_mid",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "lucas_drone_engine_mid"))
    );
    public static final RegistryObject<SoundEvent> LUCAS_DRONE_ENGINE_FAR = SOUND_EVENTS.register(
            "lucas_drone_engine_far",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "lucas_drone_engine_far"))
    );
    public static final RegistryObject<SoundEvent> DRONE_CLOSE_EXPLOSION = SOUND_EVENTS.register(
            "drone_close_explosion",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "drone_close_explosion"), 96.0F)
    );
    public static final RegistryObject<SoundEvent> DRONE_DISTANT_EXPLOSION = SOUND_EVENTS.register(
            "drone_distant_explosion",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "drone_distant_explosion"), 192.0F)
    );
    public static final RegistryObject<SoundEvent> DRONE_5000_BLOCK_EXPLOSION = SOUND_EVENTS.register(
            "drone_5000_block_explosion",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "drone_5000_block_explosion"), 5000.0F)
    );
    public static final RegistryObject<SoundEvent> DRONE_DETECTION_SIREN = SOUND_EVENTS.register(
            "drone_detection_siren",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "drone_detection_siren"))
    );
    public static final RegistryObject<SoundEvent> DRONE_RADAR_BEEP = SOUND_EVENTS.register(
            "drone_radar_beep",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "drone_radar_beep"))
    );
    public static final RegistryObject<SoundEvent> RADAR_JAMMER_TOWER_LOOP = SOUND_EVENTS.register(
            "radar_jammer_tower_loop",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "radar_jammer_tower_loop"))
    );

    private AddonSounds() {
    }
}
