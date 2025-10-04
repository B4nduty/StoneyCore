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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class LandTracker {
    private static final Map<UUID, BlockPos> lastLandCore = new HashMap<>();
    private static final Map<UUID, BlockPos> lastPlayerPos = new HashMap<>();

    public static void trackPlayerLandMovement(ServerPlayerEntity player) {
        BlockPos currentPos = player.getBlockPos();
        ServerWorld world = player.getServerWorld();
        LandState landState = LandState.get(world);
        Optional<Land> optionalLand = landState.getLandAt(currentPos);

        player.getServerWorld().getPlayers().forEach(otherPlayer -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeUuid(player.getUuid());
            buf.writeBoolean(optionalLand.isPresent());
            optionalLand.ifPresent(land -> buf.writeBlockPos(land.getCorePos()));
            buf.writeBoolean(SiegeManager.isPlayerInLandUnderSiege(player.getServerWorld(), player));
            buf.writeBoolean(SiegeManager.getPlayerSiege(player.getServerWorld(), player.getUuid())
                    .map(siege -> !siege.disabledPlayers.contains(player.getUuid()))
                    .orElse(false));

            ServerPlayNetworking.send(otherPlayer, ModMessages.LAND_CLIENT_DATA_S2C_ID, buf);
        });

        if (StoneyCore.getConfig().landOptions.hungerSiege() && SiegeManager.isPlayerInLandUnderSiege(player.getServerWorld(), player)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 80, 0, false, false, true));
        }

        UUID uuid = player.getUuid();

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
                    sendTitle(player, land.getLandTitle(world));
                } else {
                    NBTDataHelper.set((IEntityDataSaver) player, PDKeys.LAND_EXPANDED, false);
                }
            }
        }
    }

    private static void sendTitle(ServerPlayerEntity player, Text mainTitle) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeText(mainTitle);
        ServerPlayNetworking.send(player, ModMessages.LAND_TITLE_PACKET_ID, buffer);
    }
}
