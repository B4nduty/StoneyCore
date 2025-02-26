package banduty.stoneycore.sounds;

import banduty.stoneycore.StoneyCore;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent BULLET_CRACK = registerSoundEvent("bullet_crack");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(StoneyCore.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        StoneyCore.LOGGER.info("Registering Sounds for " + StoneyCore.MOD_ID);
    }
}