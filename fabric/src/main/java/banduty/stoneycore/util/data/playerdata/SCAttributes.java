package banduty.stoneycore.util.data.playerdata;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class SCAttributes {
    public static final Attribute HUNGER_DRAIN_MULTIPLIER = make("hunger_drain_multiplier", 0.0, -1024.0, 1024.0);
    public static final Attribute STAMINA = make("stamina", 0.0, 0.0, 1024.0);
    public static final Attribute MAX_STAMINA = make("max_stamina", StoneyCore.getConfig().combatOptions().maxBaseStamina(), 0.0, 1024.0);

    private static Attribute make(final String name, final double base, final double min, final double max) {
        return new RangedAttribute("attribute.name.generic." + StoneyCore.MOD_ID + '.' + name, base, min, max).setSyncable(true);
    }

    public static void registerAttributes() {
        Registry.register(BuiltInRegistries.ATTRIBUTE,
                new ResourceLocation(StoneyCore.MOD_ID, "hunger_drain_multiplier"),
                HUNGER_DRAIN_MULTIPLIER);
        Registry.register(BuiltInRegistries.ATTRIBUTE,
                new ResourceLocation(StoneyCore.MOD_ID, "stamina"),
                STAMINA);
        Registry.register(BuiltInRegistries.ATTRIBUTE,
                new ResourceLocation(StoneyCore.MOD_ID, "max_stamina"),
                MAX_STAMINA);

        StoneyCore.LOG.info("Registered custom attributes");
    }
}
