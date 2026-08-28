package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AddonBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SbwDroneRangeConfig.MOD_ID);
    public static final RegistryObject<Block> DRONE_DETECTION_SIREN = BLOCKS.register(
            "drone_detection_siren",
            () -> new DroneDetectionSirenBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
                            .lightLevel(state -> state.getValue(DroneDetectionSirenBlock.ACTIVE) ? 10 : 0)
            )
    );
    public static final RegistryObject<Block> ANTI_DRONE_NET = BLOCKS.register(
            "anti_drone_net",
            () -> new AntiDroneNetBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5F, 5.0F)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> ANTI_DRONE_NET_CARPET = BLOCKS.register(
            "anti_drone_net_carpet",
            () -> new AntiDroneNetCarpetBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.4F, 0.8F)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> ANTI_DRONE_NET_PANEL = BLOCKS.register(
            "anti_drone_net_panel",
            () -> new AntiDroneNetPanelBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.6F, 1.2F)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> EXTRACTION_CRATE = BLOCKS.register(
            "extraction_crate",
            () -> new ExtractionCrateBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOD)
            )
    );
    public static final RegistryObject<Block> JAMMER = BLOCKS.register(
            "jammer",
            () -> new JammerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
            )
    );
    public static final RegistryObject<Block> RADAR_JAMMER_TOWER = BLOCKS.register(
            "radar_jammer_tower",
            () -> new RadarJammerTowerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(6.0F, 12.0F)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> RADAR_JAMMER_TOWER_PART = BLOCKS.register(
            "radar_jammer_tower_part",
            () -> new RadarJammerTowerPartBlock(
                    BlockBehaviour.Properties.of()
                            .strength(6.0F, 12.0F)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> FUEL_MIXER = BLOCKS.register(
            "fuel_mixer",
            () -> new FuelMixerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
            )
    );

    private AddonBlocks() {
    }
}
