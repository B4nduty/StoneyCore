package banduty.stoneycore.client;

import banduty.stoneycore.model.UnderArmourBootsModel;
import banduty.stoneycore.model.UnderArmourChestplateModel;
import banduty.stoneycore.model.UnderArmourHelmetModel;
import banduty.stoneycore.model.UnderArmourLeggingsModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class SCUnderArmourRenderer implements IClientItemExtensions {
    private static UnderArmourHelmetModel HELMET_MODEL;
    private static UnderArmourChestplateModel CHEST_MODEL;
    private static UnderArmourLeggingsModel LEGGINGS_MODEL;
    private static UnderArmourBootsModel BOOTS_MODEL;

    @Override
    public @NotNull HumanoidModel<?> getGenericArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();

        if (HELMET_MODEL == null) HELMET_MODEL = new UnderArmourHelmetModel(models.bakeLayer(UnderArmourHelmetModel.LAYER_LOCATION));
        if (CHEST_MODEL == null) CHEST_MODEL = new UnderArmourChestplateModel(models.bakeLayer(UnderArmourChestplateModel.LAYER_LOCATION));
        if (LEGGINGS_MODEL == null) LEGGINGS_MODEL = new UnderArmourLeggingsModel(models.bakeLayer(UnderArmourLeggingsModel.LAYER_LOCATION));
        if (BOOTS_MODEL == null) BOOTS_MODEL = new UnderArmourBootsModel(models.bakeLayer(UnderArmourBootsModel.LAYER_LOCATION));

        HumanoidModel<?> model = switch (armorSlot) {
            case HEAD -> HELMET_MODEL;
            case CHEST -> CHEST_MODEL;
            case LEGS -> LEGGINGS_MODEL;
            case FEET -> BOOTS_MODEL;
            default -> _default;
        };

        if (model != null) {
            _default.copyPropertiesTo((HumanoidModel) model);
        }

        return model;
    }
}