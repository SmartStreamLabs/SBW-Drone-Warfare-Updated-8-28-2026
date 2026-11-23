package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AddonBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SbwDroneRangeConfig.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DroneDetectionSirenBlockEntity>> DRONE_DETECTION_SIREN = BLOCK_ENTITY_TYPES.register(
            "drone_detection_siren",
            () -> BlockEntityType.Builder.of(DroneDetectionSirenBlockEntity::new, AddonBlocks.DRONE_DETECTION_SIREN.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtractionCrateBlockEntity>> EXTRACTION_CRATE = BLOCK_ENTITY_TYPES.register(
            "extraction_crate",
            () -> BlockEntityType.Builder.of(ExtractionCrateBlockEntity::new, AddonBlocks.EXTRACTION_CRATE.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JammerBlockEntity>> JAMMER = BLOCK_ENTITY_TYPES.register(
            "jammer",
            () -> BlockEntityType.Builder.of(JammerBlockEntity::new, AddonBlocks.JAMMER.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadarJammerTowerBlockEntity>> RADAR_JAMMER_TOWER = BLOCK_ENTITY_TYPES.register(
            "radar_jammer_tower",
            () -> BlockEntityType.Builder.of(RadarJammerTowerBlockEntity::new, AddonBlocks.RADAR_JAMMER_TOWER.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FuelMixerBlockEntity>> FUEL_MIXER = BLOCK_ENTITY_TYPES.register(
            "fuel_mixer",
            () -> BlockEntityType.Builder.of(FuelMixerBlockEntity::new, AddonBlocks.FUEL_MIXER.get()).build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LucasPlatformBlockEntity>> LUCAS_PLATFORM=BLOCK_ENTITY_TYPES.register("lucas_launch_platform_lp01",()->BlockEntityType.Builder.of(LucasPlatformBlockEntity::new,AddonBlocks.LUCAS_PLATFORM.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeraniumPlatformBlockEntity>> GERANIUM_PLATFORM=BLOCK_ENTITY_TYPES.register("geranium2_launch_platform_lp02",()->BlockEntityType.Builder.of(GeraniumPlatformBlockEntity::new,AddonBlocks.GERANIUM_PLATFORM.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<Fl01PlatformBlockEntity>> FL_LP=BLOCK_ENTITY_TYPES.register("flamingo_launch_platform",()->BlockEntityType.Builder.of(Fl01PlatformBlockEntity::new,AddonBlocks.FL_LP.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GlidePlatformBlockEntity>> GLIDE_PLATFORM=BLOCK_ENTITY_TYPES.register("glide_bomb_platform_lp03",()->BlockEntityType.Builder.of(GlidePlatformBlockEntity::new,AddonBlocks.GLIDE_PLATFORM.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<Ds02BlockEntity>> DS02=BLOCK_ENTITY_TYPES.register("drone_warning_siren_ds02",()->BlockEntityType.Builder.of(Ds02BlockEntity::new,AddonBlocks.DS02.get()).build(null));

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>,BlockEntityType<HxPlatformBlockEntity>> HX_PLATFORM=BLOCK_ENTITY_TYPES.register("hx2_launch_platform",()->BlockEntityType.Builder.of(HxPlatformBlockEntity::new,AddonBlocks.HX_PLATFORM.get()).build(null));
    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>,BlockEntityType<Uf01BlockEntity>> UF01=BLOCK_ENTITY_TYPES.register("ukrainian_flag_uf01",()->BlockEntityType.Builder.of(Uf01BlockEntity::new,AddonBlocks.UF01.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<Dvt01BlockEntity>> DVT01=BLOCK_ENTITY_TYPES.register("drone_video_terminal_dvt01",()->BlockEntityType.Builder.of(Dvt01BlockEntity::new,AddonBlocks.DVT01.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<RubbleBlockEntity>> RUBBLE=BLOCK_ENTITY_TYPES.register("rubble_pile",()->BlockEntityType.Builder.of(RubbleBlockEntity::new,AddonBlocks.RUBBLE.get()).build(null));
    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>,BlockEntityType<Ps01BlockEntity>> PS01=BLOCK_ENTITY_TYPES.register("protective_shelter_ps01",()->BlockEntityType.Builder.of(Ps01BlockEntity::new,AddonBlocks.PS01.get()).build(null));
    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>,BlockEntityType<DefenseBlockEntity>> DEFENSE=BLOCK_ENTITY_TYPES.register("defense_installation",()->BlockEntityType.Builder.of(DefenseBlockEntity::new,AddonBlocks.PL01.get(),AddonBlocks.DR01.get()).build(null));
    private AddonBlockEntities() {
    }
 public static final java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<AdcBlockEntity>> ADC01=BLOCK_ENTITY_TYPES.register("anti_drone_cannon_adc01",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(AdcBlockEntity::new,AddonBlocks.ADC01.get()).build(null));
 public static final java.util.function.Supplier<BlockEntityType<Pb01BlockEntity>> PB01=BLOCK_ENTITY_TYPES.register("protective_bunker_pb01",()->BlockEntityType.Builder.of(Pb01BlockEntity::new,AddonBlocks.PB01.get()).build(null));

 public static final java.util.function.Supplier<BlockEntityType<DwfBlockEntity>> DWF01=BLOCK_ENTITY_TYPES.register("fabricator_dwf01",()->BlockEntityType.Builder.of(DwfBlockEntity::create,AddonBlocks.DWF01.get()).build(null));
 public static final java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<Sk01DecorationBlockEntity>> SK01_DECORATION=BLOCK_ENTITY_TYPES.register("soldier_skeleton_sk01",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(Sk01DecorationBlockEntity::new,AddonBlocks.SK01_DECORATION.get()).build(null));
}
