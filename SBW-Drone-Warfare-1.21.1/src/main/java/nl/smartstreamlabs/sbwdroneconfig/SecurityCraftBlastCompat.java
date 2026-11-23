package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import java.lang.reflect.*;
import java.util.*;

/** Optional, server-side material adapter. No SecurityCraft class appears in a JVM signature. */
public final class SecurityCraftBlastCompat {
    public static final TagKey<Block> INCLUDE=tag("blast_include"),EXCLUDE=tag("blast_exclude");
    private static TagKey<Block> tag(String path){return TagKey.create(Registries.BLOCK,ResourceLocation.tryParse("sbwdroneconfig:securitycraft/"+path));}
    public record Material(BlockState ordinary,RubbleMaterials.Kind kind,String id,boolean door){}
    private static final Map<BlockState,Optional<Material>> CACHE=Collections.synchronizedMap(new IdentityHashMap<>());
    private static boolean checked,warned;private static Class<?> api;private static Method vanilla;
    private static List<? extends String> materialRef,blockRef,mappingRef;
    private static Map<String,Double> materialValues=Map.of(),blockValues=Map.of();
    private static Map<String,String> mappings=Map.of();
    public static boolean enabled(){return present()&&SecurityCraftBlastConfig.ENABLED.get();}
    private static boolean present(){
        if(!checked){checked=true;try{api=Class.forName("net.geforcemods.securitycraft.api.IReinforcedBlock",false,SecurityCraftBlastCompat.class.getClassLoader());vanilla=api.getMethod("getVanillaBlock");}
            catch(ClassNotFoundException absent){api=null;}catch(ReflectiveOperationException|LinkageError failure){api=null;warn(failure);}}
        return api!=null;
    }
    private static void warn(Throwable failure){if(!warned){warned=true;SbwDroneRangeConfig.LOGGER.warn("SecurityCraft reinforced material mapping unavailable; retaining normal protection",failure);}}
    public static void clear(){CACHE.clear();}
    private static Map<String,Double> values(List<? extends String> entries){var result=new HashMap<String,Double>();for(var s:entries){int at=s.indexOf('=');result.put(s.substring(0,at).trim(),Double.parseDouble(s.substring(at+1).trim()));}return Map.copyOf(result);}
    private static void refresh(){
        var m=SecurityCraftBlastConfig.MATERIALS.get();if(m!=materialRef){materialValues=values(m);materialRef=m;}
        var b=SecurityCraftBlastConfig.BLOCKS.get();if(b!=blockRef){blockValues=values(b);blockRef=b;}
        var p=SecurityCraftBlastConfig.MAPPINGS.get();if(p!=mappingRef){var map=new HashMap<String,String>();for(var s:p){int at=s.indexOf('=');map.put(s.substring(0,at).trim(),s.substring(at+1).trim());}mappings=Map.copyOf(map);mappingRef=p;clear();}
    }
    private static boolean hardExcluded(Block b){return b==Blocks.BEDROCK||b==Blocks.BARRIER||b==Blocks.REINFORCED_DEEPSLATE||b==Blocks.COMMAND_BLOCK||b==Blocks.CHAIN_COMMAND_BLOCK||b==Blocks.REPEATING_COMMAND_BLOCK||b==Blocks.STRUCTURE_BLOCK||b==Blocks.STRUCTURE_VOID||b==Blocks.JIGSAW||b==Blocks.END_PORTAL||b==Blocks.END_PORTAL_FRAME||b==Blocks.NETHER_PORTAL;}
    public static Material material(BlockState state){
        if(!enabled()||state.isAir()||state.is(EXCLUDE)||hardExcluded(state.getBlock()))return null;
        refresh();var cached=CACHE.get(state);if(cached==null){cached=Optional.ofNullable(resolve(state));CACHE.put(state,cached);}return cached.orElse(null);
    }
    public static boolean selected(BlockState state){return material(state)!=null;}
    @SuppressWarnings({"unchecked","rawtypes"}) private static Material resolve(BlockState state){
        var block=state.getBlock();var id=BuiltInRegistries.BLOCK.getKey(block);String name=block.getClass().getName();
        if(id.getNamespace().equals("minecraft"))return null;
        boolean door=name.equals("net.geforcemods.securitycraft.blocks.reinforced.ReinforcedDoorBlock");
        // The existing generic #minecraft:doors exclusion must not defeat this specifically requested door.
        // Dedicated blast_exclude always wins, including for doors. Normal doors remain unchanged.
        if(state.is(RubbleMaterials.PROTECTED)&&!door)return null;
        if(!SecurityCraftBlastPolicy.buildingClass(name)&&!state.is(INCLUDE))return null;
        Block base=null;String override=mappings.get(id.toString());
        if(override!=null)base=BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(override));
        else if(door)base=Blocks.IRON_DOOR;
        else if(name.equals("net.geforcemods.securitycraft.blocks.reinforced.HorizontalReinforcedIronBars"))base=Blocks.IRON_BARS;
        else if(api.isInstance(block))try{base=(Block)vanilla.invoke(block);}catch(ReflectiveOperationException|LinkageError failure){warn(failure);}
        if(base==null||base==block||hardExcluded(base)||base.defaultBlockState().isAir()||base.defaultBlockState().hasBlockEntity())return null;
        var ordinary=base.defaultBlockState();for(var property:state.getProperties())if(ordinary.hasProperty(property))ordinary=ordinary.setValue((Property)property,state.getValue(property));
        var kind=RubbleMaterials.kind(ordinary);
        if(base==Blocks.IRON_DOOR||base==Blocks.IRON_TRAPDOOR||base==Blocks.IRON_BARS||base==Blocks.CHAIN||id.getPath().contains("copper")&&!id.getPath().endsWith("_ore"))kind=RubbleMaterials.Kind.METAL;
        else if(base instanceof FenceGateBlock||base instanceof LadderBlock||base instanceof ScaffoldingBlock)kind=RubbleMaterials.Kind.WOOD;
        return new Material(ordinary,kind,id.toString(),door);
    }
    public static double resistance(BlockState state,boolean own){
        var m=own?material(state):null;if(m==null)return state.getBlock().getExplosionResistance();
        String category=m.kind.name().toLowerCase(Locale.ROOT);double fallback=m.kind==RubbleMaterials.Kind.GLASS?.3:m.kind==RubbleMaterials.Kind.WOOD?3:6;
        return SecurityCraftBlastPolicy.resistance(m.ordinary.getBlock().getExplosionResistance(),fallback,SecurityCraftBlastConfig.MULTIPLIER.get(),materialValues.get(category),blockValues.get(m.id));
    }
    public static RubbleMaterials.Kind kind(BlockState state,boolean own){var m=own?material(state):null;return m==null?RubbleMaterials.kind(state):m.kind;}
    public static BlockState debris(BlockState state){var m=material(state);return m==null?state:m.ordinary;}
    public static BlockPos partner(BlockPos pos,BlockState state){var m=material(state);if(m==null||!m.door||!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))return null;return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==DoubleBlockHalf.LOWER?pos.above():pos.below();}
    private SecurityCraftBlastCompat(){}
}
