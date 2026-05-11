package banduty.stoneycore.client;

import banduty.stoneycore.model.CrownModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class CrownClientExtensions implements IClientItemExtensions {
    private CrownModel model;

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(
            LivingEntity entity,
            ItemStack stack,
            EquipmentSlot slot,
            HumanoidModel<?> defaultModel
    ) {
        if (this.model == null) {
            this.model = new CrownModel(Minecraft.getInstance().getEntityModels()
                    .bakeLayer(CrownModel.LAYER_LOCATION));
        }

        @SuppressWarnings("unchecked")
        HumanoidModel<LivingEntity> humanoid = (HumanoidModel<LivingEntity>) defaultModel;
        humanoid.copyPropertiesTo(this.model);
        return this.model;
    }
}