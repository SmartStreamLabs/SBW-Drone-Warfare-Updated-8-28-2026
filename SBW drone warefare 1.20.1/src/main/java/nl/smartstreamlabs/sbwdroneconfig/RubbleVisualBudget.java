package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import java.util.*;
/** One client-wide admission budget across this mod's explosion smoke, dust, fire and shards. */
public final class RubbleVisualBudget {
    private static final Set<Particle> LIVE=Collections.newSetFromMap(new IdentityHashMap<>());
    private static net.minecraft.client.multiplayer.ClientLevel world;private static long tick=-1;
    public static void tick(){var l=Minecraft.getInstance().level;if(l!=world){for(var p:LIVE)p.remove();LIVE.clear();world=l;tick=-1;}if(l!=null&&l.getGameTime()!=tick){tick=l.getGameTime();LIVE.removeIf(p->!p.isAlive());}}
    public static boolean claim(Particle p){tick();if(LIVE.size()>=RubbleClientConfig.GLOBAL.get()){p.remove();return false;}LIVE.add(p);return true;}
    public static int active(){tick();return LIVE.size();}
    private RubbleVisualBudget(){}
}
