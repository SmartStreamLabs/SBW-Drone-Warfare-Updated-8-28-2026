package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;

/** Server-owned progression. Costs and result come from a validated server recipe.
 * Ingredients are not escrowed: completion commits both inventories together. */
public final class DwfProduction<K> {
    public enum State{OFF,IDLE,RUNNING,PAUSED,MISSING_INPUT,OUTPUT_FULL,INVALID_RECIPE}
    public record Recipe<K>(String id,Map<K,Long> ingredients,K output,long count,int ticks,boolean creative){
        public Recipe(String id,Map<K,Long> ingredients,K output,long count,int ticks){this(id,ingredients,output,count,ticks,false);}
        public Recipe{
            Objects.requireNonNull(id);Objects.requireNonNull(output);ingredients=Map.copyOf(ingredients);
            if(count<1||ticks<1||(!creative&&ingredients.isEmpty())||ingredients.values().stream().anyMatch(n->n==null||n<1))throw new IllegalArgumentException("Invalid assembly recipe");
        }
    }
    public record Saved(String recipe,long remaining,boolean continuous,boolean enabled,boolean paused,long progress){}
    private Recipe<K> recipe;
    private String selectedId="";
    private long remaining,progress,lastTick=Long.MIN_VALUE,inputRevision=-1,outputRevision=-1;
    private boolean continuous,enabled,paused;
    private State state=State.OFF;
    public State state(){return state;}public long progress(){return progress;}public Recipe<K> recipe(){return recipe;}
    public long remaining(){return remaining;}public boolean continuous(){return continuous;}
    public void select(Recipe<K> next){
        // No materials consumed until completion, so stopping/changing cannot lose escrow.
        recipe=Objects.requireNonNull(next);selectedId=next.id();progress=0;enabled=false;paused=false;state=State.IDLE;invalidate();
    }
    private void invalidate(){inputRevision=outputRevision=-1;lastTick=Long.MIN_VALUE;}
    public void start(long amount,boolean continuous){if(recipe==null)throw new IllegalStateException("No recipe selected");if(amount<1)throw new IllegalArgumentException("Positive batch required");remaining=amount;this.continuous=continuous;enabled=true;paused=false;invalidate();}
    public void pause(){paused=true;lastTick=Long.MIN_VALUE;state=State.PAUSED;}
    public void resume(){paused=false;invalidate();}
    public void stop(){enabled=false;paused=false;progress=0;state=recipe==null?State.OFF:State.IDLE;invalidate();}
    public void unload(){lastTick=Long.MIN_VALUE;}
    public Saved save(){return new Saved(selectedId,remaining,continuous,enabled,paused,progress);}
    public void restore(Saved saved,Recipe<K> resolved){
        recipe=resolved;selectedId=Objects.requireNonNull(saved.recipe());remaining=Math.max(0,saved.remaining());continuous=saved.continuous();enabled=saved.enabled()&&(continuous||remaining>0);paused=saved.paused();progress=resolved==null?Math.max(0,saved.progress()):Math.max(0,Math.min(saved.progress(),resolved.ticks()));
        state=resolved==null?(selectedId.isEmpty()?State.OFF:State.INVALID_RECIPE):paused?State.PAUSED:enabled?State.RUNNING:State.IDLE;invalidate();
    }
    /** At most one completion per visit. Missed visits never cause burst crafting. */
    public boolean visit(long tick,DwfBulkStore<K> input,DwfBulkStore<K> output,boolean mayComplete){
        return visit(tick,input,output,mayComplete,input.revision());
    }
    /** A machine may pass a generation maintained by its ingredient dependencies;
     * unrelated deposits then do not trigger new ingredient availability checks. */
    public boolean visit(long tick,DwfBulkStore<K> input,DwfBulkStore<K> output,boolean mayComplete,long relevantInputRevision){
        if(recipe==null){state=selectedId.isEmpty()?State.OFF:State.INVALID_RECIPE;lastTick=Long.MIN_VALUE;return false;}
        if(!enabled){state=State.IDLE;lastTick=Long.MIN_VALUE;return false;}
        if(paused){state=State.PAUSED;lastTick=Long.MIN_VALUE;return false;}
        long in=relevantInputRevision,out=output.revision();
        if(in!=inputRevision||out!=outputRevision){
            inputRevision=in;outputRevision=out;State next=State.RUNNING;
            if(!recipe.creative())for(var e:recipe.ingredients().entrySet())if(input.count(e.getKey())<e.getValue()){next=State.MISSING_INPUT;break;}
            if(next==State.RUNNING&&output.insert(recipe.output(),recipe.count(),true)!=recipe.count())next=State.OUTPUT_FULL;
            if(state!=next){lastTick=Long.MIN_VALUE;state=next;}
        }
        if(state!=State.RUNNING){lastTick=Long.MIN_VALUE;return false;}
        long elapsed=lastTick==Long.MIN_VALUE?0:Math.max(0,tick-lastTick);lastTick=tick;
        progress=Math.min(recipe.ticks(),progress+Math.min(elapsed,recipe.ticks()));
        if(progress<recipe.ticks()||!mayComplete)return false;
        if(!(recipe.creative()?output.insert(recipe.output(),recipe.count(),false)==recipe.count():DwfBulkStore.produce(input,output,recipe.ingredients(),recipe.output(),recipe.count()))){invalidate();return false;}
        progress=0;if(!continuous&&--remaining==0){enabled=false;state=State.IDLE;}inputRevision=outputRevision=-1;
        return true;
    }
}
