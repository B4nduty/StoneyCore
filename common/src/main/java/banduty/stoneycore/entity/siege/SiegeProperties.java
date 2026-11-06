package banduty.stoneycore.entity.siege;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Single code-side definition of an engine: attributes, sounds and timings.
 * Tunable balance values (damage, reload time, speeds, accuracy, damage rules) stay in the datapack
 * ({@code SiegeEngineDefinitionsStorage}).
 *
 * @param cooldownTicks  lock time after an engine starts firing / attacking
 * @param fireDelayTicks ticks between the trigger and the moment the projectile / hit actually happens
 */
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
        double attackSoundRange,
        int cooldownTicks,
        int fireDelayTicks
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

    /** The cooldown value at which the "fire" moment happens (cooldown counts down). */
    public int fireTriggerCooldown() {
        return Math.max(0, cooldownTicks - fireDelayTicks);
    }

    public static class Builder {
        private final String name;
        private double maxHealth = 50.0;
        private double movementSpeed = 0.05;
        private double knockbackResistance = 265.0;
        private SoundEvent moveSound = SoundEvents.HORSE_STEP_WOOD;
        private SoundEvent reloadSound = SoundEvents.ITEM_BREAK;
        private SoundEvent shootSound = SoundEvents.GENERIC_EXPLODE.value();
        private SoundEvent attackSound = SoundEvents.GENERIC_EXPLODE.value();
        private int moveSoundDelay = 150;
        private double moveSoundRange = 30.0;
        private double reloadSoundRange = 15.0;
        private double shootSoundRange = 75.0;
        private double attackSoundRange = 40.0;
        private int cooldownTicks = 0;
        private int fireDelayTicks = 0;

        private Builder(String name) { this.name = name; }

        public Builder health(double v) { this.maxHealth = v; return this; }
        public Builder speed(double v) { this.movementSpeed = v; return this; }
        public Builder knockbackResist(double v) { this.knockbackResistance = v; return this; }
        public Builder moveSound(SoundEvent v) { this.moveSound = v; return this; }
        public Builder reloadSound(SoundEvent v) { this.reloadSound = v; return this; }
        public Builder shootSound(SoundEvent v) { this.shootSound = v; return this; }
        public Builder attackSound(SoundEvent v) { this.attackSound = v; return this; }
        public Builder moveSoundDelay(int v) { this.moveSoundDelay = v; return this; }
        public Builder moveSoundRange(double v) { this.moveSoundRange = v; return this; }
        public Builder reloadSoundRange(double v) { this.reloadSoundRange = v; return this; }
        public Builder shootSoundRange(double v) { this.shootSoundRange = v; return this; }
        public Builder attackSoundRange(double v) { this.attackSoundRange = v; return this; }
        public Builder cooldown(int ticks) { this.cooldownTicks = ticks; return this; }
        public Builder fireDelay(int ticks) { this.fireDelayTicks = ticks; return this; }

        public SiegeProperties build() {
            return new SiegeProperties(name, maxHealth, movementSpeed, knockbackResistance,
                    moveSound, reloadSound, shootSound, attackSound, moveSoundDelay, moveSoundRange,
                    reloadSoundRange, shootSoundRange, attackSoundRange, cooldownTicks, fireDelayTicks);
        }
    }
}
