package banduty.stoneycore.util.data.playerdata;

import net.minecraft.world.entity.ai.attributes.Attribute;

public class FabricAttributesHelper implements AttributesHelper {
    @Override
    public Attribute getHungerDrainMultiplier() {
        return SCAttributes.HUNGER_DRAIN_MULTIPLIER;
    }

    @Override
    public Attribute getStamina() {
        return SCAttributes.STAMINA;
    }

    @Override
    public Attribute getMaxStamina() {
        return SCAttributes.MAX_STAMINA;
    }

    @Override
    public Attribute getDeflectChance() {
        return SCAttributes.DEFLECT_CHANCE;
    }
}
