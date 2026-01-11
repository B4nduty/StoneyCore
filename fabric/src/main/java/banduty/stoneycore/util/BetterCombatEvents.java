package banduty.stoneycore.util;

import banduty.stoneycore.event.PlayerAttackHitHandler;
import net.bettercombat.api.client.BetterCombatClientEvents;

public class BetterCombatEvents {
    public static void registerEvents() {
        BetterCombatClientEvents.ATTACK_HIT.register(new PlayerAttackHitHandler());
    }
}
