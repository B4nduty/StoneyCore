package banduty.stoneycore.sounds;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public interface ModSounds {
    SoundEvent BULLET_CRACK = registerSound("bullet_crack");
    SoundEvent VISOR_OPEN = registerSound("visor_open");
    SoundEvent VISOR_CLOSE = registerSound("visor_close");

    private static SoundEvent registerSound(String name) {
        ResourceLocation id = new ResourceLocation(StoneyCore.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    static void registerSounds() {
        StoneyCore.LOG.info("Registering Sounds for " + StoneyCore.MOD_ID);
    }
}