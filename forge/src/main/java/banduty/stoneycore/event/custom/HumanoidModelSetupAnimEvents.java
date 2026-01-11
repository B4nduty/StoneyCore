package banduty.stoneycore.event.custom;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class HumanoidModelSetupAnimEvents {
    @Cancelable
    public static class Before extends Event {
        private final HumanoidModel<?> model;
        private final LivingEntity entity;
        private final float limbAngle;
        private final float limbDistance;
        private final float age;
        private final float headYaw;
        private final float headPitch;
        private final CallbackInfo ci;

        public Before(HumanoidModel<?> model, LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
            this.model = model;
            this.entity = entity;
            this.limbAngle = limbAngle;
            this.limbDistance = limbDistance;
            this.age = age;
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.ci = ci;
        }

        public HumanoidModel<?> getModel() {
            return model;
        }

        public LivingEntity getEntity() {
            return entity;
        }

        public float getLimbAngle() {
            return limbAngle;
        }

        public float getLimbDistance() {
            return limbDistance;
        }

        public float getAge() {
            return age;
        }

        public float getHeadYaw() {
            return headYaw;
        }

        public float getHeadPitch() {
            return headPitch;
        }

        public CallbackInfo getCi() {
            return ci;
        }
    }

    @Cancelable
    public static class After extends Event {
        private final HumanoidModel<?> model;
        private final LivingEntity entity;
        private final float limbAngle;
        private final float limbDistance;
        private final float age;
        private final float headYaw;
        private final float headPitch;
        private final CallbackInfo ci;

        public After(HumanoidModel<?> model, LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
            this.model = model;
            this.entity = entity;
            this.limbAngle = limbAngle;
            this.limbDistance = limbDistance;
            this.age = age;
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.ci = ci;
        }

        public HumanoidModel<?> getModel() {
            return model;
        }

        public LivingEntity getEntity() {
            return entity;
        }

        public float getLimbAngle() {
            return limbAngle;
        }

        public float getLimbDistance() {
            return limbDistance;
        }

        public float getAge() {
            return age;
        }

        public float getHeadYaw() {
            return headYaw;
        }

        public float getHeadPitch() {
            return headPitch;
        }

        public CallbackInfo getCi() {
            return ci;
        }
    }
}