package banduty.stoneycore.sounds;

import banduty.stoneycore.StoneyCore;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(StoneyCore.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> BULLET_CRACK = SOUND_EVENTS.register("bullet_crack",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(StoneyCore.MOD_ID, "bullet_crack")));

    public static void registerSounds() {
        SOUND_EVENTS.register();
        StoneyCore.LOG.info("Registering Sounds for " + StoneyCore.MOD_ID);
    }
}