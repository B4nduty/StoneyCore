package banduty.stoneycore.items.armor.underarmor;

import banduty.stoneycore.client.SCUnderArmourRenderer;
import banduty.stoneycore.items.armor.ISCUnderArmor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class SCUnderArmor extends ArmorItem implements ISCUnderArmor {
    public SCUnderArmor(Properties settings, ArmorMaterial material, Type type) {
        super(material, type, settings);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new SCUnderArmourRenderer());
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String materialName = this.getMaterial().toString().toLowerCase();

        if (type == null) {
            return "stoneycore:textures/models/armor/" + materialName + ".png";
        } else {
            return "stoneycore:textures/models/armor/" + materialName + "_" + type + ".png";
        }
    }
}