package banduty.stoneycore.event.custom;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface TrinketsModifiersEvents {
    Event<TrinketsModifiersEvents> EVENT = EventFactory.createArrayBacked(
            TrinketsModifiersEvents.class,
            listeners -> (modifiers, stack, slot, entity, uuid) -> {
                for (TrinketsModifiersEvents listener : listeners) {
                    modifiers = listener.getModifiers(modifiers, stack, slot, entity, uuid);
                }
                return modifiers;
            }
    );

    Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(Multimap<EntityAttribute, EntityAttributeModifier> modifiers,
                                                                    ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid);
}