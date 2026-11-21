package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.ClaimWorker;
import banduty.stoneycore.lands.Land;
import banduty.stoneycore.lands.LandState;
import banduty.stoneycore.lands.visitor.VisitorManager;
import banduty.stoneycore.lands.visitor.VisitorTracker;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.combat.mechanics.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class StartTickHandler {
    public static final Queue<ClaimWorker> CLAIM_TASKS = new LinkedList<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        MinecraftServer server = event.getServer();
        processClaimTasks();
        VisitorTracker.tickCooldowns();

        for (ServerLevel serverLevel : server.getAllLevels()) {
            checkAndRemoveBrokenLands(serverLevel);
            SiegeManager.tick(serverLevel);
            VisitorManager.get(serverLevel).tick(serverLevel);
        }
    }

    private static void processClaimTasks() {
        ClaimWorker current = CLAIM_TASKS.peek();
        if (current != null && current.tick()) {
            CLAIM_TASKS.poll();
        }
    }

    private static void checkAndRemoveBrokenLands(ServerLevel serverLevel) {
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