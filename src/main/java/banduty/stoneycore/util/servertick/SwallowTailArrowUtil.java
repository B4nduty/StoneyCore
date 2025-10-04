package banduty.stoneycore.util.servertick;

import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class SwallowTailArrowUtil {
    private static final Map<LivingEntity, Integer> DAMAGE_TICK_MAP = Collections.synchronizedMap(new WeakHashMap<>());

    public static void startSwallowTailTickTrack(PlayerEntity playerEntity) {
        int swallowTailArrowCount = NBTDataHelper.get((IEntityDataSaver) playerEntity, PDKeys.SWALLOWTAIL_ARROW_COUNT, 0);

        if (swallowTailArrowCount >= 1 && !playerEntity.isCreative()) {
            int damageTick = DAMAGE_TICK_MAP.getOrDefault(playerEntity, 0);
            damageTick++;
            DAMAGE_TICK_MAP.put(playerEntity, damageTick);

            if (damageTick % 20 == 0 && (playerEntity.isSprinting() || playerEntity.getVelocity().horizontalLengthSquared() > 0)) {
                playerEntity.damage(playerEntity.getDamageSources().genericKill(), 0.2f);
                DAMAGE_TICK_MAP.put(playerEntity, 0);
            }
        }
    }

}
