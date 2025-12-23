package banduty.stoneycore.lands.util;

import net.minecraft.ChatFormatting;

import java.util.*;

public class LandColors {
    private static final List<ChatFormatting> COLOR_POOL = new ArrayList<>();
    private static final Map<UUID, ChatFormatting> ASSIGNED_COLORS = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        for (ChatFormatting fmt : ChatFormatting.values()) {
            if (fmt.isColor()) {
                COLOR_POOL.add(fmt);
            }
        }
    }

    public static ChatFormatting getColorForLand(Land land) {
        return ASSIGNED_COLORS.computeIfAbsent(land.getOwnerUUID(), id -> {
            if (COLOR_POOL.isEmpty()) {
                refill();
            }
            return COLOR_POOL.remove(RANDOM.nextInt(COLOR_POOL.size()));
        });
    }

    private static void refill() {
        COLOR_POOL.clear();
        for (ChatFormatting chatFormatting : ChatFormatting.values()) {
            if (chatFormatting.isColor()) {
                COLOR_POOL.add(chatFormatting);
            }
        }
    }
}