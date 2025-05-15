package banduty.stoneycore.util;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;

public class ModifiersHelper {
    public static void updateModifier(EntityAttributeInstance attribute, EntityAttributeModifier modifier) {
        if (attribute == null) return;

        EntityAttributeModifier existingModifier = attribute.getModifier(modifier.getId());
        if (existingModifier == null || existingModifier.getValue() != modifier.getValue()) {
            attribute.removeModifier(modifier.getId());
            attribute.addTemporaryModifier(new EntityAttributeModifier(
                    modifier.getId(),
                    modifier.getName(),
                    modifier.getValue(),
                    modifier.getOperation()
            ));
        }
    }
}
