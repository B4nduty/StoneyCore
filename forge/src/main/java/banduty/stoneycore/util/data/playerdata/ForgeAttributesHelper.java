package banduty.stoneycore.util.data.playerdata;

import net.minecraft.world.entity.ai.attributes.Attribute;

public class ForgeAttributesHelper implements AttributesHelper {
    @Override
    public Attribute getHungerDrainMultiplier() {
        return SCAttributes.HUNGER_DRAIN_MULTIPLIER.get();
    }

    @Override
    public Attribute getStamina() {
        return SCAttributes.STAMINA.get();
    }

    @Override
    public Attribute getMaxStamina() {
        return SCAttributes.MAX_STAMINA.get();
    }
}
