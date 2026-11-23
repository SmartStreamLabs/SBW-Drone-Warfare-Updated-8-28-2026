package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;

/** Server policy; clients never supply ticket budgets or detonation decisions. */
public final class MissionConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue MAX_MISSIONS, CHUNK_BUDGET, STARTS, PENDING, DURATION, WAIT, RADIUS;
    public static final ForgeConfigSpec.DoubleValue DISTANCE;
    public static final ForgeConfigSpec.BooleanValue OFFLINE;
    static {
        var b=ConfigSchema.builder();
        MAX_MISSIONS=b.comment("Maximum active coordinate missions across all dimensions.").defineInRange("maxMissions",8,1,64);
        CHUNK_BUDGET=b.comment("Global unique mission chunks, shared by overlapping missions.").defineInRange("chunkBudget",72,9,576);
        RADIUS=b.comment("1: moving 3x3. 0: tight forward corridor, 1-4 chunks including the body's boundary overlap.").defineInRange("chunkRadius",1,0,1);
        STARTS=b.defineInRange("ticketStartsPerTick",2,1,9);
        PENDING=b.defineInRange("maxPendingChunks",4,1,36);
        DISTANCE=b.defineInRange("missionDistanceBlocks",10000.0,8.0,30000.0);
        DURATION=b.defineInRange("maxDurationTicks",6000,40,72000);
        WAIT=b.defineInRange("maxChunkWaitTicks",200,20,2400);
        OFFLINE=b.comment("Continue airborne missions after owner logout; preparation still requires the operator.").define("continueWhenOwnerOffline",true);
        SPEC=b.build();
    }
}
