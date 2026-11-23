package nl.smartstreamlabs.sbwdroneconfig;

/** Analytic three-axis leg solver in authored model coordinates (blocks, +Y up, -Z forward).
 * The links retain their authored lateral offsets; no bone or geometry is translated or stretched. */
public final class Qd01Ik {
 public record Pose(double hip,double thigh,double shin,double foot,boolean limited){}
 private static final double L1=Math.hypot(.36,.235),L2=Math.hypot(.385,.28),A=Math.atan2(.235,.36),B=Math.atan2(-.28,.385);
 private static double clamp(double x,double a,double b){return Math.max(a,Math.min(b,x));}
 public static Pose solve(boolean left,double x,double y,double z,double bodyPitch){
  double lateral=left?-.15:.15,down=-Math.sqrt(Math.max(.005,x*x+y*y-lateral*lateral));
  double hip=Math.atan2(y,x)-Math.atan2(down,lateral);hip=Math.atan2(Math.sin(hip),Math.cos(hip));
  double distance=Math.hypot(down,z),r=clamp(distance,.16,L1+L2-.002),knee=-Math.acos(clamp((r*r-L1*L1-L2*L2)/(2*L1*L2),-1,1));
  double top=Math.atan2(z,-down)-Math.atan2(L2*Math.sin(knee),L1+L2*Math.cos(knee));
  double thigh=A-top,shin=(B-A)-knee,foot=bodyPitch-thigh-shin;
  double h=clamp(hip,-.32,.32),t=clamp(thigh,-1.15,1.15),s=clamp(shin,-1,1.6),f=clamp(foot,-1.65,1.65);
  return new Pose(h,t,s,f,Math.abs(distance-r)>.002||h!=hip||t!=thigh||s!=shin||f!=foot);
 }
 /** Independent forward chain evaluation, also used by the contact QA overlay. */
 public static double[] contact(boolean left,Pose p){
  double sign=left?-1:1,b=p.thigh(),c=b+p.shin(),d=c+p.foot();
  double x=sign*.15,y=-.36*Math.cos(b)-.235*Math.sin(b)-.385*Math.cos(c)+.28*Math.sin(c)-.075*Math.cos(d),z=-.36*Math.sin(b)+.235*Math.cos(b)-.385*Math.sin(c)-.28*Math.cos(c)-.075*Math.sin(d);
  return new double[]{x*Math.cos(p.hip())-y*Math.sin(p.hip()),x*Math.sin(p.hip())+y*Math.cos(p.hip()),z};
 }
 private Qd01Ik(){}
}
