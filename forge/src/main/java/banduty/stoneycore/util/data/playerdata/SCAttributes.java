package banduty.stoneycore.util.data.playerdata;

import banduty.stoneycore.StoneyCore;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface SCAttributes {
    DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, StoneyCore.MOD_ID);

    RegistryObject<Attribute> HUNGER_DRAIN_MULTIPLIER = ATTRIBUTES.register("hunger_drain_multiplier",
            () -> new RangedAttribute("attribute.name.generic." + StoneyCore.MOD_ID + ".hunger_drain_multiplier",
                    0.0, -1024.0, 1024.0).setSyncable(true));

    RegistryObject<Attribute> STAMINA = ATTRIBUTES.register("stamina",
            () -> new RangedAttribute("attribute.name.generic." + StoneyCore.MOD_ID + ".stamina",
                    0.0, 0.0, 1024.0).setSyncable(true));

    RegistryObject<Attribute> MAX_STAMINA = ATTRIBUTES.register("max_stamina",
            () -> new RangedAttribute("attribute.name.generic." + StoneyCore.MOD_ID + ".max_stamina",
                    StoneyCore.getConfig().combatOptions().maxBaseStamina(), 0.0, 1024.0).setSyncable(true));

    RegistryObject<Attribute> DEFLECT_CHANCE = ATTRIBUTES.register("deflect_chance",
            () -> new RangedAttribute("attribute.name.generic." + StoneyCore.MOD_ID + ".deflect_chance",
                    StoneyCore.getConfig().combatOptions().maxBaseStamina(), 0.0, 1024.0).setSyncable(true));

    static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
        StoneyCore.LOG.info("Registered custom attributes");
    }
}