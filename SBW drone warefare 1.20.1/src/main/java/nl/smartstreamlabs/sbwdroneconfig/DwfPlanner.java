package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.util.concurrent.*;import net.minecraft.world.item.Item;
/** Minecraft ingredient evaluation is bounded on the owning server thread.
 * Only immutable key/count/mask snapshots reach the calculation worker. */
public final class DwfPlanner implements DwfWorkBudget.Job{
 private final DwfRecipes.Entry recipe;private final DwfStorage input;private final Executor executor;private final Map<Item,Long>kinds=new IdentityHashMap<>();private final List<DwfAllocator.Stock<DwfItemKey>>stock=new ArrayList<>();
 private int requirement,stackAt;private Iterator<Item>items;private DwfIndex.Cursor<DwfItemKey>cursor;private Item current;private CompletableFuture<Map<DwfItemKey,Long>>result;private boolean cancelled,prepared,exhausted;private final long[]coverage;private int solveAfter=1;
 public DwfPlanner(DwfRecipes.Entry recipe,DwfStorage input,Executor executor){this.recipe=recipe;this.input=input;this.executor=executor;coverage=new long[recipe.inputs().size()];}
 public DwfWorkBudget.Kind kind(){return DwfWorkBudget.Kind.RECIPE;}
 public void cancel(){cancelled=true;if(result!=null)result.cancel(false);}
 public Set<Item>dependencies(){return prepared?kinds.keySet():Set.of();}
 public boolean complete(){return result!=null&&result.isDone()&&(result.isCompletedExceptionally()||exhausted||result.join()!=null);}
 public Map<DwfItemKey,Long>result(){return result.join();}
 public boolean step(){
  if(cancelled)return false;if(result!=null){if(!result.isDone())return true;if(result.isCompletedExceptionally()||exhausted||result.join()!=null)return false;result=null;}
  if(!prepared){int work=0;while(requirement<recipe.inputs().size()&&work<128){var variants=recipe.inputs().get(requirement).ingredient().getItems();while(stackAt<variants.length&&work++<128){var stack=variants[stackAt++];kinds.merge(stack.getItem(),1L<<requirement,(a,b)->a|b);}if(stackAt>=variants.length){requirement++;stackAt=0;}}if(requirement<recipe.inputs().size())return true;prepared=true;items=kinds.keySet().iterator();}
  int work=0;while(work<8){if(cursor==null||!cursor.hasNext()){if(!items.hasNext()){exhausted=true;solve();return true;}current=items.next();cursor=input.index.item(current);continue;}var row=cursor.next();work++;long available=input.counts.count(row.key());if(available<1)continue;long mask=0,candidates=kinds.get(current);var probe=row.key().stack(1);for(int r=0;r<recipe.inputs().size();r++)if((candidates&(1L<<r))!=0&&recipe.inputs().get(r).ingredient().test(probe))mask|=1L<<r;if(mask!=0){stock.add(new DwfAllocator.Stock<>(row.key(),available,mask));for(int r=0;r<coverage.length;r++)if((mask&(1L<<r))!=0)coverage[r]+=Math.min(available,recipe.inputs().get(r).count()-coverage[r]);}}boolean enough=stock.size()>=solveAfter;for(int r=0;r<coverage.length;r++)if(coverage[r]<recipe.inputs().get(r).count())enough=false;if(enough){solveAfter=Math.max(stock.size()+8,stock.size()*2);solve();}return true;
 }
 private void solve(){long[]costs=recipe.inputs().stream().mapToLong(DwfRecipes.Requirement::count).toArray();result=CompletableFuture.supplyAsync(()->DwfAllocator.allocate(stock,costs),executor);}
}
