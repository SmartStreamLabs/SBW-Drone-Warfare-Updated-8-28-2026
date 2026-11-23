package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-owned game balance for ADV-01 only. Native SBW gun/inventory data stays authoritative. */
public final class Adv01Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue RADAR_RANGE,FIRE_RANGE,DAMAGE,SPREAD,FUSE_RADIUS,VEHICLE_SCALE,HEAT,COOLING;
    public static final ForgeConfigSpec.IntValue SCAN_TICKS,RPM,MAGAZINE,RELOAD,MAX_SHELLS;
    public static final ForgeConfigSpec.BooleanValue RADAR_LOS;
    static {
        var b=ConfigSchema.builder();b.push("ADV01");
        RADAR_RANGE=b.comment("Loaded-entity radar range in Minecraft blocks; never loads chunks.").defineInRange("radarRange",256d,32,512);
        FIRE_RANGE=b.defineInRange("automaticFireRange",192d,16,384);
        SCAN_TICKS=b.defineInRange("radarScanIntervalTicks",10,5,40);
        RADAR_LOS=b.comment("Require a clear block sightline for radar contacts; weapons always require one.").define("radarRequiresLineOfSight",true);
        RPM=b.defineInRange("roundsPerMinute",420,60,900);
        MAGAZINE=b.defineInRange("magazineCapacity",60,6,240);
        RELOAD=b.defineInRange("reloadTicks",100,20,400);
        MAX_SHELLS=b.defineInRange("maximumActiveShellsPerLevel",96,16,256);
        DAMAGE=b.defineInRange("airburstExplosionDamage",24d,0,200);
        SPREAD=b.defineInRange("spread",.6d,0,5);
        FUSE_RADIUS=b.defineInRange("airburstProximityRadius",2.5d,.5,4);
        HEAT=b.defineInRange("heatPerShot",8d,1,30);
        COOLING=b.defineInRange("coolingPerTick",.65d,.1,5);
        VEHICLE_SCALE=b.comment("Only ADV-01 flak against ordinary armored vehicles, before their normal SBW armor. Drones retain full damage.").defineInRange("nonDroneVehicleDamageScale",.12d,0,1);
        b.pop();SPEC=b.build();
    }
    public static String gunOverrides() {
        var j=new com.google.gson.JsonObject();
        j.addProperty("RPM",RPM.get());j.addProperty("Magazine",MAGAZINE.get());
        j.addProperty("NormalReloadTime",RELOAD.get());j.addProperty("EmptyReloadTime",RELOAD.get()+20);
        j.addProperty("ExplosionDamage",DAMAGE.get());j.addProperty("Spread",SPREAD.get());
        j.addProperty("HeatPerShoot",HEAT.get());j.addProperty("NaturalCooldown",COOLING.get());
        return j.toString();
    }
    private Adv01Config() {}
}
