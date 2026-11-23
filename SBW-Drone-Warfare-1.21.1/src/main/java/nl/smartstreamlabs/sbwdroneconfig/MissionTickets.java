package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import java.util.*;

/** Shared physical tickets; no synchronous chunk requests and no persistent loader tickets. */
public final class MissionTickets {
    private static final TicketType<ChunkPos> TYPE=TicketType.create("sbwdroneconfig:mission",Comparator.comparingLong(ChunkPos::toLong));
    private static final UUID OWNER=UUID.nameUUIDFromBytes("sbwdroneconfig:coordinate_missions".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private record Key(ServerLevel level,ChunkPos pos){}
    private final Set<Key> promoted=new HashSet<>();
    private static final net.neoforged.neoforge.common.world.chunk.TicketController CONTROLLER=new net.neoforged.neoforge.common.world.chunk.TicketController(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"coordinate_missions"),(level,tickets)->{for(UUID id:tickets.getEntityTickets().keySet())tickets.removeAllTickets(id);});
    public static void registerTicketControllers(net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent event){event.register(CONTROLLER);}
    private static void loaderTicket(Key k,boolean add){CONTROLLER.forceChunk(k.level,OWNER,k.pos.x,k.pos.z,add,true);}
    private final MissionChunkBudget<Key> budget=new MissionChunkBudget<>(new MissionChunkBudget.Backend<Key>(){
        public void add(Key k){k.level.getChunkSource().addRegionTicket(TYPE,k.pos,2,k.pos,true);}
        public void remove(Key k){if(promoted.remove(k))loaderTicket(k,false);else k.level.getChunkSource().removeRegionTicket(TYPE,k.pos,2,k.pos,true);}
        public boolean ready(Key k){
            if(k.level.getChunkSource().getChunkNow(k.pos.x,k.pos.z)==null)return false;
            if(promoted.add(k)){
                // Forge's persistent API calls getChunk internally. Only promote
                // an already FULL chunk held by our async ticket, so it never
                // generates terrain synchronously. Loader ownership also keeps
                // the dimension's no-player ticking policy informed.
                loaderTicket(k,true);
                k.level.getChunkSource().removeRegionTicket(TYPE,k.pos,2,k.pos,true);
            }
            return ticking(k.level,k.pos);
        }
    });
    public void follow(UUID id,ServerLevel level,Vec3 position,Vec3 velocity){
        var current=new ChunkPos(BlockPos.containing(position));
        Vec3 look=velocity.horizontalDistanceSqr()<1e-8?position:position.add(velocity.normalize().scale(6));
        var center=new ChunkPos(BlockPos.containing(look));
        int r=MissionConfig.RADIUS.get();
        var set=new LinkedHashSet<Key>();
        if(r==0){
            // Cover the real Lucas body (half-width 1.6 + collision margin .25)
            // and the ten-block collision probe. Width stays <16 per axis: <=4 chunks.
            Vec3 ahead=velocity.horizontalDistanceSqr()<1e-8?position:position.add(velocity.normalize().scale(12));
            int minX=net.minecraft.util.Mth.floor(Math.min(position.x,ahead.x)-1.9)>>4,maxX=net.minecraft.util.Mth.floor(Math.max(position.x,ahead.x)+1.9)>>4;
            int minZ=net.minecraft.util.Mth.floor(Math.min(position.z,ahead.z)-1.9)>>4,maxZ=net.minecraft.util.Mth.floor(Math.max(position.z,ahead.z)+1.9)>>4;
            for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)set.add(new Key(level,new ChunkPos(x,z)));
        }else for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)set.add(new Key(level,new ChunkPos(center.x+x,center.z+z)));
        budget.follow(id,set);
    }
    public void release(UUID id){budget.release(id);}
    public void clear(){budget.clear();}
    public int count(){return budget.count();}
    public int owners(){return budget.owners();}
    public boolean ready(UUID id){return budget.ready(id);}
    private static boolean ticking(ServerLevel level,ChunkPos p){return level.getChunkSource().getChunkNow(p.x,p.z)!=null && level.isPositionEntityTicking(new BlockPos(p.getMinBlockX()+8,64,p.getMinBlockZ()+8));}
    /** Prefetch is a background goal, not a reason to stop safe motion in already
     * ticking chunks. In particular, the eight-tick unpowered rail phase must
     * never lose movement ticks while an unrelated outer row is being requested. */
    public boolean flightReady(Entity entity,Vec3 savedHeading,Vec3 motion){
        var level=(ServerLevel)entity.level();
        AABB bounds=entity.getBoundingBox().inflate(.25).expandTowards(motion.scale(2));
        if(!(entity instanceof GlideBombEntity) || entity instanceof Geranium2Entity){
            Vec3 direction=motion.horizontalDistanceSqr()>1e-5?motion.normalize():savedHeading.normalize();
            bounds=bounds.minmax(entity.getBoundingBox().inflate(.25).expandTowards(direction.scale(10)));
        }
        int minX=net.minecraft.util.Mth.floor(bounds.minX)>>4,maxX=net.minecraft.util.Mth.floor(bounds.maxX)>>4;
        int minZ=net.minecraft.util.Mth.floor(bounds.minZ)>>4,maxZ=net.minecraft.util.Mth.floor(bounds.maxZ)>>4;
        if(maxX-minX>2 || maxZ-minZ>2)return false;
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)if(!ticking(level,new ChunkPos(x,z)))return false;
        return true;
    }
    public void tick(){budget.tick(MissionConfig.STARTS.get(),MissionConfig.PENDING.get(),MissionConfig.CHUNK_BUDGET.get());}
}
