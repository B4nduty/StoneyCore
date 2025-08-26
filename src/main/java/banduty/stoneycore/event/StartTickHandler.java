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
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

public class StartTickHandler implements ServerTickEvents.StartTick {
    public static final Queue<ClaimWorker> CLAIM_TASKS = new LinkedList<>();
    private static final UUID POWDER_SNOW_SLOW_ID = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4ce");

    @Override
    public void onStartTick(MinecraftServer server) {
        processClaimTasks();

        WeightUtil.clearCache();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!player.isSpectator()) {
                updatePlayerTick(player);
            }
        }

        for (ServerWorld world : server.getWorlds()) {
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

    private void updatePlayerTick(ServerPlayerEntity player) {
        boolean isDead = player.getHealth() <= 0;

        WeightUtil.setCachedWeight(player);

        ModifiersUtil.updatePlayerReachAttributes(player);
        StaminaUtil.startStaminaTrack(player);

        if (!isDead) {
            if (AccessoriesCapability.getOptionally(player).isPresent()) {
                for (SlotEntryReference equipped : AccessoriesCapability.get(player).getAllEquipped()) {
                    ItemStack equippedStack = equipped.stack();
                    if (equippedStack.isIn(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
                        player.setFrozenTicks(0);
                        EntityAttributeInstance entityAttributeInstance = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                        if (entityAttributeInstance != null) {
                            if (entityAttributeInstance.getModifier(POWDER_SNOW_SLOW_ID) != null) {
                                entityAttributeInstance.removeModifier(POWDER_SNOW_SLOW_ID);
                            }

                        }
                        break;
                    }
                }
            }

            if (StoneyCore.getConfig().combatOptions.getParry()) {
                MechanicsUtil.handleParry(player);
            }
            MechanicsUtil.handlePlayerReload(player);

            ArmorUtil.startArmorCheck(player);
            SwallowTailArrowUtil.startSwallowTailTickTrack(player);

            LandTracker.trackPlayerLandMovement(player);
        } else {
            UUID playerId = player.getUuid();
            SiegeManager.getPlayerSiege(player.getServerWorld(), playerId)
                    .ifPresent(siege -> siege.disablePlayer(playerId, player.getServerWorld()));
        }

        OutlineClaimRenderer.renderOutlineClaim(player);
    }

    private void checkAndRemoveBrokenLands(ServerWorld world) {
        var state = LandState.get(world);
        List<Land> toRemove = new ArrayList<>();

        for (Land land : state.getAllLands()) {
            if (world.getBlockState(land.getCorePos()).getBlock() != land.getLandType().coreBlock()) {
                toRemove.add(land);
            }
        }

        for (Land land : toRemove) {
            state.removeLand(land);

            for (ServerPlayerEntity player : world.getPlayers()) {
                if (!player.isSpectator()) {
                    player.sendMessage(
                            net.minecraft.text.Text.translatable(
                                    "text.land." + land.getLandType().id().getNamespace() + ".fall",
                                    land.getLandTitle(world).getString()
                            ), true
                    );
                }
            }
        }
    }
}
