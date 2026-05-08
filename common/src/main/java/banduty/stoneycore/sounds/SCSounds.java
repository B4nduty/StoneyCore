package banduty.stoneycore.sounds;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public interface SCSounds {
    Supplier<SoundEvent> BULLET_CRACK = registerSound("bullet_crack");
    Supplier<SoundEvent> VISOR_OPEN = registerSound("visor_open");
    Supplier<SoundEvent> VISOR_CLOSE = registerSound("visor_close");

    private static Supplier<SoundEvent> registerSound(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, name);

        return Services.PLATFORM.register(BuiltInRegistries.SOUND_EVENT, name,
                () -> SoundEvent.createVariableRangeEvent(id));
    }

    static void register() {
        StoneyCore.LOG.info("Registering Sounds for " + StoneyCore.MOD_ID);
    }
}