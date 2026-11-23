package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Minecraft balance values; shared server state, no client authority over cover or durability. */
public final class Ps01Config{
 public static final ModConfigSpec SPEC;public static final ModConfigSpec.DoubleValue HEALTH,DOOR_HEALTH,BLAST,FRAGMENT,DOOR_BLAST,DOOR_FRAGMENT,BLAST_WEAR,IMPACT_WEAR;public static final ModConfigSpec.IntValue MAX_BLAST_SHELTERS;
 static{var b=ConfigSchema.builder();HEALTH=b.defineInRange("structureDurability",700d,50,100000);DOOR_HEALTH=b.defineInRange("doorDurability",180d,10,100000);BLAST=b.defineInRange("intactStructureBlastReduction",.80,0,.99);FRAGMENT=b.defineInRange("intactStructureProjectileReduction",.92,0,.99);DOOR_BLAST=b.defineInRange("intactDoorBlastReduction",.72,0,.99);DOOR_FRAGMENT=b.defineInRange("intactDoorProjectileReduction",.85,0,.99);BLAST_WEAR=b.defineInRange("durabilityDamagePerExplosionRadius",35d,0,1000);IMPACT_WEAR=b.defineInRange("projectileDurabilityScale",.5,0,10);MAX_BLAST_SHELTERS=b.defineInRange("maximumSheltersPerExplosion",32,1,128);SPEC=b.build();}
 private Ps01Config(){}
}
