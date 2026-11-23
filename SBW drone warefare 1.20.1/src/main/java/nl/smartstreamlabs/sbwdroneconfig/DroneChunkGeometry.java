package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
final class DroneChunkGeometry {
    private DroneChunkGeometry() {}
    static int chunk(double block) { return Math.floorDiv((int)Math.floor(block), 16); }
    static long pack(int x, int z) { return (x & 0xffffffffL) | ((z & 0xffffffffL) << 32); }
    static Set<Long> square(int x, int z, int radius) {
        Set<Long> result = new LinkedHashSet<>();
        for (int ring=0; ring<=radius; ring++)
            for (int dx=-ring; dx<=ring; dx++)
                for (int dz=-ring; dz<=ring; dz++)
                    if (Math.max(Math.abs(dx),Math.abs(dz))==ring) result.add(pack(x+dx,z+dz));
        return result;
    }
}
