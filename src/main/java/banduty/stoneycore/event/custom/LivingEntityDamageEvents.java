package banduty.stoneycore.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public interface LivingEntityDamageEvents {
    Event<LivingEntityDamageEvents> EVENT = EventFactory.createArrayBacked(
            LivingEntityDamageEvents.class,
            listeners -> (livingEntity, source, amount) -> {
                for (LivingEntityDamageEvents listener : listeners) {
                    amount = listener.onDamage(livingEntity, source, amount);
                }
                return amount;
            }
    );

    float onDamage(LivingEntity target, DamageSource source, float amount);
}