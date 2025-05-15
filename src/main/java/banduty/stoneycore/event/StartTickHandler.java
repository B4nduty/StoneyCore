package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.servertick.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class StartTickHandler implements ServerTickEvents.StartTick {
    @Override
    public void onStartTick(MinecraftServer server) {
        for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
            if (!playerEntity.isSpectator()) {
                ModifiersUtil.updatePlayerReachAttributes(playerEntity);

                StaminaUtil.startStaminaTrack(playerEntity);

                if (StoneyCore.getConfig().getParry()) MechanicsUtil.handleParry(playerEntity);

                MechanicsUtil.handlePlayerReload(playerEntity);

                ArmorUtil.startArmorCheck(playerEntity);

                SwallowTailArrowUtil.startSwallowTailTickTrack(playerEntity);
            }
        }
    }
}