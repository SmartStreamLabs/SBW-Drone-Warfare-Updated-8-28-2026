package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Bridges the old item CompoundTag usage to Minecraft 1.21.1's custom-data
 * component while keeping all existing tag names stable.
 */
public final class AddonStackTags {
    private AddonStackTags() {
    }

    public static CompoundTag get(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static CompoundTag getOrCreate(ItemStack stack) {
        return get(stack);
    }

    // Scalar reads must not copy the whole component on every HUD/camera query.
    // Keep the backing tag private: callers still receive a defensive copy from get().
    private static CustomData data(ItemStack stack) {
        return stack == null || stack.isEmpty()
                ? CustomData.EMPTY : stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    }

    public static boolean contains(ItemStack stack, String key) {
        return data(stack).contains(key);
    }

    @SuppressWarnings("deprecation")
    public static boolean readBoolean(ItemStack stack, String key) {
        return data(stack).getUnsafe().getBoolean(key);
    }

    @SuppressWarnings("deprecation")
    public static String readString(ItemStack stack, String key) {
        return data(stack).getUnsafe().getString(key);
    }

    public static boolean has(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).size() > 0;
    }

    public static void save(ItemStack stack, CompoundTag tag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
        }
    }

    public static void update(ItemStack stack, java.util.function.Consumer<CompoundTag> updater) {
        CompoundTag tag = getOrCreate(stack);
        updater.accept(tag);
        save(stack, tag);
    }
}
