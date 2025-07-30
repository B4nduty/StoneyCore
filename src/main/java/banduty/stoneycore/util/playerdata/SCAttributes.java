package banduty.stoneycore.util.playerdata;

import banduty.stoneycore.StoneyCore;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;

public class SCAttributes {
    public static final EntityAttribute HUNGER_DRAIN_MULTIPLIER = make("hunger_drain_multiplier", 0.0, -1024.0, 1024.0);
    public static final EntityAttribute STAMINA = make("stamina", 0.0, -1024.0, 1024.0);
    public static final EntityAttribute MAX_STAMINA = make("max_stamina", StoneyCore.getConfig().combatOptions.maxBaseStamina(), -1024.0, 1024.0);

    private static EntityAttribute make(final String name, final double base, final double min, final double max) {
        return new ClampedEntityAttribute("attribute.name.generic." + StoneyCore.MOD_ID + '.' + name, base, min, max).setTracked(true);
    }
}
