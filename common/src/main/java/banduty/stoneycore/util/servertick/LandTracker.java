package banduty.stoneycore.util.servertick;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.*;

public class LandTracker {
    private static final Map<UUID, BlockPos> lastLandCore = new HashMap<>();
    private static final Map<UUID, BlockPos> lastPlayerPos = new HashMap<>();

    public static void trackPlayerLandMovement(ServerPlayer player) {
        BlockPos currentPos = player.getOnPos();
        ServerLevel serverLevel = player.serverLevel();
        LandState landState = LandState.get(serverLevel);
        Optional<Land> optionalLand = landState.getLandAt(currentPos);

        player.serverLevel().players().forEach(otherPlayer -> {
            BlockPos blockPos = BlockPos.ZERO;
            if (optionalLand.isPresent()) blockPos = optionalLand.get().getCorePos();

            Services.LAND_TRACKER.sendLandClientDataS2C(player, otherPlayer, player.getUUID(), optionalLand.isPresent(), blockPos,
                    SiegeManager.isPlayerInLandUnderSiege(player.serverLevel(), player),
                    SiegeManager.getPlayerSiege(player.serverLevel(), player.getUUID())
                            .map(siege -> !siege.disabledPlayers.contains(player.getUUID()))
                            .orElse(false));
        });

        if (StoneyCore.getConfig().landOptions().hungerSiege() && SiegeManager.isPlayerInLandUnderSiege(player.serverLevel(), player)) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0, false, false, true));
        }

        UUID uuid = player.getUUID();

        BlockPos lastPos = lastPlayerPos.get(uuid);
        if (lastPos != null && lastPos.equals(currentPos)) return;
        lastPlayerPos.put(uuid, currentPos);

        if (optionalLand.isEmpty()) {
            lastLandCore.put(uuid, null);
            return;
        }

        BlockPos newCore = optionalLand.get().getCorePos();
        BlockPos prevCore = lastLandCore.get(uuid);

        if (!Objects.equals(prevCore, newCore)) {
            lastLandCore.put(uuid, newCore);

            if (newCore != null) {
                if (!NBTDataHelper.get((IEntityDataSaver) player, PDKeys.LAND_EXPANDED, false)) {
                    Land land = optionalLand.get();
                    Services.PLATFORM.sendTitle(player, land.getLandTitle(serverLevel));
                } else {
                    NBTDataHelper.set((IEntityDataSaver) player, PDKeys.LAND_EXPANDED, false);
                }
            }
        }
    }
}
