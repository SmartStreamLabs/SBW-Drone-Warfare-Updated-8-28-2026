package nl.smartstreamlabs.sbwdroneconfig;

/** Client presentation math only. Does not create items or modify world light. */
public final class DwfVisualState{
 public enum Status{OFF,IDLE,ACTIVE,PAUSED,MISSING_INPUT,OUTPUT_FULL,LOADING,ERROR}
 public enum Stage{FEED,ASSEMBLE,INSPECT,TRANSFER}
 public record Snapshot(long serial,long serverTick,int progress,int required,Status status,int inputViewers,int outputViewers){
  public Snapshot{if(serial<0||progress<0||required<1||progress>required||inputViewers<0||outputViewers<0||status==null)throw new IllegalArgumentException("Invalid fabricator visual snapshot");}
  public double fraction(double now){double elapsed=status==Status.ACTIVE?Math.max(0,now-serverTick):0;return Math.max(0,Math.min(1,(progress+elapsed)/required));}
  public double animationSeconds(double now){return fraction(now)*12;}
  public Stage stage(double now){double p=fraction(now);return p<.2?Stage.FEED:p<.65?Stage.ASSEMBLE:p<.85?Stage.INSPECT:Stage.TRANSFER;}
  public boolean motor(){return status==Status.ACTIVE;}
 }
 public record Lamp(int band,float brightness,boolean emissive){}
 /** Bands are the original green / amber / red lenses in the REV02 stack light. */
 public static Lamp lamp(Status status,double ticks){
  return switch(status){
   case OFF,LOADING->new Lamp(-1,0,false);
   case IDLE,PAUSED->new Lamp(0,1,true);
   case ACTIVE->new Lamp(1,phase(ticks,10)<5?1:.12f,true);
   case MISSING_INPUT,OUTPUT_FULL,ERROR->new Lamp(2,phase(ticks,40)<20?1:.12f,true);
  };
 }
 private static double phase(double ticks,int period){return ticks-Math.floor(ticks/period)*period;}
 public static final class Lid{
  private boolean open;private double changed,from;
  public void viewers(int viewers,double now){boolean target=viewers>0;if(target!=open){from=value(now);open=target;changed=now;}}
  public double value(double now){double amount=Math.max(0,Math.min(1,(now-changed)/14));amount=amount*amount*(3-2*amount);return from+((open?1:0)-from)*amount;}
 }
 private DwfVisualState(){}
}
