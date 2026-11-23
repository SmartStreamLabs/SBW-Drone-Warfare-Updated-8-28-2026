package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;

/** Pure scheduler. One spatial rebuild per second; no world access or background tasks. */
public final class DistanceWarPlanner {
    public static final int VARIANTS=3, SAMPLE_TICKS=91, CLIP_TICKS=96, MAX_AREAS=64;
    public record Settings(int minSeconds,int maxSeconds,double volume,double minDistance,double maxDistance) {
        public Settings {
            minSeconds=Math.max(60,Math.min(180,minSeconds));maxSeconds=Math.max(minSeconds,Math.min(180,maxSeconds));
            volume=finite(volume,0,1,.30);minDistance=finite(minDistance,64,2000,250);maxDistance=finite(maxDistance,minDistance,2000,700);
        }
        public double radius(){return Math.min(4096,Math.max(900,maxDistance*1.5));}
    }
    public record Listener(UUID id,String dimension,double x,double y,double z) {}
    public record Event(long id,String dimension,double x,double y,double z,int clip,float volume,float pitch,List<UUID> recipients) {}
    private static final class State {
        final String dimension;final long ready;long due,quiet,end;int last=-1;
        State(Listener p,long now,Settings s,Random r){dimension=p.dimension;ready=now+s.minSeconds*20L;due=now+pause(s,r);}
    }
    private record Cell(String dimension,int x,int z) {}
    private final Map<UUID,State> states=new HashMap<>();
    private record History(int clip,long end,long quiet){}
    private final Map<UUID,History> history=new HashMap<>();
    private final Random random;private long sequence,lastExample=Long.MIN_VALUE/2;private int areas,cursor;
    public DistanceWarPlanner(Random r){random=r;}
    private static double finite(double v,double min,double max,double fallback){return Double.isFinite(v)?Math.max(min,Math.min(max,v)):fallback;}
    private static long pause(Settings s,Random r){return(s.minSeconds+r.nextInt(s.maxSeconds-s.minSeconds+1))*20L;}
    private static double distance2(Listener a,double x,double y,double z){return sq(a.x-x)+sq(a.y-y)+sq(a.z-z);}
    private static double sq(double a){return a*a;}
    private static Cell cell(Listener p){return new Cell(p.dimension,(int)Math.floor(p.x/256),(int)Math.floor(p.z/256));}
    public void remove(UUID player){states.remove(player);history.remove(player);}
    public void clear(){states.clear();areas=0;cursor=0;}
    public void resetWorld(){clear();history.clear();lastExample=Long.MIN_VALUE/2;}
    public int tracked(){return states.size();}public int areas(){return areas;}
    private Map<Cell,List<Listener>> prepare(long now,List<Listener> players,Settings s) {
        Set<UUID> live=new HashSet<>();Map<Cell,List<Listener>> grid=new HashMap<>();
        for(var p:players){live.add(p.id);var st=states.get(p.id);if(st==null||!st.dimension.equals(p.dimension)){st=new State(p,now,s,random);var memory=history.get(p.id);if(memory!=null){st.last=memory.clip;st.end=memory.end;st.quiet=memory.quiet;}st.due=Math.max(st.due,st.quiet);states.put(p.id,st);}grid.computeIfAbsent(cell(p),k->new ArrayList<>()).add(p);}
        states.keySet().retainAll(live);history.keySet().retainAll(live);return grid;
    }
    private static List<Listener> nearby(Map<Cell,List<Listener>> grid,String dimension,double x,double z,double range) {
        List<Listener> out=new ArrayList<>();int a=(int)Math.floor((x-range)/256),b=(int)Math.floor((x+range)/256),c=(int)Math.floor((z-range)/256),d=(int)Math.floor((z+range)/256);
        for(int i=a;i<=b;i++)for(int j=c;j<=d;j++){var list=grid.get(new Cell(dimension,i,j));if(list!=null)out.addAll(list);}return out;
    }
    public List<Event> tick(long now,List<Listener> input,Settings s) {
        var players=new ArrayList<>(input);players.sort(Comparator.comparing(Listener::id));if(!players.isEmpty()){Collections.rotate(players,-Math.floorMod(cursor,players.size()));cursor+=MAX_AREAS;}var grid=prepare(now,players,s);
        Set<UUID> grouped=new HashSet<>();List<Event> out=new ArrayList<>();areas=0;double groupRadius=Math.min(256,s.minDistance*.5);
        for(var leader:players){if(grouped.contains(leader.id))continue;if(++areas>MAX_AREAS){areas=MAX_AREAS;break;}
            List<Listener> group=new ArrayList<>();long due=0;
            for(var p:nearby(grid,leader.dimension,leader.x,leader.z,groupRadius))if(!grouped.contains(p.id)&&distance2(p,leader.x,leader.y,leader.z)<=sq(groupRadius)){grouped.add(p.id);group.add(p);due=Math.max(due,states.get(p.id).due);}
            if(now<due)continue;
            Event event=emit(now,leader,grid,s,false);if(event!=null)out.add(event);
        }return out;
    }
    public Event example(long now,Listener leader,List<Listener> players,Settings s) {
        if(now-lastExample<1200+CLIP_TICKS)return null;var grid=prepare(now,players,s);Event e=emit(now,leader,grid,s,true);if(e!=null)lastExample=now;return e;
    }
    private Event emit(long now,Listener leader,Map<Cell,List<Listener>> grid,Settings s,boolean example) {
        double angle=random.nextDouble()*Math.PI*2,d=s.minDistance+random.nextDouble()*(s.maxDistance-s.minDistance);
        double x=leader.x+Math.cos(angle)*d,z=leader.z+Math.sin(angle)*d,y=leader.y;
        var candidates=nearby(grid,leader.dimension,x,z,s.radius());List<Listener> heard=new ArrayList<>();long blocked=now;Set<Integer> previous=new HashSet<>();int latestClip=-1;long latestEnd=Long.MIN_VALUE;
        for(var p:candidates){if(distance2(p,x,y,z)>sq(s.radius()))continue;State st=states.get(p.id);
            if(!example&&now<st.ready)continue;
            blocked=Math.max(blocked,st.quiet);heard.add(p);if(st.last>=0){previous.add(st.last);if(st.end>latestEnd){latestEnd=st.end;latestClip=st.last;}}
        }
        if(blocked>now){states.get(leader.id).due=Math.max(states.get(leader.id).due,blocked);return null;}
        if(heard.isEmpty())return null;
        List<Integer> clips=new ArrayList<>();for(int i=0;i<VARIANTS;i++)if(VARIANTS==1||!previous.contains(i))clips.add(i);
        // When separately heard regions merge, use the latest shared-area event as the repeat reference.
        if(clips.isEmpty())for(int i=0;i<VARIANTS;i++)if(i!=latestClip)clips.add(i);
        int clip=clips.get(random.nextInt(clips.size()));long end=now+guardTicks(clip),next=end+pause(s,random);
        for(var p:heard){State st=states.get(p.id);st.end=end;st.quiet=end+s.minSeconds*20L;st.due=next;st.last=clip;history.put(p.id,new History(clip,end,st.quiet));}
        return new Event(++sequence,leader.dimension,x,y,z,clip,(float)s.volume,1f,heard.stream().map(Listener::id).toList());
    }
    public static int sampleTicks(int clip){return switch(clip){case 0->74;case 1->78;case 2->91;default->throw new IllegalArgumentException("Sound clip");};}
    public static int guardTicks(int clip){return sampleTicks(clip)+5;}
    public static double gain(double distance,double radius) {
        if(!Double.isFinite(distance)||distance<0||distance>=radius||radius<=0)return 0;
        double fade=Math.min(1,(radius-distance)/250);return .7/(1+distance/500)*fade;
    }
}
