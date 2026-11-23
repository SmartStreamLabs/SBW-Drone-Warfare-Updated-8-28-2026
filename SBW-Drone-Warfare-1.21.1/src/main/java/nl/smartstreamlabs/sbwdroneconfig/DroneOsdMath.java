package nl.smartstreamlabs.sbwdroneconfig;

/** Display units: one block is one metre; one game tick is 1/20 second. */
public final class DroneOsdMath {
    private static final String[] CARDINALS={"N","NE","E","SE","S","SW","W","NW"};
    private DroneOsdMath() {}
    public static double heading(double forwardX,double forwardZ){return normalize(Math.toDegrees(Math.atan2(forwardX,-forwardZ)));}
    public static double normalize(double a){return (a%360+360)%360;}
    public static double difference(double a,double b){return normalize(a-b+180)-180;}
    public static double speed(double dx,double dz,long ticks){return ticks<=0?Double.NaN:Math.hypot(dx,dz)*20/ticks;}
    public static String cardinal(double h){return CARDINALS[Math.floorMod((int)Math.round(h/45),8)];}
    public static double horizonY(double pitchDown,double elevation,double focal){return -Math.tan(Math.toRadians(pitchDown+elevation))*focal;}
    public static float scale(int width,int height,double requested,boolean preview){
        float base=Math.min(width/(preview?220f:480f),height/(preview?140f:300f));
        return Math.max(.01f,Math.min((float)(base*requested),Math.min(width/(preview?200f:400f),height/(preview?130f:250f))));
    }
}
