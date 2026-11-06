package banduty.stoneycore.entity.siege;

/**
 * Implemented by the client-facing animation layer (GeckoLib lives in KingdomSieges, not here),
 * so StoneyCore can request animations without depending on any animation library.
 */
public interface SiegeAnimatable {
    void triggerSiegeAnimation(SiegeAnimation animation);
}
