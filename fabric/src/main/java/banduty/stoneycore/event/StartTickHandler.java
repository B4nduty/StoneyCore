package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.lands.util.ClaimWorker;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.lands.visitor.VisitorManager;
import banduty.stoneycore.lands.visitor.VisitorTracker;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.util.render.OutlineClaimRenderer;
import banduty.stoneycore.util.servertick.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    private static final ResourceLocation POWDER_SNOW_SLOW_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "powder_snow_slow");

    @Override
    public void onStartTick(MinecraftServer server) {
        processClaimTasks();
        VisitorTracker.tickCooldowns();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isSpectator()) {
                updatePlayerTick(player);
            }
        }

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

    private void updatePlayerTick(ServerPlayer player) {
        boolean isDead = player.getHealth() <= 0;

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
        if (!FabricLoader.getInstance().isModLoaded("accessories")) {
            return;
        }

        for (ItemStack itemStack : player.getArmorSlots()) {
            for (ItemStack accessoryStack : SCUnderArmor.getAccessories(itemStack)) {
                if (accessoryStack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
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
