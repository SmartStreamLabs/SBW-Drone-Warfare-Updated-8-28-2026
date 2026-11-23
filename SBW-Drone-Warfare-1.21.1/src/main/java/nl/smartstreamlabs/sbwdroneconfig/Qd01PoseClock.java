package nl.smartstreamlabs.sbwdroneconfig;
/** Visible entities may receive tracking updates while their own client tick is paused. */
public final class Qd01PoseClock {
 private long tick=Long.MIN_VALUE;private float old,current;
 public float sample(long now,float value,float partial){
  if(tick==Long.MIN_VALUE||now<tick||now-tick>2){old=current=value;tick=now;}
  else if(now!=tick){old=current;current=value;tick=now;}
  else current=value;
  return old+(current-old)*Math.max(0,Math.min(1,partial));
 }
}
