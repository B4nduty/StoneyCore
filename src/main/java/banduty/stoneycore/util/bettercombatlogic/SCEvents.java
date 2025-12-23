package banduty.stoneycore.util.bettercombatlogic;

import banduty.stoneycore.event.PlayerAttackHitHandler;
import net.bettercombat.api.client.BetterCombatClientEvents;

public class SCEvents {
    public static void registerEvents() {
        BetterCombatClientEvents.ATTACK_HIT.register(new PlayerAttackHitHandler());
    }
}
