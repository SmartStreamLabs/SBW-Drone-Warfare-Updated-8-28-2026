package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

final class FpvGogglesClientExtensions implements IClientItemExtensions {
    static final FpvGogglesClientExtensions INSTANCE = new FpvGogglesClientExtensions();
    private FpvGogglesArmorModel model;

    private FpvGogglesClientExtensions() {
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        if (equipmentSlot != EquipmentSlot.HEAD) {
            return original;
        }
        if (model == null) {
            model = FpvGogglesArmorModel.create();
        }
        return model;
    }
}
