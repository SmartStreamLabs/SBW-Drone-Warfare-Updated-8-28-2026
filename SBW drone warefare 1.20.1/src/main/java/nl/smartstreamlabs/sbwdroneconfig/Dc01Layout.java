package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.*;
/** Native deck datum. SBW vehicle space faces +Z; authored model faces -Z. */
public final class Dc01Layout {
 public static final double DECK=1.444977065625,WHEEL_RADIUS=.487477065625;
 public static boolean fits(int slot,int profile,int mask){return slot>=0&&slot<3&&profile>0&&profile<=3&&(mask&(1<<slot))==0&&(profile!=3||slot==0&&mask==0);}
 public static double z(int slot){return slot==1?1.15:slot==2?-1.15:0;}
 public static double floor(int profile){return profile==1?.114:profile==2?.0015:0;}
 public static Matrix4d mount(Matrix4d carrier,int slot,int profile){return new Matrix4d(carrier).translate(0,DECK-floor(profile),z(slot));}
 private Dc01Layout(){}
}
