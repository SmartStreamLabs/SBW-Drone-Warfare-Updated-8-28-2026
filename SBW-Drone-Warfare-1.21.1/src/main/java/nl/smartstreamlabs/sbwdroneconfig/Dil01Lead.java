package nl.smartstreamlabs.sbwdroneconfig;
/** Bounded 3-D intercept lead in game ticks. No damage, item or world dependencies. */
public final class Dil01Lead {
 public static double time(double x,double y,double z,double vx,double vy,double vz,double speed){speed=Math.max(.1,speed);double a=vx*vx+vy*vy+vz*vz-speed*speed,b=2*(x*vx+y*vy+z*vz),c=x*x+y*y+z*z;double t=Math.sqrt(c)/speed;if(Math.abs(a)<1e-9){if(b<0)t=-c/b;}else{double disc=b*b-4*a*c;if(disc>=0){double r=Math.sqrt(disc),t1=(-b-r)/(2*a),t2=(-b+r)/(2*a);if(t1>0&&t2>0)t=Math.min(t1,t2);else if(t1>0)t=t1;else if(t2>0)t=t2;}}return Double.isFinite(t)?Math.max(0,Math.min(20,t)):0;}
 private Dil01Lead(){}
}
