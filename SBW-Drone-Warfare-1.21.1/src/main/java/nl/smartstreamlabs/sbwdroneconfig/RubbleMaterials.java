package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.*;
import net.minecraft.nbt.*;

public final class RubbleMaterials {
    public enum Kind { STONE, WOOD, GLASS, METAL, OTHER }
    public static TagKey<Block> tag(String s){return TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","rubble/"+s));}
    public static final TagKey<Block> PROTECTED=tag("protected"), CONVERTIBLE=tag("convertible"), GLASS=tag("glass"), WOOD=tag("wood"), METAL=tag("metal");
    public static boolean eligible(BlockState s){return !s.isAir()&&!s.hasBlockEntity()&&!s.is(PROTECTED)&&!s.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)&&s.is(CONVERTIBLE);}
    /** Only authenticated terrain jobs may opt into reinforced construction. */
    public static boolean eligible(BlockState state,boolean reinforced){return eligible(state)||reinforced&&SecurityCraftBlastCompat.selected(state);}
    public static Kind kind(BlockState state,boolean reinforced){return SecurityCraftBlastCompat.kind(state,reinforced);}
    public static double span(BlockState state,boolean reinforced){var kind=kind(state,reinforced);return RubbleConfig.SPAN.get()*(kind==Kind.WOOD?2:kind==Kind.METAL?3:1);}
    public static Kind kind(BlockState s){return s.is(GLASS)?Kind.GLASS:s.is(WOOD)||s.getBlock() instanceof FenceGateBlock||s.getBlock() instanceof LadderBlock||s.getBlock() instanceof ScaffoldingBlock?Kind.WOOD:s.is(METAL)||s.is(Blocks.IRON_DOOR)||s.is(Blocks.IRON_TRAPDOOR)||s.is(Blocks.IRON_BARS)||s.is(Blocks.CHAIN)||net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath().contains("copper")&&!net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath().endsWith("_ore")?Kind.METAL:s.is(CONVERTIBLE)||s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)?Kind.STONE:Kind.OTHER;}
    public static Item recovery(BlockState s){return switch(kind(s)){case WOOD->Items.STICK;case METAL->{String id=BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();yield id.contains("gold")?Items.GOLD_NUGGET:id.contains("copper")?Items.COPPER_INGOT:Items.IRON_NUGGET;}case GLASS->Items.GLASS_PANE;case STONE->Items.COBBLESTONE;default->Items.AIR;};}
    public static double span(BlockState s){return RubbleConfig.SPAN.get()*(kind(s)==Kind.WOOD?2:kind(s)==Kind.METAL?3:1);}
    public static CompoundTag write(BlockState s){return NbtUtils.writeBlockState(s);}
    public static BlockState read(CompoundTag n){return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),n);}
    private RubbleMaterials(){}
}
