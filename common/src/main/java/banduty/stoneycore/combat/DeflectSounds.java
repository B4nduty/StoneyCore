package banduty.stoneycore.combat;

import banduty.stoneycore.combat.damagetype.SCDamageType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

public final class DeflectSounds {
    private DeflectSounds() {}

    private record Cue(SoundEvent sound, float pitch, float variance) {}

    private static final Cue SLASHING = new Cue(SoundEvents.ANVIL_PLACE, 1.7F, 0.25F);
    private static final Cue BLUDGEONING = new Cue(SoundEvents.WOOL_HIT, 0.9F, 0.2F);
    private static final Cue PIERCING = new Cue(SoundEvents.CHAIN_HIT, 1.5F, 0.3F);
    private static final Cue UNKNOWN = new Cue(SoundEvents.SHIELD_BLOCK, 1.3F, 0.2F);

    private static Cue cue(SCDamageType type) {
        if (type == null) return UNKNOWN;
        return switch (type) {
            case SLASHING -> SLASHING;
            case BLUDGEONING -> BLUDGEONING;
            case PIERCING -> PIERCING;
        };
    }

    public static void play(Entity source, SCDamageType type, float volume) {
        Cue cue = cue(type);
        float pitch = cue.pitch + (source.getRandom().nextFloat() * 2.0F - 1.0F) * cue.variance;
        source.playSound(cue.sound, volume, Math.clamp(pitch, 0.5F, 2.0F));
    }
}