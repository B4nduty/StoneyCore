package banduty.stoneycore.entity.custom.siegeentity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public record SiegeProperties(
        String name,
        double maxHealth,
        double movementSpeed,
        double knockbackResistance,
        SoundEvent moveSound,
        SoundEvent reloadSound,
        SoundEvent shootSound,
        SoundEvent attackSound,
        int moveSoundDelay,
        double moveSoundRange,
        double reloadSoundRange,
        double shootSoundRange,
        double attackSoundRange
) {

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.MOVEMENT_SPEED, movementSpeed)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance);
    }

    public static class Builder {
        private final String name;
        private double maxHealth = 50.0;
        private double movementSpeed = 0.05;
        private double knockbackResistance = 265.0;
        private SoundEvent moveSound = SoundEvents.HORSE_STEP_WOOD;
        private SoundEvent reloadSound = SoundEvents.ITEM_BREAK;
        private SoundEvent shootSound = SoundEvents.GENERIC_EXPLODE;
        private SoundEvent attackSound = SoundEvents.GENERIC_EXPLODE;
        private int moveSoundDelay = 150;
        private double moveSoundRange = 30.0;
        private double reloadSoundRange = 15.0;
        private double shootSoundRange = 75.0;
        private double attackSoundRange = 40.0;

        private Builder(String name) { this.name = name; }

        public Builder health(double health) { this.maxHealth = health; return this; }
        public Builder speed(double speed) { this.movementSpeed = speed; return this; }
        public Builder knockbackResist(double resist) { this.knockbackResistance = resist; return this; }
        public Builder moveSound(SoundEvent sound) { this.moveSound = sound; return this; }
        public Builder reloadSound(SoundEvent sound) { this.reloadSound = sound; return this; }
        public Builder shootSound(SoundEvent sound) { this.shootSound = sound; return this; }
        public Builder attackSound(SoundEvent sound) { this.attackSound = sound; return this; }
        public Builder moveSoundDelay(int delay) { this.moveSoundDelay = delay; return this; }
        public Builder moveSoundRange(double range) { this.moveSoundRange = range; return this; }
        public Builder reloadSoundRange(double range) { this.reloadSoundRange = range; return this; }
        public Builder shootSoundRange(double range) { this.shootSoundRange = range; return this; }
        public Builder attackSoundRange(double range) { this.attackSoundRange = range; return this; }

        public SiegeProperties build() {
            return new SiegeProperties(name, maxHealth, movementSpeed, knockbackResistance,
                    moveSound, reloadSound, shootSound, attackSound, moveSoundDelay, moveSoundRange,
                    reloadSoundRange, shootSoundRange, attackSoundRange);
        }
    }
}