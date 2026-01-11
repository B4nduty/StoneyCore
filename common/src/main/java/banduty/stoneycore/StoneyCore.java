package banduty.stoneycore;

import banduty.stoneycore.combat.range.BowHandler;
import banduty.stoneycore.combat.range.CrossbowHandler;
import banduty.stoneycore.combat.range.MusketHandler;
import banduty.stoneycore.combat.range.RangedWeaponHandlers;
import banduty.stoneycore.config.IConfig;
import banduty.stoneycore.platform.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoneyCore {

    public static final String MOD_ID = "stoneycore";
    public static final String MOD_NAME = "StoneyCore";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        RangedWeaponHandlers.register(new BowHandler());
        RangedWeaponHandlers.register(new CrossbowHandler());
        RangedWeaponHandlers.register(new MusketHandler());

        if (Services.PLATFORM.isModLoaded(StoneyCore.MOD_ID)) {
            LOG.info("Hello to stoneycore");
        }
    }

    public static IConfig getConfig() {
        return Services.PLATFORM.getConfig();
    }
}