package nl.smartstreamlabs.sbwdroneconfig;
/** Pure policy math with finite engine conversion and explicit inheritance. */
public final class VehicleProfileValues {
 public record Factors(double health,double incomingDamage){}
 public static final Factors IDENTITY=new Factors(1,1);
 public static double resolve(double override,double general){return override==0?general:override;}
 public static float scaled(float baseline,double factor){if(factor==1||baseline==0)return baseline;return (float)Math.min(Float.MAX_VALUE,Math.max(0,(double)baseline*factor));}
 private VehicleProfileValues(){}
}
