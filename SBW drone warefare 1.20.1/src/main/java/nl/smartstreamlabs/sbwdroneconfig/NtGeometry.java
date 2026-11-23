package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.*;
import java.util.*;

/** NT-01: metres internally. Rounded quadratic joins, shared frames and arc stations. */
public final class NtGeometry {
 public record Settings(double width,double height,double spacing,double trim,boolean sides){
  public Settings{if(!finite(width,height,spacing,trim)||width<2||width>16||height<2||height>12||spacing<1||spacing>12||trim<.5||trim>32)throw new IllegalArgumentException("Width 2–16, height 2–12, post spacing 1–12, corner rounding 0.5–32 blocks.");}
  public static Settings defaults(){return new Settings(6,4.5,6,10,false);}
 }
 public record Frame(Vec3 p,Vec3 n){}
 public record Quad(Vec3 a,Vec3 b,Vec3 c,Vec3 d,boolean net){public AABB bounds(){return boundsOf(List.of(a,b,c,d)).inflate(.025);}}
 public record Triangle(Vec3 a,Vec3 b,Vec3 c){public AABB bounds(){return boundsOf(List.of(a,b,c)).inflate(.025);}}
 public record Plan(List<Vec3> points,Settings settings,double lift,List<Double> left,List<Double> right,List<String> errors){
  public Plan{points=List.copyOf(points);left=List.copyOf(left);right=List.copyOf(right);errors=List.copyOf(errors);if(left.size()>2048||right.size()>2048||errors.size()>8)throw new IllegalArgumentException("Plan too large");}
  public boolean valid(){return errors.isEmpty();}
  public String fingerprint(){long h=1469598103934665603L;for(Vec3 p:points)for(double v:new double[]{p.x,p.y,p.z})h=(h^Double.doubleToLongBits(v))*1099511628211L;for(double v:new double[]{settings.width,settings.height,settings.spacing,settings.trim,settings.sides?1:0,lift})h=(h^Double.doubleToLongBits(v))*1099511628211L;for(double v:left)h=(h^Double.doubleToLongBits(v))*1099511628211L;for(double v:right)h=(h^Double.doubleToLongBits(v))*1099511628211L;return Long.toHexString(h);}
 }
 public static final class Path {
  public final List<Vec3> dense=new ArrayList<>();public final double[] arc,stations;public final double length,half,maxCurvature;public final List<String> errors=new ArrayList<>();
  public Path(List<Vec3> input,Settings cfg){
   if(input.size()<2||input.size()>64)throw new IllegalArgumentException("Add 2–64 route points.");double sum=0,spatialLength=0;
   for(int i=0;i<input.size();i++){Vec3 p=input.get(i);if(!finite(p.x,p.y,p.z)||Math.abs(p.x)>29999980||Math.abs(p.z)>29999980||p.y< -2048||p.y>4096)throw new IllegalArgumentException("Invalid route coordinates.");if(i>0){double d=horizontal(p.subtract(input.get(i-1)));if(d<.25)throw new IllegalArgumentException("Separate consecutive points by at least 0.25 blocks.");sum+=d;spatialLength+=p.distanceTo(input.get(i-1));if(spatialLength>320)throw new IllegalArgumentException("Split sloped routes longer than 320 blocks.");}}
   if(sum>256)throw new IllegalArgumentException("Split routes longer than 256 blocks.");half=cfg.width/2+.2;Vec3 cursor=input.get(0);dense.add(cursor);
   for(int i=1;i<input.size()-1;i++){Vec3 a=input.get(i-1),b=input.get(i),c=input.get(i+1),vin=b.subtract(a),vout=c.subtract(b);double il=horizontal(vin),ol=horizontal(vout),dot=(vin.x*vout.x+vin.z*vout.z)/(il*ol);
    if(dot<-.95)throw new IllegalArgumentException("Hairpin: move the corner or add a wider approach.");if(dot>.99999&&vin.normalize().dot(vout.normalize())>.99999){line(cursor,b);cursor=b;continue;}
    double trim=Math.min(cfg.trim,Math.min(il,ol)*.45);Vec3 start=b.subtract(vin.scale(trim/il)),end=b.add(vout.scale(trim/ol));line(cursor,start);int count=Math.max(8,(int)Math.ceil(2*trim/.08));for(int j=1;j<=count;j++){double t=j/(double)count;dense.add(start.scale((1-t)*(1-t)).add(b.scale(2*(1-t)*t)).add(end.scale(t*t)));}cursor=end;
   }line(cursor,input.get(input.size()-1));for(int i=dense.size()-1;i>0;i--)if(dense.get(i).distanceToSqr(dense.get(i-1))<1e-16)dense.remove(i);
   arc=new double[dense.size()];for(int i=1;i<arc.length;i++)arc[i]=arc[i-1]+dense.get(i).distanceTo(dense.get(i-1));length=arc[arc.length-1];if(length>320)throw new IllegalArgumentException("Route exceeds the sloped length budget.");
   int samples=Math.max(4,(int)Math.ceil(length/.25));double curvature=0;Frame previous=frame(0);List<Frame> checks=new ArrayList<>();checks.add(previous);
   for(int i=1;i<=samples;i++){Frame f=frame(length*i/samples);curvature=Math.max(curvature,f.n.subtract(previous.n).length()/(length/samples));previous=f;checks.add(f);}maxCurvature=curvature;
   if(curvature*half>=.78)errors.add("Bend too tight for this width. Increase corner rounding or move the corner points farther apart.");
   outer:for(int i=0;i<checks.size();i++)for(int j=i+1;j<checks.size();j++)if((j-i)*length/samples>half*3.4&&horizontal(checks.get(j).p.subtract(checks.get(i).p))<half*2.05){errors.add("Non-local tunnel envelopes intersect. Move the crossing route points.");break outer;}
   int bays=Math.max(1,(int)Math.ceil(length*(1+curvature*half)/cfg.spacing));if(bays>=128)throw new IllegalArgumentException("Too many support pairs. Increase post spacing or split the route.");stations=new double[bays+1];for(int i=0;i<=bays;i++)stations[i]=length*i/bays;
  }
  private void line(Vec3 a,Vec3 b){int n=Math.max(1,(int)Math.ceil(a.distanceTo(b)/.1));for(int i=1;i<=n;i++)dense.add(a.lerp(b,i/(double)n));}
  public Vec3 at(double s){s=Math.max(0,Math.min(lengthValue(),s));int i=Arrays.binarySearch(arc,s);if(i<0)i=-i-2;i=Math.max(0,Math.min(arc.length-2,i));return dense.get(i).lerp(dense.get(i+1),(s-arc[i])/(arc[i+1]-arc[i]));}
  private double lengthValue(){return arc[arc.length-1];}
  public Frame frame(double s){Vec3 p=at(s),t=at(Math.min(lengthValue(),s+.08)).subtract(at(Math.max(0,s-.08)));double len=horizontal(t);if(len<1e-8)throw new IllegalArgumentException("Degenerate route tangent.");return new Frame(p,new Vec3(t.z/len,0,-t.x/len));}
 }
 public static Vec3 roof(Path p,Plan plan,int bay,double s,double u){Frame f=p.frame(s);double t=(s-p.stations[bay])/(p.stations[bay+1]-p.stations[bay]);double sag=t<=1e-10||t>=1-1e-10?0:.42*Math.pow(Math.sin(Math.PI*t),2);return f.p.add(f.n.scale(p.half*u)).add(0,plan.settings.height+.60+plan.lift-.18*(1-u*u)-sag,0);}
 public static List<Quad> panels(Path p,Plan plan){List<Quad> result=new ArrayList<>();int cols=Math.max(4,(int)Math.ceil(p.half*2/.75));if(cols%2!=0)cols++;int groundOffset=p.stations.length;for(int bay=0;bay<p.stations.length-1;bay++){
  double s0=p.stations[bay],s1=p.stations[bay+1];int rows=Math.max(2,(int)Math.ceil((s1-s0)/.75));Vec3[] last=null;Vec3[] sideLast=null;
  for(int row=0;row<=rows;row++){double s=s0+(s1-s0)*row/rows;Vec3[] current=new Vec3[cols+1];for(int j=0;j<=cols;j++)current[j]=roof(p,plan,bay,s,2*j/(double)cols-1);
   if(last!=null)for(int j=0;j<cols;j++)result.add(new Quad(last[j],last[j+1],current[j+1],current[j],true));
   if(plan.settings.sides){Frame f=p.frame(s);Vec3[] sides=new Vec3[4];double t=row/(double)rows;double lf=plan.left.size()>p.stations.length?plan.left.get(groundOffset+row):lerp(plan.left.get(bay),plan.left.get(bay+1),t),rf=plan.right.size()>p.stations.length?plan.right.get(groundOffset+row):lerp(plan.right.get(bay),plan.right.get(bay+1),t);sides[0]=new Vec3(current[0].x,lf,current[0].z);sides[1]=current[0];sides[2]=new Vec3(current[cols].x,rf,current[cols].z);sides[3]=current[cols];if(sideLast!=null)for(int side=0;side<2;side++){Vec3 a=sideLast[side*2],b=sideLast[side*2+1],c=sides[side*2+1],d=sides[side*2];int vertical=Math.max(2,(int)Math.ceil(Math.max(a.distanceTo(b),c.distanceTo(d))/.75));for(int v=0;v<vertical;v++)result.add(new Quad(a.lerp(b,v/(double)vertical),a.lerp(b,(v+1)/(double)vertical),d.lerp(c,(v+1)/(double)vertical),d.lerp(c,v/(double)vertical),true));}sideLast=sides;}
   last=current;
  }groundOffset+=rows+1;
 }if(result.size()>24000)throw new IllegalArgumentException("Too many panels: split the route.");return result;}
 public static List<Triangle> triangles(List<Quad> quads){List<Triangle> r=new ArrayList<>(quads.size()*2);for(Quad q:quads){r.add(new Triangle(q.a,q.b,q.c));r.add(new Triangle(q.a,q.c,q.d));}return r;}
 /** Exact translating AABB/triangle sweep by continuous separating axes, including edge grazes. */
 public static double sweep(AABB box,Vec3 movement,Triangle tri){Vec3 c=box.getCenter(),h=new Vec3(box.getXsize()/2+.005,box.getYsize()/2+.005,box.getZsize()/2+.005);Vec3[] edges={tri.b.subtract(tri.a),tri.c.subtract(tri.b),tri.a.subtract(tri.c)},unit={new Vec3(1,0,0),new Vec3(0,1,0),new Vec3(0,0,1)};double[] time={0,1};for(Vec3 a:unit)if(!axis(a,c,h,movement,tri,time))return Double.NaN;if(!axis(edges[0].cross(edges[1]),c,h,movement,tri,time))return Double.NaN;for(Vec3 e:edges)for(Vec3 a:unit)if(!axis(e.cross(a),c,h,movement,tri,time))return Double.NaN;return time[0];}
 private static boolean axis(Vec3 a,Vec3 c,Vec3 h,Vec3 v,Triangle t,double[] time){if(a.lengthSqr()<1e-18)return true;double p=t.a.dot(a),q=t.b.dot(a),r=t.c.dot(a),radius=Math.abs(a.x)*h.x+Math.abs(a.y)*h.y+Math.abs(a.z)*h.z,lo=Math.min(p,Math.min(q,r))-radius,hi=Math.max(p,Math.max(q,r))+radius,origin=c.dot(a),speed=v.dot(a);if(Math.abs(speed)<1e-12)return origin>=lo-1e-9&&origin<=hi+1e-9;double x=(lo-origin)/speed,y=(hi-origin)/speed;time[0]=Math.max(time[0],Math.min(x,y));time[1]=Math.min(time[1],Math.max(x,y));return time[0]<=time[1]+1e-9;}
 public static AABB boundsOf(List<Vec3> p){double x=Double.POSITIVE_INFINITY,y=x,z=x,xx=-x,yy=-x,zz=-x;for(Vec3 v:p){x=Math.min(x,v.x);y=Math.min(y,v.y);z=Math.min(z,v.z);xx=Math.max(xx,v.x);yy=Math.max(yy,v.y);zz=Math.max(zz,v.z);}return new AABB(x,y,z,xx,yy,zz);}
 public static boolean finite(double... v){for(double d:v)if(!Double.isFinite(d))return false;return true;}
 public static double horizontal(Vec3 v){return Math.hypot(v.x,v.z);}private static double lerp(double a,double b,double t){return a+(b-a)*t;}
}
