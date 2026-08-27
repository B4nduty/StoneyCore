package banduty.stoneycore.event;

import banduty.stoneycore.stamina.StaminaUtil;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.client.render.land.OutlineClaimRenderer;
import banduty.stoneycore.lands.LandTracker;
import banduty.stoneycore.combat.mechanics.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class PlayerTickHandler {

    private static final ResourceLocation POWDER_SNOW_SLOW_ID =
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "powder_snow_slow");

    private PlayerTickHandler() {
    }

    public static void tick(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        boolean isDead = player.getHealth() <= 0;

        ModifiersUtil.updatePlayerReachAttributes(player);
        StaminaUtil.startStaminaTrack(player);

        if (!isDead) {
            handleFreezeImmunity(player);

            MechanicsUtil.handlePlayerReload(player);

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
        for (ItemStack itemStack : player.getArmorSlots()) {
            for (ItemStack armorAttachment : SCUnderArmor.getArmorAttachments(itemStack)) {
                if (armorAttachment.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
                    player.setTicksFrozen(0);

                    AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (attribute != null &&
                            attribute.getModifier(POWDER_SNOW_SLOW_ID) != null) {
                        attribute.removeModifier(POWDER_SNOW_SLOW_ID);
                    }

                    return;
                }
            }
        }
    }
}