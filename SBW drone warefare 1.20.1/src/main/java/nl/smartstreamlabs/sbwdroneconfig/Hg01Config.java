package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class Hg01Config{
 public static final ForgeConfigSpec SPEC;public static final ForgeConfigSpec.IntValue FUSE,SMOKE_LIFE;public static final ForgeConfigSpec.DoubleValue DAMAGE,RADIUS,SMOKE,ENTITY_SCALE;
 static{var b=ConfigSchema.builder();FUSE=b.comment("Fuse starts only on an actual hand throw or drone release; remaining ticks are saved.").defineInRange("fuseTicks",60,20,200);DAMAGE=b.comment("One native SBW explosion. Cosmetic fragments never deal damage.").defineInRange("damage",45d,0,500);ENTITY_SCALE=b.comment("Hand-grenade blast damage multiplier for living entities, including native SBW grenades. Terrain and vehicle damage are unchanged.").defineInRange("livingEntityDamageMultiplier",3d,0,20);RADIUS=b.defineInRange("radius",4d,1,12);SMOKE=b.comment("Visual smoke density; client quality and global budgets still apply.").defineInRange("smokeStrength",1d,.25,2);SMOKE_LIFE=b.comment("Maximum smoke life in ticks, including smooth fade.").defineInRange("smokeLifeTicks",160,60,300);SPEC=b.build();}
 private Hg01Config(){}
}
