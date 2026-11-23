package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import java.util.List;

/** A consumer of SBW's existing item energy storage, not a new battery type. */
public final class Dj01Item extends Item {
    public static final int CAPACITY=100000;
    private static final String ACTIVE="DJ01Emitting";
    public Dj01Item(Properties properties){super(properties);}
    @Override public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack,CompoundTag tag){
        return new com.atsuishio.superbwarfare.capability.energy.ItemEnergyProvider(stack,CAPACITY);
    }
    public static IEnergyStorage storage(ItemStack stack){return stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);}
    public static int energy(ItemStack stack){var e=storage(stack);return e==null?0:e.getEnergyStored();}
    public static boolean is(ItemStack stack){return stack.getItem() instanceof Dj01Item;}
    public static boolean emitting(ItemStack stack){var t=stack.getTag();return t!=null&&t.getBoolean(ACTIVE);}
    public static void emitting(ItemStack stack,boolean active){if(emitting(stack)!=active)stack.getOrCreateTag().putBoolean(ACTIVE,active);}
    public static ItemStack fullStack(){var s=new ItemStack(AddonItems.DJ01.get());var e=storage(s);if(e!=null)e.receiveEnergy(CAPACITY,false);return s;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);
        if(hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.fail(stack);
        if(player.isShiftKeyDown()){
            if(!level.isClientSide){
                var battery=player.getOffhandItem();var source=storage(battery);var dest=storage(stack);int moved=0;
                if(SbwCompat.isSbwBattery(battery)&&source!=null&&dest!=null){int accepted=dest.receiveEnergy(source.extractEnergy(AddonConfig.batteryTransferRate(),true),true);int extracted=source.extractEnergy(accepted,false);moved=dest.receiveEnergy(extracted,false);if(moved<extracted)source.receiveEnergy(extracted-moved,false);}
                player.displayClientMessage(Component.translatable(moved>0?"dj01.charged":"dj01.charge_help",energy(stack),CAPACITY),true);
                player.getCooldowns().addCooldown(this,10);player.getInventory().setChanged();
            }
            return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
        }
        if(!level.isClientSide){
            String reason=Dj01System.cannotUse(player,stack);
            if(reason!=null){emitting(stack,false);player.displayClientMessage(Component.translatable(reason),true);player.getCooldowns().addCooldown(this,20);return InteractionResultHolder.fail(stack);}
        }
        player.startUsingItem(hand);return InteractionResultHolder.consume(stack);
    }
    @Override public int getUseDuration(ItemStack stack){return 72000;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.NONE;}
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining){if(!level.isClientSide)emitting(stack,false);}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){if(!level.isClientSide&&(!(entity instanceof Player p)||!p.isUsingItem()||p.getUseItem()!=stack||p.getUsedItemHand()!=InteractionHand.MAIN_HAND))emitting(stack,false);}
    @Override public boolean isBarVisible(ItemStack stack){return true;}
    @Override public int getBarWidth(ItemStack stack){return Math.min(13,Math.round(13f*energy(stack)/CAPACITY));}
    @Override public int getBarColor(ItemStack stack){return energy(stack)>0?0x72c8a1:0xd76748;}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){
        lines.add(Component.translatable("dj01.tooltip").withStyle(ChatFormatting.GREEN));
        lines.add(Component.translatable("dj01.charge_tip").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("dj01.energy",energy(stack),CAPACITY).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("dj01.friendly_tip").withStyle(ChatFormatting.GRAY));
    }
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            private Dj01ItemRenderer renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=Dj01ItemRenderer.instance();return renderer;}
            @Override public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(LivingEntity entity,InteractionHand hand,ItemStack stack){if(hand!=InteractionHand.MAIN_HAND)return null;Dj01Pose.capture(entity);return Dj01Pose.firstPersonHand?net.minecraft.client.model.HumanoidModel.ArmPose.EMPTY:net.minecraft.client.model.HumanoidModel.ArmPose.CROSSBOW_HOLD;}
        });
    }
}
