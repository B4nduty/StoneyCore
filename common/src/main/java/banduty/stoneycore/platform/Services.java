package banduty.stoneycore.platform;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.melee.ICombatHelper;
import banduty.stoneycore.entity.custom.AbstractSiegeHelper;
import banduty.stoneycore.items.custom.blueprint.IBlueprintHelper;
import banduty.stoneycore.platform.services.IPlatformHelper;
import banduty.stoneycore.util.data.entitydata.StaminaHelper;
import banduty.stoneycore.util.render.OutlineClaimRendererHelper;
import banduty.stoneycore.util.servertick.LandTrackerHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IBlueprintHelper BLUEPRINT = load(IBlueprintHelper.class);
    public static final AbstractSiegeHelper ABSTRACT_SIEGE_ENTITY = load(AbstractSiegeHelper.class);
    public static final StaminaHelper STAMINA = load(StaminaHelper.class);
    public static final OutlineClaimRendererHelper OUTLINE_CLAIM_RENDERER = load(OutlineClaimRendererHelper.class);
    public static final LandTrackerHelper LAND_TRACKER = load(LandTrackerHelper.class);
    public static final ICombatHelper COMBAT = ServiceLoaderHelper.loadHighestPriority(ICombatHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        StoneyCore.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}