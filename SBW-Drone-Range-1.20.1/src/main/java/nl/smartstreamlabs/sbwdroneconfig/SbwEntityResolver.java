package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class SbwEntityResolver {
    private SbwEntityResolver() {
    }

    public static EntityType<?> resolve(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key == null ? null : BuiltInRegistries.ENTITY_TYPE.get(key);
    }

    public static boolean entityTypeMatches(Entity entity, String id) {
        if (entity == null) {
            return false;
        }

        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && key.toString().equals(id);
    }

    public static boolean entityTypeMatchesAny(Entity entity, String... ids) {
        if (ids == null) {
            return false;
        }

        for (String id : ids) {
            if (entityTypeMatches(entity, id)) {
                return true;
            }
        }
        return false;
    }
}
