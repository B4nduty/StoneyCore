package banduty.stoneycore.items.armor.underarmor;

import banduty.stoneycore.client.SCUnderArmourRenderer;
import banduty.stoneycore.items.armor.ISCUnderArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class SCUnderArmor extends ArmorItem implements ISCUnderArmor {
    public SCUnderArmor(Properties settings, ArmorMaterial material, Type type) {
        super(material, type, settings);
    }

    private SCUnderArmourRenderer RENDERER;

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SCUnderArmourRenderer getRenderer() {
                if (RENDERER == null) {
                    RENDERER = new SCUnderArmourRenderer();
                }
                return RENDERER;
            }

            @Override
            public HumanoidModel<?> getGenericArmorModel(
                    LivingEntity entity,
                    ItemStack stack,
                    EquipmentSlot slot,
                    HumanoidModel<?> defaultModel
            ) {
                return getRenderer().getGenericArmorModel(entity, stack, slot, defaultModel);
            }
        });
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String namespace = BuiltInRegistries.ITEM.getKey(this).getNamespace();
        String materialName = this.getMaterial().toString().toLowerCase();

        if (type == null) {
            return namespace + ":textures/models/armor/" + materialName + ".png";
        } else {
            return namespace + ":textures/models/armor/" + materialName + "_" + type + ".png";
        }
    }
}