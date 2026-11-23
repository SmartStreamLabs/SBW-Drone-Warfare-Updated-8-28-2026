package nl.smartstreamlabs.sbwdroneconfig;
/** Frame-rate independent bounded flight commands and the shared rail release clock. */
public final class Hx2Kinematics {
 public static double carriage(double age){if(age<8)return 0;if(age<=20){double t=(age-8)/12;return -.5*t*t;}if(age<40)return -.5*(1-(age-20)/20);return 0;}
 public static double[] accelerate(double[] current,double[] desired,double limit){double x=desired[0]-current[0],y=desired[1]-current[1],z=desired[2]-current[2];double length=Math.sqrt(x*x+y*y+z*z),factor=length>limit?limit/length:1;return new double[]{current[0]+x*factor,current[1]+y*factor,current[2]+z*factor};}
 private Hx2Kinematics(){}
}
