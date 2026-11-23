package nl.smartstreamlabs.sbwdroneconfig;
/** Cosmetic rolling from real displacement, sampled once per entity tick, independent of frame rate. */
public final class Bt01WheelMotion {
    private int tick=Integer.MIN_VALUE;private double x,z,yaw,main,front,steer;private boolean grounded;
    public void update(int nextTick,double nextX,double nextZ,double nextYaw,boolean onGround){
        if(nextTick==tick)return;
        if(tick!=Integer.MIN_VALUE&&nextTick>tick&&onGround&&grounded){
            double dx=nextX-x,dz=nextZ-z,deltaYaw=wrap(nextYaw-yaw);
            if(dx*dx+dz*dz<64){double distance=-Math.sin(Math.toRadians(nextYaw))*dx+Math.cos(Math.toRadians(nextYaw))*dz;main-=distance/(3.4/16);front-=distance/(2.8/16);}
            steer=Math.max(-28,Math.min(28,-deltaYaw*3));
        }else steer=0;
        x=nextX;z=nextZ;yaw=nextYaw;grounded=onGround;tick=nextTick;
    }
    public double main(){return main;}public double front(){return front;}public double steering(){return Math.toRadians(steer);}
    private static double wrap(double value){value%=360;return value>=180?value-360:value< -180?value+360:value;}
}
