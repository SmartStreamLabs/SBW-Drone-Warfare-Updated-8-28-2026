package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.math.BigInteger;
public final class DwfLegacyRsCheck{
 public static void main(String[]args){
  var view=new DwfLegacyRsWindow<String>();Object network=new Object(),other=new Object();Object[]chests=new Object[100],routes=new Object[100];long[]physical=new long[100];
  for(int i=0;i<100;i++){chests[i]=UUID.randomUUID();routes[i]=new Object();if(!view.bind(chests[i],routes[i],network))throw new AssertionError();physical[i]=64000000;view.update(chests[i],routes[i],"same exact variant",physical[i]);}
  if(view.tracked(network,"same exact variant").longValueExact()!=6400000000L||!view.limited(network,"same exact variant"))throw new AssertionError("Network-wide 64-bit sum, not independent chest caps");
  int visible=Integer.MAX_VALUE;var before=view.tracked(network,"same exact variant");int preview=view.refill(network,"same exact variant",visible-64);if(preview!=64||!view.tracked(network,"same exact variant").equals(before))throw new AssertionError("Simulation is read-only");
  physical[0]-=64;view.update(chests[0],routes[0],"same exact variant",physical[0]);visible-=64;visible+=view.refill(network,"same exact variant",visible);
  if(visible!=Integer.MAX_VALUE||Arrays.stream(physical).sum()!=6399999936L||view.tracked(network,"same exact variant").longValueExact()!=6399999936L)throw new AssertionError("Refill must not create/delete physical items");
  if(view.bind(chests[0],new Object(),network)||view.bind(chests[0],new Object(),other))throw new AssertionError("Duplicate physical storage registration");
  view.update(chests[1],routes[1],"huge",Long.MAX_VALUE);view.update(chests[2],routes[2],"huge",Long.MAX_VALUE);if(!view.tracked(network,"huge").equals(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TWO)))throw new AssertionError("Sum overflow");
  var queue=new DwfWorkBudget();for(int i=0;i<99;i++){var job=view.unbind(chests[i],routes[i]);if(job!=null)queue.add(job);}view.unbind(chests[99],routes[99]);
  while(queue.size()>0)queue.tick(128,1000000,Map.of(),false);if(view.sourceCount()!=0||view.networkCount()!=0)throw new AssertionError("Unload releases route and network references");
  System.out.println("PASS 100 full chests in one legacy RS view, 6.4 billion tracked items, read-only simulations, bounded global refill, no stock changes, duplicate-route protection, >64-bit network sums and unload cleanup");
 }
}
