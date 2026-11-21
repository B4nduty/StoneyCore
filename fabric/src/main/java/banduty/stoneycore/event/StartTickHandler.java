package banduty.stoneycore.event;

import banduty.stoneycore.lands.ClaimWorker;
import banduty.stoneycore.lands.Land;
import banduty.stoneycore.lands.LandState;
import banduty.stoneycore.lands.visitor.VisitorManager;
import banduty.stoneycore.lands.visitor.VisitorTracker;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.combat.mechanics.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class StartTickHandler implements ServerTickEvents.StartTick {
    public static final Queue<ClaimWorker> CLAIM_TASKS = new LinkedList<>();
    @Override
    public void onStartTick(MinecraftServer server) {
        processClaimTasks();
        VisitorTracker.tickCooldowns();

        for (ServerLevel serverLevel : server.getAllLevels()) {
            checkAndRemoveBrokenLands(serverLevel);
            SiegeManager.tick(serverLevel);
            VisitorManager.get(serverLevel).tick(serverLevel);
        }
    }

    private void processClaimTasks() {
        ClaimWorker current = CLAIM_TASKS.peek();
        if (current != null && current.tick()) {
            CLAIM_TASKS.poll();
        }
    }

    private void checkAndRemoveBrokenLands(ServerLevel serverLevel) {
        var state = LandState.get(serverLevel);
        List<Land> toRemove = new ArrayList<>();

        for (Land land : state.getAllLands()) {
            if (serverLevel.getBlockState(land.getCorePos()).getBlock() != land.getLandType().coreBlock()) {
                toRemove.add(land);
            }
        }

        for (Land land : toRemove) {
            state.removeLand(land);

            for (ServerPlayer player : serverLevel.players()) {
                if (!player.isSpectator()) {
                    player.displayClientMessage(
                            Component.translatable(
                                    "component.land." + land.getLandType().id().getNamespace() + ".fall",
                                    land.getLandTitle(serverLevel).getString()
                            ), true
                    );
                }
            }
        }
    }
}
