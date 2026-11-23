package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class DroneJammerItem extends Item {
    public static final String TAG_ACTIVE = "sbwdroneconfigJammerActive";

    public DroneJammerItem(Properties properties) {
        super(properties);
    }

    @Override public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {if(renderer==null)renderer=new Pd01ItemRenderer();return renderer;}
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.drone_jammer.1").withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.drone_jammer.2").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.drone_jammer.3").withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.drone_jammer.4").withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.drone_jammer.5").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.drone_jammer.6").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.drone_jammer.7").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.sbwdroneconfig.drone_jammer.8").withStyle(ChatFormatting.DARK_AQUA));
        tooltipComponents.add(
                Component.translatable(
                        isActive(stack)
                                ? "tooltip.sbwdroneconfig.drone_jammer.state_on"
                                : "tooltip.sbwdroneconfig.drone_jammer.state_off"
                ).withStyle(isActive(stack) ? ChatFormatting.GREEN : ChatFormatting.RED)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isCrouching()) {
            if (!isActive(stack)) {
                setActive(stack, true);
            }
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                AddonNetwork.openPortableFpvDetector(serverPlayer, new PortableFpvDetectorOpenMessage());
                PortableFpvDetectorServer.sendScan(serverPlayer);
                player.displayClientMessage(Component.translatable("message.sbwdroneconfig.fpv_detector_on").withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        boolean active = !isActive(stack);
        setActive(stack, active);

        if (!level.isClientSide()) {
            player.displayClientMessage(
                    Component.translatable(
                            active
                                    ? "message.sbwdroneconfig.fpv_detector_on"
                                    : "message.sbwdroneconfig.fpv_detector_off"
                    ).withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED),
                    true
            );
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    active ? SoundEvents.NOTE_BLOCK_CHIME.value() : SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.PLAYERS,
                    0.25F,
                    active ? 1.65F : 0.75F
            );
            if (!active && player instanceof ServerPlayer serverPlayer) {
                AddonNetwork.sendPortableFpvDetectorScan(serverPlayer, PortableFpvDetectorScanMessage.empty(false));
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static boolean isActive(ItemStack stack) {
        return !stack.isEmpty() && stack.is(AddonItems.DRONE_JAMMER.get()) && AddonStackTags.get(stack).getBoolean(TAG_ACTIVE);
    }

    public static ItemStack findActiveJammer(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (isActive(mainHand)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (isActive(offHand)) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    public static boolean isActiveDetectorCarried(Player player) {
        if(player==null||!player.isAlive())return false;
        for(int slot=0;slot<9;slot++)if(isActive(player.getInventory().getItem(slot)))return true;
        return isActive(player.getOffhandItem());
    }

    public static boolean isActiveJammerHeld(Player player) {
        if (player == null) {
            return false;
        }
        return isActive(player.getMainHandItem()) || isActive(player.getOffhandItem());
    }

    public static boolean isDetector(ItemStack stack) {
        return !stack.isEmpty() && stack.is(AddonItems.DRONE_JAMMER.get());
    }

    public static void setActive(ItemStack stack, boolean active) {
        if (stack.isEmpty() || !stack.is(AddonItems.DRONE_JAMMER.get())) {
            return;
        }
        AddonStackTags.update(stack, tag -> tag.putBoolean(TAG_ACTIVE, active));
    }
}
