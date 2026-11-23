package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
/** Material-weighted load paths, independent of Minecraft/world access. */
public final class RubbleSupportGraph {
    public record Pos(int x,int y,int z){Pos add(int[] d){return new Pos(x+d[0],y+d[1],z+d[2]);}}
    public record Cell(boolean member,boolean anchor,double span){}
    public interface World {Cell at(Pos p);}
    public enum Result { SUPPORTED, UNSUPPORTED, UNKNOWN }
    public static final class Budget {public int left;public Budget(int n){left=n;}}
    private record Node(Pos p,double cost){}
    private static final int[][] DIRECTIONS={{0,-1,0},{0,1,0},{1,0,0},{-1,0,0},{0,0,1},{0,0,-1}};
    public static Result check(World world,Pos start,Pos centre,int radius,Budget budget){
        var todo=new PriorityQueue<Node>(Comparator.comparingDouble(Node::cost));var costs=new HashMap<Pos,Double>();todo.add(new Node(start,0));costs.put(start,0d);int nodes=0;
        while(!todo.isEmpty()){
            if(++nodes>256||budget.left--<=0)return Result.UNKNOWN;
            var n=todo.remove();if(n.cost>costs.getOrDefault(n.p,Double.POSITIVE_INFINITY))continue;
            var cell=world.at(n.p);if(cell==null)return Result.UNKNOWN;if(!cell.member)continue;
            if(Math.abs(n.p.x-centre.x)>=radius||Math.abs(n.p.z-centre.z)>=radius||n.p.y<=centre.y-radius)return Result.SUPPORTED;
            for(int[] d:DIRECTIONS){if(budget.left--<=0)return Result.UNKNOWN;Pos pos=n.p.add(d);var next=world.at(pos);if(next==null)return Result.UNKNOWN;if(d[1]==-1&&next.anchor)return Result.SUPPORTED;if(!next.member)continue;
                double cost=n.cost+(d[1]==-1?0:d[1]==1?.65:1/Math.max(.01,cell.span));
                if(cost<=1+1e-9&&cost<costs.getOrDefault(pos,Double.POSITIVE_INFINITY)){costs.put(pos,cost);todo.add(new Node(pos,cost));}
            }
        }
        return Result.UNSUPPORTED;
    }
    private RubbleSupportGraph(){}
}
