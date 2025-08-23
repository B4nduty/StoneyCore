package banduty.stoneycore.lands.util;

import net.minecraft.util.Formatting;

import java.util.*;

public class LandColors {
    private static final List<Formatting> COLOR_POOL = new ArrayList<>();
    private static final Map<UUID, Formatting> ASSIGNED_COLORS = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        for (Formatting fmt : Formatting.values()) {
            if (fmt.isColor()) {
                COLOR_POOL.add(fmt);
            }
        }
    }

    public static Formatting getColorForLand(Land land) {
        return ASSIGNED_COLORS.computeIfAbsent(land.getOwnerUUID(), id -> {
            if (COLOR_POOL.isEmpty()) {
                refill();
            }
            return COLOR_POOL.remove(RANDOM.nextInt(COLOR_POOL.size()));
        });
    }

    private static void refill() {
        COLOR_POOL.clear();
        for (Formatting fmt : Formatting.values()) {
            if (fmt.isColor()) {
                COLOR_POOL.add(fmt);
            }
        }
    }
}