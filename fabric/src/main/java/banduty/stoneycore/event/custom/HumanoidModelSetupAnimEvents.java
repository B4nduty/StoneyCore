package banduty.stoneycore.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface HumanoidModelSetupAnimEvents {
    Event<Before> BEFORE = EventFactory.createArrayBacked(
            Before.class,
            listeners -> (model, livingEntity, limbAngle, limbDistance, age, headYaw, headPitch, ci) -> {
                for (Before listener : listeners) {
                    listener.beforeSetAngles(model, livingEntity, limbAngle, limbDistance, age, headYaw, headPitch, ci);
                }
            }
    );

    Event<After> AFTER = EventFactory.createArrayBacked(
            After.class,
            listeners -> (model, livingEntity, limbAngle, limbDistance, age, headYaw, headPitch, ci) -> {
                for (After listener : listeners) {
                    listener.afterSetAngles(model, livingEntity, limbAngle, limbDistance, age, headYaw, headPitch, ci);
                }
            }
    );

    @FunctionalInterface
    interface Before {
        /**
         * Called before the model start.
         */
        void beforeSetAngles(HumanoidModel<?> model, LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci);
    }

    @FunctionalInterface
    interface After {
        /**
         * Called after the model finish.
         */
        void afterSetAngles(HumanoidModel<?> model, LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci);
    }
}