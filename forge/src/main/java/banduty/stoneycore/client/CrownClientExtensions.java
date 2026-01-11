package banduty.stoneycore.client;

import banduty.stoneycore.model.CrownModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class CrownClientExtensions implements IClientItemExtensions {

    private static final CrownModel MODEL =
            new CrownModel(CrownModel.getTexturedModelData().bakeRoot());

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(
            LivingEntity entity,
            ItemStack stack,
            EquipmentSlot slot,
            HumanoidModel<?> defaultModel
    ) {
        @SuppressWarnings("unchecked")
        HumanoidModel<LivingEntity> humanoid = (HumanoidModel<LivingEntity>) defaultModel;
        humanoid.copyPropertiesTo(MODEL);
        return MODEL;
    }
}