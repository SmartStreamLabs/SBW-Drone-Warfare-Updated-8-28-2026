package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SbwItemResolver {
    private SbwItemResolver() {
    }

    public static Item resolve(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key == null ? null : BuiltInRegistries.ITEM.get(key);
    }

    public static boolean stackMatches(ItemStack stack, String id) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null && key.toString().equals(id);
    }

    public static boolean stackMatches(ItemStack stack, ResourceLocation id) {
        return stack != null && !stack.isEmpty() && id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
