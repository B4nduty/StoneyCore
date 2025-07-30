package banduty.stoneycore.event.custom;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface BipedEntityModelAnglesEvents {
    Event<Before> BEFORE = EventFactory.createArrayBacked(
            Before.class,
            listeners -> (model, livingEntity, abstractSiegeEntity, limbAngle, limbDistance, age, headYaw, headPitch, ci) -> {
                for (Before listener : listeners) {
                    listener.beforeSetAngles(model, livingEntity, abstractSiegeEntity, limbAngle, limbDistance, age, headYaw, headPitch, ci);
                }
            }
    );

    Event<After> AFTER = EventFactory.createArrayBacked(
            After.class,
            listeners -> (model, livingEntity, abstractSiegeEntity, limbAngle, limbDistance, age, headYaw, headPitch, ci) -> {
                for (BipedEntityModelAnglesEvents.After listener : listeners) {
                    listener.afterSetAngles(model, livingEntity, abstractSiegeEntity, limbAngle, limbDistance, age, headYaw, headPitch, ci);
                }
            }
    );

    @FunctionalInterface
    interface Before {
        /**
         * Called before the model start.
         */
        void beforeSetAngles(BipedEntityModel<?> model, LivingEntity entity, AbstractSiegeEntity abstractSiegeEntity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci);
    }

    @FunctionalInterface
    interface After {
        /**
         * Called after the model finish.
         */
        void afterSetAngles(BipedEntityModel<?> model, LivingEntity entity, AbstractSiegeEntity abstractSiegeEntity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci);
    }
}