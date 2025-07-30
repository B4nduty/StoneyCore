package banduty.stoneycore.sounds;

import banduty.stoneycore.StoneyCore;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(StoneyCore.MOD_ID, RegistryKeys.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> BULLET_CRACK = SOUND_EVENTS.register("bullet_crack",
            () -> SoundEvent.of(new Identifier(StoneyCore.MOD_ID, "bullet_crack")));

    public static void registerSounds() {
        SOUND_EVENTS.register();
        StoneyCore.LOGGER.info("Registering Sounds for " + StoneyCore.MOD_ID);
    }
}