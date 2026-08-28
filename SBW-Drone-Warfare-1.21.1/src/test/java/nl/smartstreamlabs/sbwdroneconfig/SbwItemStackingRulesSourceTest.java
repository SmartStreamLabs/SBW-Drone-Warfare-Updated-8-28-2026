package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SbwItemStackingRulesSourceTest {
    private static final String[] STACK_SIZE_OVERRIDE_IDS = {
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
    };

    @Test
    void sbwStackingRulesWhitelistStackSizeOverridesAndKeepDamagedStacksSafe() throws Exception {
        String rules = readMainSource("SbwItemStackingRules.java");

        for (String id : STACK_SIZE_OVERRIDE_IDS) {
            assertTrue(rules.contains("\"" + id + "\""), "Missing stack override for superbwarfare:" + id);
        }
        assertTrue(rules.contains("private static final int STACK_SIZE = 64"));
        assertTrue(rules.contains("stack.isDamaged()"));
        assertTrue(rules.contains("return 1;"), "Damaged variants must stay unstackable so item data is not corrupted.");
        assertTrue(rules.contains("superbwarfare"), "The override must stay scoped to Superb Warfare items only.");
    }

    @Test
    void itemStackMixinRoutesMaxStackSizeThroughSafeRules() throws Exception {
        String mixin = readMixinSource("ItemStackMaxStackSizeMixin.java");
        String mixinConfig = Files.readString(Path.of("src", "main", "resources", "sbwdroneconfig.mixins.json"), StandardCharsets.UTF_8);

        assertTrue(mixin.contains("@Mixin(ItemStack.class)"));
        assertTrue(mixin.contains("getMaxStackSize"));
        assertTrue(mixin.contains("SbwItemStackingRules.getMaxStackSize"));
        assertTrue(mixin.contains("cancellable = true"));
        assertTrue(mixinConfig.contains("\"ItemStackMaxStackSizeMixin\""));
    }

    @Test
    void itemStackMergeMixinAllowsOnlySafeStackableSbwDataToMerge() throws Exception {
        String rules = readMainSource("SbwItemStackingRules.java");
        String mixin = readMixinSource("ItemStackMergeMixin.java");
        String mixinConfig = Files.readString(Path.of("src", "main", "resources", "sbwdroneconfig.mixins.json"), StandardCharsets.UTF_8);

        assertTrue(rules.contains("canMergeStackableSbwItems"));
        assertTrue(rules.contains("normalizedTagForStacking"));
        assertTrue(rules.contains("BlockEntityTag"));
        assertTrue(rules.contains("BlockStateTag"));
        assertTrue(rules.contains("Energy"));
        assertTrue(rules.contains("isHarmlessBlockEntityTag"));
        assertTrue(rules.contains("stack.getItem().getMaxStackSize(stack) > 1"));
        assertTrue(mixin.contains("@Mixin(ItemStack.class)"));
        assertTrue(mixin.contains("isSameItemSameTags"));
        assertTrue(mixin.contains("m_150942_"));
        assertTrue(mixin.contains("SbwItemStackingRules.canMergeStackableSbwItems"));
        assertTrue(mixin.contains("cancellable = true"));
        assertTrue(mixinConfig.contains("\"ItemStackMergeMixin\""));
    }

    private static String readMainSource(String fileName) throws Exception {
        return Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", fileName), StandardCharsets.UTF_8);
    }

    private static String readMixinSource(String fileName) throws Exception {
        return Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "mixin", fileName), StandardCharsets.UTF_8);
    }
}
