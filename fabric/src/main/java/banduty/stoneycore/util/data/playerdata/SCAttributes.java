package banduty.stoneycore.util.data.playerdata;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public interface SCAttributes {
    Attribute HUNGER_DRAIN_MULTIPLIER = registerAttribute("hunger_drain_multiplier", 0.0, -1024.0, 1024.0);
    Attribute STAMINA = registerAttribute("stamina", 0.0, 0.0, 1024.0);
    Attribute MAX_STAMINA = registerAttribute("max_stamina", StoneyCore.getConfig().combatOptions().maxBaseStamina(), 0.0, 1024.0);

    private static Attribute registerAttribute(final String name, final double base, final double min, final double max) {
        return Registry.register(BuiltInRegistries.ATTRIBUTE, new ResourceLocation(StoneyCore.MOD_ID, name),
                new RangedAttribute("attribute.name.generic." + StoneyCore.MOD_ID + '.' + name, base, min, max).setSyncable(true));
    }

    static void registerAttributes() {
        StoneyCore.LOG.info("Registered attributes for " +  StoneyCore.MOD_ID);
    }
}
