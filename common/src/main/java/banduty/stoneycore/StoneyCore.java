package banduty.stoneycore;

import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.combat.range.BowHandler;
import banduty.stoneycore.combat.range.CrossbowHandler;
import banduty.stoneycore.combat.range.MusketHandler;
import banduty.stoneycore.combat.range.RangedWeaponHandlers;
import banduty.stoneycore.config.IConfig;
import banduty.stoneycore.entity.SCEntities;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.particle.SCParticles;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.recipes.SCRecipes;
import banduty.stoneycore.screen.SCScreenHandlers;
import banduty.stoneycore.sounds.SCSounds;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.streq.StrEq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoneyCore {

    public static final String MOD_ID = "stoneycore";
    public static final String MOD_NAME = "StoneyCore";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private static StrEq strEq;

    public static void init() {
        SCBlocks.register();
        SCEntities.register();
        SCItems.register();
        SCParticles.register();
        SCRecipes.register();
        SCScreenHandlers.register();
        SCSounds.register();
        SCAttributes.register();
        SCDataComponents.register();

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

    public static StrEq getStrEq() {
        if (strEq == null) {
            strEq = new StrEq();
        }
        return strEq;
    }
}