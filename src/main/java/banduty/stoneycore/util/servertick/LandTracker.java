package banduty.stoneycore.util.servertick;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUUID(player.getUUID());
            buf.writeBoolean(optionalLand.isPresent());
            optionalLand.ifPresent(land -> buf.writeBlockPos(land.getCorePos()));
            buf.writeBoolean(SiegeManager.isPlayerInLandUnderSiege(player.serverLevel(), player));
            buf.writeBoolean(SiegeManager.getPlayerSiege(player.serverLevel(), player.getUUID())
                    .map(siege -> !siege.disabledPlayers.contains(player.getUUID()))
                    .orElse(false));

            ServerPlayNetworking.send(otherPlayer, ModMessages.LAND_CLIENT_DATA_S2C_ID, buf);
        });

        if (StoneyCore.getConfig().landOptions.hungerSiege() && SiegeManager.isPlayerInLandUnderSiege(player.serverLevel(), player)) {
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
                    sendTitle(player, land.getLandTitle(serverLevel));
                } else {
                    NBTDataHelper.set((IEntityDataSaver) player, PDKeys.LAND_EXPANDED, false);
                }
            }
        }
    }

    private static void sendTitle(ServerPlayer player, Component mainTitle) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeComponent(mainTitle);
        ServerPlayNetworking.send(player, ModMessages.LAND_TITLE_PACKET_ID, buffer);
    }
}
