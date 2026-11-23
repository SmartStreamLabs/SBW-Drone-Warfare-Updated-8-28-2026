package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;

/** Pure snapshot calculation. Residual paths allow a broad ingredient to yield
 * stock to a more specific ingredient; greedy matching can reject valid recipes. */
public final class DwfAllocator {
 public record Stock<K>(K key,long count,long matches){}
 private static final class Edge{final int to,reverse;long capacity;Edge(int to,int reverse,long capacity){this.to=to;this.reverse=reverse;this.capacity=capacity;}}
 private static void edge(List<List<Edge>>g,int a,int b,long capacity){var forward=new Edge(b,g.get(b).size(),capacity);var backward=new Edge(a,g.get(a).size(),0);g.get(a).add(forward);g.get(b).add(backward);}
 public static <K> Map<K,Long> allocate(List<Stock<K>>stocks,long[]required){
  if(required.length<1||required.length>64)throw new IllegalArgumentException();long need=0;for(long n:required){if(n<1)throw new IllegalArgumentException();need=Math.addExact(need,n);}
  int sink=stocks.size()+required.length+1;var g=new ArrayList<List<Edge>>(sink+1);for(int i=0;i<=sink;i++)g.add(new ArrayList<>());
  for(int i=0;i<stocks.size();i++){var s=stocks.get(i);if(s.count()<1)throw new IllegalArgumentException();edge(g,0,i+1,Math.min(need,s.count()));for(int r=0;r<required.length;r++)if((s.matches()&(1L<<r))!=0)edge(g,i+1,stocks.size()+1+r,Math.min(required[r],s.count()));}
  for(int r=0;r<required.length;r++)edge(g,stocks.size()+1+r,sink,required[r]);
  long flow=0;int[]level=new int[sink+1],next=new int[sink+1],queue=new int[sink+1];
  while(flow<need){Arrays.fill(level,-1);int head=0,tail=1;queue[0]=0;level[0]=0;while(head<tail){int at=queue[head++];for(var e:g.get(at))if(e.capacity>0&&level[e.to]<0){level[e.to]=level[at]+1;queue[tail++]=e.to;}}if(level[sink]<0)return null;Arrays.fill(next,0);long sent;while((sent=send(g,0,sink,need-flow,level,next))>0)flow+=sent;}
  var result=new LinkedHashMap<K,Long>();for(int i=0;i<stocks.size();i++){var e=g.get(0).get(i);long used=g.get(e.to).get(e.reverse).capacity;if(used>0)result.put(stocks.get(i).key(),used);}return Map.copyOf(result);
 }
 private static long send(List<List<Edge>>g,int at,int sink,long limit,int[]level,int[]next){if(at==sink)return limit;var edges=g.get(at);for(;next[at]<edges.size();next[at]++){var e=edges.get(next[at]);if(e.capacity>0&&level[e.to]==level[at]+1){long sent=send(g,e.to,sink,Math.min(limit,e.capacity),level,next);if(sent>0){e.capacity-=sent;g.get(e.to).get(e.reverse).capacity+=sent;return sent;}}}return 0;}
 private DwfAllocator(){}
}
