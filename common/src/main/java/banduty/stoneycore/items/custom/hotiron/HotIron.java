package banduty.stoneycore.items.custom.hotiron;

import net.minecraft.world.item.Item;

public class HotIron extends Item implements QuenchItem {
    private static final int IGNITE_DURATION_TICKS = 20 * 30;

    private final boolean destroysOnQuench;

    public HotIron(Properties properties) {
        this(properties, true);
    }

    public HotIron(Properties properties, boolean destroysOnQuench) {
        super(properties);
        this.destroysOnQuench = destroysOnQuench;
    }

    @Override
    public int getIgniteDuration() {
        return IGNITE_DURATION_TICKS;
    }

    @Override
    public boolean destroysOnQuench() {
        return destroysOnQuench;
    }
}