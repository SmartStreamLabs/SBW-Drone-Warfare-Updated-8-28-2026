package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AddonBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, SbwDroneRangeConfig.MOD_ID);
    public static final DeferredHolder<Block, Block> DRONE_DETECTION_SIREN = BLOCKS.register(
            "drone_detection_siren",
            () -> new DroneDetectionSirenBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
                            .lightLevel(state -> state.getValue(DroneDetectionSirenBlock.ACTIVE) ? 10 : 0)
            )
    );
    public static final DeferredHolder<Block, Block> ANTI_DRONE_NET = BLOCKS.register(
            "anti_drone_net",
            () -> new AntiDroneNetBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5F, 5.0F)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> ANTI_DRONE_NET_CARPET = BLOCKS.register(
            "anti_drone_net_carpet",
            () -> new AntiDroneNetCarpetBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.4F, 0.8F)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> ANTI_DRONE_NET_PANEL = BLOCKS.register(
            "anti_drone_net_panel",
            () -> new AntiDroneNetPanelBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.6F, 1.2F)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> EXTRACTION_CRATE = BLOCKS.register(
            "extraction_crate",
            () -> new ExtractionCrateBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOD)
            )
    );
    public static final DeferredHolder<Block, Block> JAMMER = BLOCKS.register(
            "jammer",
            () -> new JammerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
            )
    );
    public static final DeferredHolder<Block, Block> RADAR_JAMMER_TOWER = BLOCKS.register(
            "radar_jammer_tower",
            () -> new RadarJammerTowerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(6.0F, 12.0F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> RADAR_JAMMER_TOWER_PART = BLOCKS.register(
            "radar_jammer_tower_part",
            () -> new RadarJammerTowerPartBlock(
                    BlockBehaviour.Properties.of()
                            .strength(6.0F, 12.0F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> FUEL_MIXER = BLOCKS.register(
            "fuel_mixer",
            () -> new FuelMixerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
            )
    );


    public static final DeferredHolder<Block, Block> LUCAS_PLATFORM=BLOCKS.register("lucas_launch_platform_lp01",()->new LucasPlatformBlock(BlockBehaviour.Properties.of().strength(4.0F,8.0F).sound(SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));

    public static final DeferredHolder<Block, Block> GERANIUM_PLATFORM=BLOCKS.register("geranium2_launch_platform_lp02",()->new GeraniumPlatformBlock(BlockBehaviour.Properties.of().strength(4.0F,8.0F).sound(SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    public static final DeferredHolder<Block, Block> FL_LP=BLOCKS.register("flamingo_launch_platform",()->new Fl01PlatformBlock(BlockBehaviour.Properties.of().strength(4.0F,8.0F).sound(SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));

    public static final DeferredHolder<Block, Block> GLIDE_PLATFORM=BLOCKS.register("glide_bomb_platform_lp03",()->new GlidePlatformBlock(BlockBehaviour.Properties.of().strength(4.0F,8.0F).sound(SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));

    public static final DeferredHolder<Block, Block> DS02=BLOCKS.register("drone_warning_siren_ds02",()->new Ds02Block(BlockBehaviour.Properties.of().strength(4,8).sound(SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));

    public static final net.neoforged.neoforge.registries.DeferredHolder<Block,Block> HX_PLATFORM=BLOCKS.register("hx2_launch_platform",()->new HxPlatformBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(3,8).noOcclusion()));
    public static final DeferredHolder<Block, Block> NDZ01=BLOCKS.register("no_drone_zone_sign_ndz01",()->new Ndz01Block(BlockBehaviour.Properties.of().strength(1.5F,4.0F).sound(SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    public static final DeferredHolder<Block, Block> UF01=BLOCKS.register("ukrainian_flag_uf01",()->new Uf01Block(BlockBehaviour.Properties.of().strength(4.0F,8.0F).sound(SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    public static final DeferredHolder<Block, Block> DVT01=BLOCKS.register("drone_video_terminal_dvt01",()->new Dvt01Block(BlockBehaviour.Properties.of().strength(2.5F,5.0F).sound(SoundType.METAL).noOcclusion().dynamicShape()));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Block,Sd01LampBlock> SD01_LIGHT=BLOCKS.register("sd01_beam_light",()->new Sd01LampBlock(BlockBehaviour.Properties.of().noCollission().noLootTable().noOcclusion().lightLevel(state->state.getValue(net.minecraft.world.level.block.LightBlock.LEVEL))));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Block,RubbleBlock> RUBBLE=BLOCKS.register("rubble_pile",RubbleBlock::new);
    public static final DeferredHolder<Block, Block> PS01=BLOCKS.register("protective_shelter_ps01",()->new Ps01Block(BlockBehaviour.Properties.of().strength(-1,1000000).sound(SoundType.STONE).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false).lightLevel(s->s.getValue(Ps01Block.LIT)?12:0),false)), PS01_DOOR=BLOCKS.register("protective_shelter_ps01_door",()->new Ps01DoorBlock(BlockBehaviour.Properties.of().strength(-1,1000000).sound(SoundType.STONE).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false).lightLevel(s->s.getValue(Ps01Block.LIT)?12:0)));
    public static final DeferredHolder<Block, Block> PL01=BLOCKS.register("patriot_launcher_pl01",()->new DefenseBlock(BlockBehaviour.Properties.of().strength(5,12).sound(SoundType.METAL).dynamicShape().noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false),0)),DR01=BLOCKS.register("defense_radar_dr01",()->new DefenseBlock(BlockBehaviour.Properties.of().strength(5,12).sound(SoundType.METAL).dynamicShape().noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false),1));
    public static final java.util.function.Supplier<Block> LM01=BLOCKS.register("landmine_lm01",()->new Lm01Block(BlockBehaviour.Properties.of().strength(.8F,3F).sound(SoundType.METAL).noCollission().noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    private AddonBlocks() {
    }
 public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> ADC01=BLOCKS.register("anti_drone_cannon_adc01",()->new AdcBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(5,10).noOcclusion().sound(net.minecraft.world.level.block.SoundType.METAL).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
 public static final java.util.function.Supplier<Block> PB01=BLOCKS.register("protective_bunker_pb01",()->new Pb01Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(6,1000000).dynamicShape().noOcclusion().sound(net.minecraft.world.level.block.SoundType.STONE).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false).lightLevel(s->s.getValue(Pb01Block.LIT)?14:0),false)),PB01_DOOR=BLOCKS.register("protective_bunker_pb01_door",()->new Pb01DoorBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(6,1000000).dynamicShape().noOcclusion().sound(net.minecraft.world.level.block.SoundType.STONE).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false)));

 public static final java.util.function.Supplier<Block> DWF01=BLOCKS.register("fabricator_dwf01",()->new DwfBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(5,12).noOcclusion().noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
 public static final java.util.function.Supplier<Block> SK01_DECORATION=BLOCKS.register("soldier_skeleton_sk01",()->new Sk01DecorationBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(.7F,2F).sound(net.minecraft.world.level.block.SoundType.BONE_BLOCK).noOcclusion().noCollission()));
}
