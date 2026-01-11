package banduty.stoneycore.sounds;

import banduty.stoneycore.StoneyCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface ModSounds {
    DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, StoneyCore.MOD_ID);

    RegistryObject<SoundEvent> BULLET_CRACK = SOUND_EVENTS.register("bullet_crack",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(StoneyCore.MOD_ID, "bullet_crack")));

    static void registerSounds(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
        StoneyCore.LOG.info("Registering Sounds for " + StoneyCore.MOD_ID);
    }
}