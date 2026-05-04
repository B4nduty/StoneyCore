package banduty.stoneycore.util.data.entitydata;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public interface SCAttributes {
    Holder<Attribute> HUNGER_DRAIN_MULTIPLIER = register("hunger_drain_multiplier", 0.0, -1024.0, 1024.0);
    Holder<Attribute> STAMINA = register("stamina", 0.0, 0.0, 1024.0);
    Holder<Attribute> MAX_STAMINA = register("max_stamina", StoneyCore.getConfig().combatOptions().maxBaseStamina(), 0.0, 1024.0);
    Holder<Attribute> DEFLECT_CHANCE = register("deflect_chance", 0.0, -1024.0, 1024.0);

    private static Holder<Attribute> register(String name, double base, double min, double max) {
        return Services.PLATFORM.registerHolder(Registries.ATTRIBUTE, name,
                () -> new RangedAttribute("attribute.name.generic." + StoneyCore.MOD_ID + '.' + name, base, min, max).setSyncable(true)
        );
    }


    static void register() {
        StoneyCore.LOG.info("Registered attributes for " +  StoneyCore.MOD_ID);
    }
}
