package banduty.stoneycore.entity.siege;

/**
 * One-shot animations an engine can trigger. Continuous states (loaded / unloaded / reloading / picked)
 * are NOT triggered: the renderer derives them from synced entity data every frame.
 */
public enum SiegeAnimation {
    FIRE("fire"),
    ATTACK("attack");

    private final String id;

    SiegeAnimation(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
