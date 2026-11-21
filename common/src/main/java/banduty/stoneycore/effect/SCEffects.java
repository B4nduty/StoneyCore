package banduty.stoneycore.effect;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.function.Supplier;

public interface SCEffects {
    Holder<MobEffect> WINDED = registerStatusEffect("winded", () -> new WindedEffect(MobEffectCategory.HARMFUL, 0x00FFFF));

    private static Holder<MobEffect> registerStatusEffect(String name, Supplier<MobEffect> effect) {
        return Services.PLATFORM.registerHolder(Registries.MOB_EFFECT, name, effect);
    }

    static void init() {
        StoneyCore.LOG.info("Registering Effects for " + StoneyCore.MOD_ID);
    }
}
