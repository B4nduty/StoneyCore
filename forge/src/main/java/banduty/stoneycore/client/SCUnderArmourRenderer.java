package banduty.stoneycore.client;

import banduty.stoneycore.model.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class SCUnderArmourRenderer implements IClientItemExtensions {

    private HumanoidModel<LivingEntity> helmet;
    private HumanoidModel<LivingEntity> chest;
    private HumanoidModel<LivingEntity> legs;
    private HumanoidModel<LivingEntity> boots;

    @Override
    public @NotNull HumanoidModel<?> getGenericArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
        if (helmet == null) {
            EntityModelSet models = Minecraft.getInstance().getEntityModels();
            helmet = new UnderArmourHelmetModel(models.bakeLayer(UnderArmourHelmetModel.LAYER_LOCATION));
            chest = new UnderArmourChestplateModel(models.bakeLayer(UnderArmourChestplateModel.LAYER_LOCATION));
            legs = new UnderArmourLeggingsModel(models.bakeLayer(UnderArmourLeggingsModel.LAYER_LOCATION));
            boots = new UnderArmourBootsModel(models.bakeLayer(UnderArmourBootsModel.LAYER_LOCATION));
        }

        HumanoidModel<LivingEntity> model = switch (armorSlot) {
            case FEET -> boots;
            case LEGS -> legs;
            case CHEST -> chest;
            case HEAD -> helmet;
            default -> null;
        };

        if (model != null) {
            _default.copyPropertiesTo((HumanoidModel) model);
        }

        return model;
    }
}