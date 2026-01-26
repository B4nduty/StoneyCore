package banduty.stoneycore.util.data.playerdata;

import net.minecraft.world.entity.ai.attributes.Attribute;

public interface AttributesHelper {
    Attribute getHungerDrainMultiplier();
    Attribute getStamina();
    Attribute getMaxStamina();
    Attribute getDeflectChance();
}
