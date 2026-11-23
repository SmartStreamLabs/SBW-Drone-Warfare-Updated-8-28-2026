package nl.smartstreamlabs.sbwdroneconfig;
public final class MoistureMath {
 public static double step(double wet,double rate,double seconds){if(!Double.isFinite(wet))wet=0;return Math.max(0,Math.min(100,wet+rate*Math.max(0,Math.min(1,seconds))));}
 public static double severity(double wet,double threshold){return Math.max(0,Math.min(1,(wet-threshold)/Math.max(1,100-threshold)));}
 public static int stage(double wet,double warn,double camera,double motor,double severe){camera=Math.max(warn,camera);motor=Math.max(camera,motor);severe=Math.max(motor,severe);return wet>=severe?4:wet>=motor?3:wet>=camera?2:wet>=warn?1:0;}
 private MoistureMath(){}
}
