package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.ClaimWorker;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.render.OutlineClaimRenderer;
import banduty.stoneycore.util.servertick.*;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class StartTickHandler implements ServerTickEvents.StartTick {
    public static final Queue<ClaimWorker> CLAIM_TASKS = new LinkedList<>();
    private static final UUID POWDER_SNOW_SLOW_ID = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4ce");

    @Override
    public void onStartTick(MinecraftServer server) {
        processClaimTasks();

        WeightUtil.clearCache();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isSpectator()) {
                updatePlayerTick(player);
            }
        }

        for (ServerLevel world : server.getAllLevels()) {
            checkAndRemoveBrokenLands(world);
            SiegeManager.tick(world);
        }
    }

    private void processClaimTasks() {
        ClaimWorker current = CLAIM_TASKS.peek();
        if (current != null && current.tick()) {
            CLAIM_TASKS.poll();
        }
    }

    private void updatePlayerTick(ServerPlayer player) {
        boolean isDead = player.getHealth() <= 0;

        WeightUtil.setCachedWeight(player);

        ModifiersUtil.updatePlayerReachAttributes(player);
        StaminaUtil.startStaminaTrack(player);

        if (!isDead) {
            handleFreezeImmunity(player);

            if (StoneyCore.getConfig().combatOptions().getParry()) {
                MechanicsUtil.handleParry(player);
            }
            MechanicsUtil.handlePlayerReload(player);

            ArmorUtil.startArmorCheck(player);
            SwallowTailArrowUtil.startSwallowTailTickTrack(player);

            LandTracker.trackPlayerLandMovement(player);
        } else {
            UUID playerId = player.getUUID();
            SiegeManager.getPlayerSiege(player.serverLevel(), playerId)
                    .ifPresent(siege -> siege.disablePlayer(playerId, player.serverLevel()));
        }

        OutlineClaimRenderer.renderOutlineClaim(player);
    }

    private static void handleFreezeImmunity(ServerPlayer player) {
        if (AccessoriesCapability.getOptionally(player).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(player).getAllEquipped()) {
                ItemStack equippedStack = equipped.stack();
                if (equippedStack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
                    player.setTicksFrozen(0);
                    AttributeInstance entityAttributeInstance = player.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (entityAttributeInstance != null) {
                        if (entityAttributeInstance.getModifier(POWDER_SNOW_SLOW_ID) != null) {
                            entityAttributeInstance.removeModifier(POWDER_SNOW_SLOW_ID);
                        }

                    }
                    break;
                }
            }
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
