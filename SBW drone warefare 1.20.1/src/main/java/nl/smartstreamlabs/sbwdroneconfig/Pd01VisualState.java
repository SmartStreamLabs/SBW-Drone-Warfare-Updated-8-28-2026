package nl.smartstreamlabs.sbwdroneconfig;
/** Reads existing status only; this class never detects targets or changes item state. */
public final class Pd01VisualState {
 public static int state(boolean active,boolean held,boolean targets){return !active?0:held&&targets?2:1;}
 public static double sweep(double ticks){return .31*(1-Math.cos(ticks*Math.PI/16))/16;}
 private Pd01VisualState(){}
}
