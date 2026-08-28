package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Objects;
import java.util.Set;

public final class SbwItemStackingRules {
    private static final String SBW_NAMESPACE = "superbwarfare";
    private static final int STACK_SIZE = 64;
    private static final Set<String> STACKABLE_ITEM_IDS = Set.of(
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
            "neoforge:attachments"
    );
    private static final Set<DataComponentType<?>> IGNORED_STACKING_COMPONENTS = Set.of(
            DataComponents.BLOCK_ENTITY_DATA,
            DataComponents.BLOCK_STATE
    );

    private SbwItemStackingRules() {
    }

    public static int getMaxStackSize(ItemStack stack) {
        if (!isSupportedStackableStack(stack)) {
            return -1;
        }

        if (stack.isDamaged()) {
            return 1;
        }

        return STACK_SIZE;
    }

    public static boolean canMergeStackableSbwItems(ItemStack first, ItemStack second) {
        if (!isSupportedStackableStack(first) || !isSupportedStackableStack(second)) {
            return false;
        }
        if (first.isDamaged() || second.isDamaged()) {
            return false;
        }

        ResourceLocation firstId = BuiltInRegistries.ITEM.getKey(first.getItem());
        ResourceLocation secondId = BuiltInRegistries.ITEM.getKey(second.getItem());
        if (!firstId.equals(secondId)) {
            return false;
        }

        if (!Objects.equals(normalizedComponentsForStacking(first), normalizedComponentsForStacking(second))) {
            return false;
        }

        return Objects.equals(normalizedTagForStacking(first), normalizedTagForStacking(second));
    }

    private static boolean isSupportedStackableStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return SBW_NAMESPACE.equals(itemId.getNamespace()) && STACKABLE_ITEM_IDS.contains(itemId.getPath());
    }

    private static DataComponentMap normalizedComponentsForStacking(ItemStack stack) {
        return stack.getComponents().filter(componentType -> !IGNORED_STACKING_COMPONENTS.contains(componentType));
    }

    private static CompoundTag normalizedTagForStacking(ItemStack stack) {
        CustomData blockEntityData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        CompoundTag tag = blockEntityData.copyTag();
        if (tag.isEmpty() || isHarmlessBlockEntityTag(tag)) {
            return null;
        }
        return tag;
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
