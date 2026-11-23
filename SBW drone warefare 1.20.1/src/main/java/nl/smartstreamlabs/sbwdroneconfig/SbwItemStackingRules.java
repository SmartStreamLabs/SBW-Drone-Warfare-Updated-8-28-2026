package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Set;

public final class SbwItemStackingRules {
    private static final String SBW_NAMESPACE = "superbwarfare";
    private static final int STACK_SIZE = 64;
    private static final Set<String> STACK_SIZE_OVERRIDE_ITEM_IDS = Set.of(
            "tm_62",
            "ptkm_1r",
            "lunge_mine",
            "monitor",
            "artillery_indicator",
            "detonator",
            "dog_tag",
            "iff",
            "cell",
            "battery",
            "small_battery_pack",
            "medium_battery_pack",
            "large_battery_pack",
            "medical_kit",
            "charging_station",
            "creative_charging_station"
    );
    private static final Set<String> HARMLESS_BLOCK_ENTITY_KEYS = Set.of(
            "id",
            "x",
            "y",
            "z",
            "ForgeCaps",
            "ForgeData"
    );

    private SbwItemStackingRules() {
    }

    public static int getMaxStackSize(ItemStack stack) {
        if (!isExplicitStackSizeOverride(stack)) {
            return -1;
        }

        if (stack.isDamaged()) {
            return 1;
        }

        return STACK_SIZE;
    }

    public static boolean canMergeStackableSbwItems(ItemStack first, ItemStack second) {
        if (!isStackingCandidate(first) || !isStackingCandidate(second)) {
            return false;
        }
        if (first.isDamaged() || second.isDamaged()) {
            return false;
        }

        ResourceLocation firstId = ForgeRegistries.ITEMS.getKey(first.getItem());
        ResourceLocation secondId = ForgeRegistries.ITEMS.getKey(second.getItem());
        if (!Objects.equals(firstId, secondId)) {
            return false;
        }

        return Objects.equals(normalizedTagForStacking(first), normalizedTagForStacking(second));
    }

    private static boolean isStackingCandidate(ItemStack stack) {
        if (!isSbwStack(stack)) {
            return false;
        }

        return isExplicitStackSizeOverride(stack) || stack.getItem().getMaxStackSize(stack) > 1;
    }

    private static boolean isExplicitStackSizeOverride(ItemStack stack) {
        if (!isSbwStack(stack)) {
            return false;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId != null && STACK_SIZE_OVERRIDE_ITEM_IDS.contains(itemId.getPath());
    }

    private static boolean isSbwStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId != null && SBW_NAMESPACE.equals(itemId.getNamespace());
    }

    private static CompoundTag normalizedTagForStacking(ItemStack stack) {
        CompoundTag source = stack.getTag();
        if (source == null || source.isEmpty()) {
            return null;
        }

        CompoundTag normalized = source.copy();
        removeHarmlessBlockTag(normalized, "BlockEntityTag");
        removeHarmlessBlockTag(normalized, "BlockStateTag");
        removeHarmlessMonitorState(normalized);
        removeZeroNumeric(normalized, "Energy");

        return normalized.isEmpty() ? null : normalized;
    }

    private static void removeHarmlessBlockTag(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag blockTag = tag.getCompound(key);
        if (blockTag.isEmpty() || isHarmlessBlockEntityTag(blockTag)) {
            tag.remove(key);
        }
    }

    private static void removeHarmlessMonitorState(CompoundTag tag) {
        boolean linked = tag.getBoolean(SbwCompat.TAG_LINKED);
        boolean using = tag.getBoolean(SbwCompat.TAG_USING);
        boolean linkedDroneUnset = !tag.contains(SbwCompat.TAG_LINKED_DRONE)
                || "none".equals(tag.getString(SbwCompat.TAG_LINKED_DRONE))
                || tag.getString(SbwCompat.TAG_LINKED_DRONE).isEmpty();

        if (!linked && !using && linkedDroneUnset) {
            tag.remove(SbwCompat.TAG_LINKED);
            tag.remove(SbwCompat.TAG_USING);
            tag.remove(SbwCompat.TAG_LINKED_DRONE);
            removeZeroNumeric(tag, "PosX");
            removeZeroNumeric(tag, "PosY");
            removeZeroNumeric(tag, "PosZ");
        }
    }

    private static void removeZeroNumeric(CompoundTag tag, String key) {
        Tag value = tag.get(key);
        if (value != null && isNumericTag(value) && Math.abs(tag.getDouble(key)) < 1.0E-6D) {
            tag.remove(key);
        }
    }

    private static boolean isHarmlessBlockEntityTag(CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (HARMLESS_BLOCK_ENTITY_KEYS.contains(key) || isHarmlessBlockEntityValue(tag, key, value)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean isHarmlessBlockEntityValue(CompoundTag parent, String key, Tag value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CompoundTag compoundTag) {
            return compoundTag.isEmpty() || isHarmlessBlockEntityTag(compoundTag);
        }
        if (value instanceof ListTag listTag) {
            return listTag.isEmpty();
        }
        if (isNumericTag(value)) {
            return isStaticCapacityKey(key) || Math.abs(parent.getDouble(key)) < 1.0E-6D;
        }
        if (value.getId() == Tag.TAG_STRING) {
            return parent.getString(key).isEmpty();
        }
        return false;
    }

    private static boolean isNumericTag(Tag value) {
        byte id = value.getId();
        return id >= Tag.TAG_BYTE && id <= Tag.TAG_DOUBLE;
    }

    private static boolean isStaticCapacityKey(String key) {
        String lower = key.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("capacity") || lower.contains("max");
    }
}
