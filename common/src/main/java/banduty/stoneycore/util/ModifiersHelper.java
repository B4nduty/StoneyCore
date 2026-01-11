package banduty.stoneycore.util;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ModifiersHelper {
    public static void updateModifier(AttributeInstance attribute, AttributeModifier modifier) {
        if (attribute == null) return;

        AttributeModifier existingModifier = attribute.getModifier(modifier.getId());
        if (existingModifier == null || existingModifier.getAmount() != modifier.getAmount()) {
            attribute.removeModifier(modifier.getId());
            attribute.addTransientModifier(new AttributeModifier(
                    modifier.getId(),
                    modifier.getName(),
                    modifier.getAmount(),
                    modifier.getOperation()
            ));
        }
    }
}
