package banduty.stoneycore.sounds;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public interface ModSounds {
    SoundEvent BULLET_CRACK = registerSound("bullet_crack",
            SoundEvent.createVariableRangeEvent(new ResourceLocation(StoneyCore.MOD_ID, "bullet_crack")));

    private static SoundEvent registerSound(String name, SoundEvent soundEvent) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, new ResourceLocation(StoneyCore.MOD_ID, name), soundEvent);
    }

    static void registerSounds() {
        StoneyCore.LOG.info("Registering Sounds for " + StoneyCore.MOD_ID);
    }
}