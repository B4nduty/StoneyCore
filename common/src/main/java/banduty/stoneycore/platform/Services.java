package banduty.stoneycore.platform;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.AbstractSiegeHelper;
import banduty.stoneycore.entity.custom.SCBulletEntityHelper;
import banduty.stoneycore.items.blueprint.IBlueprintHelper;
import banduty.stoneycore.items.hotiron.IHotIronHelper;
import banduty.stoneycore.items.manuscript.IManuscriptHelper;
import banduty.stoneycore.items.tongs.ITongsHelper;
import banduty.stoneycore.particle.ParticleHelper;
import banduty.stoneycore.platform.services.IPlatformHelper;
import banduty.stoneycore.util.data.playerdata.AttributesHelper;
import banduty.stoneycore.util.data.playerdata.StaminaHelper;
import banduty.stoneycore.util.render.OutlineClaimRendererHelper;
import banduty.stoneycore.util.servertick.LandTrackerHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IBlueprintHelper BLUEPRINT = load(IBlueprintHelper.class);
    public static final IHotIronHelper HOT_IRON = load(IHotIronHelper.class);
    public static final IManuscriptHelper MANUSCRIPT = load(IManuscriptHelper.class);
    public static final ITongsHelper TONGS = load(ITongsHelper.class);
    public static final AbstractSiegeHelper ABSTRACT_SIEGE_ENTITY = load(AbstractSiegeHelper.class);
    public static final AttributesHelper ATTRIBUTES = load(AttributesHelper.class);
    public static final StaminaHelper STAMINA = load(StaminaHelper.class);
    public static final OutlineClaimRendererHelper OUTLINE_CLAIM_RENDERER = load(OutlineClaimRendererHelper.class);
    public static final LandTrackerHelper LAND_TRACKER = load(LandTrackerHelper.class);
    public static final ParticleHelper PARTICLE = load(ParticleHelper.class);
    public static final SCBulletEntityHelper SC_BULLET_ENTITY = load(SCBulletEntityHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        StoneyCore.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}