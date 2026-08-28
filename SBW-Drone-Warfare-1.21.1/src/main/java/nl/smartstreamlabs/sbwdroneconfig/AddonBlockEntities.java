package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AddonBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SbwDroneRangeConfig.MOD_ID);
    public static final RegistryObject<BlockEntityType<DroneDetectionSirenBlockEntity>> DRONE_DETECTION_SIREN = BLOCK_ENTITY_TYPES.register(
            "drone_detection_siren",
            () -> BlockEntityType.Builder.of(DroneDetectionSirenBlockEntity::new, AddonBlocks.DRONE_DETECTION_SIREN.get()).build(null)
    );
    public static final RegistryObject<BlockEntityType<ExtractionCrateBlockEntity>> EXTRACTION_CRATE = BLOCK_ENTITY_TYPES.register(
            "extraction_crate",
            () -> BlockEntityType.Builder.of(ExtractionCrateBlockEntity::new, AddonBlocks.EXTRACTION_CRATE.get()).build(null)
    );
    public static final RegistryObject<BlockEntityType<JammerBlockEntity>> JAMMER = BLOCK_ENTITY_TYPES.register(
            "jammer",
            () -> BlockEntityType.Builder.of(JammerBlockEntity::new, AddonBlocks.JAMMER.get()).build(null)
    );
    public static final RegistryObject<BlockEntityType<RadarJammerTowerBlockEntity>> RADAR_JAMMER_TOWER = BLOCK_ENTITY_TYPES.register(
            "radar_jammer_tower",
            () -> BlockEntityType.Builder.of(RadarJammerTowerBlockEntity::new, AddonBlocks.RADAR_JAMMER_TOWER.get()).build(null)
    );
    public static final RegistryObject<BlockEntityType<FuelMixerBlockEntity>> FUEL_MIXER = BLOCK_ENTITY_TYPES.register(
            "fuel_mixer",
            () -> BlockEntityType.Builder.of(FuelMixerBlockEntity::new, AddonBlocks.FUEL_MIXER.get()).build(null)
    );

    private AddonBlockEntities() {
    }
}
