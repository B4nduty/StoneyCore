package banduty.stoneycore.util.servertick;

import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class SwallowTailArrowUtil {
    private static final Map<LivingEntity, Integer> DAMAGE_TICK_MAP = Collections.synchronizedMap(new WeakHashMap<>());

    public static void startSwallowTailTickTrack(Player player) {
        int swallowTailArrowCount = NBTDataHelper.get((IEntityDataSaver) player, PDKeys.SWALLOWTAIL_ARROW_COUNT, 0);

        if (swallowTailArrowCount >= 1 && !player.isCreative()) {
            int damageTick = DAMAGE_TICK_MAP.getOrDefault(player, 0);
            damageTick++;
            DAMAGE_TICK_MAP.put(player, damageTick);

            if (damageTick % 20 == 0 && (player.isSprinting() || player.getDeltaMovement().horizontalDistanceSqr() > 0)) {
                player.hurt(player.damageSources().genericKill(), 0.2f);
                DAMAGE_TICK_MAP.put(player, 0);
            }
        }
    }

}
